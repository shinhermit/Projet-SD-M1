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
import message.StampedMessage;
import service.IBroadcast;
import service.ICommunication;
import service.IIdentification;
import service.MessageDispatcher;
import service.Service;

/**
 *
 * @author josuah
 */
public class CausalReliableBroadcastService extends Service implements IBroadcast
{
    protected ReliableBroadcastService _reliableBroadcaster;
    protected SynchronizedBuffer<Message> _inputBuffer;
    protected IIdentification _idService;
    
    protected CausalityManager _causalityManager;
    protected SynchronizedBuffer<Message> _causalBuffer;
    protected LogicalClock _localClock;

    public CausalReliableBroadcastService(ReliableBroadcastService reliableBroadcaster)
    {
        _reliableBroadcaster = reliableBroadcaster;
        _inputBuffer = reliableBroadcaster.getCausalBuffer();
        
        _localClock = new LogicalClock();
        _causalBuffer = new SynchronizedBuffer();
        _causalityManager = new CausalityManager(_localClock, _inputBuffer, _causalBuffer);
    }

    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {}
    
    @Override
    public void startManagers()
    {
        _causalityManager.start();
    }
    
    @Override
    public void terminateManagers()
    {
        _causalityManager.quit();
    }
    
    @Override
    public void setIdentificationService(IIdentification idService)
    {
        _idService = idService;
        _causalityManager.setIdentification(_idService.getMyIdentifier(), idService);
        
        /* ****** Initialize clock ****** */
        // as we are not directly informed when the process id has been received, wait a short time
        // to be almost sure to have received it when printing the identifier
        try { Thread.sleep(200); } catch(Exception e) { }
        
        for(ProcessIdentifier processId: _idService.getAllIdentifiers())
        {
            _localClock.addProcess(processId);
        }
        
        // Own id is not in _idService.getAllIdentifiers()...
        _localClock.addProcess(_idService.getMyIdentifier());
    }
    
    @Override
   public Message synchDeliver()
   {
        return _causalBuffer.removeElement(true);
   }

    @Override
    public Message asynchDeliver()
    {
        return _causalBuffer.removeElement(false);
    }

    @Override
    public void broadcast(Object data) throws CommunicationException
    {
        StampedMessage stampMess = new StampedMessage(_idService.getMyIdentifier(), data, _localClock);
        
        _reliableBroadcaster.broadcast(stampMess);
    }

    @Override
    public boolean availableMessage()
    {
        return _causalBuffer.available() > 0;
    }
}
