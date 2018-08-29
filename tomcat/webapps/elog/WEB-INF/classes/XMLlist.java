

/* @(#)XMLlist.java
 * 23.03.2004 modified by Elke Sombrowski
 *
 * This servlet creates a standard XML header and then reads
 * all .xml files in a directory and combines them in the temp
 * XML file. This file is then transformed together with a XSL
 * file into HTML.
 */
//package servlets;

import java.io.*;
import java.util.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.sax.SAXResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;
import org.apache.fop.apps.XSLTInputHandler;
import org.apache.fop.messaging.MessageHandler;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/** Servlet to combine xml files.
 * Transformes xml and xsl to html.
 */
public class XMLlist extends HttpServlet {
    ServletContext context_;
    HttpSession session_;
    Logger log = null;
    
    private final String ascendingDefault = "false";
    private final String pictureDefault = "false";

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
				} else if (list[onstack].level < 0) {
						if (list[s].level < 0) {
							s0 = ((Integer)stack.pop()).intValue();
							tag = tag + list[s0].epilog;
						}
				} else {
					s0 = ((Integer)stack.pop()).intValue();
					tag = list[s0].epilog;
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
					} else if (list[s].level == -1) {
						if (list[onstack].level >= 0) {
							tag = tag + list[s].prolog;
							stack.push(new Integer(s));
						}
					} else if (list[s].level == 0){
						tag = tag + list[s].prolog;
						stack.push(new Integer(s));
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
			int det = 0, s, i = 0;
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

    
    public String URLtoFile(String file) {
	String res;
	if (file.startsWith("/~")) {
	    int i = file.indexOf('/',2);
	    if (i < 3) i = 3;
	    String u = file.substring(2,i);
	    res = "/home/" + u + "/public_html" + file .substring(i,file.length());
	} else {
	    if ( context_.getInitParameter("docroot") != null ) {
		res = context_.getInitParameter("docroot") + file;
	    } else {
		res = file;
	    }
	}
	return res;
    }

    
    /**
     * Handle the GET and HEAD methods by building a simple web page.
     * HEAD is just like GET, except that the server returns only the
     * headers (including content length) not the body we write.
     */
    public void doGet (
		       HttpServletRequest	request,
		       HttpServletResponse	response
		       ) throws ServletException, IOException
    {
	String		aname = "null";
	String          url_base = "null";
	String          fullname = "null";
	String          xsl_source = "null";
	String          debug_sts = "null";
        boolean         do_PDF = false;

	final int UNASSIGNED=0;
	final int METADATA=1;       // METADATA without history (.XML files)
	final int METADATAHIST=2;   // METADATA with history (.XML files)
	final int OTHERHASMETA=3;
	final int NODISPLAY=4;
	final int ISDIR=5;
	final int IMAGE=6;
	final int IMAGE_HAS_PS=7;
	final int IMAGE_IS_PS=8;
	final int HTML=9;
        // for history (.XML.BAK files)
        final int HISTDATA=11;

	// set content type and other response header fields first
	//
	// NOTE:
	// The charset attribute is only defining the transitional
	// output from the servlet to the transformer.
	// If one wants to set the encoding in the final output (html)
	// file it has to be defined for the transformation (see below).

	// then write the data of the response
        ServletOutputStream out_stream = response.getOutputStream();
	//PrintWriter out = response.getWriter();
        PrintWriter out = new PrintWriter(out_stream);
	context_ = getServletContext();
	session_ = request.getSession(true);
        request.setCharacterEncoding("UTF-8");

	String filename[] = request.getParameterValues("file");
	String xsl_param = request.getParameter("xsl");
	String pic_param = request.getParameter("picture");
 	String format_param = request.getParameter("format");
        String history_param = request.getParameter("history");
	String ascending = request.getParameter("ascending");
	String ascendingStored = (String) session_.getValue("ascending");
        StringBuffer xmlout = new StringBuffer();


	if (ascendingStored == null) ascendingStored = ascendingDefault;
	if (ascending == null) ascending = "";
	ascending = ascending.toLowerCase();
	if (ascending.equals(""))  {	// if not specified, take session value

	    if (!ascendingStored.equals(""))
		ascending = ascendingStored;
	    else
		ascending = ascendingDefault;	     // Default value

	} else { 
	    
	    // Only "true" and "false" are valid. Default is "false".
	    if (!(ascending.equals("true") || ascending.equals("false")))
		ascending = ascendingDefault;

	    // if order has changed or wasn't specified, save new value
	    if (!ascendingStored.equals(ascending)) {
		session_.putValue("ascending", ascending);
	    }
	}

	if (pic_param == null) pic_param = "";
	pic_param = pic_param.toLowerCase();
	if (pic_param.equals(""))  {	// if not specified, take session value
		pic_param = pictureDefault;	     // Default value
	} else { 
	    
	    // Only "true" and "false" are valid. Default is "false".
	    if (! (pic_param.equals("true") || pic_param.equals("false")))
		pic_param = pictureDefault;
	}



         if ((format_param != null) && (format_param.equals("PDF")) ) {
            do_PDF = true;
            response.setContentType("application/pdf");
         } else {
            response.setContentType("text/html; charset=UTF-8");
         }

	try {
	    fullname = URLtoFile(filename[0]);

            debug_sts = "check .prolog file";
	    File profile = new File(fullname + ".prolog.xml");
	    if (profile.canRead()) {
			debug_sts = "copy from profile XML file";
                        BufferedReader in = new BufferedReader(new FileReader(profile));
                        String str;
			while ((str = in.readLine()) != null) xmlout = xmlout.append(str + "&#x0D;");
			in.close();
	    } else {
			debug_sts = "write temp XML file";
			xmlout.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			xmlout.append("<list>");
	    }
            Markup wiki = new Markup();
            for (int n=0; n < filename.length; n++) {
                // url_base is the dir name in URL notation
        	File url_file = new File(filename[n]);
                url_base = url_file.getPath() + "/";

	    	fullname = URLtoFile(filename[n]);
	    	File file = new File(fullname);
		xmlout = xmlout.append("<xml_file>" + fullname + "</xml_file>");
	    	if (file.isDirectory()) {
                    debug_sts = "if file is directory -> check filetypes in dir";
                    xmlout = xmlout.append("<url_base>" + filename[n] + "</url_base>");
                    String[] filenames = file.list();
                    int[] filetypes = new int[filenames.length];
                    for (int i=0; i<filenames.length; i++) {
                        filetypes[i] = UNASSIGNED;
                        debug_sts = "check all filetypes in dir";
                        aname = fullname + "/" + filenames[i].toString();
                        File afile = new File(aname);
                        if (afile.canRead() && !filenames[i].toString().startsWith(".") && filetypes[i] == UNASSIGNED)  {
                            String rawfilename = aname.substring(0, aname.lastIndexOf("."));
                            //System.out.println("TEST rawfilename: "+rawfilename);
                            //System.out.println("TEST history_param: "+history_param);
                            if (history_param==null && aname.endsWith(".xml") && rawfilename.endsWith("00"))
                                filetypes[i] = METADATA;
                            if (history_param==null && aname.endsWith(".xml") && !rawfilename.endsWith("00"))
                                filetypes[i] = METADATAHIST;
                            if (history_param!=null && aname.endsWith(".xml") && rawfilename.endsWith("00") && 
                                rawfilename.indexOf(history_param)>0)  filetypes[i] = METADATA;
                            if (history_param!=null && aname.endsWith(".xml") && !rawfilename.endsWith("00") && 
                                rawfilename.indexOf(history_param)>0)  filetypes[i] = METADATAHIST;
                            if (history_param!=null && aname.endsWith(".xml.BAK") && 
                                rawfilename.indexOf(history_param)>0)  filetypes[i] = HISTDATA;
                            if (aname.endsWith(".html"))  filetypes[i] = HTML;
                        }
                    }
                    if(history_param!=null) xmlout = xmlout.append("<historyof>"+history_param+"</historyof>");
                    for (int i=0; i<filenames.length; i++) {
                        debug_sts = "process and copy the XML data into a temp StringBuffer";
                        aname = fullname + "/" + filenames[i].toString();
                        File afile = new File(aname);
                        
                        // get date+time when file was last modified
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(new Date(afile.lastModified()));
                        String lastModified = cal.get(Calendar.DATE) + "." 
                            + (cal.get(Calendar.MONTH) + 1) + "." 
                            + cal.get(Calendar.YEAR) + " " 
                            + cal.get(Calendar.HOUR_OF_DAY) + ":";
                        if(cal.get(Calendar.MINUTE)>9) lastModified = lastModified + cal.get(Calendar.MINUTE);
                        else lastModified = lastModified + "0" + cal.get(Calendar.MINUTE);
                        
                        if (!afile.isDirectory() && afile.exists()) {
                            if (filetypes[i] == METADATA) {
                                BufferedReader in = new BufferedReader(new FileReader(aname));
                                debug_sts = "copy a XML file into Buffered Reader if file is not a directory";
                                xmlout = xmlout.append("<entry>");
                                xmlout = xmlout.append("<url_base>" + filename[n] + "</url_base>");
                                xmlout = xmlout.append("<edit>yes</edit>");
                                xmlout = xmlout.append("<hist>no</hist>");
                                //while ((str = in.readLine()) != null) xmlout = xmlout + str + "&#x0D;";
                                wiki.processMarkup(in ,xmlout);
                                in.close();
                                xmlout = xmlout.append("</entry>");
                            }
                            if (filetypes[i] == METADATAHIST) {
                                BufferedReader in = new BufferedReader(new FileReader(aname));
                                debug_sts = "copy a XML file into Buffered Reader if file is not a directory";
                                xmlout = xmlout.append("<entry>");
                                xmlout = xmlout.append("<url_base>" + filename[n] + "</url_base>");
                                xmlout = xmlout.append("<edit>yes</edit>");
                                xmlout = xmlout.append("<hist>yes</hist>");
                                xmlout = xmlout.append("<lastmodified>"+lastModified+"</lastmodified>");
                                //while ((str = in.readLine()) != null) xmlout = xmlout + str + "&#x0D;";
                                wiki.processMarkup(in ,xmlout);
                                in.close();
                                xmlout = xmlout.append("</entry>");
                            }
                            else if (filetypes[i] == HISTDATA) {
                                BufferedReader in = new BufferedReader(new FileReader(aname));
                                debug_sts = "copy a XML.BAK file into Buffered Reader if file is not a directory";
                                xmlout = xmlout.append("<entry>");
                                xmlout = xmlout.append("<url_base>" + filename[n] + "</url_base>");
                                xmlout = xmlout.append("<edit>no</edit>");
                                xmlout = xmlout.append("<hist>no</hist>");
                                xmlout = xmlout.append("<lastmodified>"+lastModified+"</lastmodified>");
                                wiki.processMarkup(in ,xmlout);
                                in.close();
                                xmlout = xmlout.append("</entry>");
                            }
                            else if (filetypes[i] == HTML) {
                                // is a HTML data file to include
                                debug_sts = "include a .html file";
                                xmlout = xmlout.append("<include>" + aname + "</include>");
                            }
                        }
                    }
                }
            }
            xmlout = xmlout.append("</list>");
            
            // for debugging: write xmlout to debugfile
            
            try {
                PrintWriter f = new PrintWriter( new BufferedWriter(new FileWriter("/tmp/XMLlist_debugfile.xml")));
                f.print(xmlout.toString());
                f.close();
            } catch (IOException e) {}
            
            
            // convert String xmlout to StreamSource
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlout.toString().getBytes());
            InputStream inStream = bais;
            StreamSource xmlSource = new StreamSource(inStream);

            if (xsl_param == null || xsl_param.length() < 4) {
                // no XSL file name given: just send XML file
                debug_sts = "start copy XML file to output";
                out.println(xmlout.toString());
            } else {
                // XSL was specified: transform XML+XSL-->HTML or PDF
                debug_sts = "prepare transformer";
                TransformerFactory tFactory = TransformerFactory.newInstance();
                xsl_source = URLtoFile(xsl_param);
                debug_sts = "get XSL file";
                File xslfile = new File(xsl_source);
                if (!xslfile.exists()) {
                    xsl_source = URLtoFile("/default.xsl");
                    xslfile = new File(xsl_source);
                }
                if (xslfile.exists()) {
                    debug_sts = "new stream of XSL file";
                    Source xslSource = new StreamSource(xslfile);
                    if (do_PDF ) {
                        // Prepare output for PDF (binary material)  
                        debug_sts = "start the transformation: xml+xsl to PDF";
                        if(log == null) {
                            log = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
                            MessageHandler.setScreenLogger(log);
                        }
                        // Create a renderer and attach to output stream
                        Driver driver = new Driver();
                        driver.setLogger(log);
                        driver.setOutputStream(out_stream);
                        driver.setRenderer(Driver.RENDER_PDF);
                        // Generate the transformer and do the XSL transformation
                        Transformer transformer = tFactory.newTransformer(xslSource);
                        // Check if we are goning to show the pictures (third parameter to this servlet)
                        if (pic_param != null) {
                            if (pic_param.equals("true") ) {
                                transformer.setParameter("picture", pic_param);
                            }
                        }
                        transformer.setParameter("xml_uri", fullname);
                        transformer.transform(xmlSource, new SAXResult(driver.getContentHandler()));
                   } else {
                        // Generate the transformer.
                        Transformer transformer = tFactory.newTransformer(xslSource);
                        // Perform the transformation, sending the output to the response.
                        debug_sts = "start the transformation: xml+xsl to html";
                        // This is the place were one can determine the final html output
                        // method.
                        // NOTE: If the transformer factory is invoked with defaults
                        // the standard output encoding will be UTF-8!
                        // Since this isn't autdetected by some old Netscape browsers,
                        // we use iso-8859-1 since this is the default in this browsers.
                        // transformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                        // Check if we are goning to show the pictures (third parameter to this servlet)
                        if (pic_param != null) {
                            if (pic_param.equals("true") ) {
                                transformer.setParameter("picture", pic_param);
                            }
                        }
                        transformer.setParameter("xml_uri", fullname);
			transformer.setParameter("list_ascending", ascending);
                        transformer.transform(xmlSource, new StreamResult(out));
                   }
                } 
                else  {
                    response.setContentType("text/html; charset=UTF-8");
                    out.println("<HTML><HEAD><TITLE>Error Page</TITLE></HEAD><BODY>");
                    out.println("<p>XSL parameter: " + xsl_param + "</p>");
                    out.println("<p>XSL file: " + xsl_source + "</p>");
                    out.println("<p>Error in XSL file</p></BODY>");
                }
            }
	} catch	(Exception ioe) {
            response.setContentType("text/html; charset=UTF-8");
            String bugout = xmlout.toString();
            bugout = bugout.replace('<','(');
            bugout = bugout.replace('>',')');
 	    out.println("<HTML><HEAD><TITLE>Error Page</TITLE></HEAD><BODY>");
  	    out.println("<br><font size=+2>The requested page could not be created!</font></br>");
  	    out.println("<p>This is output from XMLlist Servlet.</p>");
  	    out.println("<p>XML file: " + filename + "</p>");
  	    out.println("<p>XML full file: " + fullname + "</p>");
  	    out.println("<p>XML url base: " + url_base + "</p>");
  	    out.println("<p>XSL parameter: " + xsl_param + "</p>");
  	    out.println("<p>XSL file: " + xsl_source + "</p>");
  	    out.println("<p>Error for file: " + aname + "</p>");
  	    out.println("<p>Error in creating the HTML page</p>");
  	    out.println("<p>Error at: " + debug_sts + "</p>");
            out.println("<p>Java Exception: "+ ioe + "</p>");
            out.println("<p>xml-file: "+ bugout +"</p></BODY>");
	}
	//out.close();
    }


    public void doPost (
			HttpServletRequest	request,
			HttpServletResponse	response
			) throws ServletException, IOException
    {
	PrintWriter		out;
	String			title = "XMLlist Servlet Output";

	// set content type and other response header fields first
        response.setContentType("text/html;charset=UTF-8");

	// then write the data of the response
	out = response.getWriter();

	out.println("<HTML><HEAD><TITLE>");
	out.println(title);
	out.println("</TITLE></HEAD><BODY>");
	out.println("<H1>" + title + "</H1>");
	out.println("<p>doPost of XMLlist (Version: 2003-09-01)</p>");

        Enumeration values = request.getParameterNames();
        while(values.hasMoreElements()) {
	    String name = (String)values.nextElement();
	    String value = request.getParameterValues(name)[0];
	    out.println("<li>" + name + ": " + value + "</li>");
        }

	out.println("<P>These are  parameters from HttpServletRequest.</p>");
        out.println("<li>getAuthType: " + request.getAuthType() + "</li>");
        out.println("<li>getMethod: " + request.getMethod() + "</li>");
        out.println("<li>getPathInfo: " + request.getPathInfo() + "</li>");
        out.println("<li>getPathTranslated: " + request.getPathTranslated() + "</li>");
        out.println("<li>getQueryString: " + request.getQueryString() + "</li>");
        out.println("<li>getRemoteUser: " + request.getRemoteUser() + "</li>");
        out.println("<li>Request URI: " + request.getRequestURI() + "</li>");
        out.println("<li>getRequestedSessionId: " + request.getRequestedSessionId() + "</li>");
        out.println("<li>getServletPath: " + request.getServletPath() + "</li>");
        out.println("<li>getHeader(Referer): " + request.getHeader("Referer") + "</li>");

	out.println("<P>These are  parameters from the Header</p>");
        Enumeration names = request.getHeaderNames();
        while(names.hasMoreElements()) {
	    String name = (String)names.nextElement();
	    String hvalue = request.getHeader(name);
	    out.println("<li>" + name + ": " + hvalue + "</li>");
        }
	out.println("</BODY></HTML>");
	out.close();
    }
    
}

