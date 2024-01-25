package contentItems;
import helper.ConfFileHelper;
import helper.XMLHelper;
import types.DataComponent;
import types.DataIterator;
/*
 * Jobs.java
 *
 * Created on 17. September 2008, 08:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * represents all jobs of a logbook
 * @author jojo
 */
public class Jobs extends DataComponent{
    
    private DataIterator _list = new DataIterator();
    //private Scheduler _scheduler = null;
    
    
    /** Creates a new instance of Jobs */
    public Jobs(ConfFileHelper conf) 
    {        
        //_scheduler = SchedulerController.getInstance().getScheduler();
        // create all jobs (cron and execute jobs)
        getAllAvailableJobsFromConfFile(conf);
    }

    /**
     * starts a new scheduler
     */
    /*private void startScheduler() 
    {
        try
        {            
            // start scheduler if this has not happened yet
            if(_scheduler==null) _scheduler = new StdSchedulerFactory().getScheduler();
            if(!_scheduler.isStarted()) _scheduler.start();            
        } 
        catch (Exception ex)
        {            
        }        
    }*/
    
    /**
     * process the request
     * respond in xml
     */
    public String getData(String request) 
    {
        if (request == null) return "";
        if (request.equals(""))
        {
            String response = "";
            XMLHelper xml = XMLHelper.getInstance();
            for (int i = 0; i < _list.length(); i++) 
            {
                String lName = _list.itemAt(i).getId();                
                response += xml.mkEntry(lName,_list.itemAt(i).getData(""));
            }
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
        return "Jobs";
    }
    
    
    /**
     * reads all cron and execute jobs from conf file
     * and creates child items
     */
    private void getAllAvailableJobsFromConfFile(ConfFileHelper conf)
    {
        String shift        = conf.getElementValue("new_shift");
        String docroot      = conf.getElementValue("docroot");
        String logroot      = conf.getElementValue("logroot");
        String datapath     = conf.getElementValue("datapath");
        String langCode     = conf.getElementValue("lang_code");
        String host         = conf.getElementValue("host");
        String treeServlet  = conf.getElementValue("tree_servlet");
        String searchServlet = conf.getElementValue("srch_servlet");
        
        String workFile = "/jsp/work.xml";
        String jspFolder = "/jsp";
        String readerFile = "/bin/reader";
        String logname = logroot;
        logname = logname.replaceFirst("/","");
        // create child item
        _list.addItem(new CronJob(docroot+logroot+datapath,
            docroot+logroot+workFile,
            docroot+logroot+jspFolder,
            logname,
            host+treeServlet,
            shift,
            langCode/*,
            _scheduler*/));
        
        
        String time[] = conf.getElementsByTag("time");
        String target[] = conf.getElementsByTag("target");
        if (time.length == target.length)
        {
            for (int i = 0; i < time.length; i++) 
            {
                // create child item
                _list.addItem(new ExecuteJob(logname, 
                                             target[i], 
                                             time[i]/*,
                                             _scheduler*/));
            }
        }
        
    }
}//class end
