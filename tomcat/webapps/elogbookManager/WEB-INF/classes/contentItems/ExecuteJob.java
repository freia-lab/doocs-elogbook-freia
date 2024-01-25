package contentItems;
import controller.SchedulerController;
import cronjobs.QuartzExecuteJob;
import helper.LogHelper;
import helper.XMLHelper;
import java.text.ParseException;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import settings.Settings;
import types.DataComponent;
/*
 * ExecuteJob.java
 *
 * Created on 18. September 2008, 10:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * represents an execution job
 * @author jojo
 */
public class ExecuteJob extends DataComponent{
    
    // variables to save the job state
    private String _logname = "";
    private String _target = "";
    private String _time = "";
    private String _jobName = "";
    // the filename that is executed e.g. "doSomething.sh"
    private String _executeFile = "";
    /*private Scheduler _scheduler = null;*/
    private JobDetail _jobDetail = null;
    private CronTrigger _cronTrigger = null;
    
    /** Creates a new instance of ExecuteJob */
    public ExecuteJob(String logname, String target, String time/*, Scheduler scheduler*/) 
    {
        
        this._logname = logname;
        this._target = target;
        this._time = time;
        //System.out.println("hallo\n"+target);
        try {
            _executeFile = target.split(" ")[0];
            int last = _executeFile.split("/").length-1;
            _executeFile = _executeFile.split("/")[last];
        }
        catch (Exception ex)
        {
            if (_executeFile.equals("") )
                _executeFile = target;
        }
        //System.out.println(_executeFile);
        
        // create and start job
        try
        {
            createJobName();
            createExcecutejob();
            startJob();
        }
        catch (Exception ex)
        {
            // when a job is not createt, running job will always return false
            LogHelper.getInstance().log(ex.toString()+" ExecuteJob.java 55");
        }
    }

    /**
     * process the request
     * respond in xml
     */
    public String getData(String request) 
    {
        // start job
        if(request.equals(Settings.COMMAND_START_JOB))
        {
            try 
            {
                // must recreate job, elsewise it would force a launch on restart
                createExcecutejob();
                startJob();
            }
            catch (Exception ex) {LogHelper.getInstance().log(ex.toString()+" ExecuteJob.java 74");}
        }
        
        // stop job
        if(request.equals(Settings.COMMAND_STOP_JOB))
        {
            try {stopJob();}catch (Exception ex) {LogHelper.getInstance().log(ex.toString()+" ExecuteJob.java 80");}
        }
        
        XMLHelper xml = XMLHelper.getInstance();
        String response = "";
        response += xml.mkLabel("Exec job");
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
        //String info = "next launch:\n "+_cronTrigger.getNextFireTime()+"\n\n";
        //info += "previous launch:\n "+_cronTrigger.getPreviousFireTime()+"\n\n";
        info += "previous launch:\n "+SchedulerController.getInstance().getPrevLaunchTime(_jobName, _logname)+"\n\n";
        info += "target:\n "+_target+"\n\n";
        info += "crontime:\n "+_time+"\n\n";
        info += "jobname:\n "+_jobName+"\n\n";
        response += xml.mkInfo(info);
        response += xml.mkLabel("( "+_executeFile+" )");
        return response;
    }

    /**
     * returns the id of this object
     */   
    public String getId() 
    {
        return _jobName;
    }

    /**
     * create a unique job name
     * remove special characters, they are bad for the
     * html communication
     */
    private void createJobName()
    {
        _jobName = _logname+ _time+ _target;
        _jobName = _jobName.replace("/","");
        _jobName = _jobName.replace(" ", "");
        _jobName = _jobName.replace("?", "x");
        _jobName = _jobName.replace("*", "x");
        _jobName = _jobName.replace(",", "x");
        _jobName = _jobName.replace(".", "x");
    }
    
    /**
     * create a execute job
     */
    private void createExcecutejob() throws ParseException 
    {       
        _cronTrigger = new CronTrigger(_jobName,_logname,_time);
        _jobDetail = new JobDetail(_jobName,_logname,QuartzExecuteJob.class);
        // add the execute job parameter
        _jobDetail.getJobDataMap().put("targetPath",_target);
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
        SchedulerController.getInstance().unscheduleJob(_jobName,_logname);
    }
    
    /**
     * return true if the job is running
     * else it returns false
     */
    private boolean isRunning()
    {
        try
        {
            String jobNames[] = SchedulerController.getInstance().getJobNames(_logname);
            for (int i = 0; i < jobNames.length; i++) 
            {
                if (jobNames[i].equals(_jobName)) 
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
