/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import communication.ProcessIdentifier;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author josuah
 */
public class LogicalClock
{
    private HashMap<ProcessIdentifier, Integer> _clock;
    
    public LogicalClock()
    {
        _clock = new HashMap();
    }
    
    public void addProcess(ProcessIdentifier process)
    {
        _clock.put(process, 0);
    }
    
    public void addProcess(ProcessIdentifier process, int initialCounter)
    {
        _clock.put(process, initialCounter);
    }
    
    public int size()
    {
        return _clock.size();
    }
    
    public void newEvent(ProcessIdentifier process)
    {
        Integer nb = _clock.get(process);
        
        nb = (nb == null) ? 1 : nb+1;
        
        _clock.put(process, nb);
    }
    
    public Integer getEventCounter(ProcessIdentifier process)
    {
        return _clock.get(process);
    }
    
    public Set<ProcessIdentifier> getAllProcessId()
    {
        return _clock.keySet();
    }
    
    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof LogicalClock))
                eq = false;
            
            else
            {
                LogicalClock stampOther = (LogicalClock)other;
                
                eq = (this._clock == stampOther._clock);
            }
        }
        
        return eq;
    }
    
    public String hashString()
    {
        return _clock.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "Estampille: "+_clock.toString();
    }
}
