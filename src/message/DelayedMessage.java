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
public class DelayedMessage extends Message
{
    private HashMap<ProcessIdentifier, Integer> _waitingList;
            
    public DelayedMessage(ProcessIdentifier processId, Object data)
    {
        super(processId, data);
        
        _waitingList = new HashMap();
    }
    
    public void setWaitingCounter(ProcessIdentifier processId, int counter)
    {
        if(counter > 0)
        {
            _waitingList.put(processId, counter);
        }
    }
    
    public Integer getwaitingCounter(ProcessIdentifier processId)
    {
        return _waitingList.get(processId);
    }
    
    public Integer newEvent(ProcessIdentifier processId)
    {
        Integer counter = null;
        
        if(_waitingList.containsKey(processId))
        {
            counter = _waitingList.get(processId) - 1;
            
            if(counter == 0)
            {
                _waitingList.remove(processId);
            }
            
            else
            {
                _waitingList.put(processId, counter);
            }
        }
        
        return counter;
    }
}
