package controller;
import contentItems.Extra;
import contentItems.Logbook;
import helper.LogHelper;
import helper.XMLHelper;
import java.io.File;
import types.DataComponent;
import types.DataIterator;
/*
 * DataController.java
 *
 * Created on 3. September 2008, 20:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * main element to control the managed parts that are shown to the client
 * @author jojo
 */
public class DataController extends DataComponent{

    private static DataController _Instance = null;

    private DataIterator _list = new DataIterator();

    /** get the singleton instance */
    public static DataController getInstance()
    {
        if (_Instance == null)
            _Instance =  new DataController();
        return _Instance;
    }

    /** constructor */
    private DataController() {}

    /**
     * get the path to the servlet location
     * like "/var/lib/tomcat5/webapps/elogbookManager"
     * then the webapps folder is searched for
     * folders containing a conf.xml file
     * every conf.xml file represents a logbook
     * which will be created as a child element
     */
	public void getAllLogbooks(String path)
    {
        File f = new File(path);
        // the "extra" menu should be first
        _list.addItem(new Extra());
        if (f!=null)
        {
            f = f.getParentFile();
            // now f should be the webapps folder
            if (f.isDirectory())
            {   
                // logbooks are sorted
                File list[] = sort(f.listFiles());
                for (int i = 0; i < list.length; i++) 
                {                    
                    File confFile = new File(list[i].getAbsolutePath()+"/conf.xml");
                    if(confFile.exists())
                    {
                        LogHelper.getInstance().timerStart("loading logbook "+confFile.getAbsolutePath());
                        _list.addItem(new Logbook(confFile.getAbsolutePath()));
                        LogHelper.getInstance().timerStop();
                    }
                }
            }
        }
        
    }// function end

    /**
     * sorts a list of files and folders by name and folders
     * are bigger than files
     * @param unsorted an unsorted list of files and folders
     * @return a sorted list of files and folders
     */
    private File[] sort(File[] unsorted)
    {
        File sorted[] = new File[unsorted.length];
        File runner;
        int count = 0;
        int index;
        boolean isSorted=false;
        while(!isSorted)
        {
            runner = unsorted[0];
            index = 0;
            for (int i = 0; i < unsorted.length; i++)
            {
                if (!isBigger(runner,unsorted[i]))
                {
                    runner = unsorted[i];
                    index=i;
                }
            }
            if (runner==null)
            {
                isSorted=true;
            }
            else
            {
                sorted[count]=runner;
                unsorted[index]=null;
                count++;
            }
        }
        return sorted;
    }

    /**
     * finds out if the first file is bigger than
     * the second file
     */
    private boolean isBigger(File first, File second)
    {
        if (second == null) return true;
        if (first == null)  return false;
        return (first.getName().compareTo(second.getName())<0);
        //return (first.getName().compareTo(second.getName())>0);
    }



    /**
     * response is in xml syntax
     */
    public String getData(String request)
    {
        if (request==null) return "";
        if (request.equals(""))
        {
            String response = "";
            XMLHelper xml = XMLHelper.getInstance();
            for (int i = 0; i < _list.length(); i++)
            {
                String lName = _list.itemAt(i).getId();
                response += xml.mkEntry(lName,xml.mkLabel(lName),true);
            }
            //response += xml.mkEntry("createNewLogbook",xml.mkLink("Create a new Logbook !","createLogbook.html"));
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
        return "Manager";
    }
}
