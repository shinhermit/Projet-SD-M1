/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import communication.ProcessIdentifier;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicMessage extends Message{
    private TotalAtomicType _type;
    private ProcessIdentifier _processIdSender;
    private ProcessIdentifier _processIdReceiver;
    
    public TotalAtomicMessage (ProcessIdentifier processIdSender, 
            ProcessIdentifier processIdReciever,
            Object data,
            TotalAtomicType type) {
        super(processIdSender, data);
        _type= type;
        _processIdSender = processId;
        _processIdReceiver = processIdReciever; 
    }
    
    public TotalAtomicType getType () {
        return _type;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean eq = false;
        if(other == this)
            eq = true;
        else {
            if(! (other instanceof TotalAtomicMessage))
                eq = false;
            else {
                TotalAtomicMessage otherMessage = (TotalAtomicMessage) other;
                eq = (super.equals(other)) && 
                        (getType() == otherMessage.getType())
                        && (getProcessIdSender() == otherMessage.getProcessIdSender())
                        && (getProcessIdReceiver() == otherMessage.getProcessIdReceiver());
            }
        }
        return eq;
    }
    
    @Override
    public String hashString() {
        return super.hashString() + getType().toString() + 
                getProcessIdSender().toString()
                + getProcessIdReceiver().toString();
    }
    
    @Override
    public int hashCode() {
        return this.hashString().hashCode();
    }
    
    public ProcessIdentifier getProcessIdSender () {
        return _processIdSender;
    }
    
    public ProcessIdentifier getProcessIdReceiver () {
        return _processIdReceiver;
    }
}
