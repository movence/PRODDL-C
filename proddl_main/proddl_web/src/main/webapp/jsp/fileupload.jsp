<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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

<c:if test="${!ajaxRequest}">
<html>
<head>
	<title>PRODDL-C: FILEUPLOAD</title>
	<link href="<c:url value="/resources/css/form.css" />" rel="stylesheet"  type="text/css" />
	<script type="text/javascript" src="<c:url value="/resources/js/jquery-1.6.2.min.js" />"></script>
	<script type="text/javascript" src="<c:url value="/resources/js/jquery-form-2.8.js" />"></script>
</head>
<body>
</c:if>
	<div id="fileuploadContent">
		<h2>File Upload</h2>
		<form id="fileuploadForm" action="/pdl/w/fileupload" method="POST" enctype="multipart/form-data" class="cleanform">
			<div class="header">
		  		<h2>Form</h2>
		  		<c:if test="${not empty message}">
					<div id="message" class="success">${message}</div>
		  		</c:if>
			</div>
			<label for="file">File</label>
			<input id="file" type="file" name="file" />
			<p><button type="submit">Upload</button></p>
		</form>
		<script type="text/javascript">
			$(document).ready(function() {
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
				$("#fileuploadForm").ajaxForm({ success: function(html) {
						$("#fileuploadContent").replaceWith(html);
					}
				});
			});
		</script>
	</div>
<c:if test="${!ajaxRequest}">
</body>
</html>
</c:if>