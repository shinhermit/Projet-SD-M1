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
    private final  SynchronizedBuffer<Message> _outputBuffer;
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
    private boolean _isOn;
    //Private thread who read input buffer
    //=======================================
    private final TotalAtomicManager _totalAtomicManager;

    public TotalAtomicBroadcastService(ReliableBroadcastService serv) {
        _reliableService = serv;
        _inputBuffer = serv.getTotalBuffer();
        _idService = serv.getIdService();
        _outputBuffer = new SynchronizedBuffer();
        _ackBuffer = new SynchronizedBuffer();
        _tokenBuffer = new SynchronizedBuffer();
        _requests = new HashMap();
        _getToken = false;
        _usingToken = false;
        _totalAtomicManager = new TotalAtomicManager(_ackBuffer, _inputBuffer, _outputBuffer,
                _tokenBuffer, _requests, _token, _isOn, _getToken, _usingToken, _reliableService);
    }

    @Override
    public void initialize(MessageDispatcher mess, ICommunication com, MessageType t) {
    }

    @Override
    public void broadcast(Object data) throws CommunicationException {
        //On s'assure que l'on a le token
        _usingToken = true;
        if (!_getToken) {
            //Si on ne l'as pas, on le demande et on l'attend.
            System.out.println("TOKEN: on n'a pas le token, on envoie une requête.");
            _reliableService.broadcast(
                    new TotalAtomicMessage(_idService.getMyIdentifier(), _idService.getMyIdentifier(), "",
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
                new TotalAtomicMessage(_idService.getMyIdentifier(), _idService.getMyIdentifier(), data,
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
                System.out.println("TOKEN: on passe le token à " + id);
                ProcessIdentifier myId = _idService.getMyIdentifier();
                _token.put(myId, _token.get(myId) + 1);
                _reliableService.broadcast(
                        new TotalAtomicMessage(_idService.getMyIdentifier(),
                                id, _token, TotalAtomicType.TOKEN));
                _getToken = false;
            }
        }
        _usingToken = false;
    }
    
    @Override
    public void setIdentificationService(IIdentification idService)
    {
        _idService = idService;
        _totalAtomicManager.setIdentification(idService);
        
        //Si on est le seul service à tourner, on doit créer le token.
        while(_idService.getAllIdentifiers() == null) {}
        if(_idService.getAllIdentifiers().isEmpty()) {
            System.out.println("TOKEN: on est le seul service à" +
                    "tourner: on crée le token.");
            _token = new HashMap();
            _getToken = true;
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
        _isOn = false;
    }
}
