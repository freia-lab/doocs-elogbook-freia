package contentItems;
import controller.SchedulerController;
import controller.TreeController;
import helper.ConfFileHelper;
import helper.XMLHelper;
import java.io.File;
import settings.Settings;
import types.DataComponent;
import types.DataIterator;
/*
 * Logbook.java
 *
 * Created on September 15, 2008, 10:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * represents a logbook
 * @author jstrampe
 */
public class Logbook extends DataComponent {
    
    // variables to save logbook parameters
    private String _name = "";
    private String _confPath = "";
    private String _logbookLink = "";
    
    
    private DataIterator _list = new DataIterator();
    
    
    /** Creates a new instance of Logbook */
    public Logbook(String confPath) 
    {
        _confPath = confPath;
        init(confPath);
    }

    public String getLogbookConfig(){
        return _confPath;
    }
    /**
     * process the conf file and create child elements
     */
    private void init(String confPath)
    {
        ConfFileHelper conf = new ConfFileHelper(new File(confPath));
        String logroot = conf.getElementValue("logroot");
        _logbookLink = conf.getElementValue("host")+logroot;
        this._name = logroot.replaceFirst("/","");
        _list.clear();
        // managed content is only added when the conf contains a special tag
        if (!conf.getElementValue(Settings.REQUIRED_CONF_TAG).equals(""))
        {
            addJobs(conf);
            addConf(confPath);
            addTree(confPath);
            addSearch(confPath);
        }
    }
    
    /**
     * add job children to the logbook
     */
    private void addJobs(ConfFileHelper conf)
    {
        _list.addItem(new Jobs(conf));
    }
    
    /**
     * add conf file editor to the logbook
     */
    private void addConf(String confPath)
    {
        _list.addItem(new Conf(confPath));
    }
    
    /**
     * add tree child to the logbook
     */
    private void addTree(String confPath) 
    {
        _list.addItem(new TreeInfo(_name, confPath));
    }

    private void addSearch(String confPath)
    {
        _list.addItem(new SearchInfo(_name, confPath));
    }


    /**
     * process the request
     * respond in xml
     */
    public String getData(String request)
    {
        if (request==null) return "";
        
        if (request.equals("extra/"+Settings.COMMAND_RELOAD_LOGBOOK))
        {
            _list.clear();
            SchedulerController.getInstance().stopJobsFromLogbook(_name);
            TreeController.getInstance().reload(_name);
            init(_confPath);
            // request is changed to "" to show all data again
            request = "";
        }
        
        if (_list.length()==0)
        {
            XMLHelper xml = XMLHelper.getInstance();
            return xml.mkEntry("",xml.mkLabel("This logbook is currently not managed. It may be outdated."));
            //return xml.mkLabel("This logbook is currently not managed. It may be outdated.");
        }
        
        
        if (request.equals(""))
        {
            String response = "";
            XMLHelper xml = XMLHelper.getInstance();
            for (int i = 0; i < _list.length(); i++) 
            {                
                String lName = _list.itemAt(i).getId();
                response += xml.mkEntry(lName,xml.mkLabel(lName),true);
            }
            String extraContent = "";
            extraContent += xml.mkCommand("reload logbook",Settings.COMMAND_RELOAD_LOGBOOK);
            extraContent += xml.mkLink("visit", _logbookLink);
            response += xml.mkEntry("extra",extraContent);
            return response;
        }
        String reqId = request.split("/")[0];
        DataComponent item = _list.getItemById(reqId);
        if(item!= null)
        {
            String newReq = request.replaceFirst(reqId,"");
            if(newReq.startsWith("/")) newReq = newReq.replaceFirst("/","");
            return item.getData(newReq);
        }
        return "";
    }
    
    /**
     * returns the id of this object
     */  
    public String getId() 
    {
        return _name;
    }


}//class end
