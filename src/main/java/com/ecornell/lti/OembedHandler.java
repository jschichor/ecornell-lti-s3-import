package com.ecornell.lti;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import javax.servlet.http.*;

public class OembedHandler {

    static void oembed(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String url = request.getParameter("url");
        if(url == null) throw new Exception("No subject URL found.");

        int width=640, height=480;
        try { width = Integer.parseInt(request.getParameter("width")); } catch(Exception e) { }
        try { height = Integer.parseInt(request.getParameter("height")); } catch(Exception e) { }
        boolean json = "json".equals(request.getParameter("format"));

        String[] temp = S3Servlet.parseS3Url(url);
        String bucket = temp[0];
        String key = temp[1];

        AmazonS3Client s3 = new AmazonS3Client(S3Servlet.cred);
        ObjectMetadata meta = s3.getObjectMetadata(bucket,key);
        String type = meta.getContentType();

        String result;
        switch(ContentType.getType(type)) {
            case html:
                result= oembedIframe(url,width,height,json);
                break;
            case flash:
                result = oembedFlash(url,width,height,json,request);
                break;
            case flv:
            case mp4:
            case mov:
                result = oembedVideo(url,width,height,json);
                break;
            case mp3:
                result = oembedAudio(url,json);
                break;
            default:
                result = makeRichOembed("unknown type: " + type, width, height, json);
        } //switch//

        response.reset();
        response.setContentType(json ? "application/json" : "text/xml");
        response.getWriter().print(result);
    } //oembed//

    static String oembedIframe(String url,int width,int height,boolean json) {
        String html = "<iframe src='"+url+"' height='"+height+"' width='"+width+"'/>\n";
        html = html.replaceAll("'","\"");
        return makeRichOembed(html, width, height, json);
    } //oembedIFrame//

    static String oembedAudio(String url,boolean json) {
        String html
            = "<div class='mp3' style='width:400px;height:24px;background:#ccc;margin-bottom:1em;'>"
            + "<span class='file' style='display:none;'>"+url+("</span>"
            + "</div>").replace("'","\"");
        html = html.replaceAll("'","\"");
        return makeRichOembed(html, 0, 0, json);
    } //oembedAudio//


    static String oembedFlash(String url,int width,int height,boolean json,HttpServletRequest request) {
        String bgcolor, base;
        if((bgcolor=request.getParameter("bgcolor"))==null) bgcolor = "#ffffff";
        if((base=request.getParameter("base"))==null) base = ".";

        String html
            = "<object classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' "
            + "codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0' "
            + "width='"+width+"' height='"+height+"' id='foobar'>\n"
            + "\t<param name='movie' value='"+url+"'/>\n"
            + "\t<param name='quality' value='high' />\n"
            + "\t<param name='bgcolor' value='"+bgcolor+"' />\n"
            + "\t<param name='allowScriptAccess' value='sameDomain' />\n"
            + "\t<param name='base' value='"+base+"' />\n"
            + "\t<embed src='"+url+"' quality='high' bgcolor='"+bgcolor+"' width='"+width+"' height='"+height+"' "
            + "name='foobar' allowScriptAccess='sameDomain' base='"+url+"' type='application/x-shockwave-flash' "
            + "pluginspage='http://www.macromedia.com/go/getflashplayer' />\n"
            + "</object>\n";
        html = html.replaceAll("'","\"");

        return makeRichOembed(html,width,height,json);
    } //oembedFlash//

    static String oembedVideo(String url,int width,int height,boolean json) {
        String html
            = "<div class='jwp' style='width:"+width+"px; height:"+height+"px;margin-bottom:1em;background:#ccc;'>"
            + "<span class='file' style='display: none;'>"+url+"</span>"
            + "</div>";
        html = html.replaceAll("'","\"");

        return makeRichOembed(html,width,height,json);
    } //oembedFlash//


    static String makeRichOembed(String html,int width,int height,boolean json) {
        if(json) {
            String[] a = new String[]{"\"","\n","\t"};
            String[] b = new String[]{"\\\"","\\n","\\t"};
            for(int i=0; i<a.length; i++) html = html.replace(a[i],b[i]);

            return "{'version':'1.0','type':'rich','html':'%0','width':%1,'height':%2}"
                .replaceAll("'","\"")
                .replace("%0",html)
                .replace("%1",Integer.toString(width))
                .replace("%2",Integer.toString(height));
        } else {
            return "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n"
                + "<oembed>"
                + "<version>1.0</version>"
                + "<type>rich</type>"
                + "<html><[!CDATA["+html+"]]></html>"
                + "<width>"+width+"</width>"
                + "<height>"+height+"</height>"
                + "</oembed>";
        } //if-else//
    } //makeRichOembed//
}
