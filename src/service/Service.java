/* Coyright Eric Cariou, 2009 - 2011 */

package service;

import message.MessageType;
import message.TypedMessage;
import communication.CommunicationException;
import message.Message;
import communication.SynchronizedBuffer;

/**
 * Skeleton of a service that has to be specialized for implementing a new service
 */
public abstract class Service implements IService
{

    /**
     * The message dispatcher to use for associating the current service with its type of message
     */
    protected MessageDispatcher dispatcher;

    /**
     * Communication element to use to send messages to service parts on other processes
     */
    protected ICommunication commElt;

    /**
     * Buffer containing the received messages for the service
     */
    protected SynchronizedBuffer<Message> serviceBuffer;

    /**
     * The type of the service
     */
    protected MessageType myType;

    /**
     * Send a message, tagged with the type of the service, to a given process (more precisely,
     * to the service of the same type on this process)
     * @param msg the message to send
     * @throws communication.CommunicationException
     */
    protected void sendMessage(Message msg) throws CommunicationException
    {
        commElt.sendMessage(new TypedMessage(msg.getProcessId(), msg.getData(), myType));
    }

    /**
     * Initialize the service: register the service on the message dispatcher
     * @param dispatcher the message dispatcher to use for associating the current service with its type of message
     * @param commElt the communication element to use to send messages to service parts on other processes
     * @param myType the type of the service
     */
    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {
        this.dispatcher = dispatcher;
        this.commElt = commElt;
        this.myType = myType;
        serviceBuffer = dispatcher.associateService(myType);
    }

    @Override
    public void startManagers(){}

    @Override
    public void terminateManagers(){}
    
    @Override
    public void setServiceBuffer(SynchronizedBuffer<Message> serviceBuffer)
    {
        this.serviceBuffer = serviceBuffer;
    }
    
    @Override
    public void setDispatcher(MessageDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
   
    @Override
    public void setComElt(ICommunication commElt)
    {
        this.commElt = commElt;
    }
    
    @Override
    public void set(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {
        this.dispatcher = dispatcher;
        this.commElt = commElt;
        this.myType = myType;
    }
    
    @Override
    public SynchronizedBuffer<Message> getServiceBuffer()
    {
        return serviceBuffer;
    }
    
    @Override
    public MessageDispatcher getDispatcher()
    {
        return this.dispatcher;
    }
   
    @Override
    public ICommunication getComElt()
    {
        return this.commElt;
    }
}
