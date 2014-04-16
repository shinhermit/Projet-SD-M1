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
import service.IIdentification;
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
    }

    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {
        super.initialize(dispatcher, commElt, myType);
        
        _causalityManager = new CausalityManager(_localClock, serviceBuffer, _causalBuffer);
        _reliabilityManager.setBuffers(_causalBuffer, _reliableBuffer);
    }
    
    @Override
    public void startManagers()
    {
        super.startManagers();

        _causalityManager.start();
    }
    
    @Override
    public void terminateManagers()
    {
        super.terminateManagers();

        _causalityManager.quit();
    }

    @Override
    public void setIdentificationService(IIdentification idService)
    {
        super.setIdentificationService(idService);
        
        _causalityManager.setProcessId(idService.getMyIdentifier());

        // As above preivous call of a method on idService worked, it means
        // idService is not null, but getAllIdentifiers is not working
        for(ProcessIdentifier processId: idService.getAllIdentifiers())
        {
            _localClock.addProcess(processId);
        }
    }

    @Override
    public void broadcast(Object data) throws CommunicationException
    {
        //Encapsulate SeqMessage into a StampedMessage
        SeqMessage seqMess = new SeqMessage(this.idService.getMyIdentifier(), data, MessageType.RELIABLE_BROADCAST);
        StampedMessage stampMess = new StampedMessage(seqMess.getProcessId(), seqMess, _localClock);
        
        //Encapsulate StampedMessage into a TypedMessage
        TypedMessage mess = new TypedMessage(this.idService.getMyIdentifier(), stampMess, MessageType.RELIABLE_BROADCAST);

        synchronized(_history)
        {
            _history.add(seqMess);
        }

        _basicBroadcaster.broadcast(mess);
    }
}
