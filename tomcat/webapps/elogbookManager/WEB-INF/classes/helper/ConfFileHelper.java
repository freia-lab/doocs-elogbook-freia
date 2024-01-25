package helper;
import java.io.File;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import settings.Settings;
/*
 * ConfFileHelper.java
 *
 * Created on 17. September 2008, 08:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * helper class to work with a conf.xml file
 * @author jojo
 */
public class ConfFileHelper {
    
    private Document doc = null;
    private String _confPath = null;
    
    /** Creates a new instance of ConfFileHelper */
    public ConfFileHelper(File confFile) 
    {
        try
        {
            // load the xml in the doc variable
            _confPath = confFile.getAbsolutePath();
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();            
            DocumentBuilder builder = factory.newDocumentBuilder();            
            doc = builder.parse( confFile );
        }
        catch (Exception ex)
        {
            ex.getMessage();
            LogHelper.getInstance().log(ex.toString()+" ConfFileHelper 43 Error while reading conffile "+_confPath);
        }        
    }
    
    /**
     * gets all elements with the passed tagname
     * and returns the content values as a 
     * string list
     */
    public String[] getElementsByTag(String tagName)
    {
        try
        {
            NodeList l = doc.getElementsByTagName(tagName);
            String result[] = new String[l.getLength()];
            for (int i = 0; i < l.getLength(); i++) 
            {
                result[i]=l.item(i).getFirstChild().getNodeValue();
            }
            return result;
        }
        catch(Exception ex)
        {
            return new String[0];
        }
    }
    
    /**
     * gets the tag names of all first level
     * children of the root element. Useful
     * to get all first level elements of the
     * conf.xml file. Result is a string list
     */
    public String[] getAllElementTags()
    {
        NodeList children = doc.getFirstChild().getChildNodes();
        
        int pos = 0;
        for (int i = 0; i < children.getLength(); i++) 
        {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                pos++;
            }
        }
        String result[] = new String[pos];
        pos = 0;
        for (int i = 0; i < children.getLength(); i++) 
        {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                result[pos] = children.item(i).getNodeName();
                pos++;
            }
        }
        return result;        
    }
    
    
    /**
     * not used ??
     */
    /*public String[] getSimpleContent(String elem)
    {
        Node n = doc.getElementsByTagName(elem).item(0);
        NamedNodeMap attr = n.getAttributes();
        int len = 2 + attr.getLength();
        String result[] = new String[len];
        result[0] = elem;
        //liefert null result[len-1] = n.getNodeValue();
        NodeList children = n.getChildNodes();
        result[len-1]="";
        for (int i = 0; i < children.getLength(); i++) 
        {
            result[len-1] += nodeToString(children.item(i),true);
        }
        
        for (int i = 0; i < attr.getLength(); i++) 
        {
            result[1+i] = attr.item(i).getNodeName() 
                        + "=" 
                        + attr.item(i).getNodeValue();
        }
        return result;
    }*/
    
    /**
     * converts the first element with the
     * passed tag into a string. Open and 
     * close tags are masked
     * children are part of the result.
     * return "" when elem does not exist.
     */
    public String getElementAsString(String elem)
    {
        NodeList l = doc.getElementsByTagName(elem);
        if (l!=null && l.getLength()>0)
        {
            return nodeToString(l.item(0),true);
        }
        else
        {
            return "";
        }
    }
    
    /**
     * gets a node and converts it to a string.
     * open and close tags can be masked.
     * All children are also part of the result.
     */
    private String nodeToString(Node n,boolean maskTags)
    {        
        String openTag;
        String closeTag;
        if(maskTags)
        {
            openTag = "&lt;";
            closeTag = "&gt;";
        }
        else
        {
            openTag = "<";
            closeTag = ">";
        }
        if(n.getNodeType() == Node.COMMENT_NODE)
        {
            return openTag + "!--" +
                    n.getNodeValue() +
                    "--" + closeTag;
                    
        }
        if (n.getNodeType() == Node.TEXT_NODE)
        {
            return n.getTextContent();
        }
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String result = openTag + n.getNodeName();
            NamedNodeMap attr = n.getAttributes();
            for (int i = 0; i < attr.getLength(); i++) 
            {
                result += " "+attr.item(i).getNodeName() 
                        + "=\"" 
                        + attr.item(i).getNodeValue()+"\"";
            }
            result += closeTag;
            NodeList children = n.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) 
            {
                result += nodeToString(children.item(i),maskTags);
            }
            result += openTag+"/"+n.getNodeName()+closeTag;
            return result;
        }
        return "";
    }
    
    /**
     * gets the node value of the first
     * element with the passed tag
     * returns "" when elem does not exist
     */
    public String getElementValue(String elemName)
    {
        try
        {
            NodeList l = doc.getElementsByTagName(elemName);
            Node n = l.item(0);
            n = n.getFirstChild();
            String s = n.getNodeValue();
            return s;//doc.getElementsByTagName(item).item(0).getFirstChild().getNodeValue();
        }
        catch(Exception ex)
        {
            return "";
        }
    }

    /**
     * saves an element with the passed nodename and
     * the passed content and creates a backup file
     * of the old conf.xml file.
     * when an element with the given name already
     * exists it is overwritten.
     */
    public void saveElementInConfFile(String elem, String content) 
    {
        IOHelper.getInstance().copyFile(_confPath,_confPath+".bak");
        String result = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
        result += "\n\n<logbook>";
        NodeList l = doc.getFirstChild().getChildNodes();
        Node n;
        for (int i = 0; i < l.getLength(); i++) 
        {
            //result += "\n\n";
            n = l.item(i); 
            if (n.getNodeType()==Node.ELEMENT_NODE &&
                n.getNodeName().equals(elem))
            {
                result += content;
            }
            else
            {
                result += nodeToString(n,false);
            }
        }
        if (elem.equals(Settings.NEW_CONF_FILE_ELEMENT_ID))
        {   
            result += "\n<!-- Entry added with manager on "+ new Date().toString()+" -->";
            result += "\n" + content + "\n";
        }
        
        result += "</logbook>";
        IOHelper.getInstance().writeFile(_confPath,result);
        try
        {
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();            
            Document test = builder.parse( _confPath );
        }
        catch (Exception ex)
        {
            IOHelper.getInstance().copyFile(_confPath+".bak",_confPath);
            LogHelper.getInstance().log(ex.toString()+" ConfFileHelper 267 Error while saving conffile, try to restore backup" );
        }
    }
}
