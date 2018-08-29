<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN"
 "http://www.w3.org/TR/html4/frameset.dtd">

<%@ page info="e-logbook" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>

  <%-- --------------------------------------------------------------- --%>
	<%
	 String dir_file = null;
	 if ((dir_file = request.getParameter("dir")) == null) {
		dir_file ="";
	 }
	 String pos = null;
	 if ((pos = request.getParameter("pos")) == null) {
		pos ="";
	 }
	 %>
	<c:set var="dir_url"><%= dir_file %></c:set>
	<c:set var="pos"><%= pos %></c:set>

  <%-- --------------------------------------------------------------- --%>

<html>
 <c:import url="conf.xml" var="conf_xml" />
 <x:parse xml="${conf_xml}" var="conf" />

 <c:import url="work.xml" var="work_xml" />
 <x:parse xml="${work_xml}" var="work" />

 <head>
  <meta http-equiv="content-type" content="text/html;charset=iso-8859-1">
  <meta name="generator" content="dynamic HTML by servlets">
  <meta http-equiv="expires" content="0">
  <link href="images/Icon.ico" rel="shortcut icon"></link>
  <title><x:out select="$conf/logbook/name" /></title>
  <base target="blabla_top">
 </head>

 <frameset cols="145,*" framespacing="0" border="0" frameborder="NO" target="_top">
  <frameset rows="105,*" framespacing="0" border="0" frameborder="NO">
   <frame src="<x:out select="$conf/logbook/host"/><x:out select="$conf/logbook/logroot"/>/logo.jsp" name="logo_frame" scrolling="NO" noresize="noresize" />
   <frame src="left.jsp" name="left_frame" scrolling="NO" noresize="noresize" />
  </frameset>
  <frameset rows="105,*" framespacing="0" border="0" frameborder="NO">
   <frame src="top.jsp" name="top_frame" scrolling="NO" noresize="noresize"/>
   <frame src="<x:out select="$conf/logbook/host" /><x:out select="$conf/logbook/view_servlet"/>?file=<x:out
   select="$conf/logbook/logroot" /><x:out select="$conf/logbook/datapath" /><c:out value="${dir_url}" />&xsl=<x:out select="$conf/logbook/view_xsl"/>&picture=true#<c:out value="${pos}" />" name="list_frame" noresize="noresize"/>
  </frameset>
 </frameset>

 <noframes>
  <body>
   <h1>Page could not be displayed, because your browser supports no frames.</h1>
  </body>
 </noframes>

</html>
