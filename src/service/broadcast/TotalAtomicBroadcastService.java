/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.broadcast;

import communication.CommunicationException;
import communication.ProcessIdentifier;
import communication.SynchronizedBuffer;
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

    //Internals buffers
    //===============================
    private final SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    //Output buffer
    //==============================
    private final SynchronizedBuffer<Message> _outputBuffer;
    //Input Buffer: not clean, for manager thread use only!!!!!
    //==============================
    private final SynchronizedBuffer<TotalAtomicMessage> _inputBuffer;
    private final SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;

    //Internal stuff
    //=============================
    private final ReliableBroadcastService _reliableService;
    private IIdentification _idService;
    private boolean _getToken;
    private boolean _usingToken;
    private final HashMap<ProcessIdentifier, Integer> _requests;
    private HashMap<ProcessIdentifier, Integer> _token;
    //Private thread who read input buffer
    //=======================================
    private final TotalAtomicManager _totalAtomicManager;

    public TotalAtomicBroadcastService(ReliableBroadcastService serv) {
        _reliableService = serv;
        _inputBuffer = serv.getTotalBuffer();
        _outputBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _tokenBuffer = new SynchronizedBuffer();
        _requests = new HashMap();
        _getToken = false;
        _usingToken = false;
        _totalAtomicManager = new TotalAtomicManager(_ackBuffer, _inputBuffer, _outputBuffer,
                _tokenBuffer, _requests, 
                _reliableService, this);
    }

    @Override
    public void initialize(MessageDispatcher mess, ICommunication com, MessageType t) {
    }

    @Override
    public void broadcast(Object data) throws CommunicationException {
        //On s'assure que l'on a le token
        _usingToken = true;
        _totalAtomicManager.setUsageToken(true);
        if (!_getToken) {
            //Si on ne l'as pas, on le demande et on l'attend.
            System.out.println("TOKEN: on n'a pas le token, on envoie une requête.");
            _reliableService.broadcast(
                    new TotalAtomicMessage(_idService.getMyIdentifier(), _idService.getMyIdentifier(), "TOKEN_REQUEST",
                            TotalAtomicType.TOKEN_REQUEST));
            TotalAtomicMessage message;
            while (!_getToken) {
                message = _tokenBuffer.removeElement(true);
                if (message.getProcessIdReceiver().equals(_idService.getMyIdentifier())) {
                    _token = (HashMap<ProcessIdentifier, Integer>) message.getData();
                    _getToken = true;
                    _totalAtomicManager.setToken(true);
                }
            }
        }

        //On entre en SC: on envoie le message et on attend les accusés.
        _reliableService.broadcast(
                new TotalAtomicMessage(_idService.getMyIdentifier(), _idService.getMyIdentifier(), data,
                        TotalAtomicType.PAYLOAD));
        //On attend tous les accusés.
        int ack = 0;
        int nbSent = _idService.getAllIdentifiers().size();
        TotalAtomicMessage message;
        HashSet<ProcessIdentifier> acknoledged = new HashSet();
        System.out.println("ACK: message envoyé, en attente des ACK.");
        while (ack < nbSent) {
            message = _ackBuffer.removeElement(true);
            if (!acknoledged.contains(message.getProcessIdSender())) {
                ack++;
                acknoledged.add(message.getProcessIdSender());
            }
            System.out.println("ACK: encore " + (nbSent - ack) + " à attendre.");
        }
        System.out.println("ACK: tous les ACK ont étés reçus.");

        //On regarde si des gens attendent le token.
        for (ProcessIdentifier id : _requests.keySet()) {
            if (_token.containsKey(id)) {
                if (_requests.get(id) > _token.get(id) && _getToken) {
                    //Quelqu'un attend le token, on met à jour notre case et on 
                    //l'envoie.
                    System.out.println("TOKEN: on passe le token à " + id);
                    ProcessIdentifier myId = _idService.getMyIdentifier();
                    if (_token.containsKey(myId)) {
                        _token.put(myId, _token.get(myId) + 1);
                    } else {
                        _token.put(myId, 1);
                    }
                    _reliableService.broadcast(
                            new TotalAtomicMessage(_idService.getMyIdentifier(),
                                    id, _token, TotalAtomicType.TOKEN));
                    _getToken = false;
                    _totalAtomicManager.setToken(false);
                }
            }
        }
        _usingToken = false;
        _totalAtomicManager.setUsageToken(false);
    }

    @Override
    public void setIdentificationService(IIdentification idService) {
        _idService = idService;
        _totalAtomicManager.setIdentificationService(idService);
        if (_idService.getAllIdentifiers().isEmpty()) {
            System.out.println("TOKEN: on est le seul service à "
                    + "tourner: on crée le token.");
            _token = new HashMap();
            _getToken = true;
            _totalAtomicManager.setToken(true);
        }
    }

    @Override
    public Message synchDeliver() {
        return _outputBuffer.removeElement(true);
    }

    @Override
    public Message asynchDeliver() {
        return _outputBuffer.removeElement(false);
    }

    @Override
    public boolean availableMessage() {
        return _outputBuffer.available() > 0;
    }

    @Override
    public void startManagers() {
        _totalAtomicManager.start();
    }

    @Override
    public void terminateManagers() {
        _totalAtomicManager.terminate();
    }
    
    public void setTokenState(boolean state) {
        _getToken = state;
    }
    
    public HashMap<ProcessIdentifier, Integer> getToken () {
        return _token;
    }
    
    public void setToken(HashMap<ProcessIdentifier, Integer> token) {
        _token = token;
    }
}
