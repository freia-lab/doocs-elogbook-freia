<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@ page info="Template logbook" %>
<%@ page import="
	javax.xml.parsers.DocumentBuilderFactory,
	javax.xml.parsers.DocumentBuilder,
	org.w3c.dom.*,
	java.util.Date,
	java.text.SimpleDateFormat,
	java.util.Calendar,
	java.util.GregorianCalendar,
	org.quartz.CronTrigger
" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>

<html>
 <c:import url="conf.xml" var="conf_xml" />
 <x:parse xml="${conf_xml}" var="conf" />

 <c:import url="work.xml" var="work_xml" />
 <x:parse xml="${work_xml}" var="work" />

 <head>
   <title>Left Frame</title>
   <link href="images/Icon.ico" rel="shortcut icon"></link>
   <link rel="stylesheet" type="text/css" href="/elogbook/xsl/frames-classic.css" title="Classic"></link>
   <link rel="stylesheet" type="text/css" href="/elogbook/xsl/frames-blue.css" title="Blue"></link>
   
   <script src="/elogbook/javascript/tree.js" type="text/javascript"></script>

   <!--script src="/elogbook/javascript/helpFunctions.js" type="text/javascript"></script-->
   <meta http-equiv="content-type" content="text/html;charset=iso-8859-1">
   <meta http-equiv="Pragma" content="no-cache">
   <%
    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
    DocumentBuilder db=dbf.newDocumentBuilder();
    String pfad = application.getRealPath("/");
    Document doc=db.parse(pfad + "/conf.xml");
    String logroot = "";
    NodeList name=doc.getElementsByTagName("logroot");
    for(int i=0;i<name.getLength();i++) {
        logroot = name.item(i).getFirstChild().getNodeValue();
    }
    name=doc.getElementsByTagName("new_shift");
    for(int i=0;i<name.getLength();i++) {
        String shiftrate = name.item(i).getFirstChild().getNodeValue();
        SimpleDateFormat formatter = new SimpleDateFormat();
        Calendar cal = new GregorianCalendar();
        int jahr = cal.get(Calendar.YEAR);   
        int month = cal.get(Calendar.MONTH);           // 0=Jan, 1=Feb, ...
        int day = cal.get(Calendar.DAY_OF_MONTH);      // 1...
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, ...
        int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        Calendar xmas = null;

        if (shiftrate.length()==1)
        {
            if(shiftrate.equalsIgnoreCase("Y")){
                // GregorianCalendar(int year, int month, int date, int hour, int minute) 
            jahr++;
                xmas = new GregorianCalendar(jahr, 0, 1, 0, 1);
            }
                    else
            if(shiftrate.equalsIgnoreCase("M")){
                if (month==11) {
                    jahr++;
                    month=0;
                }
                else {
                    month++;
                }
                xmas = new GregorianCalendar(jahr, month, 1);
            }
                    else
            if(shiftrate.equalsIgnoreCase("W")){
                Calendar max = new GregorianCalendar(jahr, Calendar.DECEMBER, 31);
                int maxWeeks = max.get(Calendar.WEEK_OF_YEAR);
                if(weekOfYear == maxWeeks && cal.get(Calendar.MONTH)==11) {
                jahr++;
                    xmas = new GregorianCalendar(jahr, 0, 1, 0, 1);
                }
                else {
                    xmas = new GregorianCalendar();
                    xmas.set(Calendar.YEAR, jahr);
                    xmas.set(Calendar.WEEK_OF_YEAR, weekOfYear+1);
                    xmas.set(Calendar.DAY_OF_WEEK, 2); // Montag?
                    xmas.set(Calendar.HOUR_OF_DAY, 0);
                    xmas.set(Calendar.MINUTE, 0);
                xmas.set(Calendar.SECOND, 55);
                }
            }
                    else
            if(shiftrate.equalsIgnoreCase("D")){
                int maxDays = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
                if(dayOfYear==maxDays) {
                jahr++;
                    xmas = new GregorianCalendar(jahr, 0, 1, 0, 1);
                }
                else {
                    xmas = new GregorianCalendar();
                    xmas.set(Calendar.YEAR, jahr);
                    xmas.set(Calendar.DAY_OF_YEAR, dayOfYear+1);
                    xmas.set(Calendar.HOUR_OF_DAY, 0);
                    xmas.set(Calendar.MINUTE, 0);
                xmas.set(Calendar.SECOND, 55);
                }
            }
                    else
            if(shiftrate.equalsIgnoreCase("3")){
                int maxDays = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
                if(dayOfYear==maxDays && hour>15) {
                jahr++;
                    xmas = new GregorianCalendar(jahr, 0, 1, 0, 1);
                }
                else {
                    xmas = new GregorianCalendar();
                    xmas.set(Calendar.YEAR, jahr);
                    if(hour>=23) {
                        xmas.set(Calendar.DAY_OF_YEAR, dayOfYear+1);
                        xmas.set(Calendar.HOUR_OF_DAY, 7);
                    }
                    else if(15<=hour && hour<23) {
                        xmas.set(Calendar.DAY_OF_YEAR, dayOfYear);
                        xmas.set(Calendar.HOUR_OF_DAY, 23);
                    }
                    else if(7<=hour && hour<15){
                        xmas.set(Calendar.DAY_OF_YEAR, dayOfYear);
                        xmas.set(Calendar.HOUR_OF_DAY, 15);
                    }
                    else { // hour<7
                        xmas.set(Calendar.DAY_OF_YEAR, dayOfYear);
                        xmas.set(Calendar.HOUR_OF_DAY, 7);
                    }
                    xmas.set(Calendar.MINUTE, 1);
                xmas.set(Calendar.SECOND, 55);
                }
            }
            // calculate time difference between cal and xmas in seconds
            long diffMillis = xmas.getTimeInMillis()-cal.getTimeInMillis();
            long diffSecs = diffMillis/(1000);
            out.println("<meta http-equiv=\"Refresh\" content=\""+diffSecs+"\">");
        }
        else        
        {      
            try
            {
                CronTrigger trigger;
                trigger = new CronTrigger("triggerName", "triggerGroup", shiftrate);
                Date d = trigger.getFireTimeAfter(new Date());
                long diffSecs = (d.getTime()-(new Date()).getTime())/1000;
                out.println("<meta http-equiv=\"Refresh\" content=\""+diffSecs+"\">");
            } 
            catch (Exception ex) 
            {
                
            }
        } 
    }
   %>
   <style type="text/css">

	.active { 
				background-color:#F0F0FF; 
				border:1px dotted #E5E5FF;
				font-weight : bold;
	}
	
	.leftSpace { 
				padding-left:15px;				
	}
		
	#DivRoot-content 
	{
		background-color:#F9F9F9;
		border:1px solid #EEE;
		white-space:nowrap;font-family:sans-serif;font-size:12px;
		height:300px;
		width:100%;
		overflow:auto;
		padding-bottom: 15px;	
	}
	
	.contentSpan
	{		
		vertical-align : super; 	
		cursor:pointer;
	}
	.contentSpanEmpty
	{
		vertical-align : super; 	
		cursor:pointer;
		color:#888888;
		font-style:italic;
	}
	
	#hideShowButton
	{
		background-color:#eeeeee;
		height:100%;
		width:5px;
		margin:0;
		padding:0;
		position : fixed;
		right:0px;
		top:0px;
		cursor:pointer;
	}
	
	ul#Navigation {
		margin:0pt;
		padding:0pt;
		width:128px;}
	* html ul#Navigation {
		padding-left:0.8em;
		width:10em;}
	ul#Navigation li {
		list-style-image:none;
		list-style-position:outside;
		list-style-type:none;
		margin:5px;
		padding:0pt;}
	ul#Printer {
		background-color:#BBBBBB;
		display:block;
		font-size:10px;
		font-weight:bold;}
	ul#Navigation a {
		color:black;
		display:block;
		font-family:arial,sans-serif;
		font-size:12px;
		font-weight:bold;
		padding:0.1em;
		text-decoration:none;}
	* html ul#Navigation a {
		width:8.8em;}
	ul#Navigation a:hover {
		background-color:#FFFFFF;
		color:grey;}	
</style>

   </head>
   <body text="black" onload="init('<x:out select="$conf/logbook/host"/>','<x:out select="$conf/logbook/logroot"/>','<x:out select="$conf/logbook/imagedir"/>','<x:out select="$conf/logbook/view_servlet"/>','<x:out select="$conf/logbook/view_xsl"/>','<x:out select="$work/work/act_dir"/>','<x:out select="$conf/logbook/datapath"/>');" bgcolor="<x:out select="$conf/logbook/bgcolor"/>" >

	<a id="debug" style="display:none;" href="javascript:showDebugText();">debug</a>	
	<!--div id="hideShowButton" style="display:none;" onclick="hideShow();"></div-->
    <div id="DivRoot"></div>

   <ul id="Navigation">
     <li>  
       <a href="javascript:showActualAddress('<x:out select="$work/work/act_dir"/>');" title="Show actual logbook page">View Current</a>       
     </li>
     <li>  
       <a href="/elog/search.jsp?elog=<x:out select="$conf/logbook/logroot" />" target="_self" title="Search in this logbook">Logbook Search</a>
     </li>
     <li>
       <a href="/elogbook/help/logbook_help_<x:out select="$conf/logbook/lang_code" />.html" target="list_frame" title="Help for this logbook">Logbook Help</a>
     </li>
     <li>
       <a href="<x:out select="$conf/logbook/view_servlet"/>?file=<x:out select="$conf/logbook/logroot" /><x:out select="$conf/logbook/datapath" /><x:out select="$conf/logbook/commentdir" />&xsl=<x:out select="$conf/logbook/view_xsl"/>&picture=true" target="list_frame" title="Your feedback for this logbook">Your Feedback</a>
     </li>
     <li>
       <a href="http://doocs.desy.de" target="list_frame" title="To add or change edit LOGBOOK/jsp/left.jsp">Example Link</a>
     </li>
   </ul>

   <table>
     <tr> 
       <td width="137" valign="top" align="left" xpos="0">
         <p><font size="-2">Printer: <b><x:out select="$conf/logbook/printer" /><b></font></p>
       </td>
     </tr>
     <tr>
       <td width="300" valign="top" align="left" xpos="0">
         <% String remuser = request.getRemoteUser();
	    if(remuser==null){
		remuser = "";
	    }
	    else {
		remuser = "Hello: " + remuser + "<br><a href=\"logout.jsp\" target=\"_top\"> Logout</a>";
	    }
	 %>
	 <p><font size="-2"><%= remuser %></b></font></p>
       </td>
     </tr>
     <tr>
       <td width="137" align="left" xpos="0">
         <p><a href="http://tesla.desy.de/doocs" target="new_frame"><img src="<x:out select="$conf/logbook/imagedir"/>/doocs_elog.gif" width="60" name="elogbook" border="0" title="DOOCS Home page"/></a></p>
       </td>
     </tr>
   </table>
 </body>
</html>
