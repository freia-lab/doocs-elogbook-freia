<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 FRAMESET//EN">
<%@ page info="Template logbook" %>

<%@ page import="java.io.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<c:import url="conf.xml" var="conf_xml" />
<x:parse xml="${conf_xml}" var="conf" />

	      
<html>
 <head>
  <meta http-equiv="content-type" content="text/html;charset=iso-8859-1">
  <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
  <link href="<x:out select="$conf/logbook/logroot"/>/images/Icon.ico" rel="shortcut icon"></link>
  <title>Execute the creation of a new folder in the e-logbook</title>

 <c:set var="binpath" scope="request"><x:out select="$conf/logbook/docroot"/><x:out select="$conf/logbook/logroot"/>/bin</c:set>
 
 <%
  String dir_name = null;
  if ((dir_name = request.getParameter("dirpath")) == null) {
 	dir_name ="noParameter";
  }
  String new_dir = null;
  if ((new_dir = request.getParameter("NewDir")) == null) {
 	new_dir ="noFolder";
  }
  String title = null;
  if ((title = request.getParameter("Title")) == null) {
 	title ="";
  }
  String res = "ok";
  String binpath = request.getAttribute("binpath").toString();
  String callArg = null;
  String res2 = null;
  String callArg2 = null;
  try {
   callArg = "/export/web/htdocs/elogbook/bin/create_folder " + dir_name + " " + new_dir + " " + title;
   Process pr = Runtime.getRuntime().exec(callArg);
   pr.waitFor();

   java.io.InputStream in = pr.getInputStream();
   res = (new BufferedReader(new InputStreamReader(in))).readLine();

   callArg2 = "cd " + binpath + "; ./reader; echo 'done' ";
   Process pr2 = Runtime.getRuntime().exec(callArg2);
   pr2.waitFor();
   java.io.InputStream in2 = pr2.getInputStream();
   res2 = (new BufferedReader(new InputStreamReader(in2))).readLine();

  } catch (IOException e) {
     res = "Error: " + e.getMessage();
  }
  
  %>
  

 </head>

 <body text="black" bgcolor="<x:out select="$conf/logbook/bgcolor"/>">
 <h1><x:out select="$conf/logbook/name"/></h1>
 <table border="1" bgcolor="f0f0f0" cellpadding="5">
   <tr><td align="right">Creating a new Folder:</td> <td><b><%= new_dir%></b></td><td> </td></tr>
   <tr><td align="right">Dir path:</td> <td> <%= dir_name%></td></tr>
   <tr><td align="right">Return message from creation of folder:</td> <td>  <%= res%></td><td>Cmd: <%= callArg%></td></tr>
   <tr><td align="right">Return message from building the tree view:</td> <td>  <%= res2%></td><td>Cmd: <%= callArg2%></td></tr>
 </table>
 <br/>
 Please wait a minute for the update of the tree<br/><br/>
 <a href="javascript:history.go(-2)">Go Back</a>
 </body>
</html>
