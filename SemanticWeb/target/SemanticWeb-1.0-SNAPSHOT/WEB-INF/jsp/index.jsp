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
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Semantic Web</title>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <link rel="stylesheet" href="${cp}/resources/css/bootstrap.min.css">
        <link rel="stylesheet" href="${cp}/resources/css/bootstrap-theme.min.css">
        <link rel="stylesheet" type="text/css" href="${cp}/resources/css/index.css" />
        <script src="${cp}/resources/js/bootstrap.min.js"></script>
        <script src="${cp}/resources/js/index.js"></script>

    </head>
    <body>
        <h1>Movie Search</h1>
        <hr>
        <div id="content_wrapper">
            <div role="tabpanel" id="search_panel" class="panel panel-default">
                <h3>Search</h3>
                <!-- Nav tabs -->
                <ul class="nav nav-tabs" role="tablist" id="search_tab">
                    <li role="presentation"><a href="#faceted_search_tabpanel" aria-controls="faceted_search" role="tab" data-toggle="tab">Faceted Search</a></li>
                    <li role="presentation"><a href="#advance_search_tabpanel" aria-controls="advance_search" role="tab" data-toggle="tab">Advanced Search</a></li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content">
                    <div role="tabpanel" class="tab-pane" id="faceted_search_tabpanel">
                        <form action="${pageContext.request.contextPath}/facetedSearch" id="facetedSearch" class="form-inline" method="get">
                            <!--                            <div class="form-group">
                                                            <input type="text" class="form-control" id="keyword" placeholder="Enter keyword ..."/>
                                                        </div>-->
                            <div class="form-group">
                            <label for="category" class="col-sm-3 control-label">Search for</label>
                            <div class="dropdown">
                                <button class="btn btn-default dropdown-toggle" type="button" id="category" data-toggle="dropdown" aria-expanded="true">
                                    <span class="selection">${categories[0].name}</span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="category">
                                    <c:forEach items="${categories}" var="category">
                                        <li role="presentation"><a role="menuitem" tabindex="-1" href="#">${category.name}</a></li>
                                        </c:forEach>
                                </ul>
                            </div>
                        </div>
                                    <input id="type" value="Movie" hidden="true"/>
                            <div class="form-group">
                                    <label for="filter" class="col-sm-3 control-label">Add filter</label>
                            <div class="dropdown">
                                <button class="btn btn-default dropdown-toggle" type="button" id="filter" data-toggle="dropdown" aria-expanded="true">
                                    <span class="selection">${categories[0].properties[0]}</span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="filter">
                                    <c:forEach items="${categories[0].properties}" var="property">
                                        <li role="presentation"><a role="menuitem" tabindex="-1" href="#">${property}</a></li>
                                        </c:forEach>
                                </ul>
                            </div>
                            </div>
                            <div id="filter_wrapper" hidden="true">
                                <strong>Add filter: </strong> 
                            </div>
                            <input type="submit" class="btn btn-primary center" value="Search"/>
                        </form>
                    </div>
                    <div role="tabpanel" class="tab-pane" id="advance_search_tabpanel">
                        <form action="${pageContext.request.contextPath}/advanceSearch" id="advanceSearch" method="get">
                            <div class="form-group">
                                <label for="queryString">SPARQL:</label>
                                <p>PREFIX owl: &lt;http://www.w3.org/2002/07/owl#><br>
                                    PREFIX xsd: &lt;http://www.w3.org/2001/XMLSchema#><br>
                                    PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#><br>
                                    PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#><br>
                                    PREFIX foaf: &lt;http://xmlns.com/foaf/0.1/><br>
                                    PREFIX oddlinker: &lt;http://data.linkedmdb.org/resource/oddlinker/><br>
                                    PREFIX map: &lt;file:/C:/d2r-server-0.4/mapping.n3#><br>
                                    PREFIX db: &lt;http://data.linkedmdb.org/resource/><br>
                                    PREFIX dbpedia: &lt;http://dbpedia.org/property/><br>
                                    PREFIX skos: &lt;http://www.w3.org/2004/02/skos/core#><br>
                                    PREFIX dc: &lt;http://purl.org/dc/terms/><br>
                                    PREFIX movie: &lt;http://data.linkedmdb.org/resource/movie/></p>
                                <textarea type="text" class="form-control" rows="10" id="queryString"></textarea>
                            </div>
                            <input type="submit" class="btn btn-primary center" value="Search"/>
                        </form>
                    </div>
                </div>

            </div>

            <div id="search_result_wrapper" >
                <h3>Result</h3>
                <div id="search_result"></div>
            </div>

        </div>
    </body>
</html>
