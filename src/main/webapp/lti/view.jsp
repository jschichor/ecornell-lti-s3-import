<%@page language="java"%>

<% String content = (String)request.getAttribute("content"); %>

<%@include file="header.jspf"%>

<% if(content != null) out.println(content); %>

<%@include file="footer.jspf"%>
