<%--
  ~ Copyright J. Craig Venter Institute, 2011
  ~
  ~ The creation of this program was supported by the U.S. National
  ~ Science Foundation grant 1048199 and the Microsoft allocation
  ~ in the MS Azure cloud.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%--
  ~ Copyright J. Craig Venter Institute, 2011
  ~
  ~ The creation of this program was supported by the U.S. National
  ~ Science Foundation grant 1048199 and the Microsoft allocation
  ~ in the MS Azure cloud.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%--
  Created by IntelliJ IDEA.
  User: hkim
  Date: 1/3/12
  Time: 1:35 PM
  To change this template use File | Settings | File Templates.
--%>
<%@taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>PRODDL-C Admin Main</title>

    <script src="/resources/js/jquery-1.6.2.min.js"></script>
    <script src="/resources/js/jquery-ui-1.8.16.custom.min.js"></script>
    <!--[if IE]>
    <script>
    </script>
    <![endif]-->
    <script>
        $(document).ready(function() {
            $('#topMenuTabs').tabs();
            /*setInterval(setIframeSize, 5);*/
            setIframeSize();
        });

        function setIframeSize() {
            $('iframe').height($(window).height() * .8);
            //$('iframe').width("100%");
        }
    </script>
    <link href="/resources/css/redmond/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css"/>
    <link href="/resources/css/main.css" rel="stylesheet" type="text/css"/>
    <style type="text/css">
    </style>
</head>
<body>
<div id="topUserInfo" style="text-align: right;">
    <form action="/pdl/w/logout" method="post" name="logoutform">
        Hello!! <%= request.getUserPrincipal().getName() %> <a href="javascript:document.logoutform.submit();">log
        out</a>
    </form>
</div>
<div id="topMenuTabs" style="width: 100%; height: 100%;">
    <ul>
        <li><a href="#file">FileUpload</a></li>
        <li><a href="#test">Test</a></li>
        <li><a href="#admin">Admin</a></li>
    </ul>
    <div id="file">
        <iframe src="/pdl/w/fileupload" id="fileIframe" width="90%" frameborder="0" scrolling="no"></iframe>
    </div>
    <div id="test">
        <iframe src="/pdl/w/test" id="testIframe" width="90%" frameborder="0" scrolling="no"></iframe>
    </div>
    <div id="admin">
        <iframe src="/pdl/w/admin/main" id="adminIframe" width="90%" frameborder="0" scrolling="no"></iframe>
    </div>
</div>
</body>
</html>
