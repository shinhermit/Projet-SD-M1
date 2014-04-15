/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.CommunicationException;
import communication.SynchronizedBuffer;
import message.Message;
import message.MessageType;
import message.TotalAtomicMessage;
import service.IBroadcast;
import service.ICommunication;
import service.IIdentification;
import service.MessageDispatcher;
import service.Service;
import service.id.IdentificationService;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicBroadcastService extends Service implements IBroadcast {
    private SynchronizedBuffer<TotalAtomicMessage> _tokenRequestBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    private ReliableBroadcastService _reliableService;
    private IIdentification _idService;
    
    public TotalAtomicBroadcastService () {
        _tokenBuffer = new SynchronizedBuffer();
        _tokenRequestBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _reliableService = new ReliableBroadcastService();
    }
    
    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType) {
        super.initialize(dispatcher, commElt, myType);
        _reliableService.initialize(dispatcher, commElt, myType);
        
    }
    
    @Override 
    public void setIdentificationService(IIdentification idService) {
        this._idService = idService;
        this._reliableService.setIdentificationService(idService);
    }
    
    @Override
    public void broadcast(Object data) throws CommunicationException{
        
    }
    
        @Override
    public Message synchDeliver()
    {
        return _reliableService.synchDeliver();
    }

    @Override
    public Message asynchDeliver()
    {
        return _reliableService.asynchDeliver();
    }

    @Override
    public boolean availableMessage()
    {
        return _reliableService.availableMessage();
    }
}
