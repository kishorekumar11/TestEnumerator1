<%--
  Created by IntelliJ IDEA.
  User: kishore-pt3063
  Date: 02-08-2019
  Time: 06:12 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<html>
<head>
    <title>Hello World</title>
</head>
<body>
Hello <br/>
<h1><bean:write name="helloWorldForm" property="message" />
</h1>
</body>
</html>
