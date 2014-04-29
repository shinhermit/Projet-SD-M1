/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import communication.ProcessIdentifier;

/**
 *
 * @author josuah
 */
public class StampedMessage extends Message
{
    LogicalClock _stamp;
    
    public StampedMessage(ProcessIdentifier processId, Object data, LogicalClock stamp)
    {
        super(processId, data);
        
        _stamp = stamp;
    }
    
    public void setStamp(LogicalClock stamp)
    {
        _stamp = stamp;
    }
    
    public LogicalClock getStamp()
    {
        return _stamp;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof StampedMessage))
                eq = false;
            
            else
            {
                StampedMessage messOther = (StampedMessage)other;
                
                eq = (super.equals(messOther) && this._stamp == messOther._stamp);
            }
        }
        
        return eq;
    }
    
    @Override
    public String hashString()
    {
        return super.hashString() + _stamp.hashString();
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "[ " + processId + ", Stamp: "+_stamp+" ] -> " + data;
    }
    
    public Message toMessage()
    {
        return new Message(this.processId, this.data);
    }
}
