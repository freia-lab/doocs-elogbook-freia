package controller;
import helper.LogHelper;
import java.util.Date;
import search.*;

/*
 * SearchController.java
 *
 * Created on September 29, 2008, 9:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * main controller to manage the quartz scheduler
 * @author jstrampe
 * changed for search by anna
 */
public class SearchController {
    
    private static SearchController _Instance = null;

    /** get the singleton instance of the search manager */
    public static SearchController getInstance()
    {
        if (_Instance == null)
            _Instance =  new SearchController();
        return _Instance;
    }
    
    public void init(String docroot)
    {
 
        try{
            if(docroot != null) SearchServer.getInstance().setParam(docroot);
            SearchServer.getInstance().setDebug("NORMAL");
            SearchServer.getInstance().startServer();
            //SearchServer.getInstance().getInfo(); writes status info into LogHelper
        } catch (Exception ex){
            LogHelper.getInstance().log("Search Controller: Error occured while connecting to Search Servlet. Error message : "+ ex.toString());
        } 
    }
    /** Creates a new instance of SearchController */
    private SearchController(){     
    }

    public void setSrchDebug(String deb){
        SearchServer.getInstance().setDebug(deb);
        LogHelper.getInstance().log("Setting Search Server debug level to " + deb);
    }


    public void setRequest(final String req){

        Thread worker = new Thread(){
            public void run(){
                LogHelper.getInstance().log("Search Controller: ------> sent new request:" + req + " at " + (new Date()).toString());
                SearchServer.getInstance().setRequest(req);
            }
        };
        worker.start();
    }

    private void stop(){
        _Instance = null;
    }
    public String getData(String request){
       String ans = "";
       // do output: ans = ... request...
       return ans;
    }
    public void stopSearch(){
        try{
            LogHelper.getInstance().log("stopping Search  at " + (new Date()).toString());
            SearchServer.getInstance().ThreadMustStop();
            SearchServer.getInstance().stopServer();
            stop();
        } catch (Exception e){
            LogHelper.getInstance().log("!!!!!!!! couldn't stop the search, reason: " + e.toString());
        }
    }

    public String recreateIndex(String logname){
        String log = "";
        /*try{
            log = "recreateIndex started for " + logname + " at " + (new Date()).toString();
            LogHelper.getInstance().log(log);

            String req = "logbook="+logname+"&path=/data&create=true&flag=xml";
            SearchServer.getInstance().setRequest(req);
         
        } catch (Exception e){
            log = "couldn't recreate index for " + logname + ", reason: " + e.toString();
            LogHelper.getInstance().log(log);
        }*/

        return log;
    }

    public String getStatistics(String logname){
        String  _statistic = "no statistics available for " + logname;
        //SearchServer.getInstance().getInfo();
        
        return _statistic;
    }

    public String getStatistics(){
        String  _statistic = "no statistics available";
        SearchServer ins = SearchServer.getInstance();
        if(ins != null){
            if(ins.isAlive() )
                _statistic = "Search Server is running";
            else
                _statistic = "Search Server is NOT running";

        }else
            _statistic = "Search Server instance is NULL !! Stopped previously?... ";
        //SearchServer.getInstance().getInfo();

        return _statistic;
    }

    /**
     * returns the id of this object
     */
    public String getId()
    {
        return "Lucene Search Service";
    }    
    
}
