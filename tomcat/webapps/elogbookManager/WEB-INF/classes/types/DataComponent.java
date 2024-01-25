package types;
/*
 * DataProvider.java
 *
 * Created on  16. September 2008, 15:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author jojo
 */
public abstract class DataComponent {
    
    /** Creates a new instance of DataProvider */
    //public DataElement() {
    //}
    
    public abstract String getData(String request);
    public abstract String getId();
}//class end
