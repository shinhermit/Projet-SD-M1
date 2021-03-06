/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import message.Message;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import message.SeqMessage;
import message.StampedMessage;
import message.TotalAtomicMessage;

/**
 *
 * @author josuah
 */
    
    public class ReliabilityManager extends Thread
    {
        protected SynchronizedBuffer<Message> _serviceBuffer;
        protected SynchronizedBuffer<Message> _reliableBuffer;
        protected SynchronizedBuffer<Message> _causalBuffer;
        protected SynchronizedBuffer<TotalAtomicMessage> _totalBuffer;
        protected BasicBroadcastService _basicBroadcaster;
        protected ArrayList<SeqMessage> _history;
        
        protected final Object _historyLock;
        
        protected boolean _isOn;
        
        public ReliabilityManager(Object historyLock)
        {
            _historyLock = historyLock;
            _isOn = false;
        }
        
        public void initialize(BasicBroadcastService basicBroadcaster, SynchronizedBuffer<Message> serviceBuffer,
                SynchronizedBuffer<Message> reliableBuffer, SynchronizedBuffer<Message> causalBuffer, 
                SynchronizedBuffer<TotalAtomicMessage> totalBuffer, ArrayList<SeqMessage> history)
        {
            _serviceBuffer = serviceBuffer;
            _reliableBuffer = reliableBuffer;
            _causalBuffer = causalBuffer;
            _totalBuffer = totalBuffer;
            _basicBroadcaster = basicBroadcaster;
            _history = history;
        }
        
        public SeqMessage fetchMessage()
        {
            Message mess = _serviceBuffer.removeElement(true);
            Object data = mess.getData();
            
            if(! (data instanceof SeqMessage) )
            {
                throw new IllegalStateException("ReliabilityManager::fetchMessage: messages in reliable mode should be of type SeqMessage.\n\t found "+data.getClass().getName());
            }
            
            return (SeqMessage)data;
        }
        
        public void quit()
        {
            _isOn = false;
        }

        @Override
        public void run()
        {
            boolean unknown;
            
            _isOn = true;
            while(_isOn)
            {
                SeqMessage seqMess = fetchMessage();
                
                synchronized(_historyLock)
                {
                    unknown = !_history.contains(seqMess);
                    if(unknown)
                        _history.add(seqMess);
                }

                if(unknown)
                {
                    try
                    {
                        _basicBroadcaster.broadcast(seqMess);
                        
                        Object encapsulated = seqMess.getData();
                        
                        if(encapsulated instanceof StampedMessage)
                            _causalBuffer.addElement((StampedMessage)encapsulated);
                        
                        else if(encapsulated instanceof TotalAtomicMessage)
                            _totalBuffer.addElement((TotalAtomicMessage)encapsulated);
                         
                        // It is not an encapsulated message, but a raw
                        else
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
        
        public void setCausalBuffer(SynchronizedBuffer<Message> causalBuffer)
        {
            this._causalBuffer = causalBuffer;
        }
        
        public void setTotalBuffer(SynchronizedBuffer<TotalAtomicMessage> totalBuffer)
        {
            this._totalBuffer = totalBuffer;
        }
        
        public void setBuffers(SynchronizedBuffer<Message> serviceBuffer,
                SynchronizedBuffer<Message> reliableBuffer, SynchronizedBuffer<Message> causalBuffer,
                SynchronizedBuffer<TotalAtomicMessage> totalBuffer)
        {
            this._serviceBuffer = serviceBuffer;
            this._reliableBuffer = reliableBuffer;
            this._causalBuffer = causalBuffer;
            this._totalBuffer = totalBuffer;
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
        
        public SynchronizedBuffer<Message> getCausalBuffer()
        {
            return this._causalBuffer;
        }
        
        public SynchronizedBuffer<TotalAtomicMessage> getTotalBuffer()
        {
            return this._totalBuffer;
        }
        
        public BasicBroadcastService getBasicBroadcaster()
        {
            return this._basicBroadcaster;
        }
    }
