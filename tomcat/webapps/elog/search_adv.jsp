<!!doctype html public "-//w3c//dtd html 4.0 transitional//en" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page info="Template logbook" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt"  prefix="fmt" %>

<c:import url="http://HOST:8080${param.elog}/conf.xml" var="conf_xml" />
<x:parse xml="${conf_xml}" var="conf" />

<c:set var="lang_code"><x:out select="$conf/logbook/lang_code"/></c:set>
<fmt:setLocale value="${lang_code}"/> 
<fmt:setBundle basename="de.desy.logbook.i18n.SearchBundle" />

<c:import url="http://HOST/elog/severities-${lang_code}.xml" var="severities_xml" />
<x:parse xml="${severities_xml}" var="severities" />

<c:import url="http://HOST:8080/${param.elog}/keywords.xml" var="keywords_xml" />
<x:parse xml="${keywords_xml}" var="keywords" />

<html>
 <head>
  <meta http-equiv="content-type" content="text/html"; charset=iso-8859-1">
  <META HTTP-EQUIV="Pragma">
  <link href="images/Icon.ico" rel="shortcut icon"></link>
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/frames-classic.css" title="Classic"></link>
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/frames-blue.css" title="Blue"></link>
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/default/main.css">
  <link rel="stylesheet" type="text/css" href="/elogbook/styles/default/calendar.css">
  
  <title><x:out select="$conf/logbook/name" /> search page</title>

	<SCRIPT LANGUAGE="JavaScript"
	SRC="/elogbook/javascript/CalendarPopup.js"></SCRIPT>
	<SCRIPT LANGUAGE="JavaScript" ID="js18">
	  var cal18 = new CalendarPopup("testdiv1");
	  //cal18.setCssPrefix("TEST");
	  cal18.setYearSelectStartOffset(4);
	  cal18.showYearNavigation();
	  cal18.showNavigationDropdowns();
          cal18.offsetX = -180;
	  cal18.setSize(140,175);
           cal18.setMonthNames('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');

	  // cal18.showNavigationDropdowns();
	</SCRIPT>

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
	var key	    = document.search_form.keyword.options[document.search_form.keyword.options.selectedIndex].value;
	<x:if select="$conf/logbook/location_list/@enabled='true'">
	   var loc  = document.search_form.location.options[document.search_form.location.options.selectedIndex].value;
        </x:if>
	var author  = document.search_form.author.value;	
	var sev	    = document.search_form.severity.options[document.search_form.severity.options.selectedIndex].value;
	var docs		= document.search_form.docs.options[document.search_form.docs.options.selectedIndex].value;
	var date1  	= document.search_form.date1.value;
	var date2  	= document.search_form.date2.value;
	var year1  	= "";
	var month1 	= "";
	var day1 	= "";
	var year2  	= "";
	var month2 	= "";
	var day2		= "";
	
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

	if((key != "")&&(key != "select")){
		while ( (i=key.search(' ')) != -1){
	 		key = key.replace(' ', '_');
		}
		ans = req + "&key=" + key;
		req = ans;
		
	}

	<x:if select="$conf/logbook/location_list/@enabled='true'">
	  if((loc != "")&&(loc != "select")){
		while ( (i=loc.search(' ')) != -1){
	 		loc = loc.replace(' ', '_');
		}
		ans = req + "&loc=" + loc;
		req = ans;
		
	  }
	</x:if>

	if(author != ""){
		/*while ( (i=author.search(' ')) != -1){
	 		author = author.replace(' ', '_');
		}*/
		ans = req + "&author=" + author;
		req = ans;		
	}

	if((sev != "") && (sev != "select")){
		while ( (i=sev.search(' ')) != -1){
	 		sev = sev.replace(' ', '_');
		}
		ans = req + "&severity=" + sev;
		req = ans;				
	}

	if(date1 != "dd/MM/yyyy" && date1 != ""){
		day1 = date1.slice(0, 2);
		month1 = date1.slice(3,5);
		year1 = date1.slice(6);
		ans = req + "&yr1=" + year1 + "&mon1=" + month1 + "&day1=" + day1;
		req = ans;	
		
	}
	
	if(date2 != "dd/MM/yyyy" && date2 != ""){
		day2 = date2.slice(0, 2);
		month2 = date2.slice(3,5);
		year2 = date2.slice(6);
		ans = req + "&yr2=" + year2 + "&mon2=" + month2 + "&day2=" + day2;
		req = ans;	
		
	}	

	/*if(year_1 != ""){
		ans = req + "&yr1=" + year_1;
		req = ans;				
	}

	if(month_1 != ""){
		ans = req + "&mon1=" + month_1;
		req = ans;				
	}

	if(year_2 != ""){
		ans = req + "&yr2=" + year_2;
		req = ans;				
	}

	if(month_2 != ""){
		ans = req + "&mon2=" + month_2;
		req = ans;				
	}*/

	if(docs != ""){
		ans = req + "&docs=" + docs;
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
    <tr><td><font size="-1" color="blue"><fmt:message key="LabelAllWords"/></font></td></tr>
    <tr><td><input name="textfield_all" type="text"  length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>
    
    <tr><td><font size="-1" color="blue"><fmt:message key="LabelExactPhrase"/></font></td></tr>
    <tr><td><input name="textfield_phr" type="text"  length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>
   
    <tr><td><font size="-1" color="blue"><fmt:message key="LabelAnyWord"/></font></td></tr>
    <tr><td><input name="textfield_or" type="text" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>   
    
    <tr><td><font size="-1" color="blue"><fmt:message key="LabelWithout"/></font></td></tr>
    <tr><td><input name="textfield_not" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"></td></tr>    

     <tr><td><b><fmt:message key="LabelAuthor"/></b> <br></td></tr>
     <tr><td><input name="author" type="text" width="140px" length="200" value="" onKeyPress="return submitenter(this,event)"><br></td></tr>
   </table>
	
	
   <table cellpadding="0px" cellspacing="0px" width="100%">
    <tr><td align = "left">
   	<b><fmt:message key="LabelKeyword"/></b><br>
    </td></tr>
    <tr><td align="left">
	<select size="1" name="keyword">
     <option value="select">-------------</option>
  	<x:forEach var="keyword" select="$keywords/keywords/keyword" >
  	<option value="<x:out select='$keyword/value'/>"><x:out select="$keyword/short"/></option>
  	</x:forEach>
	</select>
    </td></tr> 
    <tr><td align = "left"> 
   	<b><fmt:message key="LabelSeverity"/></b> <br>
    </td></tr>
    <tr><td align="left">   
    <select size="1" name="severity">
     <option value="select">-------------</option>
     
  <x:forEach var="sev" select="$severities/severities/special" >
  <option value="<x:out select='$sev/value'/>"><x:out select="$sev/name"/></option>
  </x:forEach>
  <x:forEach var="sev" select="$severities/severities/severity" >
  <option value="<x:out select='$sev/value'/>"><x:out select="$sev/name"/></option>
  </x:forEach>
  
     
    </select>
    </td></tr> 
    <x:if select="$conf/logbook/location_list/@enabled='true'">
    <tr><td align="left"> 
     <b><fmt:message key="LabelLocation"/></b> <br>
    </td></tr>
    <tr><td align="left">
     <select size="1" name="location">
      <option value="select">-------------</option>
      <x:forEach select="$conf/logbook/location_list/location" var="loc">
       <option value="<x:out select="$loc"/>"><x:out select="$loc"/></option>
      </x:forEach>
     </select>
         </td></tr>
    </x:if>
    </table>
	 
   <table width="100%"> 
	 <tr><td colspan="2"><fmt:message key="LabelFromDate"/></td></tr>
	 <tr>  
	  <td align="left" ><input name="date1" size="14" type="text" maxlength="10" value="dd/MM/yyyy"></td>
	  <td align="right"><A align="right" HREF="#"
					onClick="cal18.select(document.forms['search_form'].date1,'anchor18','dd/MM/yyyy'); return false;"
					NAME="anchor18" ID="anchor18"><img src="/elogbook/images/calendar1.gif" border="0" alt="date picker"></A>
	  </td>
	 </tr>	 
	 <tr><td colspan="2"><fmt:message key="LabelToDate"/></td></tr>
	 <tr>	
	  <td align="left" ><input name="date2" size="14" type="text" maxlength="10" value="dd/MM/yyyy"></td>
	  <td align="right"><A HREF="#"
					onClick="cal18.select(document.forms['search_form'].date2,'anchor18','dd/MM/yyyy'); return false;"
					NAME="anchor18" ID="anchor18"><img src="/elogbook/images/calendar1.gif" border="0" alt="date picker"></A>
	  </td>
  </table>	 
	 
   <table width="100%">
   	<tr><td> 

  <br><fmt:message key="LabelSearchTarget"/><br>
    <select name="docs">
     <option value=""><fmt:message key="OptionTargetLogbookDocs"/></option>
     <option value="1"><fmt:message key="OptionTargetLogbookOnly"/></option>
     <option value="2"><fmt:message key="OptionTargetDocsOnly"/></option>
    </select><br>
    </td></tr>
    </table>

  <ul id="Navigation">
     <li>  
       <a href="javascript:assign('/elogbook/xsl/search.xsl')" name="search_img" 
       	title="<fmt:message key="LinkTitleSearch"/>">
	<fmt:message key="StartSearch"/></a>
     </li>
     <li>  
       <a href="search.jsp?elog=<c:out value="${param.elog}" />" target="_self" name="Simple Search" 
       title="<fmt:message key="LinkTitleQuickSearch"/>">
       <fmt:message key="LinkQuickSearch"/></a>
     </li>
    <x:choose>
    <x:when select="$conf/logbook/top_navigation/@enabled = 'true'">
    </x:when>
    <x:otherwise>
     <li>  
       <a href="<c:out value="${param.elog}" />/index.jsp" target="_parent" name="back" 
       title="<fmt:message key="LinkTitleBack"/>">
       <fmt:message key="LinkBack"/></a>
     </li>
     </x:otherwise>
    </x:choose>
   </ul>

 </form>
 <DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;"></DIV> 
</body>
</html>
