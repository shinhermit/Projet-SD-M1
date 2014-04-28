/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.ProcessIdentifier;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import message.DelayedMessage;
import message.LogicalClock;
import message.Message;
import message.SeqMessage;
import message.StampedMessage;

/**
 *
 * @author josuah
 */
public class CausalityManager extends Thread
{
    protected SynchronizedBuffer<Message> _reliableBuffer;
    protected SynchronizedBuffer<Message> _causalBuffer;
    protected ArrayList<DelayedMessage> _delayedMessages;
    protected LogicalClock _localClock;
    protected ProcessIdentifier _myId;
    
    protected boolean _isOn;
    
    public CausalityManager(LogicalClock localClock,
            SynchronizedBuffer<Message> reliableBuffer, SynchronizedBuffer<Message> causalBuffer)
    {
        _delayedMessages = new ArrayList();
        
        _reliableBuffer = reliableBuffer;
        _causalBuffer = causalBuffer;
        _localClock = localClock;
        
        _isOn = true;
    }

    public void setProcessId(ProcessIdentifier myId)
    {
        _myId = myId;
    }

    public StampedMessage fetchMessage()
    {
        Message mess = _reliableBuffer.removeElement(true);
        Object data = mess.getData();

        if(! (data instanceof StampedMessage) )
        {
            throw new IllegalStateException("CausalityManager.fetchMessage: messages in causal mode should be of type StampedMessage.\n\t found "+data.getClass().getName());
        }

        return (StampedMessage)data;
    }
    
    public DelayedMessage checkCausality(StampedMessage stampMess)
    {
        boolean first = true;
        DelayedMessage delayedMess = null;
        
        LogicalClock messClock = stampMess.getStamp();
        
        if(_localClock.size() != messClock.size())
        {
            throw new IllegalStateException("CausalityManager.checkCausality: the stamps don't have the same size");
        }
        
        for(ProcessIdentifier processId : messClock.getAllProcessId())
        {
            if(processId != _myId)
            {
                int nbToWait = messClock.getEventCounter(processId) - _localClock.getEventCounter(processId);

                if(nbToWait > 0)
                {
                    if(first)
                    {
                        delayedMess = new DelayedMessage(stampMess);
                        first = false;
                    }
                    
                    delayedMess.setWaitingCounter(processId, nbToWait);
                }
            }
        }
        
        return delayedMess;
    }
    
    public void updateWaitingList(ProcessIdentifier processId)
    {
        for(DelayedMessage delayedMess : _delayedMessages)
        {
            delayedMess.newEvent(processId);
            
            if(delayedMess.isReady())
            {
                //Add id back to serviceBuffer, so it will be processed by this method again
                _reliableBuffer.addElement(delayedMess.getStampesMessage());
                _delayedMessages.remove(delayedMess); // Is it safe to continue iterating the list?
            }
        }
    }
    
    public void quit()
    {
        _isOn = false;
    }
    
    @Override
    public void run()
    {
        DelayedMessage delayedMess;
        
        while(_isOn)
        {
            StampedMessage stampMess = fetchMessage();
            
            _localClock.newEvent(stampMess.getProcessId()); // Should this be done after delivery?
            
            delayedMess = this.checkCausality(stampMess);
            
            if(delayedMess != null)
            {
                _delayedMessages.add(delayedMess);
            }
            
            else
            {
                Object data = stampMess.getData();
                
                if(! (data instanceof SeqMessage) )
                {
                    throw new IllegalStateException("CausalityManager.run: messages inside StampedMessage should be SeqMessage.\n\t found "+data.getClass().getName());
                }
                
                _causalBuffer.addElement((SeqMessage)data);
                this.updateWaitingList(stampMess.getProcessId());
            }
        }
    }
}
