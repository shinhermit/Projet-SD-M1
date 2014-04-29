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
public class DelayedMessage
{
    private HashMap<ProcessIdentifier, Integer> _waitingList;
    private StampedMessage _mess;
            
    public DelayedMessage(StampedMessage mess)
    {
        _mess = mess;
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
    
    public boolean isReady()
    {
        return _waitingList.isEmpty();
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
    
    public StampedMessage getStampedMessage()
    {
        return _mess;
    }
}
