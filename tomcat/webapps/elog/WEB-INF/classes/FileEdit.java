/*
 * FileEdit.java
 *
 * Created on March 17, 2003, 6:27 PM
 */

// Java standard IO methods
import java.io.*;
import java.util.*;

// Standard servlet API
import javax.servlet.*;
import javax.servlet.http.*;

// This is needed for the XSL transformation
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

// for file upload
import org.apache.commons.fileupload.*;

// for sending email to experts
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.net.*;

/** Servlet for creation and editing of eLogBook entries.
 * @author Raimund Kammering
 * @version 1.2
 *
 * Changes to 1.1:
 * Now metainfo, file and link tags only hold the plain
 * file names (needs corresponding XSL's for proper working).
 *
 */
public class FileEdit extends HttpServlet {
    
    ////////////////////////////////////////////////////////////////////////////
    // Global definitions
    
    // Assign session and context handler
    ServletContext context_;
    HttpSession session_;
    
    // Define source code version
    private static final String VERSION = "1.2";
    // Define strings for possible 'mode' parameters
    private static final String EDIT = "edit";
    private static final String CREATE = "create";
    // Name for temp file
    private static final String TMP_FILE_NAME = "FileEdit";
    // Extension for XML files
    private static final String XML_FILE_EXT = ".xml";
    // Extension for backup files
    private static final String BAK_FILE_EXT = ".BAK";
    // Start value for file numbering (adjust if >99)
    private static final String FILE_NAMENUMBER_SEP = "-";
    // XML header to be printed to temp file
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                                             "<?xml-stylesheet type=\"text/xsl\" href=\"default.xsl\"?>\n" +
                                             "<!-- FileEdit (doGet) Version: " + VERSION + "-->";
    private static final String IMG_CONVERT = "/elogbook/bin/thumb_jpeg";
    
    private String xsl_param  = null;

    /**
     *
     */
    public class ExecThread extends Thread{
	String prog = null;
	String new_file = null;
	String old_file = null;

	public  ExecThread(String new_file, String old_file){
	   String logpath = new_file.substring(0, new_file.indexOf("/data"));

	   prog = logpath + "/bin/";
	   this.new_file = new_file;
	   this.old_file = old_file;
	}

	public void run(){
	   try{
		Process p = Runtime.getRuntime().exec(prog + "search-index add " + new_file);
		p.waitFor();
		if (old_file.compareTo("none") != 0) 
			p = Runtime.getRuntime().exec(prog + "search-index remove " + old_file);
	   }catch(Exception e){}
	  
	}
    }


    ////////////////////////////////////////////////////////////////////////////
    // Error handling
    
    /** Error for file parameter.
     *  file param empty
     */
    private static void error_empty_file_param(PrintWriter out,
                                               String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: file parameter must not be empty!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** Error for file parameter.
     *  file param is realtive URL (not supported)
     */
    private static void error_relative_URL_file_param(PrintWriter out,
                                                      String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: file parameter must not be a relative URL!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for file parameter.
     *  file param incomplete (found only '/')
     */
    private static void error_incomplete_file_param(PrintWriter out,
                                                    String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: file parameter must specify a directory!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** Error for file parameter.
     *  file param incomplete: Missing file name
     */
    private static void error_missing_file_name(PrintWriter out,
                                                String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: No file name in " + EDIT + " mode not allowed!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for file parameter.
     *  file param incomplete: Missing path name
     */
    private static void error_missing_file_path(PrintWriter out,
                                                String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: No path name in '" + CREATE + "' mode not allowed!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for file parameter.
     *  can not write to dir specified by file param
     */
    private static void error_no_dir_write_access(PrintWriter out,
                                                  String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: Can not write to directory!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** Error for file parameter.
     *  can not write to file specified by file param
     */
    private static void error_no_file_write_access(PrintWriter out,
                                                  String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: Can not write to file!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for mode parameter.
     *  file parameter dosn't specifiy a directory
     */
    private static void error_dir_not_found(PrintWriter out,
                                            String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: Directory not found!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** Error for file parameter.
     *  file parameter specifies a file that already exists
     */
    private static void error_file_exists(PrintWriter out,
                                          String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: This file already exists!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for file parameter.
     *  file parameter specifies a dir in 'EDIT' mode
     */
    private static void error_is_dir(PrintWriter out,
                                     String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: You must specifiy a file name!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for file parameter.
     *  file parameter specifies a file that dosn't exist
     */
    private static void error_file_not_found(PrintWriter out,
                                             String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: File not found!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Error for mode parameter.
     *  mode parameter not matching or missing
     */
    private static void error_mode_mismatch(PrintWriter out,
                                            String file_param, String xsl_param, String mode_param) {
        out.println("<HTML><HEAD><TITLE>FileEdit parameter error</TITLE></HEAD><BODY>");
        out.println("<p>file parameter: " + file_param + "</p>");
        out.println("<p>XSL parameter: " + xsl_param + "</p>");
        out.println("<p>mode parameter: " + mode_param + "</p>");
        out.println("<p><b>ERROR: Only '" + EDIT + "' and '" + CREATE + "' are allowed modes!</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** Error reading file.
     *  Unknown file extension
     */
    private static void error_unknown_file_type(PrintWriter out, String file) {
        out.println("<HTML><HEAD><TITLE>FileEdit read error</TITLE></HEAD><BODY>");
        out.println("<p>file: " + file + "</p>");
        out.println("<p><b>ERROR: Unknown file extension for file" + file + ". Must be: " + XML_FILE_EXT + "</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** General IO exception in master routine.
     *  (NOTE this hides the original exception)
     */
    private static void error_catched_io_exception(PrintWriter out) {
        out.println("<HTML><HEAD><TITLE>FileEdit IO exception error</TITLE></HEAD><BODY>");
        out.println("<p><b>ERROR: IO exception</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** File Upload Exception.
     *  file size > 3MB
     */
    private static void error_file_upload_exception(PrintWriter out, String error_string) {
        out.println("<HTML><HEAD><TITLE>FileEdit FileUploadException error</TITLE></HEAD><BODY>");
        out.println("<p><b>ERROR: " + error_string + "</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** File Delete Exception.
     *  
     */
    private static void error_file_delete_exception(PrintWriter out, String error_string) {
        out.println("<HTML><HEAD><TITLE>FileEdit FileDeleteException error</TITLE></HEAD><BODY>");
        out.println("<p><b>ERROR: " + error_string + "</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    /** Send Mail Exception.
     *  
     */
    private static void error_send_mail_exception(PrintWriter out, String error_string) {
        out.println("<HTML><HEAD><TITLE>FileEdit SendMailException error</TITLE></HEAD><BODY>");
        out.println("<p><b>ERROR: " + error_string + "</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }
    
    /** General purpose error routine.
     *
     */
    private static void error_generic(PrintWriter out, String error_string) {
        out.println("<HTML><HEAD><TITLE>FileEdit error</TITLE></HEAD><BODY>");
        out.println("<p><b>ERROR: " + error_string + "</b></p>");
        out.println("</BODY></HTML>");
        out.close();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Global utility functions
    
    /** Transform a filename to full URL using
     * "docroot" (provided by webmodule).
     *
     * @param file filename to be transformed
     * @return String filename URL relative to "docbase"
     */
    private String URLtoFile(String file)
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

    /**
     * Append a leading zero if necessary
     *
     * @param int integer to be padded
     * @return String returns "i" if i>10 else returns "0i"
     */
    private static String intPad(int i)
    {
	if (i<10) return "0" + i;
	else	  return "" + i;
    }

    /** Correct logbook entries
     *
     * @param String value
     * @return StringBuffer returns corrected value
     */
    private static StringBuffer correct(String value) {
        int   cc = 0;
        StringBuffer modvalue = new StringBuffer(value.length());
        for (int i=0;i<value.length();i++) {
            // Replace XML special characters
            if (value.charAt(i) == '>') modvalue.append("&gt;");
            else if (value.charAt(i) == '<') modvalue.append("&lt;");
            else if (value.charAt(i) == '&') modvalue.append("&amp;");
            else if (value.charAt(i) == '\'') modvalue.append("&apos;");
            else if (value.charAt(i) == '\"') modvalue.append("&quot;");
            else if (value.charAt(i) == '\n') cc = 0;
            // Check for chars outside standard char set
            // We replace: everything outside the range: 31-127
            // except TAB (09), LF (10) and CR (13)
	    //else if (value.charAt(i) >= 127 || value.charAt(i) <=31 &&
	//	     value.charAt(i) != 9 &&
	//	     value.charAt(i) != 10 &&
	//	     value.charAt(i) != 13 ) {
	//	String unichar = "&#" + value.charAt(i) + ";";
		//		System.out.println("DEBUG: Value of " + value.charAt(i) + " translated to " + unichar);
	//	modvalue.append(unichar);
	 //   }
	    // This was the old solution - bad result (no german umlaute)
	    //else if (value.charAt(i) >= 127 || value.charAt(i) <=31 &&
	    else if (value.charAt(i) <=31 &&
	          value.charAt(i) != 9 &&
	          value.charAt(i) != 10 &&
	          value.charAt(i) != 13 ) modvalue.append("&#xBF;");
            //else if (value.charAt(i) == ' ' && (cc > 80)) {                               
            //    modvalue.append("\n"); cc = 0;
            //}
            else modvalue.append(value.charAt(i));
            cc++;
        }
        return modvalue;
    }
    
    /** Convert image files to jpeg
     *
     * @param String image_filename, int shrinking_factor(optional)
     * @return String returns message of "thumb_jpeg"
     */
    private static String convertSource(String imageFullName)
    {
    	try{
	  Runtime rt = Runtime.getRuntime();
	  String logpath = imageFullName.substring(0, imageFullName.indexOf("/data"));
	  String[] callArgs = {logpath + "/.." + IMG_CONVERT,imageFullName};
	  Process proc = rt.exec(callArgs);
	  proc.waitFor();
	  BufferedReader pout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	  String rcode = pout.readLine();
	  if (rcode == null) return "Convert OK";
	  else return rcode;
	}
	catch(Exception e){return "Exception: " + e;}
    }

    /** delete existing image file
     *
     * @param String image_filename
     * @return String returns message of "rm"
     */
    private static String deleteFile(String imageFullName)
    {
    	try{
	  Runtime rt = Runtime.getRuntime();
	  String[] callArgs = {"rm",imageFullName};
	  Process proc = rt.exec(callArgs);
	  proc.waitFor();
	  BufferedReader pout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	  String rcode = pout.readLine();
	  if (rcode == null) return "Delete OK";
	  else return rcode;
	}
	catch(Exception e){return "Exception: " + e;}
    }

    /** creates link to pdf.jpeg 
     *
     * @param String linkname
     * @param String defaultImage
     * @return String returns message of "rm"
     */
    private static String linkImage(String defaultImage, String linkname)
    {
    	try{
	  Runtime rt = Runtime.getRuntime();
	  String[] callArgs = {"ln", "-s", defaultImage, linkname};
	  Process proc = rt.exec(callArgs);
	  proc.waitFor();
	  BufferedReader pout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	  String rcode = pout.readLine();
	  if (rcode == null) return "Link OK";
	  else return rcode;
	}
	catch(Exception e){return "Exception: " + e;}
    }

    /** send email to experts
     *
     * @param String from, String[] to, int mcount, String subject, String text, String fileAttachment
     * @return void
     */
    private static void postMail (String from, String[] to, int mcount, String subject, String text, String fileAttachment, String backedit) 
      throws MessagingException {
        // Get system properties
        Properties props = System.getProperties();
        // Setup mail server
	props.put("mail.smtp.host", "localhost");
	try {
	    props.put("mail.from", props.getProperty("user.name")+"@"+
		      InetAddress.getLocalHost().getCanonicalHostName());
	} 
	catch (Exception e) { System.out.println ("Exception: " + e); }
        // Get session
        Session session = Session.getInstance(props, null);
        // Define message
        MimeMessage message = new MimeMessage(session);
        if (!from.equals("")){
            message.setFrom(new InternetAddress(from));
        }
        for (int i=0; i<mcount;i++){
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
        }
        message.setSubject("Elogbook Email: " + subject);
        // create the message part 
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        //fill message
        text = text+"\n\n------------------------------------------------------------------------\n\nLink to e-logbook entry:\n"+ backedit + "\n\n------------------------------------------------------------------------\n\n";
        text = text+"This email is created by the e-logbook\nand contains an error report about "+subject+".";
        messageBodyPart.setText(text);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        // if attachment exists -> add attachment
        if (!fileAttachment.equals("")){
          messageBodyPart = new MimeBodyPart();
          DataSource source = new FileDataSource(fileAttachment);
          messageBodyPart.setDataHandler(new DataHandler(source));
          messageBodyPart.setFileName(fileAttachment);
          multipart.addBodyPart(messageBodyPart);
        }
        // Put parts in message
        message.setContent(multipart);
        // Send the message
        Transport.send( message );
    }
   
    /** Initializes the servlet.
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
     
    /** The doGet method of this servlet is used for creation
     * and editing an eLogBook entry.
     * @param request Standard HTTP request handler
     * @param response Standard HTTP response handler
     * @throws ServletException Signaling a servlet exception
     * @throws IOException signaling a IO exception
     */    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {

        // Assign context and session information
	context_ = getServletContext();
	session_ = request.getSession(true);
        
	// Get writer for HTTP output and assing to HTTP response
	response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
	
	// Get all parameter from servlet call
	String file_param = request.getParameter("file");
	xsl_param  = request.getParameter("xsl");
	String mode_param = request.getParameter("mode");
        // Check client browser type
        // if brower is javascript compatible -> String browser=1
        String browser_type = request.getHeader("User-Agent").toLowerCase();
        String browser="0";
        if(browser_type.indexOf("5.0") >= 0) browser="1";
        
        // Create File instance for further tests and real access later on
        File file = new File(URLtoFile(file_param));

        // Initial file parameter check (see documentation)
        if (file_param.equals(""))
            error_empty_file_param(out, file_param, xsl_param, mode_param);
        else if (file_param.charAt(0) != file.separatorChar)
            error_relative_URL_file_param(out, file_param, xsl_param, mode_param);
        else if (file_param.length() == 1)
            error_incomplete_file_param(out, file_param, xsl_param, mode_param);

        // mode = CREATE (see documentation)
        if (mode_param.equals(CREATE)) {
            if (!file.exists()) {
                if (!file.getParentFile().isDirectory())
                    error_no_dir_write_access(out, file_param, xsl_param, mode_param);
            }
            else if (!file.isDirectory()) {
                if (file.isFile())
                    error_file_exists(out, file_param, xsl_param, mode_param);
                else
                    error_dir_not_found(out, file_param, xsl_param, mode_param);
            }
        }
        // mode = EDIT (see documentation)
        else if (mode_param.equals(EDIT)) {
            if (file.isDirectory())
                error_is_dir(out, file_param, xsl_param, mode_param);
            if (!file.exists())
                error_file_not_found(out, file_param, xsl_param, mode_param);
            else if(!file.canWrite())
                error_no_file_write_access(out, file_param, xsl_param, mode_param);
        }
        // Unknown mode: error
        else
            error_mode_mismatch(out, file_param, xsl_param, mode_param);

        // Check xsl_param argument. If not ok fallback to 'default.xsl'.
        File xsl_file;
        if (xsl_param != null && xsl_param.length() > 4) {
            xsl_file = new File(URLtoFile(xsl_param));
            if (!xsl_file.exists()) xsl_file = new File(URLtoFile("/default.xsl"));
        }
        else
            xsl_file = new File(URLtoFile("/default.xsl"));

        // Done with all servlet parameter tests
        
        ////////////////////////////////////////////////////////////////////////
        // Master routine doing read/write of eLogBook entry
        try {

            // Create a date instance for use as unique file name
            Calendar calendar = Calendar.getInstance();
            // Create date string in ISO format (2003-03-25T14:08:58)
            int Y = calendar.get(Calendar.YEAR);
            String M = intPad(calendar.get(Calendar.MONTH)+1); // Jan = 0
            String d = intPad(calendar.get(Calendar.DAY_OF_MONTH));
            String h = intPad(calendar.get(Calendar.HOUR_OF_DAY));
            String m = intPad(calendar.get(Calendar.MINUTE));
            String s = intPad(calendar.get(Calendar.SECOND));
            // eg. 10:12:54
            String time = h + ":" + m + ":" + s;
            // eg. 2003-03-26
            String ISODate_short = Y + "-" + M + "-" + d;
            // eg. 2003-03-26T10:12:54
            String ISODate = Y + "-" + M + "-" + d + "T" + h + ":" + m + ":" + s;
            
            // URL base address will be send right after the XML header
            String url_base = null;
            if (file.isDirectory()) {
                url_base = file_param;
            }
            else if (file.isFile()) {
                File temp_file = new File(file_param);
                url_base = temp_file.getParent();
            }
            else if (file.getParentFile().isDirectory() && mode_param.equals(CREATE)) {
                File temp_file = new File(file_param);
                url_base = temp_file.getParent();
            }
            else
                error_generic(out, "Couldn't resolve URL base address (should never happen)!");
            
            // Create temp XML stream to be converted
	    File tmp_path = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
	    File tmp_file = File.createTempFile(TMP_FILE_NAME, XML_FILE_EXT, tmp_path);
	    FileOutputStream streamout = new FileOutputStream(tmp_file);
	    PrintWriter tmp_out = new PrintWriter(streamout, true);

            // Print out the XML header
	    tmp_out.println(XML_HEADER);
            // Print root element for transformation of the temp file
	    tmp_out.println("<list>");

	    ////////////////////////////////////////////////////////////////////
	    // Edit an existing file
	    if (mode_param.equals(EDIT)) {
                if (file.getName().endsWith(XML_FILE_EXT)) {
		    int count = 0;
		    int c;
		    FileReader in = new FileReader(file);
		    tmp_out.println("<entry>");
		    while ((c = in.read()) != -1) {tmp_out.write(c); count++;}
                    tmp_out.write(" EDITED");
		    in.close();
		    tmp_out.println("<url_base>" + url_base + "</url_base>");
		    tmp_out.println("</entry>");
		} else {
                    error_unknown_file_type(out, file.toString());
		}
	    }
	    //////////////////////////////////////////////////////////////
	    // Create a new file
            else if (mode_param.equals(CREATE)){
                // Check file for type (file or dir)
                String filename = null;
                if (file.isDirectory()) // file param is dir
                    filename = file.toString() + file.separatorChar + ISODate + FILE_NAMENUMBER_SEP + "00" + XML_FILE_EXT;
                else if (file.getParentFile().isDirectory()) // file param is file
                    filename = file.toString();
                else
                    error_generic(out, "file param not interpretable (should never happen)");
                                
                // Now filename holds full path, name and extension
                file = new File(filename);
                
                // If file param holds file name with extension
                if (filename.endsWith(XML_FILE_EXT))
                    filename = filename.substring(0, filename.length()-XML_FILE_EXT.length());
                
                // Check if the file exists
                while(file.exists()) {
                    filename = filename + "1";
                    file = new File(filename + XML_FILE_EXT);
                }
                // Equalize file and filename
                filename = file.toString();
                
                // Metainfo is path and filename starting at docroot
                String metainfo = filename.substring(filename.lastIndexOf(file.separatorChar)+1);
                
                // Collect some additional information
                String author = (String)session_.getAttribute("AUTHOR");
                if (author == null) author = "";

                // Now we are ready to dump the data to the temp file
                tmp_out.println("<entry>");
                tmp_out.println( "<author>" + author + "</author>");
                tmp_out.println( "<category>USERLOG</category>");
                tmp_out.println( "<metainfo>" + metainfo + "</metainfo>");
                tmp_out.println( "<isodate>" + ISODate_short + "</isodate>");
                tmp_out.println( "<time>" + time + "</time>");
                tmp_out.println( "<url_base>" + url_base + "</url_base>");
                tmp_out.println( "<severity>NONE</severity>");
                tmp_out.println( "<keywords></keywords>");
                tmp_out.println( "<location></location>");
                tmp_out.println( "<additional_list></additional_list>");
                tmp_out.println( "<title></title>");
                tmp_out.println( "<text></text>");
                tmp_out.println("</entry>");                
            }
	    //////////////////////////////////////////////////////////////
	    // Mode dosen't allow creation of new file
	    else {
                error_mode_mismatch(out, file_param, xsl_param, mode_param);
            }

            // Close root element and temp file
	    tmp_out.println("</list>");
	    tmp_out.close();
        
            ////////////////////////////////////////////////////////////////////
            // XSL transformation
            FileInputStream tmp_in = new FileInputStream(tmp_file);
            // Create Transformer factory instance
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Streams for the transfromation
            Source xml_stream = new StreamSource(tmp_in);
            Source xsl_stream = new StreamSource(xsl_file);
            // Get instance of transformer
            Transformer transformer = tFactory.newTransformer(xsl_stream);
            // Pass parameter as external param. to XSL file 
            transformer.setParameter("xml_uri", file.toString());
            transformer.setParameter("browser", browser);
            // Perform the transformation and send output as HTTP response
            transformer.transform(xml_stream, new StreamResult(out));

            // Clean up tmp file
            //tmp_file.delete();

        } catch	(Exception ioe) {
            throw new ServletException(ioe);
           // error_catched_io_exception(out);
        }
        
        // Close HTML output stream
        out.close();
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException  */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        File oldfile = new File("");
        File backupfile = new File("");
        PrintWriter out;
	String title = "Save File Output";
	String backlink = null;
        List items = null;
	String imageFileName = null;
	int imagewrite = 0;
        
        // Used for setting anchors at an entry
	String anchor1 = new String("");    
	String anchor2 = new String("");

	// set content type and other response header fields first
	response.setContentType("text/html;charset=UTF-8");
//	response.setContentType("text/html");

	// then write the data of the response
	out = response.getWriter();

	// Check that we have a file upload request
	boolean isMultipart = FileUpload.isMultipartContent(request);	
        if (isMultipart){
            try{
                // Create a new file upload handler
                DiskFileUpload upload = new DiskFileUpload();
                // Parse request object
                items = upload.parseRequest(request);
                // set upload parameters
                // maximum size before a FileUploadException will be thrown
                upload.setSizeMax(3000000);
            }
            catch(FileUploadException fue) {
                throw new ServletException(fue);
            }
        }
	
        // Assign servlet context and session information
	context_ = getServletContext();
	session_ = request.getSession(true);

        // Get servlet parameter (this is the only one)
	String file_param = request.getParameter("source");

        // File to write to
        String filename = URLtoFile(file_param);
	File file = new File(filename);

	// Prepare HTML output
        out.println("<HTML><HEAD><TITLE>");
	out.println(title);
	out.println("</TITLE></HEAD><BODY>");

        // Main loop writing all data to disk
        try {
            if (file_param == null) {
                error_empty_file_param(out, file_param, file_param, file_param);
            }
            else {
                boolean canwrite = false;
		if (file.exists()) {
		    if (file.canWrite()) {
                        canwrite = true;
                        int counter = 0;
                        // Save old file
                        String oldfilename = filename;
                        oldfile = new File(oldfilename);
                        // Get new name
                        // e.g. 2003-03-31T15:00:00-00
                        filename = filename.substring(0, filename.lastIndexOf(XML_FILE_EXT));
                        // Check if it is a filename in ISOdate format
                        String plainfilename = filename.substring(filename.lastIndexOf(file.separatorChar)+1);
			
                        if(plainfilename.charAt(0) >= 48 && plainfilename.charAt(0) <= 57 &&
                           plainfilename.charAt(1) >= 48 && plainfilename.charAt(1) <= 57 &&
                           plainfilename.charAt(2) >= 48 && plainfilename.charAt(2) <= 57 &&
                           plainfilename.charAt(3) >= 48 && plainfilename.charAt(3) <= 57 &&
                           plainfilename.charAt(4) == 45 &&
                           plainfilename.charAt(5) >= 48 && plainfilename.charAt(5) <= 57 &&
                           plainfilename.charAt(6) >= 48 && plainfilename.charAt(6) <= 57 &&
                           plainfilename.charAt(7) == 45 &&
                           plainfilename.charAt(8) >= 48 && plainfilename.charAt(8) <= 57 &&
                           plainfilename.charAt(9) >= 48 && plainfilename.charAt(9) <= 57 &&
                           plainfilename.charAt(10) == 84 &&
                           plainfilename.charAt(11) >= 48 && plainfilename.charAt(5) <= 57 &&
                           plainfilename.charAt(12) >= 48 && plainfilename.charAt(6) <= 57 &&
                           plainfilename.charAt(13) == 58 &&
                           plainfilename.charAt(14) >= 48 && plainfilename.charAt(5) <= 57 &&
                           plainfilename.charAt(15) >= 48 && plainfilename.charAt(6) <= 57 &&
                           plainfilename.charAt(16) == 58 &&
                           plainfilename.charAt(17) >= 48 && plainfilename.charAt(5) <= 57 &&
                           plainfilename.charAt(18) >= 48 && plainfilename.charAt(6) <= 57 &&
                           plainfilename.charAt(19) == 45 &&
                           plainfilename.charAt(17) >= 48 && plainfilename.charAt(5) <= 57 &&
                           plainfilename.charAt(18) >= 48 && plainfilename.charAt(6) <= 57) {
                             while(file.exists()) {
                                // Get running number from filename (starts at 0)
                                counter = Integer.parseInt(filename.substring(filename.lastIndexOf(FILE_NAMENUMBER_SEP)+1));
                                counter = counter + 1;
                                // e.g. 2003-03-31T15:00:00
                                filename = filename.substring(0, filename.lastIndexOf(FILE_NAMENUMBER_SEP));
                                // Reassemble filename and assign to file obj. for further testing
                                filename = filename + FILE_NAMENUMBER_SEP + intPad(counter) + XML_FILE_EXT;
                                file = new File(filename);
                            }
                        }
                        else {
                            filename = filename + XML_FILE_EXT;   
                        }

                        // Rename old file
                        backupfile = new File(oldfilename + BAK_FILE_EXT);
                        oldfile.renameTo(backupfile);
                    } //end of if (file.canWrite())
                } // end of if (file.exists())
                else {
                    // Just for write access testing
		    File dir = new File(file.getParent());
		    if (dir.canWrite())	canwrite = true;
		}
		if (canwrite) {
                    // Output stream
		    FileOutputStream streamout = new FileOutputStream(file);
		    PrintWriter fileout = new PrintWriter(streamout, true);

                    // Stings for tmp storage of the element values
                    String metainfo = null;
                    String isodate = null;
                    String time = null;
                    // Array for tmp storage of all other elements
                    String Elements = "";
                    String imagetemp = "";
                    String imageFullName = "";
                    String filetemp = "";
                    String filefile = "";
                    String filelink = "";
                    String email = "";
                    String temptext = "";
                    // uncorrected text for mail to expert
                    String etemptext = "";
                    String auth_str = "";
                    // uncorrected title for mail to expert
                    String etitletext = "";
                                        
                    // Array for output of all collected data
                    String[] Out = new String[128];

		    try{
		      // Process the uploaded items
		      Iterator iter = items.iterator();
		      while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();
                        if (item.isFormField()) {
                            // Main loop processing all elogbook elements (except images)
                            String name = item.getFieldName();
                            String value = item.getString();
			    //			    System.out.println("DEBUG: Value of " + value + " before correction");
                            StringBuffer modvalue = correct(value);

                            // Due to change of the filename we also have to trim metainfo
                            if(name.compareTo("metainfo") == 0) {
                                // In the old elog format "metainfo" was holding path + filename rel. to docroot
                                // since this isn't needed we cut that down to filename in the new versions.
                                // If "metainfo starts with "/", it's path to file starting at "docroot"
                                if (value.charAt(0) == file.separatorChar) {
                                    // take old path and new filename
                                    metainfo = value.substring(0, value.lastIndexOf(file.separatorChar)+1);
                                    metainfo += filename.substring(filename.lastIndexOf(file.separatorChar)+1);
                                }
                                else // is only filename (or rel. path - should not happen?!)
                                    metainfo = filename.substring(filename.lastIndexOf(file.separatorChar)+1);
                                Elements += "<" + name + ">" + metainfo + "</" + name + ">";
                            }
                            // Since the image could be edited
                            if(name.equals("imageold") && imagetemp.length()==0){
                                imagetemp = "<image>" + value + "</image>";
                            }
                            if(name.startsWith("file-")) {
                                filetemp = "<file>" + value + "</file>";
                                filefile = value;
                            }
                            if(name.equals("link")) {
                                filetemp += "<link>" + value + "</link>";
                                filelink = value;
                            }
                               
                            // If the date is altered manualy we have to take care about format
                            if(name.compareTo("date") == 0) {
                                int day, month, year;
                                if (value.length() != 10)
                                    throw new NumberFormatException();
                                // Check if it matchs any of the following formats
                                // 'MM/dd/yyyy', 'dd.MM.yyyy' or 'yyyy-MM-dd'
                                else if (value.charAt(2) == '/' || value.charAt(5) == '/') {
                                    month = Integer.parseInt(value.substring(0, 2));
                                    day = Integer.parseInt(value.substring(3, 5));
                                    year = Integer.parseInt(value.substring(6, 10));
                                }
                                else if (value.charAt(2) == '.' || value.charAt(5) == '.') {
                                    day = Integer.parseInt(value.substring(0, 2));
                                    month = Integer.parseInt(value.substring(3, 5));
                                    year = Integer.parseInt(value.substring(6, 10));
                                }
                                else if (value.charAt(4) == '-' || value.charAt(7) == '-') {
                                    year = Integer.parseInt(value.substring(0, 4));
                                    month = Integer.parseInt(value.substring(5, 7));
                                    day = Integer.parseInt(value.substring(8, 10));
                                }
                                else
                                    throw new NumberFormatException();
                                // Check for logical consistence
                                if (month < 1 || month > 12 || day < 1 || day > 31)
                                    throw new NumberFormatException();
                                else
                                    isodate = year + "-" + intPad(month) + "-" + intPad(day);
                                Elements += "<isodate>" + isodate + "</isodate>";
                                // The isodate is used as the first part of the anchor
                                anchor1 = isodate;
                            }
                            // time will be used as the second part of the anchor
                            if(name.compareTo("time") == 0) {
                                // The time is used as the second part of the anchor
                                anchor2 = value;
                                Elements += "<" + name + ">" + value + "</" + name + ">";
                            }
                            // Get author from session
                            if(name.compareTo("author") == 0) {
                                auth_str = value;
                                session_.setAttribute("AUTHOR",auth_str);
                            }
                             // Get backlink from HTML form 
                            if(name.compareTo("backlink") == 0) {
                                backlink = value;
                            }
                             // Get title from HTML form 
                            if(name.equals("title")) {
                                etitletext += value;
                            }
                            // Get text from HTML form 
                            if(name.equals("text")) {
                                temptext += modvalue;
                                etemptext += value;
                            }
                             // Add expert list to text
                            if(name.equals("experts") && !value.equals("")) {
                                temptext += "\n\nThis elogbook entry was send to following experts:\n" + modvalue;
                                etemptext += "\n\nThis elogbook entry was send to following experts:\n" + value;
                            }
                             // Get email adresses for expert mail 
                            if(name.equals("email")) {
                                email += value;
                            }
                            // Get all other elements
                            if(name.compareTo("source") != 0 && name.compareTo("date") != 0 &&
                               !name.startsWith("file") && name.compareTo("metainfo") != 0 &&
                               name.compareTo("link") != 0 && name.compareTo("backlink") != 0 &&
                               !name.equals("image") && !name.equals("imageold") &&
                               name.compareTo("time") != 0 && 
                               name.compareTo("topic") !=0 && name.compareTo("expertlist") !=0 &&
                               !name.equals("experts") && ! name.equals("text") && !name.equals("email")) {
                                   Elements += "<" + name + ">" + modvalue + "</" + name + ">";
                            }
                        // processing images
                        } else {
                            imageFileName = item.getName();
                            if (!imageFileName.equals("")){
                                imagetemp = "<image>" + correct(imageFileName) + "</image>";
                                String imageSuffix = imageFileName.substring(imageFileName.lastIndexOf("."),imageFileName.length());
                                imageFileName = filename.substring(filename.lastIndexOf(file.separatorChar)+1,filename.lastIndexOf(".")) + imageSuffix;
                                // File name relative to docroot
                                //String imageName = metapath + file.separatorChar + imageFileName;
                                //String imageName = metainfo.lastIndexOf(file.separatorChar) + imageFileName;
                                // Full file name on system
                                //imageFullName = URLtoFile(imageName);
                                imageFullName = filename.substring(0,filename.lastIndexOf(file.separatorChar)+1) + imageFileName;
                                // check for upload file
                                if (imageFileName.length()>5){
                                    long sizeInBytes = item.getSize();
                                    // check for file size
                                    if (sizeInBytes < 30000000){
                                        String contentType = item.getContentType();
                                        // check for MIME type
                                        if (contentType.equals("application/pdf") ||
					    contentType.equals("application/postscript") ||
					    contentType.equals("type=application/postscript") ||
                                            contentType.equals("image/jpeg") ||
                                            contentType.equals("image/pjpeg") ||
					    contentType.equals("image/png") ||
					    contentType.equals("image/gif") ||
                                            contentType.equals("image/tiff") ||
                                            contentType.equals("image/bmp") ||
                                            contentType.equals("image/x-cmu-raster") ||
					    contentType.equals("application/matlab") ||
					    contentType.equals("application/octet-stream") ||
					    contentType.equals("application/msword") ||
					    contentType.equals("application/vnd.ms-excel") ||
					    contentType.equals("application/vnd.ms-powerpoint") ||
					    contentType.equals("application/x-zip-compressed") ||
                                            contentType.equals("image/cmu-raster")){
                                            // Process a file upload
                                            File uploadedFile = new File(imageFullName);
                                            if (uploadedFile.exists()) uploadedFile.delete();
                                            item.write(uploadedFile);
                                            // set imageBaseName
                                            String imageBaseName = "";
                                            int p = imageFullName.lastIndexOf(".");
                                            if (p == -1) imageBaseName = imageFullName;
                                            else imageBaseName = imageFullName.substring(0,p);
                                            // Convert into jpeg
                                            String msg = convertSource(imageFullName);
        
					/////////// debug
					System.out.println ("DEBUG: File name to convert:"+imageFullName+". Content type: "+contentType+
							    "\nDEBUG: Message from convertSource: "+ msg);
					//////////

                                    if (!msg.equals("Convert OK")) {
                                            // if conversion fails -> thumbnail is link to elogbook/images/pdf.jpeg 
                                            String base = context_.getInitParameter("docroot");
                                            String defaultImage = null;
                                            if (contentType.equals("application/pdf")) defaultImage = base+"/elogbook/images/pdf.jpeg";
                                            else defaultImage = base+"/elogbook/images/anyfile.gif";
						String linkname = filename.substring(0,filename.lastIndexOf("."))+".jpeg";
						msg = linkImage(defaultImage, linkname);
						if (!msg.equals("Link OK")) {
                                                    error_file_delete_exception(out,"Link failed! ERROR message: " + msg);
                                                }
					    }
                                            imageBaseName = metainfo.substring(0, metainfo.lastIndexOf("."));
                                            if (filetemp.length()>0){
                                                msg = deleteFile(URLtoFile(filefile));
                                                if (!msg.equals("Delete OK")) {
                                                    error_file_delete_exception(out,"Delete "+filefile+" failed! ERROR message: " + msg);
                                                }
                                                msg = deleteFile(URLtoFile(filelink));
                                                if (!msg.equals("Delete OK")) {
                                                    error_file_delete_exception(out,"Delete "+filelink+" failed! ERROR message: " + msg);
                                                }
                                            }
                                            
                                            filetemp = "<file>" + imageBaseName + ".jpeg" + "</file>";
                                            //filetemp += "<link>" + imageName  + "</link>";
                                            filetemp += "<link>" + imageBaseName + imageSuffix + "</link>";
                                        }
                                        else error_file_upload_exception(out,"File has wrong Mime Type. Detected " + contentType + ". <p></p> Following Types are allowed: pdf, ps, eps, jpg, png, gif, tiff, rs, bmp");
                                    }
                                    else error_file_upload_exception(out, "File Size > 30 Mb!");
                                }
                            }
                        }
                      }
		    }
		    catch(FileUploadException fue) {throw new ServletException(fue);}
		    catch(Exception e) {throw new ServletException(e);}
		    
                    // Print out all other stuff
                    Elements += "<text>" + temptext + "</text>";
                    fileout.print(Elements);
                    // Dump file element
                    if (filetemp.length()>1){
                        fileout.print(imagetemp);
                        fileout.print(filetemp);
                    }
                    // Detach file handle
                    fileout.close();
					
		    		/**********lucene index updates*************/
		    		ExecThread thr = null;
		    
		    		if (backupfile.exists())
		    			thr = new ExecThread(filename, oldfile.getPath());
		   	 		else
						thr = new ExecThread(filename, "none");
		    		thr.start();
                    /**********end lucene index updates*************/
					
                    // Send Email to experts and FeedBack to Raimund
                    if (filename.indexOf("comments")>0) email = email + "raimund.kammering@desy.de ";
                    if (!email.equals("")){
                        int mcount = 0;
                        String[] to = new String[20];
                        while(email.indexOf(" ")!=-1 && email.length()>3){
                        to[mcount] = email.substring(0,email.indexOf(" "));
                        email = email.substring(email.indexOf(" ")+1,email.length());
                        mcount++;
                        }
                        etemptext = "Author: " + auth_str + "\n\n" + etemptext;
                        String tmp = request.getRequestURL().toString();
                        tmp = tmp.substring(0, tmp.indexOf("/",10));
                        String newfilename = file_param.substring(0,file_param.lastIndexOf("/")) + filename.substring(filename.lastIndexOf("/"),filename.length());
                        //http://ttfinfo.desy.de/elog/servlet/FileEdit?file=/TESTelog/data/2004/08/18.02_a/2005-11-09T14:01:16-01.xml&xsl=/elogbook/xsl/elog-fileform-ttf.xsl&mode=edit
                        //String backedit = tmp.toString()+"?file="+newfilename+"&xsl="+xsl_param+"&mode=edit";
                        String logbookname = file_param.substring(0, file_param.indexOf("/",3));
                        if (filename.indexOf("comments")>0 || filename.indexOf("news")>0) {
                            if (imagetemp.equals("")) postMail("", to, mcount, etitletext, etemptext, "", logbookname);
                            else postMail("", to, mcount, etitletext, etemptext, imageFullName, logbookname);
                        }
                        else {
                            String tmp2 = file_param.substring(file_param.indexOf("data")+4, file_param.length()-4);
			    System.out.println ("DEBUG: tmp2: " + tmp2 + "  file_param: " + file_param); ////////////////// debug
                            String date = tmp2.substring(0, tmp2.indexOf("/",11));
                            String timestamp = tmp2.substring(tmp2.indexOf("/",11)+1, tmp2.length());
                            //http://ttfinfo.desy.de/TTFelog/show.jsp?dir=/2005/43/26.10_M&pos=2005-10-26T08:04:26
                            String backedit = tmp + logbookname +"/show.jsp?dir="+date+"&pos="+timestamp;
                            if (imagetemp.equals("")) postMail("", to, mcount, etitletext, etemptext, "", backedit);
                            else postMail("", to, mcount, etitletext, etemptext, imageFullName, backedit);
                        }
                    }
                    
                } else {
		    out.println("<h2>Can't save file</h2>");
		    out.println("<p>File name not writeable:<br>");
		    out.println(filename + "</p><br>");
                    out.close();
		}
            }

	} catch	(Exception ioe) {
	    //throw new ServletException(ioe);
            // Rename old file back (otherwise we might loose data)
            backupfile.renameTo(oldfile);
            // Delete newly created file
            file.delete();
            // Print unspecific error message (FIXME)
            throw new ServletException(ioe);

	    //out.println("<p>Error in writing to file " + filename + "</p>");
            //out.close();
        }
        // Navigate back to shift (frames[3] is list frame)
	out.println("<script LANGUAGE='JavaScript'>");
        //out.println("parent.frames[3].location.href=\"" + backlink + "&picture=true" + "#" + anchor1 + "T" + anchor2 + "\"");
        out.println("self.location.href=\"" + backlink + "&picture=true" + "#" + anchor1 + "T" + anchor2 + "\"");
        out.println("</script>");
        out.println("</BODY></HTML>");
	out.close();
    }
    
    /** Returns a short description of the servlet.
     * @return String Purpose of this servlet
     */
    public String getServletInfo() {
        return "FileEdit servlet " + VERSION + "\nServlet for creating and editing eLogBook entries.";
    }
}
