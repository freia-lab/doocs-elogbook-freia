/*
 * @(#)SimpleServlet.java	1.22 97/10/25
 *
 * Copyright (c) 1996-1997 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a simple example of an HTTP Servlet.  It responds to the GET
 * and HEAD methods of the HTTP protocol.
 */
public class Simple extends HttpServlet
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
			res = "/home/" + u + "/public_html" + file.substring(i,file.length());
		} else {
                  res = context_.getInitParameter("docroot") + file;
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
	PrintWriter		out;
	String			title = "Simple Servlet Output";
	context_ = getServletContext();
	session_ = request.getSession(true);

	// set content type and other response header fields first
        response.setContentType("text/html");

	// then write the data of the response
	out = response.getWriter();

        out.println("<HTML><HEAD><TITLE>");
	out.println(title);
	out.println("</TITLE></HEAD><BODY>");
	out.println("<H1>" + title + "</H1>");
	out.println("<p>This is output from Simple Servlet:</p>");

        String filename = request.getParameter("file");

        Enumeration values = request.getParameterNames();
        while(values.hasMoreElements()) {
                String name = (String)values.nextElement();
		String value = request.getParameterValues(name)[0];
                //if(name.compareTo("submit") != 0) {
                    out.println("<li>" + name + ": " + value + "</li>");
                //}
        }


	out.println("<P>These are  parameters from HttpServletRequest.</p>");

	//URL theurl = new URL(filename);
        //out.println("<li>URL(file): " + theurl.getFile() + "</li>");
        //out.println("<li>URL(host): " + theurl.getHost() + "</li>");
        out.println("<li>getAuthType: " + request.getAuthType() + "</li>");
        out.println("<li>getProperty(user.name): " + System.getProperty("user.name") + "</li>");
        out.println("<li>getProperty(user.home): " + System.getProperty("user.home") + "</li>");
        out.println("<li>getProperty(user.dir): " + System.getProperty("user.dir") + "</li>");
        out.println("<li>getProperty(java.home): " + System.getProperty("java.home") + "</li>");
        //out.println("<li>getContextPath: " + request.getContextPath() + "</li>");
        out.println("<li>getMethod: " + request.getMethod() + "</li>");
        out.println("<li>getPathInfo: " + request.getPathInfo() + "</li>");
        out.println("<li>getPathTranslated: " + request.getPathTranslated() + "</li>");
        out.println("<li>getQueryString: " + request.getQueryString() + "</li>");
        out.println("<li>getRemoteUser: " + request.getRemoteUser() + "</li>");
        out.println("<li>Request URI: " + request.getRequestURI() + "</li>");
        //out.println("<li>Request URL: " + request.getRequestURL() + "</li>");
        out.println("<li>getRequestedSessionId: " + request.getRequestedSessionId() + "</li>");
        out.println("<li>getServletPath: " + request.getServletPath() + "</li>");
        out.println("<li>File: " + filename + "</li>");
        //out.println("<li>Context Path info: " + request.getContextPath() + "</li>");
        out.println("<li>getHeader(Referer): " + request.getHeader("Referer") + "</li>");

	out.println("<P>These is the env of this process:</p>");
        out.println("<li>sun.boot.class.path: " + System.getProperty("sun.boot.class.path") + "</li>");
        out.println("<li>sun.boot.library.path: " + System.getProperty("sun.library.class.path") + "</li>");
        out.println("<li>path: " + System.getProperty("path") + "</li>");

	out.println("<P>These are  parameters from the Context:</p>");
        out.println("<li>context: " + context_.toString() + "</li>");
        out.println("<li>context(tmp dir): " + context_.getAttribute("javax.servlet.context.tempdir") + "</li>");
        out.println("<li>context(/): " + context_.getRealPath("/") + "</li>");
        out.println("<li>get context doc root: " + context_.getInitParameter("docroot") + "</li>");

        Enumeration e = context_.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = context_.getInitParameter(name).toString();
            out.println("<li><b>InitParameterNames, Name:</b> " + name + " =: " + value + "</li>");
        }

	out.println("<P>These are  parameters from the Session:</p>");

        Date created = new Date(session_.getCreationTime());
        Date accessed = new Date(session_.getLastAccessedTime());
        out.println("<li>ID " + session_.getId() + "</li>");
        out.println("<li>Created: " + created + "</li>");
        out.println("<li>Last Accessed: " + accessed + "</li>");

        // set session info if needed

        String dataName = request.getParameter("dataName");
        if (dataName != null && dataName.length() > 0) {
            String dataValue = request.getParameter("dataValue");
            session_.setAttribute(dataName, dataValue);
        }

        // print session contents

        e = session_.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = session_.getAttribute(name).toString();
            out.println("<li> <b>SessionName:</b>" + name + " = " + value + "</li>");
        }



	out.println("<P>These are  parameters from the Header</p>");
        Enumeration names = request.getHeaderNames();
        while(names.hasMoreElements()) {
                String name = (String)names.nextElement();
		String hvalue = request.getHeader(name);
                out.println("<li>" + name + ": " + hvalue + "</li>");
        }
	
	// get environment params from UNIX runtime
	out.println("<P>These are  parameters from the UNIX environment</p>");
    	try{
	  Runtime rt = Runtime.getRuntime();
	  String callArgs = "env";
	  Process proc = rt.exec(callArgs);
	  proc.waitFor();
	  BufferedReader pout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	  String rcode = pout.readLine();
	  while(rcode != null) {
	      out.println("<li>" + rcode + "</li>");
	      rcode = pout.readLine();
	  }
	}
	catch(Exception re){ out.println("<li> PATH: " + re + "</li>");}

	out.println("</BODY></HTML>");
	out.close();
//	System.out.println("HALLO WELT");

    }

}

