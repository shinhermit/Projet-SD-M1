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
import service.id.IdentificationService;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicBroadcastService{
    //Internals buffers
    //===============================
    private SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    //Output buffer
    //==============================
    private SynchronizedBuffer<TotalAtomicMessage> _outputBuffer;
    //Input Buffer: not clean, for manager thread use only!!!!!
    //==============================
    private SynchronizedBuffer<TotalAtomicMessage> _inputBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    
    
    //Internal stuff
    //=============================
    private ReliableBroadcastService _reliableService;
    private IIdentification _idService;
    private boolean _getToken;
    private boolean _wantToSendStuff;
    private HashMap<ProcessIdentifier, Integer> _requests;
    private HashMap<ProcessIdentifier, Integer> _token;
    //Private thread who read input buffer
    //=======================================
    private TotalAtomicManager _totalAtomicManager;
    
    public TotalAtomicBroadcastService () {
        _ackBuffer = new SynchronizedBuffer();
        _getToken = false;
        _wantToSendStuff = false;
    }
    
    public void initialize(IdentificationService idServ, ReliableBroadcastService serv, SynchronizedBuffer<TotalAtomicMessage> input,
            SynchronizedBuffer<TotalAtomicMessage> output, IIdentification idservice) {
        _idService = idservice;
        _reliableService = serv;
        _inputBuffer = input;
        _outputBuffer = output;
        _totalAtomicManager = new TotalAtomicManager(_ackBuffer, _inputBuffer,  
                _tokenBuffer, _token, _getToken, _wantToSendStuff, serv, idServ);
        _totalAtomicManager.start();
    }
    
    public void broadcast(Object data) throws CommunicationException{
        //On s'assure que l'on a le token
        _wantToSendStuff = true;
        HashMap<ProcessIdentifier, Integer> token;
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
                    token = (HashMap<ProcessIdentifier, Integer>) message.getData();
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
        
        //On regarde si des gens attendent le token.
        for(ProcessIdentifier id : _requests.keySet()) {
            if(_requests.get(id) > token.get(id) && _getToken) {
                //Quelqu'un attend le token, on met à jour notre case et on 
                //l'envoie.
                ProcessIdentifier myId = _idService.getMyIdentifier();
                token.put(myId, token.get(myId) + 1);
                _reliableService.broadcast(
                        new TotalAtomicMessage(_idService.getMyIdentifier(),
                                id, token, TotalAtomicType.TOKEN));
                _getToken = false;
            }
        }
        _wantToSendStuff = false;
        //Sinon on garde le token. Que faire???? TODO
        
    }
    
    public Message synchDeliver()
    {
        return _outputBuffer.removeElement(true);
    }

    public Message asynchDeliver()
    {
        return _outputBuffer.removeElement(false);
    }

    public boolean availableMessage()
    {
        return _outputBuffer.available() > 0;
    }
}
