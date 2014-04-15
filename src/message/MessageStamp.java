/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import communication.ProcessIdentifier;
import java.util.HashMap;

/**
 *
 * @author josuah
 */
public class MessageStamp
{
    private HashMap<ProcessIdentifier, Integer> _stamp;
    
    public MessageStamp()
    {
        _stamp = new HashMap();
    }
    
    public void newEvent(ProcessIdentifier process)
    {
        Integer nb = _stamp.get(process);
        
        nb = (nb == null) ? 1 : nb+1;
        
        _stamp.put(process, nb);
    }
    
    public String hashString()
    {
        return _stamp.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "Estampille: "+_stamp.toString();
    }
}
