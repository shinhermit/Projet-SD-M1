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
import service.IIdentification;

/**
 *
 * @author ninjatrappeur
 */
public class TotalAtomicManager extends Thread{
    private final SynchronizedBuffer<TotalAtomicMessage> _ackBuffer;
    private final SynchronizedBuffer<TotalAtomicMessage> _tokenBuffer;
    private final SynchronizedBuffer<TotalAtomicMessage> _inputBuffer;
    private final SynchronizedBuffer<Message> _outputBuffer;
    private final HashMap<ProcessIdentifier, Integer> _request;
    private HashMap<ProcessIdentifier, Integer> _token;
    private  IIdentification _idServ;
    private final ReliableBroadcastService _reliableService;
    private final boolean _getToken;
    private final boolean _usingToken;
    private final boolean _isOn;
    
    public TotalAtomicManager (
            SynchronizedBuffer<TotalAtomicMessage> ack,
            SynchronizedBuffer<TotalAtomicMessage> input,
            SynchronizedBuffer<Message> output,
            SynchronizedBuffer<TotalAtomicMessage> tokenBuffer,
            HashMap<ProcessIdentifier, Integer> requests,
            HashMap<ProcessIdentifier, Integer> token,
            boolean isOn,
            boolean getToken,
            boolean usingToken,
            ReliableBroadcastService serv) {
        _isOn = isOn;
        _getToken = getToken;
        _ackBuffer = ack;
        _tokenBuffer = tokenBuffer;
        _request = requests;
        _token = token;
        _reliableService = serv;
        _inputBuffer = input;
        _outputBuffer = output;
        _usingToken = usingToken;
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
                    _outputBuffer.addElement(new Message(message.getProcessIdSender(), message.getData()));
                    break;
                    
                //Si on reçoit le jeton, on le recopie et on réveille le service qui était en attente à l'aide du buffer de jeton.    
                case TOKEN :
                    System.out.println("TOKEN: token reçu de "+ message.getProcessIdSender());
                    if(message.getProcessIdReceiver().equals(_idServ.getMyIdentifier())) {
                        _token = (HashMap<ProcessIdentifier, Integer>) message.getData();
                        _tokenBuffer.addElement(message);
                    }
                    break;
                    
                //Si on reçoit une requête de jeton, on regarde si on a le jeton et que l'on ne s'en sert pas.
                //Si c'est le cas on l'envoie. Sinon on incrémente les demandes.
                case TOKEN_REQUEST:
                    System.out.println("TOKEN: Token request reçu de " + message.getProcessId());
                    if(_request.containsKey(message.getProcessIdSender())) {
                        _request.put(message.getProcessIdSender(), _request.get(message.getProcessIdSender()) + 1);
                    } else {
                        _request.put(message.getProcessIdSender(), 1);
                    }
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
                    System.out.println("ACK: ACK reçu de "+ message.getProcessIdSender());
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
           
        if(! (mess instanceof TotalAtomicMessage) )
        {
            throw new IllegalStateException("TotalAtomicManager.fetchMessage: messages in total mode should be of type TotalAtomicMessage.\n\t found "+mess.getClass().getName());
        }
           
        return (TotalAtomicMessage) mess;
        }

    public void setIdentification(IIdentification idService)
    {
        _idServ = idService;
    }
}
