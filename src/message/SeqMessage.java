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
public class SeqMessage extends TypedMessage
{
    private final long _sequenceNum;
    private final ProcessIdentifier _creatorId;

    protected static long CurrentNumber;
    static
    {
        CurrentNumber = 0;
    }
    
    public SeqMessage(ProcessIdentifier processId, Object data, MessageType type)
    {
        super(processId, data, type);
        
        this._sequenceNum = CurrentNumber;
        ++CurrentNumber;
        
        this._creatorId = processId;
    }
    
    public String getSequenceNumber()
    {
        return this._creatorId.toString()+"["+String.valueOf(this._sequenceNum)+"]";
    }
    
    public TypedMessage toTypedMessage()
    {
        return new TypedMessage(this.processId, this.data, this.type);
    }
    
    public Message toMessage()
    {
        return new Message(this.processId, this.data);
    }
    
    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        // Same reference
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof SeqMessage))
                eq = false;
            
            else
            {
                SeqMessage messOther = (SeqMessage)other;
                
                eq = this.getSequenceNumber().equals(messOther.getSequenceNumber());
            }
        }
        
        return eq;
    }
    
    @Override
    public String hashString()
    {
        return super.hashString() + getSequenceNumber();
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "[ " + processId + ", seq:" + this.getSequenceNumber() + ", " + Message.messageTypeToString(this.type)+" ] -> " + data;
    }
}
