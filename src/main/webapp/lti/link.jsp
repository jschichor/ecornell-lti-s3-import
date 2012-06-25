<%@page language="java"%>
<%@page import="com.ecornell.lti.S3Servlet" %>
<%
    String action = (String)request.getAttribute("action");
    String retUrl = (String)request.getAttribute(S3Servlet.URL_PARAM);
    String oembed = (String)request.getAttribute("url");

    int index = oembed.lastIndexOf("/");
    String text = index==-1 ? oembed : oembed.substring(index+1);
%>

<%@include file="header.jspf"%>

<form method="get" action="<%=action%>">
    <input type="hidden" name="launch_presentation_return_url" value="<%=retUrl%>">
    <input type="hidden" name="url" value="<%=oembed%>">
    <b>Filename</b><br>
    <%=oembed%><br><br>
    <b>Link Text</b><br>
    <input type="text" name="text" value="<%=text%>" size="50"><br><br>
    <b>Title (optional)</b><br>
    <input type="text" name="title" size="50"><br><br>
    <input type="Submit" value="Submit">
</form>

<%@include file="footer.jspf"%>
