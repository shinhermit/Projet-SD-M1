/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.SynchronizedBuffer;
import message.Message;
import message.TotalAtomicMessage;
import service.IBroadcast;
import service.Service;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicBroadcastService extends Service implements IBroadcast {
    SynchronizedBuffer<TotalAtomicMessage> _tokenRequestBuffer;
    SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    ReliableBroadcastService _reliableService;
    
    public TotalAtomicBroadcastService () {
        _tokenBuffer = new SynchronizedBuffer();
        _tokenRequestBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _reliableService = new ReliableBroadcastService();
    }
    
}
