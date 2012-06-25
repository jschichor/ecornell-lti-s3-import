package com.ecornell.lti;

import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class S3Servlet extends HttpServlet {
    public static final String URL_PARAM = "launch_presentation_return_url";
    static String LIST_TEMP;

    static boolean testing;
    static String prefix, cloudBucket, server, s3Server, cloudServer, context;
    static BasicAWSCredentials cred;

    public void init(ServletConfig config) {
        context = config.getServletContext().getContextPath();

        testing = "Y".equalsIgnoreCase(config.getInitParameter("testing"));
        server = config.getInitParameter("server");
        s3Server = config.getInitParameter("s3Server");
        cloudServer = config.getInitParameter("cloudServer");
        prefix = context + config.getInitParameter("prefix");
        cloudBucket = config.getInitParameter("cloudBucket");
        String accessKey = config.getInitParameter("accessKey");
        String secretKey = config.getInitParameter("secretKey");
        cred = new BasicAWSCredentials(accessKey,secretKey);

        LIST_TEMP = "<a href='%U'><img src='"+context+"/icon/bullet_%P'>&nbsp;<img src='"+context+"/icon/%I'>&nbsp;%N</a><br>";
    } //init//

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Pattern p = Pattern.compile("^"+ prefix +"/(.+)",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(request.getRequestURI());
        if(!m.matches()) {
            response.sendRedirect(prefix +"/"+S3Action.list);
            return;
        } //if//

        S3Action action;
        try { action = S3Action.valueOf(m.group(1)); }
        catch(Exception e) { throw new ServletException("Unknown action: "+m.group(1));}

        try {
            switch(action) {
                case list   : ListHandler.list(request); break;
                case link   : TypeHandler.link(request); break;
                case iframe : TypeHandler.iframe(request); break;
                case html   : TypeHandler.html(request); break;
                case flash  : TypeHandler.flash(request); break;
                case media  : TypeHandler.media(request, response); break;
                case oembed : OembedHandler.oembed(request, response); break;
                case returnLink  : TypeHandler.returnLink(request, response); break;
                case returnOembed: TypeHandler.returnOembed(request, response); break;
            } //switch//
        } catch(Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        } //try-catch//

        if(action.isDisplay())
            request.getRequestDispatcher("/lti/"+action.getView()+".jsp").forward(request,response);

    } //service//

    ///////////////////////
    // Utility functions //
    ///////////////////////

    static String makeUrl(String url,String[] keys, String[] vals) throws UnsupportedEncodingException {
        String result = url;

        if(keys!=null && vals!=null && keys.length<=vals.length) {
            boolean first = true;

            for(int i=0; i<keys.length; i++) {
                if(vals[i]==null) continue;
                result += (first?"?":"&") + keys[i] + "=" + URLEncoder.encode(vals[i], "utf-8");
                first = false;
            } //for//
        } //if//

        return result;
    } //makeUrl//

    static String[] parseS3Url(String url) {
        String bucket=null, key = null;
        Pattern p = Pattern.compile(s3Server+"/([^/]+)/(.+)");
        Matcher m = p.matcher(url);
        if(m.matches()) {
            bucket = m.group(1);
            key    = m.group(2);
        } else {
            p = Pattern.compile(cloudServer+"/(.+)");
            m = p.matcher(url);
            if(m.matches()) {
                bucket = cloudBucket;
                key    = m.group(1);
            } //if//
        } //if-else//
        return new String[]{bucket,key};
    } //parseS3Url//


    static final Grant PUBLIC = new Grant(GroupGrantee.AllUsers, Permission.Read);

    static boolean isPublic(String bucket,String key,AmazonS3Client s3) {
        AccessControlList acl = s3.getObjectAcl(bucket,key);
        return acl.getGrants().contains(PUBLIC);
    } //isPublic//

    static String getS3Url(String bucket, String key) {
        return cloudBucket.equals(bucket)
            ? cloudServer+"/"+key
            : s3Server+"/"+bucket+"/"+key;
    } //getS3Url//



} //class S3Servlet//
