/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service;

import communication.SynchronizedBuffer;
import message.Message;
import message.MessageType;

/**
 *
 * @author josuah
 */
public interface IService
{

    /**
     * Initialize the service: register the service on the message dispatcher
     * @param dispatcher the message dispatcher to use for associating the current service with its type of message
     * @param commElt the communication element to use to send messages to service parts on other processes
     * @param myType the type of the service
     */
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType);
   
   /**
    * Start all subservice managers (private buffers managers)
    */
   public void startManagers();
   
   /**
    * Termiantes all subservice managers (private buffers managers)
    */
   public void terminateManagers();
   
   /**
    * Allows to change service buffer.
    * @param serviceBuffer the new service buffer
    */
   public void setServiceBuffer(SynchronizedBuffer<Message> serviceBuffer);
   
   /**
    * Allows to change the dispatcher.
    * @param dispatcher the new service dispatcher
    */
   public void setDispatcher(MessageDispatcher dispatcher);
   
   /**
    * Allows to change the communication module.
    * @param commElt the new communication element.
    */
   public void setComElt(ICommunication commElt);
   
   /**
    * Resets all the properties
    * @param dispatcher
    * @param commElt
    * @param myType 
    */
   public void set(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType);
   
   /**
    *  gets the service buffer for this service
    * @return the service buffer
    */
   public SynchronizedBuffer<Message> getServiceBuffer();
   
   /**
    * gets the dispatcher for this service
    * @return the dispatcher
    */
   public MessageDispatcher getDispatcher();
   
   /**
    * gets the communication element for this service
    * @return the communication element
    */
   public ICommunication getComElt();
}
