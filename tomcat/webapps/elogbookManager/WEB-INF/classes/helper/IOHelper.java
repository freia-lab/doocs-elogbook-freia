package helper;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/*
 * IOHelper.java
 *
 * Created on 16. September 2008, 09:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * helper class to read and write files to disk
 * @author jojo
 */
public class IOHelper {
    
    private static IOHelper _Instance=null;
    
    public static IOHelper getInstance()
    {
        if(_Instance==null)
            _Instance = new IOHelper();
        return _Instance;
    }
    
    /** Creates a new instance of IOHelper */
    private IOHelper() {
    }
    
    /**
     * Renames the "from" file to the "to" file
     * if creation or overwriting does not succeed
     * no exception is launched
     */
    public void copyFile(String from, String to)
    {
        try
        {
            File fFrom = new File(from);
            File fTo = new File(to);
            fFrom.renameTo(fTo);
        }
        catch (Exception ex){LogHelper.getInstance().log(ex.toString()+" IOHelper.java 47 while copying from-to "+from+"-"+to);}
    }
    
    /**
     * saves the 'content' to the file with the given 'path'
     */
    public boolean writeFile(String path, String content)
    {   
        FileWriter fw = null;
        File f = new File(path);
        boolean writingSuccessful = true;
        try
        {
            f.createNewFile();           
            fw = new FileWriter(path);            
            fw.write(content);                       
        }
        catch(IOException ex)
        {            
            // could not write file
            writingSuccessful = false;
            LogHelper.getInstance().log(ex.toString()+" IOHelper.java 68 while writing file "+path);
        }
        finally 
        {
            if(fw!=null)
                try {fw.close();} catch (IOException ex) {LogHelper.getInstance().log(ex.toString()+" IOHelper.java 73");}
            return writingSuccessful;
        }
    }
    
    /**
     * reads file from 'path' and returns its content
     * on error an empty string is returned
     */   
    public String readFile(String path)
    {
        String result = "";
        FileReader fr = null;        
        try
        {
            fr = new FileReader(path);
            char[] buf = new char[10000];
            int readResult = fr.read(buf);
            while(readResult!=-1)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(buf);
                result+=sb.substring(0,readResult);
                readResult = fr.read(buf);
            }
        }
        catch(IOException ex)
        {
            LogHelper.getInstance().log(ex.toString()+" IOHelper.java 101 while reading "+path);
            // could not read 
        }
        finally
        {
            if(fr!=null) 
                try {fr.close();} catch (IOException ex) {LogHelper.getInstance().log(ex.toString()+" IOHelper.java 107");}
        }
        return result;
    }
}//class end
