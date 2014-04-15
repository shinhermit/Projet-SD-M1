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
    MessageStamp _stamp;
    
    public StampedMessage(ProcessIdentifier processId, Object data, MessageStamp stamp)
    {
        super(processId, data);
        
        _stamp = stamp;
    }
    
    public void setStamp(MessageStamp stamp)
    {
        _stamp = stamp;
    }
    
    public MessageStamp getStamp()
    {
        return _stamp;
    }

    
    @Override
    public String hashString()
    {
        return super.hashString() + _stamp;
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "";
    }
}
