/* Coyright Eric Cariou, 2009 - 2011 */

package service;

import communication.Message;
import communication.ProcessIdentifier;

/**
 * A typed message is a message with its associated type of service
 */
public class TypedMessage extends Message {

    /**
     * Type of the service
     */
    protected MessageType type;

    /**
     * @return the type of the service
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @param type the type of the service
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * @return the message embedded in the typed message
     */
    public Message untypeMessage() {
        return new Message(this.processId, this.data);
    }

    /**
     * Create a typed message
     * @param processId the process identifier for the message
     * @param data data the data of the message
     * @param type the type of the service
     */
    public TypedMessage(ProcessIdentifier processId, Object data, MessageType type) {
        super(processId, data);
        this.type = type;
    }
    
    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof TypedMessage))
                eq = false;
            
            else
            {
                TypedMessage messOther = (TypedMessage)other;
                
                eq = (super.equals(messOther) && this.type == messOther.type);
            }
        }
        
        return eq;
    }
    
    @Override
    public String hashString()
    {
        return super.hashString() + String.valueOf(this.type);
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }

    @Override
    public String toString()
    {
        return "[ " + processId + ", " + Message.messageTypeToString(this.type)+" ] -> " + data;
    }
}
