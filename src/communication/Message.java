/* Coyright Eric Cariou, 2009 - 2011 */

package communication;

import service.MessageType;

/**
 * Messages are sent and received by processes. A message contains two fields: the identifier
 * of the sender (or receiver) and the data to send (or being received). Data is simply any
 * kind of Java object (that must nevertheless be serializable).
 */
public class Message implements java.io.Serializable
{

    /**
     * The identifier of the process that either sent the message or either is the
     * receiver of the message
     */
    protected ProcessIdentifier processId;
    /**
     * Data embedded in the message
     */
    protected Object data;

    /**
     * @return the process identifier embedded in the message
     */
    public ProcessIdentifier getProcessId()
    {
        return processId;
    }

    /**
     * @param processId the process identifier to set
     */
    public void setProcessId(ProcessIdentifier processId)
    {
        this.processId = processId;
    }

    /**
     * @return the data of the message
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data)
    {
        this.data = data;
    }

    /**
     * @param processId the process identifier for the message
     * @param data the data of the message
     */
    public Message(ProcessIdentifier processId, Object data)
    {
        super();
        this.processId = processId;
        this.data = data;
    }
    
    @Override
    public boolean equals(Object other)
    {
        boolean eq = false;
        
        if(other == this)
            eq = true;
        
        else
        {
            if(! (other instanceof Message))
                eq = false;
            
            else
            {
                Message messOther = (Message)other;
                
                eq = (data.hashCode() == messOther.data.hashCode());
            }
        }
        
        return eq;
    }
    
    public String hashString()
    {
        return this.data.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.hashString().hashCode();
    }
    
    public static String messageTypeToString(MessageType type)
    {
       String stringType = "";
       
       switch(type)
       {
           case NONE:
               stringType = "None";
               break;

           case IDENTIFICATION:
               stringType = "Identification";
               break;

           case BASIC_BROADCAST:
               stringType = "Basic broadcast";
               break;

           case RELIABLE_BROADCAST:
               stringType = "Reliable broadcast";
               break;

           case CAUSAL_RELIABLE_BROADCAST:
               stringType = "Causal reliable broadcast";
       }
       
       return stringType;
    }

    @Override
    public String toString()
    {
        return "[ " + processId + " ] -> " + data;
    }
}
