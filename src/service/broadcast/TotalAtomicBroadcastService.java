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
import service.IService;
import service.MessageDispatcher;
import service.Service;
import service.id.IdentificationService;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicBroadcastService implements IService, IBroadcast {

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
    private boolean _isOn;
    //Private thread who read input buffer
    //=======================================
    private TotalAtomicManager _totalAtomicManager;

    public TotalAtomicBroadcastService(ReliableBroadcastService serv) {
        _reliableService = serv;
        _inputBuffer = serv.getTotalBuffer();
        _outputBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _getToken = false;
        _wantToSendStuff = false;
        _totalAtomicManager = new TotalAtomicManager(_ackBuffer, _inputBuffer,
                _tokenBuffer, _token, _isOn, _getToken, _wantToSendStuff, _reliableService, _idService);
    }

    @Override
    public void initialize(MessageDispatcher mess, ICommunication com, MessageType t) {}

    public void broadcast(Object data) throws CommunicationException {
        //On s'assure que l'on a le token
        _wantToSendStuff = true;
        if (!_getToken) {
            //Si on ne l'as pas, on le demande et on l'attend.
            _reliableService.broadcast(
                    new TotalAtomicMessage(_idService.getMyIdentifier(), null, null,
                            TotalAtomicType.TOKEN_REQUEST));
            TotalAtomicMessage message;
            while (!_getToken) {
                message = _tokenBuffer.removeElement(true);
                if (message.getProcessIdReceiver().equals(_idService.getMyIdentifier())) {
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
        while (ack < nbSent) {
            message = _ackBuffer.removeElement(true);
            if (!acknoledged.contains(message.getProcessIdSender())) {
                ack++;
                acknoledged.add(message.getProcessIdSender());
            }
        }

        //On regarde si des gens attendent le token.
        for (ProcessIdentifier id : _requests.keySet()) {
            if (_requests.get(id) > _token.get(id) && _getToken) {
                //Quelqu'un attend le token, on met à jour notre case et on 
                //l'envoie.
                ProcessIdentifier myId = _idService.getMyIdentifier();
                _token.put(myId, _token.get(myId) + 1);
                _reliableService.broadcast(
                        new TotalAtomicMessage(_idService.getMyIdentifier(),
                                id, _token, TotalAtomicType.TOKEN));
                _getToken = false;
            }
        }
        _wantToSendStuff = false;
    }
    
    @Override
    public void setIdentificationService(IIdentification idService)
    {
        _idService = idService;
    }
    public Message synchDeliver() {
        return _outputBuffer.removeElement(true);
    }

    public Message asynchDeliver() {
        return _outputBuffer.removeElement(false);
    }

    public boolean availableMessage() {
        return _outputBuffer.available() > 0;
    }

    @Override
    public void startManagers() {
        _totalAtomicManager.start();
    }

    @Override
    public void terminateManagers() {
        _isOn = false;
    }
}
