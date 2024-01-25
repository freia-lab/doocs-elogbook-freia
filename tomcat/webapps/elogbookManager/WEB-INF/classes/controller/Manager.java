package controller;
/*
 * Manager.java
 *
 * Created on 3. September 2008, 18:45
 */

import helper.LogHelper;
import java.io.*;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;
import settings.Settings;

/**
 * main servlet that manages all incoming requests
 * @author jojo
 * @version
 */
public class Manager extends HttpServlet {
    
    
    String debugText = "";


    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
        Cookie cList[] = request.getCookies();
        
        // when servlet is called with a query string, the query is passed 
        // to the corresponding tree/or search controller
        String qs = request.getQueryString();
        
        // query string which is not the special query string for internet explorer, to 
        // stop ie from caching ajax requests to same adresses. This is because info is transfered
        // throu cookies an not html url parameters.
        if (request.getQueryString()!=null && !request.getQueryString().startsWith("IE_Sux"))
        {
            // only for debugging
            if(request.getQueryString().equals("debug"))
            {
                response.setContentType("text/html");
                out.print("<html><pre>"+debugText+"</html></pre>");
            }
            else // pass the request to the tree/search controller
            {   
                if( request.getQueryString().startsWith("search") ){
                    response.setContentType("text/html;charset=UTF-8");
                    
                    LogHelper.getInstance().log("Manager: Sending search request: " + qs + " at " + (new Date()).toString());
                    SearchController.getInstance().setRequest(qs);
                    //out.print(SearchController.getInstance().getData(qs));
                    sendInitialPage(out, getServletContext().getRealPath(""),request.getRequestURL().toString());
                    LogHelper.getInstance().log("Manager: Returned from request: " + qs + " at " + (new Date()).toString());
                    LogHelper.getInstance().log("Manager: --------------------------");
                }else{
                    response.setContentType("application/xml");
                    out.print(TreeController.getInstance().getData(request.getQueryString()));
                }
            }
            out.flush();
            out.close();
        }
        else // no query string or special ie query string
        {
            String cContent = getCookie(cList,Settings.REQUEST_COOKIE);
            debugText = "cookie: " + cContent + " - ";
            
            // nor query string neither cookie was sent
            // sending initial page
            if ( cContent == null)
            {
                String servletURL = request.getRequestURL().toString();
                response.setContentType("text/html;charset=UTF-8");
                sendInitialPage(out, getServletContext().getRealPath(""),servletURL);
            }
            else // a cookie was sent, its content is passed to the data controller
            {
                response.setContentType("application/xml");
                // check if cookie was legal (user didnt enter a
                // wrong password for manager)
                if (!cContent.equals("error"))
                {
                    out.print(Settings.RESPONSE_OPEN);
                    String result = DataController.getInstance().getData(cContent);
                    out.print(result);
                    debugText += result;
                    out.print(Settings.RESPONSE_SUB_CLOSE);
                }
                else // user entered wrong password for manager
                {
                    out.print(Settings.RESPONSE_OPEN);
                    out.print("login failed");
                    out.print(Settings.RESPONSE_SUB_CLOSE);
                }
            }
            out.flush();
            out.close();
        }// else end
    }

    /**
     * when servlet is stopped
     * all quartz jobs should
     * be stopped too
     */

    @Override
    public void destroy()
    {
        SchedulerController.getInstance().stopAllJobs();
        SearchController.getInstance().stopSearch();
        LogHelper.getInstance().log("System shutting down");
        LogHelper.getInstance().writeLogFile(true);
    }
        
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "eLogbook Manager Servlet";
    }

    // </editor-fold>

    /**
     * On servlet startup, the logbook confs are read
     * the tree controller is initialized
     */
    @Override
    public void init() throws ServletException 
    {        
        // initialize the dataController
        if (Settings.DEBUG)
        {
            //String debugpath = "/var/lib/tomcat5/webapps/elog";
            String debugPath = "/home/jstrampe/tomcat5.5/webapps/elog";
            
            DataController.getInstance().getAllLogbooks(debugPath);            
            TreeController.getInstance().init(debugPath);
            SearchController.getInstance().getId();            
        }
        else
        {
            DataController.getInstance().getAllLogbooks(getServletContext().getRealPath(""));
            TreeController.getInstance().init(getServletContext().getRealPath(""));
            //SearchController.getInstance().init( getServletContext().getRealPath("") );
            SearchController.getInstance().init( getServletContext().getInitParameter("docroot"));
        }
    }

    
    /**
     * gets a cookie list and returns the content
     * of the passed cookie name
     * null is returned if cookie does not exist
     * empty string is returned if cookie contains
     * the correct password. An empty cookie will produce
     * an error
     */
    private String getCookie(Cookie[] cookieList, String cName)
    {
        if (cookieList==null) return null;
        for (int i = 0; i < cookieList.length; i++) 
        {
            if (cookieList[i].getName().equals(cName))
            {
                // when cookie content is "" the inital information is sent
                // the cookie content is only "" when the cookie contains
                // "password"+ the password set in the Settings
                if (cookieList[i].getValue().equals("")) return "error";
                if (cookieList[i].getValue().equals("password"+Settings.MANAGER_PASSWORD)) return "";
                if (cookieList[i].getValue().startsWith("password")) return "error";
                return cookieList[i].getValue();
            }            
        }
        return null;
    }
    
       
    /**
     * error message when servlet is manually called, and not through the
     * ajax obejct of the index.html
     */
    private void sendInitialPage(PrintWriter out, String prefix, String servletURL) 
    {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Manager</title>");
        out.println("</head>");
        out.println("<h2>Error</h2>");
        out.println("<div>The Manager Servlet should not be manually called !</div>");
        out.println("</html>");
    }
    
    

    private void print(String text) 
    {
        debugText += "\n"+text;
    }

}
