package contentItems;
import controller.SchedulerController;
import cronjobs.QuartzCronJob;
import helper.LogHelper;
import helper.XMLHelper;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import settings.Settings;
import types.DataComponent;
/*
 * CronJob.java
 *
 * Created on 18. September 2008, 10:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * represents a cronjob to create new logbook shifts
 * @author jojo
 */
public class CronJob extends DataComponent
{
    // variables to save the job state
    private String _datapath = "";
    private String _workFile = "";
    private String _jspFolder = "";
    private String _logname = "";
    private String _servletURL = "";
    private String _shift = "";
    private String _langCode = "";
    /*private Scheduler _scheduler = null;*/
    private JobDetail _jobDetail = null;
    private CronTrigger _cronTrigger = null;
    
    
    /** Creates a new instance of CronJob */
    public CronJob(String datapath, String workFile, String jspFolder, String logname, String servletURL, String shift, String langCode/*, Scheduler scheduler*/) 
    {
        this._datapath = datapath;
        this._langCode = langCode;
        this._logname = logname;
        this._jspFolder = jspFolder;
        this._servletURL = servletURL;
        this._shift = shift;
        this._workFile = workFile;
        /*this._scheduler = scheduler;*/
        
        // create and start the job
        try
        {
            createMainCronjob();
            startJob();
        }
        catch (Exception ex)
        {
            LogHelper.getInstance().log(ex.toString() + " Cronjob.java 61");
            // when a job is not createt, running job will always return false
        }
    }

    /**
    * process the request
    * respond in xml
    */
    @Override
    public String getData(String request) 
    {
        // start the job
        if(request.equals(Settings.COMMAND_START_JOB))
        {
            try {startJob();}catch (Exception ex) {LogHelper.getInstance().log(ex.toString()+" Cronjob.java 75");};
        }
        
        // stop the job
        if(request.equals(Settings.COMMAND_STOP_JOB))
        {
            try {stopJob();}catch (Exception ex) {LogHelper.getInstance().log(ex.toString()+" Cronjob.java 81");};
        }
        
        XMLHelper xml = XMLHelper.getInstance();
        String response = "";        
        response += xml.mkLabel("Cron job");
        boolean isRunning = isRunning();
        response += xml.mkStatus(isRunning);
        if (isRunning)
        {
            response += xml.mkCommand("pause job",Settings.COMMAND_STOP_JOB);
        }
        else
        {
            response += xml.mkCommand("start job",Settings.COMMAND_START_JOB);
        }
        String info = "next launch:\n "+_cronTrigger.getFireTimeAfter(new Date())+"\n\n";
        //info += "previous launch:\n "+_cronTrigger.getPreviousFireTime()+"\n\n";
        info += "previous launch:\n "+SchedulerController.getInstance().getPrevLaunchTime(Settings.MAIN_CREATION_CRONJOB_NAME, _logname)+"\n\n";
        info += "shift interval:\n "+_shift+"\n\n";
        info += "datapath :\n "+_datapath+"\n\n";
        info += "language:\n "+_langCode+"\n\n";
        info += "reader:\n "+_jspFolder+"\n\n";
        info += "workfile:\n "+_workFile+"\n\n";
        info += "servlet:\n "+_servletURL+"\n";        
        response += xml.mkInfo(info);
        return response;
    }

    /**
     * returns the id of this object
     */    
    public String getId() 
    {
        return "cronjob";
    }
    
    /**
     * create the main cronjob
     */
    private void createMainCronjob() throws Exception
    {
        // save the job parameters
        _jobDetail = new JobDetail(Settings.MAIN_CREATION_CRONJOB_NAME,_logname,QuartzCronJob.class);
        _jobDetail.getJobDataMap().put("datapath",_datapath);
        _jobDetail.getJobDataMap().put("workFile",_workFile);
        _jobDetail.getJobDataMap().put("jspFolder",_jspFolder);
        _jobDetail.getJobDataMap().put("logname",_logname);
        _jobDetail.getJobDataMap().put("servletURL",_servletURL);
        _jobDetail.getJobDataMap().put("shift",_shift);
        _jobDetail.getJobDataMap().put("langCode",_langCode);
        _jobDetail.getJobDataMap().put("sleepTime",Settings.getSleepTime());

        String triggerInterval = null;

        // used to check if there was a missed shift
        // is only possible with Y,M,W or D settings
        boolean singleTrigger = true;

        switch(_shift.toUpperCase().charAt(0))
        {
            //s m h dm m dw (y)
            case 'Y' : triggerInterval = "1 0 0 1 1 ?"; break;
            case 'M' : triggerInterval = "1 0 0 1 * ?"; break;
            case 'W' : triggerInterval = "1 0 0 ? * MON"; break;
            case 'D' : triggerInterval = "1 0 0 * * ?"; break;
            case '3' : 
                if (_shift.length()==1)
                {
                    triggerInterval = "0 0 7,15,23 * * ?"; 
                    break;
                }
                else
                {
                    triggerInterval = _shift; 
                    singleTrigger=false;
                    break;
                }                
            default : triggerInterval = _shift; singleTrigger=false; break;
            //default : triggerInterval = null; break;
        }

        // create trigger
        _cronTrigger = new CronTrigger(Settings.MAIN_CREATION_CRONJOB_NAME,_logname,triggerInterval);

        //say(trigger.getFireTimeAfter(new Date()).toString());

        // single trigger is launched if shift is Y M W D or 3
        // then a missed shift can be created
        if (singleTrigger)
        {
            JobDetail sJobDetail = new JobDetail(Settings.SIMPLE_JOB_NAME,_logname,QuartzCronJob.class);
            sJobDetail.getJobDataMap().put("datapath",_datapath);
            sJobDetail.getJobDataMap().put("workFile",_workFile);
            sJobDetail.getJobDataMap().put("readerFile",_jspFolder);
            sJobDetail.getJobDataMap().put("logname",_logname);
            sJobDetail.getJobDataMap().put("servletURL",_servletURL);
            sJobDetail.getJobDataMap().put("shift",_shift);
            sJobDetail.getJobDataMap().put("langCode",_langCode);
            sJobDetail.getJobDataMap().put("sleepTime",Settings.getSleepTime());
            long startTime = System.currentTimeMillis() + 3000L;                
            SimpleTrigger sTrigger = new SimpleTrigger(Settings.SIMPLE_JOB_NAME, _logname, new java.util.Date(startTime),null,0,0L);
            // is launched to get the last shift right
            SchedulerController.getInstance().scheduleJob(sJobDetail, sTrigger);        
        }
    }

    /**
     * start the job
     */
    private void startJob() throws SchedulerException 
    {
        SchedulerController.getInstance().scheduleJob(_jobDetail,_cronTrigger);
    }
    
    /**
     * stop the job
     */
    private void stopJob() throws SchedulerException
    {
        SchedulerController.getInstance().unscheduleJob(Settings.MAIN_CREATION_CRONJOB_NAME,_logname);
    }

    /**
     * returns true if the job is running
     * else returns false
     */
    private boolean isRunning()
    {
        try
        {
            String jobNames[] = SchedulerController.getInstance().getJobNames(_logname);
            for (int i = 0; i < jobNames.length; i++) 
            {
                if (jobNames[i].equals(Settings.MAIN_CREATION_CRONJOB_NAME)) 
                {
                    return true;
                }
            }        
            return false;
        }
        catch (SchedulerException ex)
        {
            return false;
        }
    }


    
}// class end
