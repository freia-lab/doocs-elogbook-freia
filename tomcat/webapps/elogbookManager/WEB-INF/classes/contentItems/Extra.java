/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package contentItems;

import controller.SchedulerController;
import controller.SearchController;
import controller.TreeController;
import helper.XMLHelper;
import helper.LogHelper;
import settings.Settings;
import types.DataComponent;

/**
 *
 * @author jstrampe
 */
public class Extra extends DataComponent{

    private String cutMessage = " ...The log has been cut !\nSee logfile for whole log: ";

    public Extra() {
    }

    public String getData(String request) {
        if (request==null) return "";
        
        if (request.equals(""))
        {
            String result = "";
            
            String log = "";
            XMLHelper xml = XMLHelper.getInstance();
            log += xml.mkLabel("Log");
            log += xml.mkInfo(getLogMessage());
            result += xml.mkEntry("log", log);
            
            String cronjobs = "";
            cronjobs += xml.mkLabel("Cronjobs");
            cronjobs += xml.mkInfo(SchedulerController.getInstance().toString());
            result += xml.mkEntry("cronjobs", cronjobs);
            
            String trees = "";
            trees += xml.mkLabel("Trees");
            trees += xml.mkInfo(TreeController.getInstance().getStatistics());
            result += xml.mkEntry("trees", trees);

            // adding a search service to controlled Items:
            String search = "";
            search += xml.mkLabel("Search");
            search += xml.mkInfo(SearchController.getInstance().getStatistics());
            search += xml.mkLabel("set debug: ");
            search += xml.mkCommand("NONE",Settings.COMMAND_NO_DEBUG);
            search += xml.mkCommand("NORMAL",Settings.COMMAND_NORMAL_DEBUG);
            search += xml.mkCommand("HIGH", Settings.COMMAND_HIGH_DEBUG);
            search += xml.mkCommand("HIGHEST", Settings.COMMAND_HIGHEST_DEBUG);
            
            result += xml.mkEntry("search", search);
            return result;
        }
        if (request.equals("extra/"+Settings.COMMAND_NO_DEBUG)){
            SearchController.getInstance().setSrchDebug(Settings.COMMAND_NO_DEBUG);

        }else if (request.equals("extra/"+Settings.COMMAND_NORMAL_DEBUG)){
            SearchController.getInstance().setSrchDebug(Settings.COMMAND_NORMAL_DEBUG);

        }else if (request.equals("extra/"+Settings.COMMAND_HIGH_DEBUG)){
            SearchController.getInstance().setSrchDebug(Settings.COMMAND_HIGH_DEBUG);
            
        }else if(request.equals("extra/"+Settings.COMMAND_HIGHEST_DEBUG)){
            SearchController.getInstance().setSrchDebug(Settings.COMMAND_HIGHEST_DEBUG);
        }
        return "";
    }

    public String getId() {
        return "Extra";
    }
    
    /**
     * Generate a logmessage that will be shown
     * in the manager.
     * @return the generated logmessage.
     */
    private String getLogMessage() {
        String result = LogHelper.getInstance().getLog();
        // long messages get a note that the message was cut
        if (LogHelper.CUT_LENGTH < result.length())
        {
            result = result.concat(cutMessage).concat(Settings.LOGFILE);
        }
        return result;
    }
    

}
