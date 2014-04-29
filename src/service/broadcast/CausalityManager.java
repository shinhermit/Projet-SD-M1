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
        
        _isOn = false;
    }

    public void setProcessId(ProcessIdentifier myId)
    {
        _myId = myId;
    }

    public StampedMessage fetchMessage()
    {
        Message mess = _reliableBuffer.removeElement(true);

        if(! (mess instanceof StampedMessage) )
        {
            throw new IllegalStateException("CausalityManager.fetchMessage: messages in causal mode should be of type StampedMessage.\n\t found "+mess.getClass().getName());
        }

        return (StampedMessage)mess;
    }
    
    public DelayedMessage checkCausality(StampedMessage stampMess)
    {
        boolean first = true;
        DelayedMessage delayedMess = null;
        
        LogicalClock messClock = stampMess.getStamp();
        
        if(_localClock.size() != messClock.size())
        {
            throw new IllegalStateException("CausalityManager.checkCausality: the stamps don't have the same size (number of processes).\n\tSome processes may have crash.");
        }
        
        for(ProcessIdentifier processId : messClock.getAllProcessId())
        {
            if(processId.getId() != _myId.getId())
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
                //Add id back to reliableBuffer, so it will be processed by fetchMessage again
                _reliableBuffer.addElement(delayedMess.getStampedMessage());
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
        
        _isOn = true;
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
                _causalBuffer.addElement(stampMess.toMessage());
                this.updateWaitingList(stampMess.getProcessId());
            }
        }
    }
}
