/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.CommunicationException;
import communication.ProcessIdentifier;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import message.Message;
import message.MessageType;
import message.TotalAtomicMessage;
import message.TotalAtomicType;
import service.IBroadcast;
import service.ICommunication;
import service.IIdentification;
import service.MessageDispatcher;
import service.Service;

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
    private HashMap<ProcessIdentifier, Integer> _token;
    private HashMap<ProcessIdentifier, Integer> _requests;
    private boolean _getToken;
    
    public TotalAtomicBroadcastService () {
        _tokenBuffer = new SynchronizedBuffer();
        _tokenRequestBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _reliableService = new ReliableBroadcastService();
        _getToken = false;
    }
    
    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType) {
        super.initialize(dispatcher, commElt, myType);
        _reliableService.initialize(dispatcher, commElt, myType);
        
    }
    
    @Override 
    public void setIdentificationService(IIdentification idService) {
        _idService = idService;
        _reliableService.setIdentificationService(idService);
    }
    
    @Override
    public void broadcast(Object data) throws CommunicationException{
        //On s'assure que l'on a le token
        if(!_getToken)
        {
            //Si on ne l'as pas, on le demande et on l'attend.
            _reliableService.broadcast(
                    new TotalAtomicMessage(_idService.getMyIdentifier(),
                                           null,
                                           _idService.getMyIdentifier(),
                                           TotalAtomicType.TOKEN_REQUEST));
            TotalAtomicMessage message;
            while(!_getToken) {
                message = _tokenBuffer.removeElement(true);
                if(message.getProcessIdReceiver().equals(_idService.getMyIdentifier())) {
                    _token = (HashMap<ProcessIdentifier, Integer>) message.getData();
                    _getToken = true;
                }
            }
        }
        
        //On entre en SC: on envoie le message et on attend les accusés.
        _reliableService.broadcast(
                new TotalAtomicMessage(_idService.getMyIdentifier(), null, data,
                                       TotalAtomicType.PAYLOAD));
        //On attend tous les accusés.
        int ack = 0;
        int nbSent = _idService.getAllIdentifiers().size();
        TotalAtomicMessage message;
        HashSet<ProcessIdentifier> acknoledged = new HashSet();
        while(ack < nbSent) {
            message = _ackBuffer.removeElement(true);
            if(!acknoledged.contains(message.getProcessIdSender())) {
                ack++;
                acknoledged.add(message.getProcessIdSender());
            }
        }
        
        boolean sent = false;
        //On regarde si des gens attendent le token.
        for(ProcessIdentifier id : _requests.keySet()) {
            if(_requests.get(id) > _token.get(id) && !sent) {
                //Quelqu'un attend le token, on met à jour notre case et on 
                //l'envoie.
                ProcessIdentifier myId = _idService.getMyIdentifier();
                _token.put(myId, _token.get(myId) + 1);
                _reliableService.broadcast(
                        new TotalAtomicMessage(_idService.getMyIdentifier(),
                                id, _token, TotalAtomicType.TOKEN));
                sent = true;
            }
        }
        //Sinon on garde le token. Que faire???? TODO
        
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
