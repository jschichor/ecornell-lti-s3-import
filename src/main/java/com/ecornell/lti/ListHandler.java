package com.ecornell.lti;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import javax.servlet.http.*;

public class ListHandler {

    static void list(HttpServletRequest request) throws Exception {
        String bucket = request.getParameter("bucket");
        String prefix = request.getParameter("prefix");
        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        AmazonS3Client s3 = new AmazonS3Client(S3Servlet.cred);

        // If no bucket is specified, list buckets:
        if(bucket == null) {
            request.setAttribute("content",listBuckets(retUrl,s3));
            return;
        } //if//

        // Validate bucket name:
        if(!s3.doesBucketExist(bucket)) throw new Exception("Invalid bucket \""+bucket+"\".");

        // List items:
        ObjectListing list;
        StringBuilder result = new StringBuilder();
        String marker = null;
        do {
            list = s3.listObjects(new ListObjectsRequest(bucket,prefix,marker,null,null));
            list.getNextMarker();

            java.util.List<S3ObjectSummary> summaries = list.getObjectSummaries();

            for(S3ObjectSummary summary : summaries) {
                String temp = listObject(summary,bucket,prefix,retUrl,s3);
                if(temp != null) result.append(temp);
            } //for//

            marker = list.getNextMarker();
        } while(list.isTruncated());

        // Add "back" link if necessary:
        if(prefix != null) {
            int index = prefix.substring(0,prefix.length()-1).lastIndexOf("/");
            String url = S3Servlet.makeUrl(
                S3Servlet.prefix + "/" + S3Action.list,
                new String[]{"launch_presentation_return_url", "bucket", "prefix"},
                new String[]{retUrl, bucket, index == -1 ? null : prefix.substring(0, index + 1)}
            );

            result.append("<a href='").append(url).append("'><img src='").append(S3Servlet.context).append("/icon/arrow_left'>&nbsp;Back</a><br>");
        } //if//

        request.setAttribute("content",result.toString());
    } //list//

    static String listBuckets(String retUrl,AmazonS3Client s3) throws Exception {
        StringBuilder result = new StringBuilder();
        java.util.List<Bucket> buckets = s3.listBuckets();
        for(Bucket b : buckets) {
            String url = S3Servlet.makeUrl(
                S3Servlet.prefix + "/" + S3Action.list,
                new String[]{S3Servlet.URL_PARAM, "bucket"},
                new String[]{retUrl, b.getName()}
            );

            result.append(
                S3Servlet.LIST_TEMP
                    .replaceFirst("%U",url)
                    .replaceFirst("%P","white")
                    .replaceFirst("%I","box")
                    .replaceFirst("%N",b.getName())
            );
        } //for//
        return result.toString();
    } //listBuckets//

    static String listObject(S3ObjectSummary summary,String bucket,String prefix,String retUrl,AmazonS3Client s3) throws Exception {
        String key    = summary.getKey();
        String name   = prefix==null ? key : key.substring(prefix.length());

        if("".equals(name)) return null;
        if(name.contains("/") && name.indexOf("/")<name.length()-1) return null;

        boolean isPublic = S3Servlet.isPublic(bucket, key, s3);
        boolean isFolder = name.endsWith("/");

        String url, icon, type=null;
        if(isFolder) {
            url = S3Servlet.makeUrl(
                S3Servlet.prefix + "/" + S3Action.list,
                new String[]{"launch_presentation_return_url", "bucket", "prefix"},
                new String[]{retUrl, bucket, key}
            );
            icon = "folder";
        } else {
            ObjectMetadata meta = s3.getObjectMetadata(bucket,key);
            type = meta.getContentType();

            switch(ContentType.getType(type)) {
                case mp4:
                case mov:
                case flv:
                case mp3:
                case image:
                    url = S3Servlet.makeUrl(
                        S3Servlet.prefix + "/" + S3Action.media,
                        new String[]{"launch_presentation_return_url", "bucket", "key"},
                        new String[]{retUrl, bucket, key}
                    );
                    switch(ContentType.getType(type)) {
                        case mp3  : icon = "music"; break;
                        case image: icon = "image"; break;
                        default   : icon = "film";
                    } //switch//
                    break;
                case flash:
                    url = S3Servlet.makeUrl(
                        S3Servlet.prefix + "/" + S3Action.flash,
                        new String[]{"launch_presentation_return_url", "url"},
                        new String[]{retUrl, S3Servlet.getS3Url(bucket, key)}
                    );
                    icon = "page_white_flash";
                    break;
                case html:
                    url = S3Servlet.makeUrl(
                        S3Servlet.prefix + "/" + S3Action.html,
                        new String[]{"launch_presentation_return_url", "url"},
                        new String[]{retUrl, S3Servlet.getS3Url(bucket, key)}
                    );
                    icon = "page_white_code";
                    break;
                case doc:
                case pdf:
                case xls:
                case zip:
                    url = S3Servlet.makeUrl(
                        S3Servlet.prefix + "/" + S3Action.link,
                        new String[]{"launch_presentation_return_url", "url"},
                        new String[]{retUrl, S3Servlet.getS3Url(bucket, key)}
                    );
                    switch(ContentType.getType(type)) {
                        case doc: icon = "page_white_word"; break;
                        case pdf: icon = "page_white_acrobat"; break;
                        case xls: icon = "page_white_excel"; break;
                        default : icon = "page_white_zip"; break;
                    } //switch//
                    break;
                default:
                    url = null;
                    icon = "exclamation";
            } //switch//
        } //if-else//

        if(url == null) {
            return "<img src='"+S3Servlet.context+"/icon/bullet_yellow'>&nbsp;"
                + "<img src='"+S3Servlet.context+"/icon/exclamation'>&nbsp;"+name+" (unknown type: "+type+")<br>";
        } //if//

        return S3Servlet.LIST_TEMP
            .replaceFirst("%U",url)
            .replaceFirst("%P",isFolder ? "white" : isPublic ? "green" : "red")
            .replaceFirst("%I",icon)
            .replaceFirst("%N",name);
    } //listObject//




}
