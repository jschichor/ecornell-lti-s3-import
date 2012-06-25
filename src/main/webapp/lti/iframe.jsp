<%@page language="java"%>
<%@page import="com.ecornell.lti.S3Servlet" %>
<%
    String action = (String)request.getAttribute("action");
    String retUrl = (String)request.getAttribute(S3Servlet.URL_PARAM);
    String url = (String)request.getAttribute("url");
%>

<%@include file="header.jspf"%>

<form method="get" action="<%=action%>">
    <input type="hidden" name="launch_presentation_return_url" value="<%=retUrl%>">
    <input type="hidden" name="url" value="<%=url%>">
    <b>URL</b><br>
    <%=url%><br><br>
    <b>Width</b><br>
    <input type="text" name="width" value="640" size="4" maxlength="4"><br><br>
    <b>Height</b><br>
    <input type="text" name="height" value="480" size="4" maxlength="4"><br><br>
    <input type="Submit" value="Submit">
</form>

<%@include file="footer.jspf"%>
