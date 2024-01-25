/*
 * TreeInfo.java
 *
 * Created on October 15, 2008, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package contentItems;

import controller.TreeController;
import helper.XMLHelper;
import settings.Settings;
import types.DataComponent;

/**
 * represents the tree info and client controller part
 * @author jstrampe
 */
public class TreeInfo extends DataComponent{
    
    private String _logname = "";
    private String _confPath = "";
    
    private String _reloadInfo = "";
    private String _createInfo = "";
    private String _statisticInfo = "";
    
    /** Creates a new instance of TreeInfo */
    public TreeInfo(String logname, String confPath) 
    {
        _logname = logname;
        _confPath = confPath;
    }

    /**
     * process the request
     * respond in xml
     */
    public String getData(String request) 
    {
        if (request==null) return "";
        
        String response = "";
        _statisticInfo = "hier muessen statistiken hin";
        XMLHelper xml = XMLHelper.getInstance();
        response = xml.mkCommand("reload",Settings.COMMAND_RELOAD_TREE);
        response += xml.mkCommand("create database",Settings.COMMAND_CREATE_TREE_DATABASE);
        
        if (request.endsWith(Settings.COMMAND_RELOAD_TREE))
        {
            _reloadInfo = TreeController.getInstance().reload(_logname) + "\n";
            _statisticInfo = TreeController.getInstance().getStatistics(_logname);
            response += xml.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
            //response += xml.mkLabel(result + "(real)");
            return response;
        }
        
        if (request.endsWith(Settings.COMMAND_CREATE_TREE_DATABASE))
        {
            _createInfo = TreeController.getInstance().createTreeXML(_confPath) + "\n";
            //_reloadInfo = TreeController.getInstance().reload(_logname) + "\n";
            _statisticInfo = TreeController.getInstance().getStatistics(_logname);
            response += xml.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
            //response += xml.mkLabel("Database created");
            return response;
        }
        
        _statisticInfo = TreeController.getInstance().getStatistics(_logname);
        response += xml.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
        //response += xml.mkLabel("no changes since startup");
        return xml.mkEntry(_logname,response);
    }

    /**
     * returns the id of this object
     */ 
    public String getId() 
    {
        return "Tree";
    }
    
}//class end
