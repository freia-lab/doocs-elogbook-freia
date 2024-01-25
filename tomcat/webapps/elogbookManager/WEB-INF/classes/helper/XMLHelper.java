package helper;
import settings.Settings;
/*
 * IOHelper.java
 *
 * Created on 16. September 2008, 09:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * helper to create xml tags
 * @author jojo
 */
public class XMLHelper {
    
    private static XMLHelper _Instance=null;
    
    /** get the singleton instance of the XMLHelper class */
    public static XMLHelper getInstance()
    {
        if(_Instance==null)
            _Instance = new XMLHelper();
        return _Instance;
    }
    
    /** Creates a new instance of IOHelper */
    private XMLHelper() {
    }
    
    /**
     * create an entry xml element with no subdir structure
     */
    public String mkEntry(String id, String content)
    {
        String attr[] = {Settings.ATTRIBUT_ID,id};
        return mkXMLEntry(Settings.ELEMENT_ENTRY,attr,content);        
    }
    
    /**
     * create an entry xml element with or without
     * a subdir structure
     */
    public String mkEntry(String id, String content,boolean hasSub)
    {
        if(!hasSub)
            return mkEntry(id,content);
        
        String attr[] = {Settings.ATTRIBUT_ID,id,
                         Settings.ATTRIBUT_HAS_SUB,Settings.VALUE_TRUE};
        
        return mkXMLEntry(Settings.ELEMENT_ENTRY,attr,content);
    }
    
    /**
     * create an label xml element with passed label text
     */
    public String mkLabel(String txt)
    {
        return mkXMLEntry(Settings.ELEMENT_LABEL,txt);
    }

    /**
     * create a command xml element with passed label text and commando code
     */
    public String mkCommand(String label, String code)
    {
        String attr[] = {Settings.ATTRIBUT_CODE,code};
        return mkXMLEntry(Settings.ELEMENT_COMMAND,attr,label);
    }
    
    /**
     * create a status xml element with the passed running status
     */
    public String mkStatus(boolean isRunning) 
    {
        if (isRunning)
        {
            String attr[] = {Settings.ATTRIBUT_RUNNING,Settings.VALUE_TRUE};
            return mkXMLEntry(Settings.ELEMENT_STATUS,attr,"");
        }
        else
        {
            String attr[] = {Settings.ATTRIBUT_RUNNING,Settings.VALUE_FALSE};
            return mkXMLEntry(Settings.ELEMENT_STATUS,attr,"");
        }
    }

    /**
     * create an edit xml element with passed id and content
     */
    public String mkEdit(String id, String content) 
    {
        String attr[] = {Settings.ATTRIBUT_ID,id};
        return mkXMLEntry(Settings.ELEMENT_EDIT,attr,content);
    }
    
    /**
     * create an info xml element with passed content
     */
    public String mkInfo(String content) 
    {
        //content = replaceAllSpecialCharacters(content);
        return mkXMLEntry(Settings.ELEMENT_INFO,content);
    }
    
    /**
     * create a link xml element with passed label text and link url
     */
    public String mkLink(String label, String link) 
    {
        String attr[] = {Settings.ATTRIBUT_LINK,link};
        return mkXMLEntry(Settings.ELEMENT_LINK,attr,label);
    }
    
    /**
     * build a xml entry with the passed parameters
     */
    private String mkXMLEntry(String tag, String[] attr, String content)
    {
        String result = "<" + tag;
        // attributes are listed as paires: name/value
        for (int i = 0; i < attr.length/2; i++) 
        {
            int pos=i*2;
            if(attr.length>pos+1)
            {
                result += " "+attr[pos]+"=\""+attr[pos+1]+"\"";
            }
        }
        result += ">" + content + "</" + tag + ">";
        return result;
    }
    
    /**
     * build a xml entry with the passed parameters
     */
    private String mkXMLEntry(String tag, String content)
    {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    /**
     * replaces all problematic html characters with its mask
     * @param content String with characters that should be masked or null
     * @return String with the masked characters
     
    private String replaceAllSpecialCharacters(String content) 
    {
        if (content==null) return "";
        return content.replace("&", "&amp;");
    }*/

    
    
}
