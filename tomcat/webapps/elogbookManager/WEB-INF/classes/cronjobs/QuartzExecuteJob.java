package cronjobs;
import helper.LogHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/*
 * executeJob.java
 *
 * Created on July 21, 2008, 8:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author jstrampe
 *
 * job that makes an execute call
 *
 */
public class QuartzExecuteJob implements Job{
    
    String targetPath;

    public QuartzExecuteJob()
    {
        
    }
    
    /**
     * enter here when the job is launched ...
     * parameters to the job are read and target path
     * is executed
     */
    public void execute(JobExecutionContext context) throws JobExecutionException 
    {
        readParameters(context.getJobDetail().getJobDataMap());
        executeTarget();        
    }
    
    /**
     * reads parameters that are passed to this job
     */
    private void readParameters(JobDataMap jdm)
    {
        try
        {            
            targetPath = (String)jdm.get("targetPath");            
        }
        catch(Exception ex)
        {
            LogHelper.getInstance().log(ex.toString() + " QuartzExecuteJob.java 54");
        }  
    }

    /**
     * executes the targetPath
     */
    private void executeTarget() 
    {
        
        try 
        {
            Process p;
            p = Runtime.getRuntime().exec(targetPath);
            p.waitFor();
        } 
        catch (Exception ex) 
        {
            LogHelper.getInstance().log(ex.toString() + " QuartzExecuteJob.java 72");
        }
       
    }
    
}