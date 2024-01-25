/*
 * TreeController.java
 *
 * Created on October 2, 2008, 10:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package controller;

import helper.ConfFileHelper;
import helper.LogHelper;
import java.io.File;
import java.util.Date;
import javascriptTree.Tree;
import javascriptTree.makeTreeXML;
import types.DataComponent;
import types.DataIterator;

/**
 * main controller to manage all requests from the
 * javascript trees of the logbooks
 * @author jstrampe
 */
public class TreeController extends DataComponent{
    
    private static TreeController _Instance = null;
    
    private DataIterator _list = new DataIterator();
    
    /** get the singleton instance of the tree controller */
    public static TreeController getInstance()
    {
        if (_Instance == null)
            _Instance =  new TreeController();
        return _Instance;
    }
    
    /** Creates a new instance of TreeController */
    private TreeController() 
    {
        
    }
    
    // map to save every tree object
    //private Map map = new HashMap();
    private String debugText = "";
    private boolean debug = false;
    // webapps folder
    private String _servletPath = "";
          
    /***
     * for initialization the servlet path must be passed
     * eg /var/lib/tomcat5/webapps/elogbookManager
     */
    public void init(String servletPath)
    {
        // save the context path (webapps folder)
        File context = new File(servletPath);
        _servletPath = context.getParent()+"/";//servletPath.substring(0,servletPath.length()-4);//.replace("/elog","");
        
    }
    
    /**
     * process the request
     * respond in xml
     */    
    public String getData(String request) 
    {
        String response = "";
        // create value is the name of the logbook
        String create = getParameter("create",request);
        debug = (getParameter("debug",request)!=null);
        
        // create parameter was passed so create new tree
        if (create!=null && create.compareTo("")!=0)
        {
            Tree tree = new Tree();
            String path = _servletPath + create;
            
            long nachher;
            long vorher = System.currentTimeMillis();
            // check if subServlet is initialized
            if ((tree!=null) && (tree.init(create,path)))
            {                
                // if key already exists it is overwritten
                _list.addItem(tree);
                //map.put(create,tree);
                nachher = System.currentTimeMillis();
                return "logbook created in: "+(nachher-vorher)+ "ms on date: "+(new Date()).toString();                
            }
            return "tree could not be initialized";            
        }
        else // no create parameter
        {
            // get name of the logbook
            String name = getParameter("name",request);
            DataComponent tree = _list.getItemById(name);
            //Tree tree = (Tree)map.get(name);
            if (tree!=null)
            {                
                return tree.getData(request);
            }
            else
            {
                String path = _servletPath + name;
                Tree newTree = new Tree();
                if ((newTree!=null) && (newTree.init(name,path)))
                {
                    // if key already exists it is overwritten
                    _list.addItem(newTree);
                    //map.put(name,newTree);
                    return newTree.getData(request);
                }
                else
                {
                    return("<E>error with tree init. name:"+name+" path:"+path+"</E>");                    
                }                
            }
        }
    }

    /**
     * returns the id of this object
     */ 
    public String getId() 
    {
        return "Tree Controller";
    }

    /**
     * Gets a parameter string like
     * para1=val1&para2=val2
     * and a parameter name (para1)
     * and returns the parameter value (val1)
     */
    private String getParameter(String paraName, String request) 
    {
        String arr[] = request.replaceAll("&","=").split("=");
        for (int i = 0; i < arr.length; i++) 
        {
            if (i%2==0)
            {
                if (arr[i].equals(paraName))
                {
                    if ((i+1)<arr.length)
                    {
                        return arr[i+1];
                    }
                    else
                    {
                        return "";
                    }
                    
                }
            }
        }
        return null;
    }

    /**
     * Get some statistics about the loaded trees.
     * @return Some statistics about the loaded trees
     */
    public String getStatistics()
    {
        String result = "Number of trees: "+_list.length()+"\n\n";
        for (int i = 0; i < _list.length(); i++) 
        {
            result +=_list.itemAt(i).getData("getShortStatistics")+"\n";
        }
        return result;
    }
    
    /**
     * get some statistics for the loaded tree data
     * of the logbook with the passed logname
     */
    public String getStatistics(String logname)
    {
        //Tree tree = (Tree)map.get(logname);
        DataComponent tree = _list.getItemById(logname);
        if (tree!=null)
        {            
            return tree.getData("getStatistics");//.getStatistics();
        }
        else
        {
            return "the tree for "+logname+" is not loaded.";
        }
    }
    
    /**
     * Is used when treeData is created manually.
     * gets the path to the conf file and creates
     * a new treeData.xml file with the fix parameters
     * -d -n -de
     * first data then folder
     * first number then letters
     * decreasing
     * response is some statusinfo of the 
     * creation process
     */
    public String createTreeXML(String confPath) 
    {
        ConfFileHelper cfh = new ConfFileHelper(new File(confPath));        
        
        String docroot = cfh.getElementValue("docroot");
        String logroot = cfh.getElementValue("logroot");
        String datapath = cfh.getElementValue("datapath");
        
        // create the treeData.xml
        createTreeXML(docroot.concat(logroot).concat(datapath), docroot.concat(logroot).concat("/jsp"));
        
        String response = "tree XML manually created on: "+(new Date()).toString()+"\n";
        LogHelper.getInstance().log("write treeData.xml for "+logroot.replaceFirst("/", ""));
        return response;
    }
    
    /**
     * Is used 
     * gets the path to the conf file and creates
     * a new treeData.xml file with the fix parameters
     * -d -n -de
     * first data then folder
     * first number then letters
     * decreasing
     * response is some statusinfo of the 
     * creation process
     */
    public void/*String*/ createTreeXML(String datapath, String jspFolder) 
    {
        // /var/www/TESTelog/data /var/www/TESTelog/jsp -d -n -de
        String args[] = new String[5];
        args[0] = datapath;
        args[1] = jspFolder;
        args[2] = "-d";
        args[3] = "-n";
        args[4] = "-de";
        new makeTreeXML(args);
        
    }

    /**
     * reload the treeData.xml file of
     * the logbook with the passed name
     */
    public String reload(String logname) 
    {      
        Tree tree = new Tree();

        String path = _servletPath + logname;
     
        long nachher;
        long vorher = System.currentTimeMillis();
        // check if subServlet is initialized
        if ((tree!=null) && (tree.init(logname,path)))
        {                
            // if key already exists it is overwritten
            _list.addItem(tree);
            //map.put(logname,tree);
            nachher = System.currentTimeMillis();
            return "logbook created in: "+(nachher-vorher)+ "ms on date: "+(new Date()).toString()+"\n";
        }
        return "tree could not be initialized";
    }
    
}//class end
