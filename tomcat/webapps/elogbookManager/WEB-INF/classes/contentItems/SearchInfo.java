/*
 * TreeInfo.java
 *
 * Created on October 15, 2008, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package contentItems;

import controller.SearchController;
import helper.XMLHelper;
import settings.Settings;
import types.DataComponent;

/**
 * represents the tree info and client controller part
 * @author jstrampe
 */
public class SearchInfo extends DataComponent{

    private String _logname = "";
    private String _confPath = "";

    private String _createInfo = "";
    private String _statisticInfo = "";

    /** Creates a new instance of SearchInfo */
    public SearchInfo(String logname, String confPath)
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
        _createInfo = "no recreate since last call" ;
        _statisticInfo = "hier muessen statistiken hin";

        XMLHelper xml = XMLHelper.getInstance();
        response += xml.mkCommand("recreate index",Settings.COMMAND_CREATE_INDEX);

        if (request.endsWith(Settings.COMMAND_CREATE_INDEX))
        {
            _createInfo = SearchController.getInstance().recreateIndex(_logname) + "\n";
            _statisticInfo = SearchController.getInstance().getStatistics(_logname);
            response += xml.mkInfo(_createInfo+" " +_statisticInfo);
            //response += xml.mkLabel(result + "(real)");
            return response;
        }

        _statisticInfo = SearchController.getInstance().getStatistics(_logname);
        response += xml.mkInfo(_createInfo+ " " +_statisticInfo);
        //response += xml.mkLabel("no changes since startup");
        return xml.mkEntry(_logname,response);
    }

    /**
     * returns the id of this object
     */
    public String getId()
    {
        return "Search";
    }

}//class end
