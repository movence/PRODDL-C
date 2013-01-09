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
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>PRODDL-C Admin Main</title>

    <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.8.24/jquery-ui.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.3/jquery.dataTables.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.1.1/bootstrap.min.js"></script>
    <script src="/resources/js/jquery.form.js"></script>
    <script src="/resources/js/proddl.js"></script>
    <script>
        $(document).ready(function() {
            var r = new proddl.run();
        });
    </script>

    <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    <link rel="stylesheet" type="text/css" href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css">
    <link rel="stylesheet" type="text/css" href="/resources/css/redmond/jquery-ui-1.8.16.custom.css">
    <link rel="stylesheet" type="text/css" href="/resources/css/proddl.css">
</head>
<body>
<div id="top" style="text-align: right;">
    <form action="/pdl/w/logout" method="post" name="logoutform">
        Hello!! <%= request.getUserPrincipal().getName() %> <a href="javascript:sessionStorage.clear();document.logoutform.submit();">log
        out</a>
    </form>
</div>
<div id="main" style="width:100%;height:100%"></div>
</body>
</html>
