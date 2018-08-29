<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 FRAMESET//EN">
<%@ page info="Template logbook" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<c:import url="conf.xml" var="conf_xml" />
<x:parse xml="${conf_xml}" var="conf" />



<c:set var="info_path">http://ttfinfo/e..</c:set>

	      
       <html>
	<head>
	 <meta http-equiv="content-type" content="text/html;charset=iso-8859-1">
	 <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	 <link href="<x:out select="$conf/logbook/logroot"/>/images/Icon.ico" rel="shortcut icon"></link>
	 <title>Create a new filder in the e-logbook</title>
	<%
	 String dir_name = null;
	 if ((dir_name = request.getParameter("dir")) == null) {
		dir_name ="newFolder";
	 }
	 %>
	<!--<x:out select="$conf/logbook/docroot"/><x:out select="$conf/logbook/logroot"/><x:out
	    select="$conf/logbook/datapath"/>/<%= dir_name %> -->
	<c:set var="dirpath"><x:out select="$conf/logbook/docroot"/><%= dir_name %></c:set>
	<c:set var="dir"><%= dir_name %></c:set>

	</head>

	<body text="black" bgcolor="<x:out select="$conf/logbook/bgcolor"/>">
	<h1><x:out select="$conf/logbook/name"/></h1>
	<form action="do_new_dir.jsp" method="get" >
		<input type="hidden" name="dirpath" value='<c:out value="${dirpath}"/>'>
	   <table border="0" height="43">
	    <tr>
	     <td align="right"><b>Create Folder in:</b></td>
	     <td><c:out value="${dir}"/></td>
	    </tr>
	    <tr>
	     <td align="right"><b>New Folder Name: </b></td>
	     <td><input  type="text" size="20" maxlength="20" name="NewDir" value="NewFolder"/></td>
	    </tr>
	    <tr>
	     <td align="right"><b>Description: </b></td>
	     <td><input  type="text" size="60" maxlength="80" name="Title" value=""/></td>
	    </tr>
	    <tr>
	      <td><input type="reset" value="Cancel"></td>
	      <td align="right"><input type="submit" value="Create" /></td>
	    </tr>
	   </table>
	</form>
	</body>
       </html>
