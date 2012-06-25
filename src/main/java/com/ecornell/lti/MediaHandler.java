package com.ecornell.lti;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.coremedia.iso.*;
import com.coremedia.iso.boxes.*;
import com.sun.media.*;

import javax.imageio.*;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class MediaHandler {
    private static int getFlvDimension(byte[] b, String property) {
        String metadata = new String(b);
        int index = metadata.indexOf(property);
        if(index == -1) return 0;
        index += property.length() + 1;
        return (int)java.nio.ByteBuffer.wrap(Arrays.copyOfRange(b, index, index + 8)).getDouble();
    } //getFlvDimension//

    static int[] getMediaDimensions(String bucket,String key,AmazonS3Client s3) throws Exception {
        ObjectMetadata meta = s3.getObjectMetadata(bucket,key);
        Map<String,String> userMeta = meta.getUserMetadata();

        int width,height;
        try {
            width  = Integer.parseInt(userMeta.get("width"));
            height = Integer.parseInt(userMeta.get("height"));
        } catch(NumberFormatException e) {
            width = height = -1;
        } //try-catch//
        if(width>0 && height>0) return new int[]{width,height};

        // If the dimensions aren't there, fetch the image, determine the dimensions and save them:
        byte[] b;
        S3Object obj;

        String type = meta.getContentType();
        switch(ContentType.getType(type)) {
            case mp3:
                return new int[]{0,0};

            case flv:
                obj = s3.getObject(bucket,key);
                InputStream in = obj.getObjectContent();
                b = new byte[400];
                in.read(b);
                in.close();

                width = getFlvDimension(b,"width");
                height = getFlvDimension(b,"height");
                if(width<=0 || height<=0) throw new Exception("Could not find dimensions of flv.");

                break;

            case mov:
                Manager.setHint(Manager.CACHING, false);
                String url = S3Servlet.getS3Url(bucket,key).replace("https", "http").replace(" ", "+");
                DataSource ds = Manager.createDataSource(new MediaLocator(url));

                BasicSourceModule source = BasicSourceModule.createModule(ds);
                if(source == null) throw new Exception("Couldn't create source module.");
                if(!source.doRealize()) throw new Exception("Couldn't realize source.");

                Demultiplexer parser = source.getDemultiplexer();
                if(parser == null) throw new Exception("Couldn't get demultiplexer.");

                Track[] tracks = parser.getTracks();
                if(tracks==null || tracks.length==0) throw new Exception("No tracks");

                for(Track track : tracks) {
                    if(track.getFormat() instanceof VideoFormat) {
                        VideoFormat f = (VideoFormat)track.getFormat();
                        Dimension d = f.getSize();
                        width  = (int)d.getWidth();
                        height = (int)d.getHeight();
                        break;
                    } //if//
                } //for//
                if(width<=0 || height<=0) throw new Exception("Could not find dimensions of mov.");
                break;

            case image:
                obj = s3.getObject(bucket,key);
                BufferedImage image = ImageIO.read(obj.getObjectContent());
                width  = image.getWidth();
                height = image.getHeight();
                break;

            case mp4:
                int count;
                b = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream input = s3.getObject(bucket,key).getObjectContent();
                while((count = input.read(b))>0) baos.write(b,0,count);

                IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(baos.toByteArray()));
                isoFile.parse();
                MovieBox movie = isoFile.getMovieBox();

                long[] trackIds = movie.getTrackNumbers();
                for(int i=0; i<trackIds.length && (width<=0 || height<=0); i++) {
                    TrackHeaderBox trackHeader = movie.getTrackMetaData(trackIds[i]).getTrackBox().getTrackHeaderBox();
                    width  = (int)trackHeader.getWidth();
                    height = (int)trackHeader.getHeight();
                } //for//
                if(width<=0 || height<=0) throw new Exception("Could not find dimensions of mp4.");
                break;
            default:
                throw new Exception("Unknown type: "+type);
        } //switch//

        // Oddly, the only way to update metadat in s3 is to replace the whole object:
        CopyObjectRequest copy = new CopyObjectRequest(bucket,key,bucket,key);
        meta.addUserMetadata("width",Integer.toString(width));
        meta.addUserMetadata("height",Integer.toString(height));
        copy.setNewObjectMetadata(meta);

        AccessControlList acl = s3.getObjectAcl(bucket,key);
        s3.copyObject(copy);
        s3.setObjectAcl(bucket,key,acl);

        // return dimensions:
        return new int[]{width,height};
    } //getMediaDimensions//

    static String getMediaRedirect(String bucket,String key,String retUrl,int width,int height,AmazonS3Client s3) throws Exception {
        ObjectMetadata meta = s3.getObjectMetadata(bucket,key);
        String type = meta.getContentType();
        String s3Url = S3Servlet.getS3Url(bucket,key);

        switch(ContentType.getType(type)) {
            case image:
                String name = key.contains("/") ? key.substring(key.lastIndexOf("/")+1) : key;
                return S3Servlet.makeUrl(
                    retUrl,
                    new String[]{"embed_type","url","alt","width","height"},
                    new String[]{"image",s3Url,name,Integer.toString(width),Integer.toString(height)}
                );
            case mp4:
            case mov:
            case flv:
                return S3Servlet.makeUrl(
                    S3Servlet.prefix +"/"+S3Action.returnOembed,
                    new String[]{S3Servlet.URL_PARAM,"url","width","height"},
                    new String[]{retUrl,s3Url,Integer.toString(width),Integer.toString(height)}
                );
            case mp3:
                return S3Servlet.makeUrl(
                    S3Servlet.prefix +"/"+S3Action.returnOembed,
                    new String[]{S3Servlet.URL_PARAM,"url"},
                    new String[]{retUrl,s3Url}
                );
        } //switch//
        return null;
    } //getMediaRedirect//


}
