/*
 * @(#)XMLSingle.java	1.22 97/10/25
 *
 * This servlet creates a standard XML header and then reads
 * one .xml file.
 */

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

/**
 * This is a simple example of an HTTP Servlet.  It responds to the GET
 * and HEAD methods of the HTTP protocol.
 */
public class XMLSingle extends HttpServlet
{
    ServletContext context_;
    HttpSession session_;

    public String URLtoFile(String file)
    {
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


    public String getNewest(String startPath, boolean recursive) throws FileNotFoundException {
	long newTime = 0;
	String subs = "";
	int pi = 0, sl = 0;
	File newFile = new File(startPath);

	File tempDir = new File(startPath);
	List<File> files = getFileListing( tempDir );

	for (File file : files ) {
	    if ( file.isFile() ) {
		String fileName = file.getName();
		pi = fileName.lastIndexOf(".");
		if(pi == -1) pi = 0;
		sl = fileName.length();
		subs = fileName.substring(pi, sl);
		if ( subs.equals(".xml")) {
		    long curtime = file.lastModified();
		    if (curtime >= newTime) {
			newTime = file.lastModified();
			newFile = file;
		    }
		}
	    }
	}
	return newFile.toString();
    }


    /**
     * Recursively walk a directory tree and return a List of all
     * Files found; the List is sorted using File.compareTo.
     *
     * @param aStartingDir is a valid directory, which can be read.
     */
    static public List<File> getFileListing(File aStartingDir) throws FileNotFoundException{
	validateDirectory(aStartingDir);
	List<File> result = new ArrayList<File>();
	
	File[] filesAndDirs = aStartingDir.listFiles();
	List<File> filesDirs = Arrays.asList(filesAndDirs);
	for(File file : filesDirs) {
	    result.add(file); //always add, even if directory
	    if ( ! file.isFile() ) {
		//must be a directory
		//recursive call!
		List<File> deeperList = getFileListing(file);
		result.addAll(deeperList);
	    }
	    
	}
	Collections.sort(result);
	return result;
    }


    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     */
    static private void validateDirectory (
					   File aDirectory
					   ) throws FileNotFoundException {
	if (aDirectory == null) {
	    throw new IllegalArgumentException("Directory should not be null.");
	}
	if (!aDirectory.exists()) {
	    throw new FileNotFoundException("Directory does not exist: " + aDirectory);
	}
	if (!aDirectory.isDirectory()) {
	    throw new IllegalArgumentException("Is not a directory: " + aDirectory);
	}
	if (!aDirectory.canRead()) {
	    throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
	}
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
	PrintWriter	out;
	String		title		= "Servlet to read one XML file";
	String		fullname	= "null";
	String		fullpath	= "null";
	String		newestfile	= "null";
	String		xsl_source	= "null";
	String		debug_sts	= "null";

	// set content type and other response header fields first
	//
	// NOTE:
	// The charset attribute is only defining the transitional
	// output from the servlet to the transformer.
	// If one wants to set the encoding in the final output (html)
	// file it has to be defined for the transformation (see below).
	response.setContentType("text/html; charset=iso-8859-1");

	// then write the data of the response
	out = response.getWriter();

	StringBuffer tmpout = new StringBuffer();

	context_ = getServletContext();
	session_ = request.getSession(true);

	String filename = request.getParameter("file");
	String dirname = request.getParameter("dir");

	String tagname = request.getParameter("tag");
	String reqstag = "null";
	String reqetag = "null";
	if(tagname != null) {
	    reqstag = "<" + tagname + ">";
	    reqetag = "</" + tagname + ">";
	}

	try {
	    if (filename != null) {
		// Full path to file
		fullname = URLtoFile(filename);
	    }
	    else if (dirname != null) {
		// Full path to dir
		fullpath = URLtoFile(dirname);
		// Look up the newest file in url_dir
		fullname = getNewest(fullpath, false);
	    }

	    debug_sts = "write header";
	    File profile = new File(fullname);
	    
	    // We will serve the whole XML file
	    if (tagname == null) {
		out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		out.println("<!--doGet of XMLSingle Version: 2004-08-09-->");
		out.println("<list>");
		out.println("<entry>");
		if (profile.canRead()) {
		    debug_sts = "copy from XML file";
		    FileReader in = new FileReader(profile);
		    int c;
		    while ((c = in.read()) != -1) out.write(c);
		    in.close();
		}
		out.println("</entry>");
		out.println("</list>");
		out.close();
	    } else { // only a tag content request
		if (profile.canRead()) {
		    debug_sts = "parse from XML file";
		    BufferedReader in = new BufferedReader(new FileReader(profile));
		    String str;
		    while ((str = in.readLine()) != null) tmpout = tmpout.append(str);
		    in.close();
		}
		if (tmpout.length() != 0) {
		    if ( tmpout.toString().indexOf(tagname) != -1 ) {
			//do get reqtag content
			out.append( tmpout.substring( tmpout.indexOf(reqstag)+reqstag.length(),tmpout.indexOf(reqetag) ) );
			out.close();
		    } else {
			out.println("<HTML><HEAD><TITLE>");
			out.println("Error " + tagname + " not found");
			out.println("</TITLE></HEAD><BODY>");
			out.println("<p style=\"font-weight:bold\">ERROR:</p>");
			out.println("<p style=\"font-weight:bold\">Could not find <font style=\"color:blue\">" + tagname + "</font></p>");
			out.println("<p>Content is: " + tmpout.toString() + "</p>");
			out.println("</BODY></HTML>");
		    }
		}
	    }
	} catch	(Exception ioe) {
	    out.println("</entry>");
	    out.println("</list>");
	    out.println("debug_sts: " + debug_sts);
	    out.println("Exception has been thrown: " + ioe);
            out.close();
	}
    }


    public void doPost (
			HttpServletRequest	request,
			HttpServletResponse	response
			) throws ServletException, IOException
    {
	PrintWriter		out;
	String			title = "XMLSingle Servlet Output";

	// set content type and other response header fields first
        response.setContentType("text/html");

	// then write the data of the response
	out = response.getWriter();

	out.println("<HTML><HEAD><TITLE>");
	out.println(title);
	out.println("</TITLE></HEAD><BODY>");
	out.println("<H1>" + title + "</H1>");
	out.println("<p>doPost of XMLSingle (Version: 2004-08-09)</p>");

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

