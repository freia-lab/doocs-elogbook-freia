package contentItems;
import helper.ConfFileHelper;
import helper.XMLHelper;
import java.io.File;
import settings.Settings;
import types.DataComponent;
/*
 * Conf.java
 *
 * Created on 16. September 2008, 15:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * represents an editable conf file
 * @author jojo
 */
public class Conf extends DataComponent{
    
    // saves the absolute conf file path
    private String _confPath = null;
    
    /** Creates a new instance of Conf */
    public Conf(String confPath) {
        _confPath = confPath;
    }
    
    /**
     * process the request
     * respond in xml
     */
    public String getData(String request) 
    {        
        if (request==null) return "";
        ConfFileHelper conf = new ConfFileHelper(new File(_confPath));
        
        // return all elemnts of the conf file
        if (request.equals(""))
        {
            XMLHelper xml = XMLHelper.getInstance();
            String result = "";
            String elements[] = conf.getAllElementTags();
            for (int i = 0; i < elements.length; i++) 
            {
                result += makeEntry(elements[i],conf);//xml.mkEntry(elements[i],xml.mkLabel(elements[i]));
            }
            result += makeEntry(Settings.NEW_CONF_FILE_ELEMENT_ID,conf);
            return result;
        }
        
        // here comes a change entry request
        //"version/edit/<version>1.3.1</version>"
        request = request.replaceAll("&gt", ">");
        request = request.replaceAll("&lt", "<");
        request = request.replaceAll("&space", " ");
        request = request.replaceAll("&question", "?");
        
        String arr[] = request.split("/");
        if (arr.length>2)
        {
            String elem = arr[0];
            String command = arr[1];
            String content = request.replaceFirst(elem+"/"+command+"/","");//arr[2];
            if (command.equals(Settings.COMMAND_EDIT))
            {
                saveEntry(elem,content,conf);
                //System.out.println("save "+content);
            }
            content = content.replaceAll("<","&lt;");
            content = content.replaceAll(">","&gt;");
            String result = "";
            XMLHelper xml = XMLHelper.getInstance();
            //result += xml.mkLabel(elem);        
            result += xml.mkEdit(elem,content);
            result += xml.mkCommand("save",Settings.COMMAND_EDIT);
            return result;
            
        }
        // unknown request ?
        return "";
    }
    
    /**
     * creates a xml entry with a conf file entry data
     * as content. response consist of an edit field
     * and a "save" command (button)
     */
    private String makeEntry(String elem, ConfFileHelper conf)
    {
        XMLHelper xml = XMLHelper.getInstance();
        String content = conf.getElementAsString(elem);
        content = content.replaceAll("\n","");
        if (content.equals("")) content="...";
        String result = "";       
        result += xml.mkEdit(elem,content);
        result += xml.mkCommand("save",Settings.COMMAND_EDIT);
        return xml.mkEntry(elem,result);
    }
    
    /**
     * returns the id of this object
     */
    public String getId() 
    {
        return "Conf";
    }

    /**
     * overwrites or creates new element in the conf file
     */
    private void saveEntry(String elem, String content, ConfFileHelper conf) 
    {
        conf.saveElementInConfFile(elem,content);
    }
    
}
