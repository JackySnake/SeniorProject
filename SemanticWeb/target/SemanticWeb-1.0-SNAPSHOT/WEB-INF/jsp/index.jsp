<%-- 
    Document   : index
    Created on : Feb 13, 2015, 2:36:20 PM
    Author     : mtmmoei
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<c:set var="cp" value="${pageContext.request.servletContext.contextPath}" scope="request" />
 
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Semantic Web</title>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">

        <!-- Optional theme -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">

        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
        
        <link rel="stylesheet" type="text/css" href="${cp}/resources/css/site.css" />
        <script src="${cp}/resources/js/js.js"></script>
        
    </head>
    <body>
        <h4>Advanced Search</h4>
        <c:url var="action" value="/query" ></c:url>
        <form:form action="${action}" modelAttribute="searchForm" method="POST" name="searchForm">
            <form:textarea type="text" path="queryString"/>
            <input type="submit" value="Search"/>
        </form:form>
    </body>
</html>
