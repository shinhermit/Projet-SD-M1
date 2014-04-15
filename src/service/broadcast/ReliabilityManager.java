/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.Message;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import service.MessageType;
import service.SeqMessage;
import service.TypedMessage;

/**
 *
 * @author josuah
 */
    
    public class ReliabilityManager extends Thread
    {
        protected SynchronizedBuffer<Message> _serviceBuffer;
        protected SynchronizedBuffer<Message> _reliableBuffer;
        protected BasicBroadcastService _basicBroadcaster;
        protected ArrayList<SeqMessage> _history;
        
        public ReliabilityManager(BasicBroadcastService basicBroadcaster,
                SynchronizedBuffer<Message> serviceBuffer, SynchronizedBuffer<Message> reliableBuffer,
                ArrayList<SeqMessage> history)
        {
            _serviceBuffer = serviceBuffer;
            _reliableBuffer = reliableBuffer;
            _basicBroadcaster = basicBroadcaster;
            _history = history;
        }
        
        public SeqMessage fetchMessage()
        {
            Message mess = _serviceBuffer.removeElement(true);
            Object data = mess.getData();
            
            if(! (data instanceof SeqMessage) )
            {
                throw new IllegalStateException("ReliabilityManager.fetchMessage: messages in reliable mode should be of type SeqMessage.\n\t found "+mess.getClass().getName());
            }
            
            return (SeqMessage)data;
        }

        @Override
        public void run()
        {
            boolean unknown;
            
            while(true)
            {
                SeqMessage seqMess = fetchMessage();
                
                synchronized(_history)
                {
                    unknown = !_history.contains(seqMess);
                    if(unknown)
                        _history.add(seqMess);
                }

                if(unknown)
                {
                    try
                    {
                        _basicBroadcaster.broadcast(new TypedMessage(seqMess.getProcessId(), seqMess, MessageType.RELIABLE_BROADCAST));
                    
                        _reliableBuffer.addElement(seqMess.toMessage());
                    }
                    
                    catch(Exception e)
                    {
                        System.err.println("ReliableBroadcastService.ReliabilityManager.run: Erreur. "+e.getMessage());
                    }
                }
            }
        }
        
        public void setServiceBuffer(SynchronizedBuffer<Message> serviceBuffer)
        {
            this._serviceBuffer = serviceBuffer;
        }
        
        public void setReliableBuffer(SynchronizedBuffer<Message> reliableBuffer)
        {
            this._reliableBuffer = reliableBuffer;
        }
        
        public void setBasicBroadcaster(BasicBroadcastService basicBroadcaster)
        {
            this._basicBroadcaster = basicBroadcaster;
        }
        
        public SynchronizedBuffer<Message> getServiceBuffer()
        {
            return this._serviceBuffer;
        }
        
        public SynchronizedBuffer<Message> getReliableBuffer()
        {
            return this._reliableBuffer;
        }
        
        public BasicBroadcastService getBasicBroadcaster()
        {
            return this._basicBroadcaster;
        }
    }
