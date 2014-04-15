/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.SynchronizedBuffer;
import message.TotalAtomicMessage;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicManager extends Thread{
    private SynchronizedBuffer<TotalAtomicMessage> _tokenRequestBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    private ReliableBroadcastService _reliableService;
    
    public TotalAtomicManager (SynchronizedBuffer<TotalAtomicMessage> token,
            SynchronizedBuffer<TotalAtomicMessage> ack,
            SynchronizedBuffer<TotalAtomicMessage> reqToken,
            ReliableBroadcastService serv) {
        _tokenRequestBuffer = reqToken;
        _ackBuffer = ack;
        _tokenBuffer = token;
        _reliableService = serv;
    }
    
    @Override
    public void run() {
        
    }
}
