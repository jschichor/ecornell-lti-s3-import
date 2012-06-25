<%@page language="java"%>
<%@page import="com.ecornell.lti.S3Servlet" %>
<%
    String action = (String)request.getAttribute("action");
    String retUrl = (String)request.getAttribute(S3Servlet.URL_PARAM);
    String oembed = (String)request.getAttribute("url");
    String base   = (String)request.getAttribute("base");
%>

<%@include file="header.jspf"%>

<form method="get" action="<%=action%>">
    <input type="hidden" name="launch_presentation_return_url" value="<%=retUrl%>">
    <input type="hidden" name="url" value="<%=oembed%>">
    <b>Filename</b><br>
    <%=oembed%><br><br>
    <b>Width</b><br>
    <input type="text" name="width" value="640" size="4" maxlength="4"><br><br>
    <b>Height</b><br>
    <input type="text" name="height" value="480" size="4" maxlength="4"><br><br>
    <b>Background</b><br>
    <input type="text" name="bgcolor" value="#ffffff" size="7" maxlength="7"><br><br>
    <b>Base URL</b><br>
    <input type="text" name="base" value="<%=base%>" size="40"><br><br>
    <input type="Submit" value="Submit">
</form>

<%@include file="footer.jspf"%>
