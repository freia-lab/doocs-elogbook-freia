<%@ page import = "javax.servlet.*, javax.servlet.http.*, java.io.*, org.apache.lucene.analysis.*,
org.apache.lucene.analysis.standard.StandardAnalyzer, org.apache.lucene.analysis.standard.StandardTokenizer,
org.apache.lucene.document.*, org.apache.lucene.index.*, org.apache.lucene.search.*, org.apache.lucene.search.FilteredTermEnum,
org.apache.lucene.search.highlight.*,org.apache.lucene.queryParser.*, java.net.URLEncoder, java.util.*, java.util.Calendar, 
java.lang.*, javax.xml.transform.TransformerFactory, javax.xml.transform.Transformer, javax.xml.transform.OutputKeys, 
javax.xml.transform.Source,javax.xml.transform.sax.SAXResult, javax.xml.transform.stream.StreamSource, 
javax.xml.transform.stream.StreamResult "%><%@ page import = "org.apache.avalon.framework.logger.ConsoleLogger, org.apache.avalon.framework.logger.Logger,
org.apache.fop.apps.Driver, org.apache.fop.messaging.MessageHandler " %><%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jstl/xml"  prefix="x" %>
<jsp:directive.page contentType="text/html; charset=UTF-8" />
<%!

public String escapeHTML(String s) {
  s = s.replaceAll("&", "&amp;");
  s = s.replaceAll("<", "&lt;");
  s = s.replaceAll(">", "&gt;");
  s = s.replaceAll("\"", "&quot;");
  //s = s.replaceAll("'", "&apos;");
  s = s.replaceAll("!", "\u0021");  
  return s;
}
public String correct_escapeHTML(String s) {
  s = s.replaceAll("&amp;lt;","&lt;");
  s = s.replaceAll("&amp;gt;","&gt;");
  s = s.replaceAll("&amp;quot;", "&quot;");
  s = s.replaceAll("&amp;apos;", "&apos;");
  //s = s.replaceAll("<?xml", "&lt;?xml");
  //s = s.replaceAll("?>", "?&gt;");
  return s;
}
%><%!

	// Wiki markup procesing START
	// ===========================
	
	class Markup
	{
		public class singleMarkup
		{
			boolean	begOfLine;
			String	pattern;
			int		state;
			String	prolog;
			String	epilog;
			String	start;
			String	end;
			int		level;
			boolean add_br;
			public singleMarkup(boolean beg, String pat, int st, String pro, String epi, 
								String sta, String e, int l, boolean ab) 
			{
				begOfLine = beg; pattern = pat; state = st; prolog = pro; epilog = epi; 
				start = sta; end = e; level = l; add_br = ab;
			}
		}
		
		
		// singleMarkup constructor:
		// pattern_at_begin_of_line, pattern, number, HTML_prolog, HTML_epilog, HTML_start_tag, HTML_end_tag, level, add_br_at_end_of_line
		// number:
		// 1 .. 100:    2 * pattern as a bracket of the range or pattern at start of line 
		// 101 .. 200:  single pattern in a line (no terminating end pattern)
		// 201 .. :     URLs (termination in source is a blank or end_of_line)
		
		
		singleMarkup[] list = {
				new singleMarkup(false, "neverfindthispattern", 0, "", "", "", "", 0, true),
				new singleMarkup(true, "|",         1, "<table class='ut' >", "</table>", "<tr><td>", "</td></tr>", 0, false),
				new singleMarkup(true, "*",         2, "<ul>", "</ul>", "<li>",  "</li>", 1, false),
				new singleMarkup(true, " *",        3, "<ul>", "</ul>", "<li>",  "</li>", 2, false),
				new singleMarkup(true, "  *",       4, "<ul>", "</ul>", "<li>",  "</li>", 3, false),
				new singleMarkup(true, "#",         5, "<ol>", "</ol>", "<li>",  "</li>", 1, false),
				new singleMarkup(true, " #",        6, "<ol>", "</ol>", "<li>",  "</li>", 2, false),
				new singleMarkup(true, "  #",       7, "<ol>", "</ol>", "<li>",  "</li>", 3, false),
				new singleMarkup(false, "__",       8, "",     "",      "<b>",   "</b>",  0, true),
				new singleMarkup(true, "----",      9, "",     "",      "<hr />", "",     0, false),
				new singleMarkup(true, "!!",       10, "",     "",      "<h2>", "</h2>",  0, false),
				new singleMarkup(true, "!",        11, "",     "",      "<h1>", "</h1>",  0, false),
				new singleMarkup(false, "``",       12, "",     "",      "<i>",   "</i>",  0, true),
				new singleMarkup(false, "_`",       13, "",     "",      "<b><i>",   "</i></b>",  0, true),
				new singleMarkup(true, "{{",       14, "<div class='cb'>",     "</div>",      "", "", -1, true),
				new singleMarkup(true, "}}",       15, "",     "",      "", "",  -2, false),
				//new singleMarkup(true, "}}",       14, "<div class='pp'>",     "</div>",      "", "",  0, false),
				//new singleMarkup(false, "//",    101, "",     "",       "<br />",    "",  0, false),
				new singleMarkup(false, "http:",  201, "",     "",      "<a>",   "</a>",  0, true),
				new singleMarkup(false, "https:", 202, "",     "",      "<a>",   "</a>",  0, true),
				new singleMarkup(false, "file:",  203, "",     "",      "<a>",   "</a>",  0, true),
				new singleMarkup(false, "mailto:",204, "",     "",      "<a>",   "</a>",  0, true),
				new singleMarkup(false, "neverfindthispattern", 0, "", "", "",   "",      0, true)			
		};
		
		singleMarkup sm;
		
		public Markup() { };

		public int setBegLine(StringBuffer line)
		{
			int s = 0, l;
			int j = 0;
			//SingleMarkup sm=0;
			for (int i=0; i < list.length; i++) {
				sm = list[i];
				if (sm.begOfLine && ((j=line.indexOf(sm.pattern)) == 0)) {
					s = sm.state;
					line.replace(j, j+sm.pattern.length(), sm.start).append(sm.end);
                                    //if ( line.indexOf("!!") == 0) line.insert(0,"=="+new Integer(s).toString()+"=="); // debug
					if (sm.prolog.startsWith("<table")) {
						while ((j=line.indexOf(sm.pattern, j)) > 0) {
							l = sm.pattern.length();
							line.replace(j, j+l, "</td><td>");
						}
					}
					if (sm.add_br == false)	do_br = false;
					return s;
            }
			}
			//??? if (list[s].add_br == false)	do_br = false;
			return s;
		}
		public int setInLine(StringBuffer line)
		{
			int s = 0;
			int i, j;
			//SingleMarkup sm=0;
			for (i=0; i < list.length; i++) {
				sm = list[i];
				if (!sm.begOfLine) {
					int i2 = 0;
					while (i2 >= 0 && ((j=line.indexOf(sm.pattern, i2)) >= 0)) {
                         if (sm.state > 100 && sm.state < 200) {   // single patterns in line
							   line.replace(j, j+sm.pattern.length(), sm.start);
							   i2 = j+sm.start.length();
                         } else if (sm.state > 200) {   // URLs
                               i2 = line.indexOf(" ", j+1);
                               if (i2 >= 0)    line.insert(i2, sm.end);
                               else            line.append(sm.end);
                               line.insert(j, sm.start);
                         } else {
                               i2 = line.indexOf(sm.pattern, j+sm.pattern.length());
                               int itable = line.indexOf("|", j+sm.pattern.length());
                               if ((i2>0 && itable<0 ) || (i2>0 && (itable>0 && i2<itable))){
							   		line.replace(i2,i2+sm.pattern.length(), sm.end);
							   		line.replace(j,j+sm.pattern.length(), sm.start);
                                                           //System.out.println("!!!!!!!!!\n"+line.toString());
						       }
                         }
                                            s = i;
					}
				}
			}
			if (s != 0 && list[s].add_br == false)	do_br = false;
			return s;
		}
		boolean do_br;


		public int evalMarkup(StringBuffer line, Stack stack)
		{
			int s, si, i, l, n;
			do_br = true;
			si = setInLine(line);	// set all in a line
			if (!stack.empty() && ((Integer)stack.peek()).intValue() == 14) {
							s = 0;
							// ignore markup in 'code block' except the end
							if (line.indexOf("}}") == 0)	s = setBegLine(line);
			} else		s  = setBegLine(line);
			if (do_br)	line.append("<br/>");
			int onstack = 0;
			int s0=0;
			String tag = "";
			if (!stack.empty()) onstack = ((Integer)stack.peek()).intValue();
			if (s != onstack && 0 != onstack) {				// epilog of last
				if (list[onstack].level > 0) {
					n = list[onstack].level - list[s].level; // stack level - actual level
					for (i = 0; i < n; i++) {
						if (!stack.empty()) {
							s0 = ((Integer)stack.pop()).intValue();
							tag = tag + list[s0].epilog;
						}
					}
					//line.append(" �StackEpiGT�"+list[s0].pattern+"�"); // debug
				} else if (list[onstack].level < 0) {
						if (list[s].level < 0) {
							s0 = ((Integer)stack.pop()).intValue();
							tag = tag + list[s0].epilog;
							//line.append(" �StackEpiLT�"+list[s0].pattern+"�"); // debug
						}
				} else {
					s0 = ((Integer)stack.pop()).intValue();
					tag = list[s0].epilog;
					//line.append(" �StackEpi0�"+list[s0].pattern+"�"); // debug
				}
			}
			onstack = 0;
			if (!stack.empty()) onstack = ((Integer)stack.peek()).intValue();
			if (s != 0 && s != onstack) {					// prolog of this
				if (list[s].prolog.length() > 0) {
					if (list[s].level > 0) {
						n = list[s].level - list[onstack].level; // actual level - stack level
						for (i = 0; i < n; i++) {
							tag = tag + list[s].prolog;
							stack.push(new Integer(s));
						}
						//line.append(" �StackProGT�"+list[s].pattern+"�"); // debug
					} else if (list[s].level == -1) {
						if (list[onstack].level >= 0) {
							tag = tag + list[s].prolog;
							stack.push(new Integer(s));
						//line.append(" �StackProLT�"+list[s].pattern+"�"); // debug
						}
					} else if (list[s].level == 0){
						tag = tag + list[s].prolog;
						stack.push(new Integer(s));
						//line.append(" �StackPro0�"+list[s].pattern+"�"); // debug
					}
				}
			}
			// limit line length to 90:
         l = line.indexOf("<text>",0) + 96;
			while ((n = line.indexOf(" ",l)) > 0) {
                            line.insert(n, "<br/>");
                            l = n + 90;
                        }
			line.insert(0, tag);
			return s;
		}
	
		public void processMarkup(BufferedReader in ,StringBuffer out)
		{
			int det = 0, s, i;
			Stack stack = new Stack();
			StringBuffer line;// = new StringBuffer();
			String	l, rest;
                       rest = "";
			boolean in_text = false;
			//BufferedReader in = new BufferedReader(file_in);
			try {
				while ((l = in.readLine()) != null) {
					line = new StringBuffer(l);
               if ((i = line.indexOf("<text>")) >= 0) { // remove stuff before <text>
							out.append(line.substring(0, i+6));
							line.replace(0, i+6, "");
							//line.append(" _ V3.01 _ "); // debug
							in_text = true;
               }
               if ((i = line.indexOf("</text>")) >= 0) { // add stuff after </text> later
							rest = line.substring(i);
							line.replace(i, line.length(), "");
               }
					if (in_text)   evalMarkup(line, stack);
					//             ==========
               if (i >= 0) {    // end of <text>
							in_text = false;
							while (!stack.empty()) {	// close all html tags
                              s = ((Integer)stack.pop()).intValue();
					  					//out.append(" ?Stack?"); // debug
                              line.append(list[s].epilog);
							}
							out.append(line.toString());//.append(rest);
							while (!stack.empty()) {	// close all html tags, should have been closed before
                                    s = ((Integer)stack.pop()).intValue();
                                    out.append(list[s].epilog);// line.append(list[s].epilog);
												//out.append("?Stuff in Stack? "); // debug
							}
							out.append(rest); 
                } else {
                            out.append(line.toString());
					    			 if (in_text && (sm==null)) out.append("<br/>");
                }
				}
			} catch	(Exception ioe) {
				out = out.append("<p>Error in reading: " + ioe.toString() + "</p>");
			}
		}
	}
	// Wiki markup processing END
	// ==========================
	

%><% 
		out.println("<html><body>");

      boolean    			error = false;                //used to control flow for error messages
      String   			indexName = null;
      IndexSearcher 		searcher = null;              //the searcher used to open/search the index
      Query 				query = null;                 //the Query created by the QueryParser
      Hits 					hits = null;                	//the search results
      int 					startindex = 0;          		//the first index displayed on this page
      int 					maxpage    = 100;            	//the maximum items displayed on this page
      String 				queryString = null;         	//the query entered in the search page
      String 				query_all = null;
      String 				query_phr = null;
      String 				query_or = null;
      String 				query_not = null;
      String 				authorString = null; 
      String 				logname = null;
      String 				sev = null; 
      String 				key = null; 
      String 				loc = null; 
      String 				yr1 = null; 
      String 				mon1 = null; 
      String 				day1 = null; 
      String 				yr2 = null; 
      String 				mon2 = null; 
      String 				day2 = null; 
      String 				phrase = null;
      String				docs = null;
      String 				xsl = null; 	
      String 				startVal    = null;           //string version of startindex
      String 				maxresults  = null;           //string version of maxpage
      int 					thispage = 0;                 //used for the for/next either maxpage or
                                                	 	//hits.length() - startindex - whichever is
                                                	  	//less
																		 	
      String				docroot = request.getParameter("docroot");
      String 				logroot = request.getParameter("logroot");
      String 				index = request.getParameter("index");																		 																	
      String 				pic_param = request.getParameter("picture");
      String 				format_param = request.getParameter("format");
		
      boolean 				do_PDF = false;																											
      String 				complexQuery = null;
 
      String				sparam = null;
      Analyzer 			analyzer = null;		  
 		
      org.apache.avalon.framework.logger.Logger  log = null;
      boolean 				exception_occured = false;
      Highlighter 		highlighter = null;
		Highlighter 		highlighter2 = null;
      String 				result = null;		
		
      if (docroot == null || logroot == null) throw new ServletException("docroot/logroot not specified");
      if (index == null ) index = "/work/index";
		
      indexName = docroot + logroot + index;		// absolute path to logbook index
      try {	
			//searcher = new IndexSearcher(IndexReader.open(indexName));
			if(indexName == null)  indexName = "";
			searcher = new IndexSearcher(indexName);
			
      } catch (Exception e) {                           //any error that happens is probably due
                                                        //to a permission problem or non-existant
                                                        //or otherwise corrupt index
         out.println("<p>ERROR opening the Index - contact sysadmin!</p>");
         out.println("<p>Error message: " + escapeHTML(e.getMessage())+ "</p>");            
	  		error = true;                          
      }

  if (error == false) {                                           //did we open the index?
     	    request.setCharacterEncoding("UTF-8");
	 queryString 	= request.getParameter("request");            //get the search criteria
	 query_all		= request.getParameter("request_all");
	 query_phr		= request.getParameter("request_phr");
	 query_or		= request.getParameter("request_or");
	 query_not		= request.getParameter("request_not");	  
	 phrase 			= request.getParameter("phrase");
	 authorString	= request.getParameter("author");          
	 sev 				= request.getParameter("severity");          
	 key 				= request.getParameter("key");
	 loc 				= request.getParameter("loc");
	 yr1 				= request.getParameter("yr1");          
	 mon1 			= request.getParameter("mon1");          
	 day1 			= request.getParameter("day1");   	
	 yr2 				= request.getParameter("yr2");          
	 mon2 			= request.getParameter("mon2");
	 day2    		= request.getParameter("day2");    		 
	 xsl 				= request.getParameter("xsl");         
	 startVal    	= request.getParameter("start");         		//get the start index
	 maxresults  	= request.getParameter("entries");      		//get max results per page
	 docs 			= request.getParameter("docs");
	 logname 		= request.getParameter("logname");
			
	 try {
		maxpage    	= Integer.parseInt(maxresults);    		//parse the max results first
		startindex 	= Integer.parseInt(startVal);      		//then the start index  
	 } catch (Exception e) { } 						//we don't care if something happens 
                                          					//we'll just start at 0 or end at 50               

	 if( queryString == null &&  query_all == null && query_phr == null && query_or == null 
	    && query_not == null && authorString == null  && sev == null && key == null 
	    && loc == null && yr1 == null && mon1 == null && yr2 == null && mon2 == null 
	 ){           
		throw new ServletException("no query specified");
					
	 }      		
								
	 //construct our usual analyzer
	 analyzer = new StandardAnalyzer();
		
	 sparam = "docroot=" + docroot + "&amp;logroot=" + logroot + "&amp;index=" + index;
		
	 if(queryString != null) {
		sparam  += "&amp;request=" + queryString;
		if(phrase != null){ 						
			complexQuery = "(text:\"" + queryString + "\" OR title:\"" + queryString + "\")";
			sparam +="&amp;phrase=yes";
		}else{
			StringTokenizer st = new StringTokenizer(queryString, " \t\n\r\f,;");
			String text = "";
			String title = "";
			String next = "";
			while (st.hasMoreTokens()) {
				next = st.nextToken();
				if (text == "" ) text +=  "*" + next + "*";
				else		 text += " AND *" + next + "*";
				
				if(title == "" ) title += "title:*" + next + "*";
				else		 title += " AND title:*" + next + "*";
			}
			complexQuery = "( (" + text + ") OR (" +  title + ") )";
		}
		    	
	 }else{

		if(query_all != null ){
			sparam  += "&amp;request_all=" + query_all;
			
			StringTokenizer st = new StringTokenizer(query_all, " \t\n\r\f,;");
			String text = "";
			String title = "";
			String next = "";
			
			while (st.hasMoreTokens()) {
				next = st.nextToken();
				if (text == "" ) text +=  "*" + next + "*";
				else		 text += " AND *" + next + "*";

				if(title == "" ) title += "title:*" + next + "*";
				else		 title += " AND title:*" + next + "*";
			}
			if(complexQuery != null)
				complexQuery += " AND ( (" + text + ") OR (" +  title + ") )";
			else
				complexQuery = "( (" + text + ") OR (" +  title + ") ) ";
		}
	
		if(query_phr != null ){
			sparam  += "&amp;request_phr=" + query_phr;

			if(complexQuery != null)
				complexQuery += " AND (text:\"" + query_phr + "\" OR title:\"" + query_phr + "\")";
			else
				complexQuery = "(text:\"" + query_phr + "\" OR title:\"" + query_phr + "\")";
		}
		
		if(query_or != null ){
			sparam  += "&amp;request_or=" + query_or;
			
			StringTokenizer st = new StringTokenizer(query_or, " \t\n\r\f,;");
			String text = "";
			String title = "";
			String next = "";
			
			while (st.hasMoreTokens()) {
				next = st.nextToken();
				if (text == "" ) text +=  "text:*" + next + "*";
				else		 text += " OR text:*" + next + "*";

				if(title == "" ) title += "title:*" + next + "*";
				else		 title += " OR title:*" + next + "*";
			}
			if(complexQuery != null)
				complexQuery += " AND (" + text + " OR " +  title + ") )";
			else
				complexQuery = "( " + text + " OR " +  title + " ) ";
		}
		
		if(query_not != null ){
			sparam  += "&amp;request_not=" + query_not;
			
			StringTokenizer st = new StringTokenizer(query_not, " \t\n\r\f,;");
			String text = "";
			String next = "";
			
			while (st.hasMoreTokens()) {
				next = st.nextToken();				
				text += " NOT (text:*" + next + "* OR title:*" + next + "*) ";
			}
			
			if(complexQuery != null)
				complexQuery += " AND  " + text ;
			else
				complexQuery = "( " + text + " ) ";
		}
						
			
	}
		
	if(authorString != null ){			
		StringTokenizer st = new StringTokenizer(authorString, " \t\n\r\f,;");
		String next = "";
		boolean first = true;
		while (st.hasMoreTokens()) {
			next = st.nextToken();
			if (first){ 
				if(complexQuery != null)
					complexQuery += " AND ( author:*" + next + "*" ;
				else
					complexQuery = "( author:*" + next + "*" ;
				first = false;
			}else
				complexQuery = complexQuery + " OR author:*" + next + "*";					

		}
		complexQuery += ")";											
		sparam += "&amp;author=" +	authorString;
	}
		
	if(sev != null) {			
		StringTokenizer st = new StringTokenizer(sev, " \t\n\r\f,;");
		String next = "";
		boolean first = true;
		while (st.hasMoreTokens()) {
			next = st.nextToken();
			if (first){ 
				if(complexQuery != null)
					complexQuery += " AND ( severity:*" + next.toUpperCase() + "*" ;
				else
					complexQuery = "( severity:*" + next.toUpperCase() + "*" ;
				first = false;
			}else
				complexQuery = complexQuery + " OR severity:*" + next + "*";					

		}
		complexQuery += ")";
		sparam += "&amp;severity=" + sev;    
	}
		

	if(loc != null) {
		if(complexQuery != null ) 
			complexQuery = complexQuery + " AND location:" + loc;
		else
			complexQuery = "location:" + loc;
			sparam += "&amp;location=" + loc;		    
	}
		
	if(logname != null) {
		if(complexQuery != null ) 
			complexQuery = complexQuery + " AND dirpath:*" + logname + "*";
		else
			complexQuery = "dirpath:*" + logname + "*";
		sparam += "&amp;logname=" + logname;		    
	}
		
	if(key != null) {
		if(complexQuery != null ) 		
			complexQuery = complexQuery + " AND keywords:" + key;
		else
			complexQuery = "keywords:" + key;
		sparam += "&amp;key=" + key;	    
	}
		
	if(yr1 != null || yr2 != null) {		
		if(complexQuery != null )
			complexQuery = complexQuery + " AND luc_date:";
		else
			complexQuery = "luc_date:";
		
		    
		if (yr1 != null){
			sparam += "&amp;yr1=" + yr1;
			if(mon1 != null){
			   if(day1 != null){
				   yr1 = yr1 + mon1 + day1;
				   sparam += "&amp;mon1=" + mon1 + "&amp;day1=" + day1;
				}else{
					yr1 = yr1 + mon1 + "01";
					sparam += "&amp;mon1=" + mon1;
				}
			}else		 yr1 = yr1 + "0101";
		}else
			yr1 = "20010101";
		    
		if(yr2 != null){
			sparam += "&amp;yr2=" + yr2;
			if(mon2 != null){			
				if(day2 != null){	
					yr2 = yr2 + mon2 + day2;
					sparam += "&amp;mon2=" + mon2 + "&amp;day2=" + day2;				
				}else{			
					yr2 = yr2 + mon2 + "31";
					sparam += "&amp;mon2=" + mon2;
				}
			}else		
				yr2 = yr2 + "1231";
		}else{
		    	
			Calendar cld = Calendar.getInstance();
			cld.setTimeInMillis(System.currentTimeMillis());
			String m2 = null;
			String d2 = null;
					
			if (cld.get(2)+1 <= 10) m2 = "0" +Integer.toString(cld.get(2)+1);
			else			m2 = Integer.toString(cld.get(2)+1);
					
			if (cld.get(5) < 10) 	d2 = "0" +Integer.toString(cld.get(5));
			else			d2 = Integer.toString(cld.get(5));
										
			yr2 = Integer.toString(cld.get(1)) + m2 + d2; 
		}
				    
		complexQuery = complexQuery + "[" + yr1 + " TO " + yr2 + " ]";
	 }
	
 	 if(docs != null){
		sparam = sparam + "&amp;docs=" + docs;
		if(docs.compareTo("1") == 0) complexQuery += " AND NOT docs:yes";		
		if(docs.compareTo("2") == 0) complexQuery += " AND docs:yes";					
	 }
			
	 if(xsl != null)  		sparam += "&amp;xsl=" + xsl;
	 if(maxresults != null) 		sparam += "&amp;entries=" + maxresults;
		 	
	 try {	
		//parse the query and construct the Query:  
		QueryParser qp = new QueryParser("text", analyzer);
		query = qp.parse(complexQuery);	
															  
	 } catch (ParseException e) {       //send a nice error HTML 	                                                               
          	//out.println("<!--p>Error while parsing query: " + escapeHTML(e.getMessage()) + "</p-->");
           	error = true;                   //don't bother with the rest of the page                                                                 
	 }
	
  } //end of "if (error == false)"

  if (error == false && searcher != null) {               // if we've had no errors
                                                          // searcher != null was to handle
	 try{                                                  // a weird compilation bug 
	
		thispage = maxpage;                                 // default last element to maxpage
		Sort mysort = new Sort("luc_date", true);

		////query = query.rewrite(IndexReader.open(indexName));
		//hits = searcher.search(query, null, mysort);  

		BooleanQuery bquery = new BooleanQuery();
		bquery.add(query, BooleanClause.Occur.SHOULD);
		bquery.setMaxClauseCount(50000);			
		hits = searcher.search(bquery, null, mysort);        // run the query 

	}catch(BooleanQuery.TooManyClauses e){
		error = true; 				 
		//exception_occured = true;
		throw new ServletException("query was: " + complexQuery + "\nThis resolves to too many search terms, please, try \"Exact phrase\" search\n");
	}
	catch(Exception e){
	
		error = true; 
		exception_occured = true;
		//throw new ServletException("query was: " + complexQuery + ", try \"Exact search\" checkbox");		
	 }
	
	 //if (true)	throw new ServletException("query was: " + complexQuery + ", try \"Exact search\" checkbox");
					
	 /*if (hits.length() == 0) {                       // if we got no results tell the user
	
		//out.println("<p> I'm sorry I couldn't find what you were looking for. </p>");
		error = true;                              // don't bother with the rest of the page
		
      //throw new ServletException("query was: " + complexQuery + ", hits.length == 0 , error occured on search");                                                     
	 }*/
  }

  if (searcher != null) { 
			 
	try{ 
		//PrintWriter		outp = new PrintWriter(response.getOutputStream());	
		StringBuffer 		xmlout = new StringBuffer();
		   
	      
		xmlout.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		xmlout.append("<list>\n");
		xmlout.append("<sparam>" + sparam + "</sparam>\n");
		
		if(queryString != null){
			xmlout.append("<req_text>"+escapeHTML(queryString)+"</req_text>\n");
		}
		if(query_all != null){
			xmlout.append("<req_all>"+escapeHTML(query_all)+"</req_all>\n");
		}		
		if(query_phr != null){
			xmlout.append("<req_phr>"+escapeHTML(query_phr)+"</req_phr>\n");
		}
		if(query_or != null){
			xmlout.append("<req_or>"+escapeHTML(query_or)+"</req_or>\n");
		}
		if(query_not != null){
			xmlout.append("<req_not>"+escapeHTML(query_not)+"</req_not>\n");
		}											
		if(authorString != null){
			xmlout.append("<auth>"+escapeHTML(authorString)+"</auth>\n");
		}
		if(key != null){
			xmlout.append("<keywds>"+key+"</keywds>\n");
		}
		if(loc != null){
			xmlout.append("<loc>"+loc+"</loc>\n");
		}
		if(sev != null){
			xmlout.append("<sev>"+sev.toUpperCase()+"</sev>\n");
		}
		
		if(yr1 != null){
			xmlout.append("<yr1>"+yr1+"</yr1>\n");
		}
		if(yr2 != null){
			xmlout.append("<yr2>"+yr2+"</yr2>\n");
		}
		
	/*	if(mon1 != null){
			xmlout.append("<mon1>"+mon1+"</mon1>\n");
		}
		if(mon2 != null){
			xmlout.append("<mon2>"+mon2+"</mon2>\n");
		}
		if(day1 != null){
			xmlout.append("<day1>"+day1+"</day1>\n");
		}
		if(day2 != null){
			xmlout.append("<day2>"+day2+"</day2>\n");
		}   */
		
	   if (error == false){  
				if ((startindex + maxpage) > hits.length()) {
						thispage = hits.length() - startindex;      // set the max index to maxpage or last
				}                                           	     // actual search result whichever is less
				
				BooleanQuery	bq = new BooleanQuery();
			
		    	if(queryString != null){ 
					StringTokenizer st = new StringTokenizer(queryString.toLowerCase(), " \t\n\r\f,:;");
					String qstr = "";
					if(phrase != null) {
						PhraseQuery pq = new PhraseQuery();
						while (st.hasMoreTokens()) {
							qstr = st.nextToken();
							pq.add(new Term("text",  qstr));
						}
						bq.add(pq, BooleanClause.Occur.MUST);
					}else{
			    		while (st.hasMoreTokens()) {
							qstr = st.nextToken();
							if ( (qstr.indexOf('*') != -1) || (qstr.indexOf('?') != -1)){
								WildcardQuery	query2 = new WildcardQuery(new Term("text",  qstr));
								bq.add(query2, BooleanClause.Occur.MUST);
					
							}else if((qstr.indexOf('~') != -1)){ 
								FuzzyQuery query3 = new FuzzyQuery(new Term("text",  qstr));
								bq.add(query3, BooleanClause.Occur.MUST);
							}else{  
								TermQuery query4 = new TermQuery(new Term("text",  qstr));
								bq.add(query4, BooleanClause.Occur.MUST);
							}
			    		}	
					}		
			
		    	}else{
				
					if(query_all != null){
						StringTokenizer st = new StringTokenizer(query_all.toLowerCase(), " \t\n\r\f,:;");
						String qstr = "";
					
						while (st.hasMoreTokens()) {
							qstr = "*" + st.nextToken() + "*";
							if ( (qstr.indexOf('*') != -1) || (qstr.indexOf('?') != -1)){
								WildcardQuery	query2 = new WildcardQuery(new Term("text",  qstr));
								bq.add(query2, BooleanClause.Occur.MUST);					
							}else if((qstr.indexOf('~') != -1)){ 
								FuzzyQuery query3 = new FuzzyQuery(new Term("text",  qstr));
								bq.add(query3, BooleanClause.Occur.MUST);
							}else{  
								TermQuery query4 = new TermQuery(new Term("text",  qstr));
								bq.add(query4, BooleanClause.Occur.MUST);
							}
			    		}
					
					}
				
					if(query_phr != null){
						PhraseQuery query5 = new PhraseQuery();
						StringTokenizer st = new StringTokenizer(query_phr.toLowerCase(), " \t\n\r\f,:;");
						while (st.hasMoreTokens()) {
							query5.add(new Term( "text", st.nextToken() ));
						}					
					
						bq.add(query5, BooleanClause.Occur.MUST);
					}
				
					if(query_or != null){
						StringTokenizer st = new StringTokenizer(query_or.toLowerCase(), " \t\n\r\f,:;");
						String qstr = "";
					
						while (st.hasMoreTokens()) {
							qstr = "*" + st.nextToken() + "*";
						
							if ( (qstr.indexOf('*') != -1) || (qstr.indexOf('?') != -1)){
								WildcardQuery	query6 = new WildcardQuery(new Term("text",  qstr));
								bq.add(query6, BooleanClause.Occur.MUST_NOT);					
							}else if((qstr.indexOf('~') != -1)){ 
								FuzzyQuery query7 = new FuzzyQuery(new Term("text",  qstr));
								bq.add(query7, BooleanClause.Occur.MUST_NOT);
							}else{  
								TermQuery query8 = new TermQuery(new Term("text",  qstr));
								bq.add(query8, BooleanClause.Occur.MUST_NOT);
							}
			    		}					
					}
		    	}				
			
			
 				Query q = bq.rewrite(IndexReader.open(indexName));
								
				/*highlighter = new Highlighter(new SimpleHTMLFormatter("<b><font style='background-color:yellow'>", "</font></b>"), new QueryScorer(q, "text"));*/
		  
				highlighter = new Highlighter(new SimpleHTMLFormatter("<b><hlt>", "</hlt></b>"), new QueryScorer(q, "text"));													  
				highlighter.setTextFragmenter(new SimpleFragmenter(50000));
			
				//highlighter2 = new Highlighter(new SimpleHTMLFormatter("<i>", "</i>"), new QueryScorer(q, "title"));
				//highlighter2.setTextFragmenter(new SimpleFragmenter(10000));
			
				int maxNumFragmentsRequired = 1; 
 
				for (int i = startindex; i < (thispage + startindex); i++) {  // for each element
					//get the next document
					Document 	doc = hits.doc(i);                        	   			
					String 		author 	= doc.get("author");         
					String 		severity = doc.get("severity");          
					String 		isodate = doc.get("isodate");                     
					String 		time = doc.get("time");                     
					String 		keywords = doc.get("keywords");
					String 		location = doc.get("location");                     
					String 		title 	= doc.get("title");                     
					String 		category = doc.get("category");                        
					String 		metainfo = doc.get("metainfo");                      
					String 		text = doc.get("text");
					String 		file = doc.get("file");
					String 		link = doc.get("link");
					String 		dirp = doc.get("dirpath");
					String 		dir = doc.get("dir");
					String		tresult = null;

					if(queryString != null || query_all != null || query_phr != null || query_or != null){
			 	  	 	TokenStream tokenStream = analyzer.tokenStream( "text",new StringReader(escapeHTML(text)));							
						if(tokenStream != null) result = highlighter.getBestFragment(tokenStream, escapeHTML(text));
															
				 		//TokenStream tokenStream2 = analyzer.tokenStream( "title",new StringReader(escapeHTML(title)) );			
						//if(tokenStream2 != null) tresult = highlighter2.getBestFragment(tokenStream, escapeHTML(title));					
					}

					xmlout.append("<entry>\n"); 
					if(dir != null) 	 			xmlout.append("<dir>" + dir + "</dir>\n");
					if(dirp != null)				xmlout.append("<dirpath>" + dirp + "</dirpath>\n");
					if(author != null) 			xmlout.append("<author>" + author + "</author>\n");  
					if(severity != null) 		xmlout.append("<severity>" + severity + "</severity>\n");
					if(isodate != null)  		xmlout.append("<isodate>" + isodate + "</isodate>\n");
					if(time != null) 	 			xmlout.append("<time>" + time + "</time>\n");
					if(keywords != null) 		xmlout.append("<keywords>" + keywords + "</keywords>\n");
					if(location != null) 		xmlout.append("<location>" + location + "</location>\n");
					if(tresult != null)			xmlout.append("<title>" + tresult + "</title>\n");
					else if(title != null) 		xmlout.append("<title>" + escapeHTML(title) + "</title>\n");
					if(category != null) 		xmlout.append("<category>" + category + "</category>\n");
					if(metainfo != null) 		xmlout.append("<metainfo>" + metainfo + "</metainfo>\n");
										
					StringBuffer 			sb = new StringBuffer();				
					if( ( query_all != null || query_phr != null || query_or != null ) && (result != null) ){	
								sb.append("<text>" + result + "</text>\n");
					}else    sb.append("<text>" + escapeHTML(text) + "</text>\n");
				
					BufferedReader 			in = new BufferedReader( new StringReader( sb.toString() ) );				          					
					StringBuffer 				x1 = new StringBuffer();
					Markup 						wiki = new Markup();				
										
					wiki.processMarkup(in , x1);
				
					if(x1 != null)       		xmlout.append(x1);
					else if(result != null)   	xmlout.append("<text>" + result + "</text>\n");
					else if(text != null)   	xmlout.append("<text>" + escapeHTML(text) + "</text>\n");
				     		else 						xmlout.append("<text></text>\n");
								     
					in.close();
						
					if(file != null) 	 xmlout.append("<file>" + file + "</file>\n");
					if(link != null) 	 xmlout.append("<link>" + link + "</link>\n");
					xmlout.append("</entry>\n");    
				}
		
	   }else{
	     	
			xmlout.append("<entry>");
			xmlout.append("<author>Search</author>");
			xmlout.append("<severity>NONE</severity>");
			Calendar cld = Calendar.getInstance();
			cld.setTimeInMillis(System.currentTimeMillis());
			String m2 = null;
			String d2 = null;
			String h2 = null;
			String mi2 = null;
			String s2 = null;				
					
			if (cld.get(2)+1 < 10) m2 = "0" +Integer.toString(cld.get(2)+1);
			else			m2 = Integer.toString(cld.get(2)+1);

			if (cld.get(5) < 10) 	d2 = "0" +Integer.toString(cld.get(5));
			else			d2 = Integer.toString(cld.get(5));

			if (cld.get(11) < 10) 	h2 = "0" +Integer.toString(cld.get(11));
			else			h2 = Integer.toString(cld.get(11));

			if (cld.get(12) < 10) 	mi2 = "0" +Integer.toString(cld.get(12));
			else			mi2 = Integer.toString(cld.get(12));

			if (cld.get(13) < 10) 	s2 = "0" +Integer.toString(cld.get(13));
			else			s2 = Integer.toString(cld.get(13));
																	
			xmlout.append("<isodate>" + Integer.toString(cld.get(1))+ "-" + m2 + "-" + d2 + "</isodate>");
			xmlout.append("<time>" + h2+ ":" + mi2 + ":" + s2 + "</time>");
			xmlout.append("<title>Search result...</title>");
			xmlout.append("<keywords>not set</keywords>");
			xmlout.append("<category>USERLOG</category>");
			xmlout.append("<metainfo></metainfo>");			
			if(!exception_occured)
				xmlout.append("<text>Nothing found</text>");
			else	xmlout.append("<text>Exception occured,please try \"Exact phrase\"</text>");
			xmlout.append("</entry>");

		}	

		xmlout.append("<start>"+ startVal +"</start>\n");
		xmlout.append("<entries>"+ maxresults +"</entries>\n");
		if(hits != null && hits.length() >=0 ) 
			xmlout.append("<total>"+ Integer.toString(hits.length())  + "</total>\n");
		else 	xmlout.append("<total>0</total>\n");
		xmlout.append("</list>");

		if(format_param != null && format_param.equals("PDF")){
			do_PDF = true;
			response.setContentType("application/pdf");
		} else{
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
		}

		//File							ansfile = new File("/home/anna/test/ans.xml"); //debugging help
		//FileOutputStream			fs = new FileOutputStream(ansfile);
		//fs.write(xmlout.toString().getBytes());	
			
		ByteArrayInputStream 	bais = new ByteArrayInputStream(xmlout.toString().getBytes());
      InputStream 				inStream = bais;
		StreamSource 				xmlSource = new StreamSource(inStream);			
			
		TransformerFactory 		tFactory = TransformerFactory.newInstance();
		String 						xsl_source = null;		
				
		if (xsl == null) 			xsl_source = "/export/web/htdocs/elogbook/xsl/search-luc.xsl";
		else{	
			String res;
			if (xsl.startsWith("/~")) {
				int i = xsl.indexOf('/',2);
				if (i < 3) i = 3;
				String u = xsl.substring(2,i);
				res = "/home/" + u + "/public_html" + xsl.substring(i,xsl.length());
			} else { 		
				res = docroot + xsl;	 
			}
			xsl_source = res;
		}   
		
		File xslfile = new File(xsl_source);

	   if (xslfile.exists()) {
			Source xslSource = new StreamSource(xslfile);
			String tmp_logname = docroot + logroot + "/data";
			// Generate the transformer:
			Transformer transformer = tFactory.newTransformer(xslSource);
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setParameter("xml_uri", tmp_logname);

			if (do_PDF ) {
				// Check if we are goning to show the pictures:
				if(pic_param != null && pic_param.equals("true"))
                        		transformer.setParameter("picture", pic_param);	
				else
					transformer.setParameter("picture", "false");		
		    		// Prepare output for PDF (binary material)  
		    		if(log == null) {
						log = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
						MessageHandler.setScreenLogger(log);
		    		}
		    		// Create a renderer and attach to output stream
		    		Driver driver = new Driver();
		    		driver.setLogger(log);
		    		driver.setOutputStream(response.getOutputStream());
		    		driver.setRenderer(Driver.RENDER_PDF);					
		    		//Perform XSL transformation
		    		transformer.transform(xmlSource, new SAXResult(driver.getContentHandler()));
			} else {		
				//Perform XSL transformation					
		   		transformer.transform(xmlSource, new StreamResult(out));
					
			}
		}else{
		  	//print out the xml as plain text?

			out.println("<p><b>total results: " + hits.length() +"</b></p>");
			out.println("<p><b>Query: " + complexQuery + "</b></p>");
			out.println("<table>");
			out.println("<tr><td><b>Field__________________</b></td>");
			out.println("<td><b>Value__________________</b></td></tr>");
                	  			  

			 if(hits.length() != 0){
			   for (int i = startindex; i < (thispage + startindex); i++) {  // for each element
                  Document 	doc = hits.doc(i);                           //get the next document 			
                  String 	 	author = doc.get("author");         
                  String   	severity = doc.get("severity");          
                  String   	isodate = doc.get("isodate");                     
                  String   	time = doc.get("time");                     
                  String   	keywords = doc.get("keywords"); 
						String 	 	location = doc.get("location");                    
                  String   	title = doc.get("title");                     
                  String   	category = doc.get("category");                        
                  String   	metainfo = doc.get("metainfo");                      
                  String   	text = doc.get("text");
						String   	dirp = doc.get("dirpath"); 
                  String   	path = doc.get("uid2");
                  String   	docum = doc.get("docs");

						out.println("<tr><td>author:</td> <td>" + author + "</td></tr>");

						out.println("<tr><td>severity:</td> <td>" + severity + "</td></tr>");

						out.println("<tr><td>isodate:</td> <td>" + isodate + "</td></tr>");

						out.println("<tr><td>time:</td> <td>" + time + "</td></tr>");

						out.println("<tr><td>keywords:</td> <td>" + keywords + "</td></tr>");

						out.println("<tr><td>location:</td> <td>" + location + "</td></tr>");

						out.println("<tr><td>title:</td> <td>" + title + "</td></tr>");

						out.println("<tr><td>category:</td> <td>" + category + "</td></tr>");

						out.println("<tr><td>metainfo:</td> <td>" + metainfo + "</td></tr>"); 

						out.println("<tr><td>text:</td> <td> " + text + "</td></tr>");

						out.println("<tr><td>dirpath:</td> <td>" + dirp + "</td></tr>");

						out.println("<tr><td>docu:</td> <td>" + docum + "</td></tr>");

						// out.println("<tr><td>path:</td><td>" + path + "</td></tr>");							
						out.println("<tr><td colspan='2'>*****************************************</td></tr>");

			   }      
			
			   if ( (startindex + maxpage) < hits.length()) {   //if there are more results...display 
                                                                   	    //the more link
                        	String moreurl="results.jsp?request=" + 
                                       URLEncoder.encode(queryString) +  //construct the "more" link
                                       "&amp;entries=" + maxpage + 
                                       "&amp;start=" + (startindex + maxpage);
													
                		out.println("<tr><td></td> <td><a href=\"" + moreurl + "\">More Results>></a></td></tr>");
                		
           }
			}

			out.println("</table>");   
                         
		} // end printing plain text (xsl file doesn't exist)
		
	} catch	(Exception e) {
		throw new ServletException(e);
				
		//out = res.getWriter();
		//out.println("<HTML><HEAD><TITLE>Error Page</TITLE></HEAD><BODY>");
		//out.println("<p>This was the query to search, please, try something else:</p>");
		//out.println("<p>" + result  + "</p></BODY></HTML>");
		//out.close();
			
	}
						
  }   // the end of "if(searcher != null)" 
		
  out.println("</body></html>");
%>
