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
 * @author ninjatrappeur
 */
public class TotalAtomicMessage extends Message{
    private TotalAtomicType _type;
    
    public TotalAtomicMessage (ProcessIdentifier processId, Object data,
            TotalAtomicType type) {
        super(processId, data);
        this._type= type;
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
                        (this.getType() == otherMessage.getType());
            }
        }
        return eq;
    }
    
    @Override
    public String hashString() {
        return super.hashString() + getType().toString();
    }
    
    @Override
    public int hashCode() {
        return this.hashString().hashCode();
    }
    
}
