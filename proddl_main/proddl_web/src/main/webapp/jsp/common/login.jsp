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

<%@taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>PRODDL-C Login</title>

    <script src="/resources/js/jquery-1.6.2.min.js"></script>
    <script src="/resources/js/jquery-ui-1.8.16.custom.min.js"></script>
    <script>
        $(document).ready(function() {
            var $iserror = '${error}';
            if ($iserror != 'true')
                $(".ui-state-error").hide();

            $("#btnLogin").click(function() {
                //TODO needs some validation check here!!
                $("form").submit();
            });
        });

    </script>
    <link href="/resources/css/redmond/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css"/>
    <link href="/resources/css/main.css" rel="stylesheet" type="text/css"/>
    <style type="text/css">
        #login #content {
            width: 500px;
            margin: 20px auto;
        }

        #login section {
            font-size: 1em;
            width: 475px;
            background-color: #999999;
            position: relative;
            z-index: 1003;
        }

        #login header, #login section div.ui-widget-content {
            padding: 12px;
        }
    </style>
</head>
<body id="login">

<form action="/j_spring_security_check" method="post">
    <div id="content">
        <section class="ui-widget ui-corner-all">
            <header class="h1 ui-widget-header ui-corner-top">PRODDL-C Login</header>
            <div class="ui-widget-content ui-corner-bottom">
                <h1>Please use the login form below to login to PRODDL-C Cloud Management</h1>

                <div class="ui-state-error" style="padding: 0 .7em;margin-top: 15px">
                    <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
                        <strong>Alert:</strong> Login username/password incorrect.</p>
                </div>

                <form>
                    <div style="text-align: center;margin-top: 15px">
                        <label for="j_username">Username</label>
                        <input width="100%" id="j_username" name="j_username" type="text"/>
                    </div>
                    <div style="text-align: center;margin-top: 15px">
                        <label for="j_password">Password</label>
                        <input id="j_password" name="j_password" type="password"/>
                    </div>
                    <div style="text-align: center;margin-top: 15px">
                        <input type="submit"
                               id="btnLogin"
                               class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
                               role="button"
                               value="Log in"/>
                    </div>
                </form>
            </div>
        </section>
    </div>
    <div class="ui-widget-overlay" style="z-index: 1002;"></div>
</form>
</body>
</html>