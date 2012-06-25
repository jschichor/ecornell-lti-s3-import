package com.ecornell.lti;

import com.amazonaws.services.s3.*;

import javax.servlet.http.*;
import java.util.*;

public class TypeHandler {

    static void flash(HttpServletRequest request) throws Exception {
        flashOrLink(request,S3Action.returnOembed);
    } //flash//

    static void link(HttpServletRequest request) throws Exception {
        flashOrLink(request,S3Action.returnLink);
    } //link//

    static void iframe(HttpServletRequest request) throws Exception {
        flashOrLink(request,S3Action.returnOembed);
    } //link//

    static void flashOrLink(HttpServletRequest request,S3Action action) throws Exception {
        String oembed = request.getParameter("url");

        String[] temp = S3Servlet.parseS3Url(oembed);
        String bucket = temp[0];
        String key    = temp[1];
        String prefix = key.contains("/") ? key.substring(0,key.lastIndexOf("/")) : "";

        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        request.setAttribute("action", prefix +"/"+action);
        request.setAttribute(S3Servlet.URL_PARAM,retUrl);
        request.setAttribute("url",oembed);
        request.setAttribute("base",(S3Servlet.cloudBucket.equals(bucket)?"":"/"+bucket)+"/"+prefix);
    } //flashOrLink//


    static void html(HttpServletRequest request) throws Exception {
        String url = request.getParameter("url");
        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        request.setAttribute("linkAction", S3Servlet.prefix +"/"+S3Action.link);
        request.setAttribute("iframeAction", S3Servlet.prefix +"/"+S3Action.iframe);
        request.setAttribute(S3Servlet.URL_PARAM,retUrl);
        request.setAttribute("url",url);
    } //flashOrLink//




    static void media(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String bucket = request.getParameter("bucket");
        String key    = request.getParameter("key");
        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        AmazonS3Client s3 = new AmazonS3Client(S3Servlet.cred);

        int[] dim = MediaHandler.getMediaDimensions(bucket,key,s3);
        int width = dim[0];
        int height = dim[1];

        response.sendRedirect(MediaHandler.getMediaRedirect(bucket,key,retUrl,width,height,s3));
    } //image//


    static void returnLink(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String url = request.getParameter("url");
        if(url == null) throw new Exception("No subject URL found.");

        String text = request.getParameter("text");
        if(text == null) throw new Exception("No link text found.");

        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        String title  = request.getParameter("title");
        String target = request.getParameter("target")==null ? "null" : "_blank";

        String oembedUrl = S3Servlet.makeUrl(
            retUrl,
            new String[]{"embed_type","url","title","text","target"},
            new String[]{"link",url,title,text,target}
        );

        response.reset();
        response.sendRedirect(oembedUrl);
    } //returnLink//

    static void returnOembed(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String url = request.getParameter("url");
        if(url == null) throw new Exception("No subject URL found.");

        String retUrl = request.getParameter(S3Servlet.URL_PARAM);
        if(retUrl == null && !S3Servlet.testing) throw new Exception("Recieved no launch presentation return url");

        Enumeration<String> en = request.getParameterNames();
        ArrayList<String> keys=new ArrayList<String>(), vals=new ArrayList<String>();

        String key, value;
        while(en.hasMoreElements()) {
            key = en.nextElement();
            if("action".equals(key)) continue;
            if("launch_presentation_return_url".equals(key)) continue;
            if("url".equals(key)) continue;

            if((value = request.getParameter(key)) != null) {
                keys.add(key);
                vals.add(value);
            } //if//
        } //while//

        String endpoint = S3Servlet.makeUrl(
            S3Servlet.server + S3Servlet.prefix + "/" + S3Action.oembed,
            keys.toArray(new String[keys.size()]),
            vals.toArray(new String[vals.size()])
        );

        String oembedUrl = S3Servlet.makeUrl(
            retUrl,
            new String[]{"embed_type","endpoint","url"},
            new String[]{"oembed",endpoint,url}
        );

        response.reset();
        response.sendRedirect(oembedUrl);
    } //returnOembed//

}
