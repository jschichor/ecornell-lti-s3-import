<%@page language="java"%>
<%@page import="com.ecornell.lti.S3Servlet" %>
<%
    String linkAction   = (String)request.getAttribute("linkAction");
    String iframeAction = (String)request.getAttribute("iframeAction");
    String retUrl = (String)request.getAttribute(S3Servlet.URL_PARAM);
    String url    = (String)request.getAttribute("url");
%>

<%@include file="header.jspf"%>

<form method="get" action="/">
    <input type="hidden" name="launch_presentation_return_url" value="<%=retUrl%>">
    <input type="hidden" name="url" value="<%=url%>">
    <b>URL</b><br>
    <%=url%><br><br>

    <input type="button" value="As Link" onclick="this.form.action='<%=linkAction%>'; this.form.submit();">
    <input type="button" value="As iFrame" onclick="this.form.action='<%=iframeAction%>'; this.form.submit();">
</form>

<%@include file="footer.jspf"%>
