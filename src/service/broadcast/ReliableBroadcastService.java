/* Coyright Eric Cariou, 2009 - 2011 */

package service.broadcast;

import communication.CommunicationException;
import communication.Message;
import communication.SynchronizedBuffer;
import service.IBroadcast;
import service.ICommunication;
import service.IIdentification;
import service.MessageDispatcher;
import service.MessageType;
import service.SeqMessage;
import service.Service;

public class ReliableBroadcastService  extends Service implements IBroadcast
{

    protected IIdentification idService;
    protected BasicBroadcastService _basicBroadcaster;
    protected ReliabilityManager _reliabilityManager;

    /**
     * Buffer containing the filtered received messages.
     */
    protected SynchronizedBuffer<Message> _reliableBuffer;

    
    public ReliableBroadcastService()
    {
        _basicBroadcaster = new BasicBroadcastService();
        _reliableBuffer = new SynchronizedBuffer();
    }

    /**
     * Initialize the service: register the service on the message dispatcher
     * @param dispatcher the message dispatcher to use for associating the current service with its type of message
     * @param commElt the communication element to use to send messages to service parts on other processes
     * @param myType the type of the service
     */
    @Override
    public void initialize(MessageDispatcher dispatcher, ICommunication commElt, MessageType myType)
    {
        super.initialize(dispatcher, commElt, myType);
        _basicBroadcaster.initialize(dispatcher, commElt, myType);
        
        serviceBuffer = dispatcher.associateService(myType);
        
        _reliabilityManager = new ReliabilityManager(_basicBroadcaster, serviceBuffer, _reliableBuffer);
        _reliabilityManager.start();
    }

    @Override
    public void setIdentificationService(IIdentification idService)
    {
        this.idService = idService;
        this._basicBroadcaster.setIdentificationService(idService);
    }

    @Override
    public void broadcast(Object data) throws CommunicationException
    {
        _basicBroadcaster.broadcast(new SeqMessage(null, data, MessageType.RELIABLE_BROADCAST));
    }

    @Override
    public Message synchDeliver()
    {
        return _reliableBuffer.removeElement(true);
    }

    @Override
    public Message asynchDeliver()
    {
        return _reliableBuffer.removeElement(false);
    }

    @Override
    public boolean availableMessage()
    {
        return _reliableBuffer.available() > 0;
    }
}
