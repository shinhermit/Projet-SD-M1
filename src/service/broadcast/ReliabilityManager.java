/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.Message;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import service.SeqMessage;

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
        
        public ReliabilityManager(BasicBroadcastService basicBroadcaster, SynchronizedBuffer<Message> serviceBuffer, SynchronizedBuffer<Message> reliableBuffer)
        {
            _history = new ArrayList();
            _serviceBuffer = serviceBuffer;
            _reliableBuffer = reliableBuffer;
            _basicBroadcaster = basicBroadcaster;
        }
        
        public SeqMessage fetchMessage()
        {
            Message mess = _serviceBuffer.removeElement(true);
            
            if(! (mess instanceof SeqMessage) )
            {
                throw new IllegalStateException("ReliabilityManager.fetchMessage: messages in reliable mode should be of type SeqMessage.\n\t found "+mess.getClass().getName());
            }
            
            return (SeqMessage)mess;
        }

        @Override
        public void run()
        {
            while(true)
            {
                SeqMessage mess = fetchMessage();
                
                if(!_history.contains(mess))
                {
                    _history.add(mess);
                    
                    try
                    {
                        _basicBroadcaster.broadcast(mess);
                    
                        _reliableBuffer.addElement(mess.untypeMessage());

                        //Thread.sleep(100);
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
