<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String pageBody = (String) request.getAttribute( "body" );
    if ( pageBody == null ) {
        pageBody = "not_found.jsp";
    }
%>
<html>
<head>
    <title>KN-P-213</title>
</head>
<body>
<header>Header</header>

<jsp:include page='<%= pageBody %>' />

<footer>Footer</footer>
</body>
</html>
