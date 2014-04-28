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
import message.Message;
import message.TotalAtomicMessage;
import message.TotalAtomicType;
import service.id.IdentificationService;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicManager extends Thread{
    private SynchronizedBuffer<TotalAtomicMessage> _tokenRequestBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _inputBuffer;
    private SynchronizedBuffer<TotalAtomicMessage> _outputBuffer;
    private HashMap<ProcessIdentifier, Integer> _request;
    private HashMap<ProcessIdentifier, Integer> _token;
    private IdentificationService _idServ;
    private ReliableBroadcastService _reliableService;
    private boolean _getToken;
    private boolean _usingToken;
    
    public TotalAtomicManager (
            SynchronizedBuffer<TotalAtomicMessage> ack,
            SynchronizedBuffer<TotalAtomicMessage> input,
            SynchronizedBuffer<TotalAtomicMessage> tokenBuffer,
            HashMap<ProcessIdentifier, Integer> token,
            boolean getToken,
            boolean usingToken,
            ReliableBroadcastService serv,
            IdentificationService idServ) {
        _tokenRequestBuffer = new SynchronizedBuffer();
        _ackBuffer = ack;
        _tokenBuffer = tokenBuffer;
        _token = token;
        _reliableService = serv;
        _inputBuffer = input;
        _usingToken = usingToken;
        _idServ = idServ;
    }
    
    @Override
    public void run() {
        while(true) {
            TotalAtomicMessage message = fetchMessage();
            switch(message.getType()) {
                //Si on reçoit du payload, on envoie un acquittement et on fait passer dans le buffer sortie.
                case PAYLOAD :
                    try {
                        _reliableService.broadcast(new TotalAtomicMessage(_idServ.getMyIdentifier(),
                            message.getProcessIdSender(), "", TotalAtomicType.ACK));
                    } catch(CommunicationException c) {System.err.println("Impossible d'envoyer un message TotalAtomicManager.run " + c);}
                    _outputBuffer.addElement(message);
                    break;
                    
                //Si on reçoit le jeton, on le recopie et on réveille le service qui était en attente à l'aide du buffer de jeton.    
                case TOKEN :
                    if(message.getProcessIdReceiver().equals(_idServ.getMyIdentifier())) {
                        _token = (HashMap<ProcessIdentifier, Integer>) message.getData();
                        _getToken = true;
                        _tokenBuffer.addElement(message);
                    }
                    break;
                    
                //Si on reçoit une requête de jeton, on regarde si on a le jeton et que l'on ne s'en sert pas.
                //Si c'est le cas on l'envoie. Sinon on incrémente les demandes.
                case TOKEN_REQUEST:
                    _request.put(message.getProcessIdSender(), _request.get(message.getProcessIdSender()) + 1);
                    if(_getToken && !_usingToken) {
                        try{
                            _reliableService.broadcast(new TotalAtomicMessage(_idServ.getMyIdentifier(), message.getProcessIdSender(), _token, TotalAtomicType.TOKEN));
                        }
                        catch(CommunicationException c) {System.err.println("Impossible d'envoyer un message TotalAtomicManager.run " + c);}
                    } else {
                        _request.put(message.getProcessIdSender(), _request.get(message.getProcessIdSender()) + 1);
                    }
                    break;
                    
                //On fait passer l'acquittement au service (c'est lui qui se charge de les compter).    
                case ACK:
                    if(message.getProcessIdReceiver() == _idServ.getMyIdentifier())
                        _ackBuffer.addElement(message);
                    break;
                    
                default:
                    throw new IllegalStateException("TotalAtomicManager.run: message type unknown: " + message.getType().toString());
            }
        }
    }
    
    
    public TotalAtomicMessage fetchMessage()
    {
        Message mess = _inputBuffer.removeElement(true);
        Object data = mess.getData();
           
        if(! (data instanceof TotalAtomicMessage) )
        {
            throw new IllegalStateException("TotalAtomicManager.fetchMessage: messages in total mode should be of type TotalAtomicMessage.\n\t found "+mess.getClass().getName());
        }
           
        return (TotalAtomicMessage) data;
        }
}
