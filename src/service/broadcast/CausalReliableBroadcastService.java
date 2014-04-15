/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.CommunicationException;
import communication.ProcessIdentifier;
import communication.SynchronizedBuffer;
import message.LogicalClock;
import message.Message;
import message.MessageType;
import message.SeqMessage;
import message.StampedMessage;
import message.TypedMessage;
import service.ICommunication;
import service.MessageDispatcher;

/**
 *
 * @author josuah
 */
public class CausalReliableBroadcastService  extends ReliableBroadcastService
{
    protected CausalityManager _causalityManager;
    protected SynchronizedBuffer<Message> _causalBuffer;
    protected LogicalClock _localClock;

    public CausalReliableBroadcastService()
    {
        _causalBuffer = new SynchronizedBuffer();
        _localClock = new LogicalClock();
        
        for(ProcessIdentifier processId: idService.getAllIdentifiers())
        {
            _localClock.addProcess(processId);
        }
    }

    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {
        super.initialize(dispatcher, commElt, myType);
        
        // super call above runs the reliabilityManager of ReliableBroadcastService
        // We don't want this one (it doesn't have the good buffers)
        _reliabilityManager.quit();
        _reliabilityManager = null;
        
        _causalityManager = new CausalityManager(idService.getMyIdentifier(), _localClock, serviceBuffer, _causalBuffer);
        _reliabilityManager = new ReliabilityManager(_basicBroadcaster, _causalBuffer, _reliableBuffer, _history);
        
        _causalityManager.start();
        _reliabilityManager.start();
    }

    @Override
    public void broadcast(Object data) throws CommunicationException
    {
        SeqMessage seqMess = new SeqMessage(this.idService.getMyIdentifier(), data, MessageType.RELIABLE_BROADCAST);
        StampedMessage stampMess = new StampedMessage(seqMess.getProcessId(), data, _localClock);
        
        //Encapsulate SeqMessage into a TypedMessage
        TypedMessage mess = new TypedMessage(this.idService.getMyIdentifier(), stampMess, MessageType.RELIABLE_BROADCAST);

        synchronized(_history)
        {
            _history.add(seqMess);
        }

        _basicBroadcaster.broadcast(mess);
    }
}
