/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.SynchronizedBuffer;
import message.LogicalClock;
import message.Message;
import message.StampedMessage;

/**
 *
 * @author josuah
 */
public class CausalityManager extends Thread
{
    protected SynchronizedBuffer<Message> _serviceBuffer;
    protected SynchronizedBuffer<StampedMessage> _causalBuffer;
    protected LogicalClock _localClock;
    
    public CausalityManager()
    {
        
    }

    public StampedMessage fetchMessage()
    {
        Message mess = _serviceBuffer.removeElement(true);
        Object data = mess.getData();

        if(! (data instanceof StampedMessage) )
        {
            throw new IllegalStateException("CausalityManager.fetchMessage: messages in reliable mode should be of type StampedMessage.\n\t found "+mess.getClass().getName());
        }

        return (StampedMessage)data;
    }
    
    public boolean checkCausality(StampedMessage stampMess)
    {
        boolean ontime = true;
        
        LogicalClock messClock = stampMess.getStamp();
        
        return ontime;
    }
    
    @Override
    public void run()
    {
        boolean ontime;
        
        while(true)
        {
            StampedMessage stampMess = fetchMessage();
            
            _localClock.newEvent(stampMess.getProcessId());
            
            ontime = false;
        }
    }
}
