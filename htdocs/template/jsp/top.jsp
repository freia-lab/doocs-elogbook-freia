<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@ page info="Template logbook" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<c:import url="conf.xml" var="conf_xml" />
<x:parse xml="${conf_xml}" var="conf" />

<c:import url="status.xml" var="status_xml" />
<x:parse xml="${status_xml}" var="status" />

<c:set var="info_path"><x:out select="$conf/logbook/host"/>/elog/servlet/XMLSingle?dir=<x:out select="$conf/logbook/logroot"/>/data/news</c:set>
<c:import url="${info_path}" var="info_xml" />
<x:parse xml="${info_xml}" var="info" />

<html>
 <head>
  <meta http-equiv="content-type" content="text/html;charset=iso-8859-1">
  <meta http-equiv="expires" content="30">
  <meta http-equiv="Pragma" content="no-cache">
  <meta http-equiv="Refresh" content="300">
  <meta name="keywords" content="DOOCS, TTF, VUVFEL, XFEL, DESY, XML, JAVA, XSL, web-service"/>
  <link href="images/Icon.ico" rel="shortcut icon"></link>
  <title>Top Frame of the e-logbook</title>
 </head>
 
 <SCRIPT LANGUAGE="JavaScript" SRC="/elogbook/javascript/SearchUtil.js"></SCRIPT>
 
 <body text="black" bgcolor="<x:out select="$conf/logbook/bgcolor"/>"font="-1">
	<table width="100%"><tr><td width="80%">
	 <center><font size="-2">
	   <table border="0" height="43">
     <tr BGCOLOR="#CCFFFF">
      <td rowspan="2" ></td>
      <td><font size="-1"><b><x:out select="$status/status_info/status_val1"/></b></font></td>
      <td><font size="-1"><b><x:out select="$status/status_info/status_val2"/></b></font></td>
      <td><font size="-1"><jsp:useBean id="now" class="java.util.Date" /><fmt:formatDate value="${now}" pattern="dd.MM.yy"/><fmt:formatDate value="${now}" pattern=" HH:mm"/></font></td>
     </tr>
     <tr>
      <td><font size="-1"><x:out select="$status/status_info/status_val3"/> <x:out select="$status/status_info/status_val4"/></font></td>
      <td><font size="-1"><x:out select="$status/status_info/status_val5"/> <x:out select="$status/status_info/status_val6"/></font></td>
     </tr>
	    <tr BGCOLOR="#dddde6">
	     <td><font size="-1"><b>News:</b></font></td>
	     <td colspan="3"><font size="-1"><a href="<x:out select="$conf/logbook/view_servlet"/>?file=<x:out select="$conf/logbook/logroot"/>/data/news&xsl=<x:out select="$conf/logbook/view_xsl"/>&picture=true" target="list_frame"><x:out select="$info/list/entry/title"/></font></td>
    </tr>
	   </table>
	  </font>
	 </center>
        </td>
        <td align="right"><table height="43><tr><td align="right">
         <form name="search_form">
          <table cellspacing="0" cellpadding="0" border="0" align="right">
           <tbody><tr><td colspan="2"></td></tr>
            <tr align="right">
             <td><input  size="15" onKeyPress="return; javascript:assign('<x:out select="$conf/logbook/host"/>/elog/results.jsp?docroot=<x:out select="$conf/logbook/docroot"/>&logroot=<x:out select="$conf/logbook/logroot"/>&index=<x:out select="$conf/logbook/srch_index"/>&start=0&entries=100&xsl=/elogbook/xsl/search.xsl')" type="text" name="Search" value="Search" onfocus="if (value == 'Search') {value =''}" onblur="if (value == '') {value = 'Search'}"/></td>
             <td><input onclick="javascript:assign('<x:out select="$conf/logbook/host"/>/elog/results.jsp?docroot=<x:out select="$conf/logbook/docroot"/>&logroot=<x:out select="$conf/logbook/logroot"/>&index=<x:out select="$conf/logbook/srch_index"/>&start=0&entries=100&xsl=/elogbook/xsl/search.xsl')" type="image" class="searchbutton" alt="Search" src="/elogbook/images/search.png"/></td>
            </tr>
            <tr><td colspan="2"></td></tr>
            <tr><td align="left" colspan="2"><font size="-2"><a href="/elog/search_adv.jsp?elog=<x:out select="$conf/logbook/logroot" />" target="left_frame">Advanced Search</a></font></td></tr>
            <tr><td colspan="2"></td></tr>
           </tbody></table>
          </form>
          </td>
         </tr></table>
        </td></tr></table>
 </body>
</html>
