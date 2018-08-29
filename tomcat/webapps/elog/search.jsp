<!!doctype html public "-//w3c//dtd html 4.0 transitional//en" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page info="Template logbook" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<c:import url="http://HOST:8080${param.elog}/conf.xml" var="conf_xml" />
<x:parse xml="${conf_xml}" var="conf" />

<!--
<fmt:setLocale value="de"/> 
-->
<c:set var="lang_code"><x:out select="$conf/logbook/lang_code"/></c:set>
<fmt:setLocale value="${lang_code}"/> 
<fmt:setBundle basename="de.desy.logbook.i18n.SearchBundle" />

<html>
 <head>
  <meta http-equiv="content-type" content="text/html"; charset=iso-8859-1">
  <!--<meta http-equiv="expires" content="0">-->
  <!--META HTTP-EQUIV="Pragma" CONTENT="no-cache"-->
  <META HTTP-EQUIV="Pragma">
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/frames-classic.css" title="Classic"></link>
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/frames-blue.css" title="Blue"></link>
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/default/main.css">
  <link href="images/Icon.ico" rel="shortcut icon"></link>
  <title><x:out select="$conf/logbook/name" /> search page</title>

<script language="JavaScript" type="text/javascript">
<!--

function replaceSubstring(inputString, fromString, toString) {
// Goes through the inputString and replaces every occurrence of fromString with toString
	var temp = inputString;
	if (fromString == "") {
	      	return inputString;
	}
	if (toString.indexOf(fromString) == -1) { 
		// If the string being replaced is not a part of the replacement string (normal situation)
                while (temp.indexOf(fromString) != -1) {
			var toTheLeft = temp.substring(0, temp.indexOf(fromString));
			var toTheRight = temp.substring(temp.indexOf(fromString)+fromString.length, temp.length);
			temp = toTheLeft + toString + toTheRight;
		}
	} else { 
		// String being replaced is part of replacement string (like "+" being replaced with "++")
		// - prevent an infinite loop
                var midStrings = new Array("~", "`", "_", "^", "#");
                var midStringLen = 1;
                var midString = "";
                // Find a string that doesn't exist in the inputString to be used
                // as an "inbetween" string
                while (midString == "") {
                	for (var i=0; i < midStrings.length; i++) {
				var tempMidString = "";
				for (var j=0; j < midStringLen; j++) { tempMidString += midStrings[i]; }
				if (fromString.indexOf(tempMidString) == -1) {
                                        midString = tempMidString;
                                        i = midStrings.length + 1;
				}
                	}
		} // Keep on going until we build an "inbetween" string that doesn't exist

                // Now go through and do two replaces - first, replace the "fromString" with the "inbetween" string
		while (temp.indexOf(fromString) != -1) {
                	var toTheLeft = temp.substring(0, temp.indexOf(fromString));
                	var toTheRight = temp.substring(temp.indexOf(fromString)+fromString.length, temp.length);
                	temp = toTheLeft + midString + toTheRight;
		}

		// Next, replace the "inbetween" string with the "toString"
		while (temp.indexOf(midString) != -1) {
			var toTheLeft = temp.substring(0, temp.indexOf(midString));
			var toTheRight = temp.substring(temp.indexOf(midString)+midString.length, temp.length);
			temp = toTheLeft + toString + toTheRight;
		}

	} // Ends the check to see if the string being replaced is part of the replacement string or not

	return temp; // Send the updated string back to the user
} // Ends the "replaceSubstring" function


function assign(xsl_f){

	var str_all = document.search_form.textfield_all.value;
	var str_phr = document.search_form.textfield_phr.value;
	var str_or  = document.search_form.textfield_or.value;
	var str_not = document.search_form.textfield_not.value;			
	var author  = document.search_form.author.value;	
	
	var serv="<x:out select="$conf/logbook/host"/>/elog/results.jsp?docroot=<x:out
	select="$conf/logbook/docroot"/>&logroot=<x:out select="$conf/logbook/logroot"/>&index=<x:out
	select="$conf/logbook/srch_index"/>&start=0&entries=100&xsl=" + xsl_f;

	var ans;	
	var req = "";
	var str = "";
	var i;
	
	str = str_all;
	
	while(str_all != "" || str_phr!= "" || str_or != "" || str_not != ""){
	  
	  if(str != ""){ 
		/*while ( (i=str.search(' ')) != -1){
	 		str = str.replace(' ', '_');
		}
		while ( (i=str.search('%')) != -1){
	 		str = replaceSubstring(str, "%", "percent25");
		}
		while ( (i=str.search('\'')) != -1){
	 		str = str.replace('\'', '_');
		}*/
		
		while ( (i=str.search(/\+/)) != -1){
	 		str = replaceSubstring(str, "+", "%2b");
		}
		while ( (i=str.search('<')) != -1){
	 		str = replaceSubstring(str, "<", "%3c");
		}
		while ( (i=str.search('#')) != -1){
	 		str = replaceSubstring(str, "#", "%23");
		}
		/*while ( (i=str.search('=')) != -1){
	 		str = replaceSubstring(str, "=", "%3d");
		}*/
		while ( (i=str.search('&')) != -1){
	 		//str = replaceSubstring(str, "&", "%26");
			str = replaceSubstring(str, "&", " ");
		}
		while ( (i=str.search('ä')) != -1){
	 		str = replaceSubstring(str, "ä", "ae");
		}
		while ( (i=str.search('Ä')) != -1){
	 		str = replaceSubstring(str, "Ä", "Ae");
		}
		while ( (i=str.search('ö')) != -1){
	 		str = replaceSubstring(str, "ö", "oe");
		}
		while ( (i=str.search('Ö')) != -1){
	 		str = replaceSubstring(str, "Ö", "Oe");
		}
		while ( (i=str.search('ü')) != -1){
	 		str = replaceSubstring(str, "ü", "ue");
		}
		while ( (i=str.search('Ü')) != -1){
	 		str = replaceSubstring(str, "Ü", "Ue");
		}
		while ( (i=str.search('ß')) != -1){
	 		str = replaceSubstring(str, "ß", "ss");
		}
		
		if(str_all != ""){ 	
			ans = "&request_all=" + str; 
			str_all = "";
		}
		else if(str_phr != ""){ 
			ans = "&request_phr=" + str;
			str_phr = "";
		}
		else if(str_or != ""){
		 	ans = "&request_or=" + str;
			str_or = "";
		}
		else if(str_not != ""){
		 	ans = "&request_not=" + str;
			str_not = "";
		}
		
		req += ans;
	  }
	  
	  if(str_phr != ""){
	  	str = str_phr;
		continue;
	  }
	  if(str_or != ""){
	  	str = str_or;
		continue;
          }
	  if(str_not != ""){
	  	str = str_not;
		continue;
	  }
	}

	if(author != ""){
		/*while ( (i=author.search(' ')) != -1){
	 		author = author.replace(' ', '_');
		}*/
		ans = req + "&author=" + author;
		req = ans;		
	}

	ans = serv + req;
	//alert(serv);
	parent.list_frame.location.href=ans;
	return;
}
 
function submitenter(myfield,e)
{
var keycode;
if (window.event) keycode = window.event.keyCode;
else if (e) keycode = e.which;
else return true;

if (keycode == 13)
   {
   assign('/elogbook/xsl/search.xsl');
   return false;
   }
else
   return true;
} 
 
//-->
</script>

 </head>

 <body style="color: black; background-color: <x:out select="$conf/logbook/bgcolor"/>">
  <form name="search_form">
   <table cellpadding="0px" cellspacing="0px">
    <tr><td><b><fmt:message key="SectionTextTitle"/></b></td></tr>
    <tr><td><font size="-1"><fmt:message key="LabelAllWords"/></font></td></tr>
    <tr><td><input name="textfield_all" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>
    
    <tr><td><font size="-1"><fmt:message key="LabelExactPhrase"/></font></td></tr>
    <tr><td><input name="textfield_phr" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>
   
    <tr><td><font size="-1"><fmt:message key="LabelAnyWord"/></font></td></tr>
    <tr><td><input name="textfield_or" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>   
    
    <tr><td><font size="-1"><fmt:message key="LabelWithout"/></font></td></tr>
    <tr><td><input name="textfield_not" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>    

    <tr><td><b><fmt:message key="LabelAuthor"/></b> </td></tr>
    <tr><td><input name="author" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>
   </table>

  <ul id="Navigation">
     <li>  
       <a href="javascript:assign('/elogbook/xsl/search.xsl')" name="search_img" title="<fmt:message key="LinkTitleSearch"/>"><fmt:message key="StartSearch"/></a>
     </li>
     <li> 
       <a href="search_adv.jsp?elog=<c:out value="${param.elog}" />" target="_self" name="advanced" title="<fmt:message
       key="LinkTitleAdvanced"/>"><fmt:message key="LinkAdvanced"/></a>
     </li>
     <!--li> 
       <a href="search_pdf.jsp?elog=${param.elog}" target="_self" name="pdf search" title="Go to pdf search page">PDF search page</a>
     </li-->	  
    <x:choose>
    <x:when select="$conf/logbook/top_navigation/@enabled = 'true'">
    </x:when>
    <x:otherwise>
     <li>  
	
       <a href="<c:out value="${param.elog}" />/index.jsp" target="_parent" name="back" 
       title="<fmt:message key="LinkTitleBack"/>"><fmt:message key="LinkBack"/></a>
     </li>
     </x:otherwise>
    </x:choose>
   </ul>
	
 </form>
</body>
</html>
