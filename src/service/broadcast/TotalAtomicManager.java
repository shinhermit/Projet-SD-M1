/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.SynchronizedBuffer;
import message.Message;
import message.TotalAtomicMessage;
import message.TotalAtomicType;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicManager extends Thread{
    private SynchronizedBuffer<TotalAtomicMessage> _tokenRequestBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    private ReliableBroadcastService _reliableService;
    private SynchronizedBuffer<Message> _serviceBuffer;
    
    public TotalAtomicManager (SynchronizedBuffer<TotalAtomicMessage> token,
            SynchronizedBuffer<TotalAtomicMessage> ack,
            SynchronizedBuffer<TotalAtomicMessage> reqToken,
            SynchronizedBuffer<Message> serviceBuffer,
            ReliableBroadcastService serv) {
        _tokenRequestBuffer = reqToken;
        _ackBuffer = ack;
        _tokenBuffer = token;
        _reliableService = serv;
    }
    
    @Override
    public void run() {
        while(true) {
            TotalAtomicMessage message = fetchMessage();
            switch(message.getType()) {
                case PAYLOAD :
                    _reliableService.
            }
        }
    }
    
    
    public TotalAtomicMessage fetchMessage()
    {
        Message mess = _serviceBuffer.removeElement(true);
        Object data = mess.getData();
           
        if(! (data instanceof TotalAtomicMessage) )
        {
            throw new IllegalStateException("TotalAtomicManager.fetchMessage: messages in total mode should be of type TotalAtomicMessage.\n\t found "+mess.getClass().getName());
        }
           
        return (TotalAtomicMessage) data;
        }
}
