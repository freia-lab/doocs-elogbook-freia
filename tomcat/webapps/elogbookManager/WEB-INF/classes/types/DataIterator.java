package types;

/*
 * Iterator.java
 *
 * Created on 16.  September 2008, 15:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author jojo
 */
public class DataIterator {
    
    /** Creates a new instance of Iterator */
    public DataIterator() {
    }
    
    private DataComponent[] _list = null;
    
    
    /**
     * adds an item to the list
     * if item with existing id is added,
     * the old element is overwritten
     */
    public void addItem(DataComponent e)
    {
        if(e!=null && getItemById(e.getId())!=null)
        {
            for (int i = 0; i < length(); i++)
            {
                if (_list[i].getId().equals(e.getId()))
                {
                    // here element exists and is overwritten
                    _list[i] = e;
                }
            }
        }
        else // add a new element
        {
            DataComponent[] newList = new DataComponent[length()+1];
            for (int i = 0; i < length(); i++) 
            {
                newList[i] = _list[i];            
            }
            newList[newList.length-1] = e;
            _list = newList;
        }
    }
    
    
    /**
     * gets the number of items in the list
     */
    public int length()
    {
        if (_list==null) return 0;
        return _list.length;
    }
    
    /**
     * not implemented yet
     */
    public void removeItem(DataComponent item)
    {
        // implement when needed
    }
    
    /**
     * removes all items from the list
     */
    public void clear()
    {
        _list = null;
    }
    
    /**
     * gets the item at the position "pos"
     */
    public DataComponent itemAt(int pos)
    {
        if (pos<length() && pos>=0)
        {
            return _list[pos];
        }
        else return null;
    }
    
    public DataComponent getItemById(String id)
    {
        
        for (int i = 0; i < length(); i++) 
        {
            if (_list[i].getId().equals(id)) return _list[i];            
        }
        return null;        
    }
    
    
    
}//class end
