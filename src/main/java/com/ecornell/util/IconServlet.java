package com.ecornell.util;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * A very simple servlet for serving famfamfam icons out of their jar.
 */
public class IconServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException {
        String uri = request.getRequestURI();
        if(uri.endsWith("/")) return;

        int index = uri.lastIndexOf("/");
        if(index == -1) return;

        String name = uri.substring(index+1);
        ClassLoader classLoader = getClass().getClassLoader();

        InputStream icon = classLoader.getResourceAsStream("com/famfamfam/silk/"+name+".png");
        if(icon == null) return;

        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();

        int count;
        byte[] b = new byte[1024];
        while((count = icon.read(b))>0) out.write(b,0,count);
        out.flush();
        icon.close();
    } //doGet//

} //class IconServlet//
