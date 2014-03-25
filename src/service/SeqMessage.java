/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service;

import communication.Message;
import communication.ProcessIdentifier;

/**
 *
 * @author josuah
 */
public class SeqMessage extends TypedMessage
{
    private long _sequenceNum;

    protected static long CurrentNumber;
    static
    {
        CurrentNumber = 0;
    }
    
    public SeqMessage(ProcessIdentifier processId, Object data, MessageType type)
    {
        super(processId, data, type);
        
        this._sequenceNum = CurrentNumber;
    }
    
    public long getSequenceNumber(long seqNum)
    {
        return this._sequenceNum;
    }
    
    public TypedMessage unseqMessage()
    {
        return new TypedMessage(this.processId, this.data, this.type);
    }
    
    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof SeqMessage))
                eq = false;
            
            else
            {
                SeqMessage messOther = (SeqMessage)other;
                
                eq = (super.equals(messOther) && this._sequenceNum == messOther._sequenceNum);
            }
        }
        
        return eq;
    }
    
    @Override
    public String hashString()
    {
        return super.hashString() + String.valueOf(this._sequenceNum);
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "[ " + processId + ", seq:" + this._sequenceNum + ", " + Message.messageTypeToString(this.type)+" ] -> " + data;
    }
}
