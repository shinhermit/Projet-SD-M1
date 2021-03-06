/* Coyright Eric Cariou, 2009 - 2011 */

package service.broadcast;

import communication.CommunicationException;
import communication.SynchronizedBuffer;
import java.util.ArrayList;
import message.Message;
import message.MessageType;
import message.SeqMessage;
import message.TotalAtomicMessage;
import service.IBroadcast;
import service.ICommunication;
import service.IIdentification;
import service.MessageDispatcher;
import service.Service;

public class ReliableBroadcastService  extends Service implements IBroadcast
{

    protected IIdentification idService;
    protected BasicBroadcastService _basicBroadcaster;
    protected ReliabilityManager _reliabilityManager;
    protected ArrayList<SeqMessage> _history;
    public final Object historyLock = new Object();

    /**
     * Buffer containing the filtered received messages.
     */
    protected SynchronizedBuffer<Message> _reliableBuffer;
    protected SynchronizedBuffer<Message> _causalBuffer;
    protected SynchronizedBuffer<TotalAtomicMessage> _totalBuffer;

    
    public ReliableBroadcastService()
    {
        _basicBroadcaster = new BasicBroadcastService();
        _reliabilityManager = new ReliabilityManager(historyLock);
        
        _reliableBuffer = new SynchronizedBuffer();
        _causalBuffer = new SynchronizedBuffer();
        _totalBuffer = new SynchronizedBuffer();
        _history = new ArrayList();
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
        //_basicBroadcaster.initialize(dispatcher, commElt, myType);
        _basicBroadcaster.set(dispatcher, commElt, myType);
        
        _reliabilityManager.initialize(_basicBroadcaster, this.serviceBuffer,
                _reliableBuffer, _causalBuffer, _totalBuffer, _history);
    }
    
    @Override
    public void startManagers()
    {
        _reliabilityManager.start();
    }
    
    @Override
    public void terminateManagers()
    {
        _reliabilityManager.quit();
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
        SeqMessage seqMess = new SeqMessage(this.idService.getMyIdentifier(), data, this.myType);

        synchronized(historyLock)
        {
            _history.add(seqMess);
        }

        _basicBroadcaster.broadcast(seqMess);
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
    
    public SynchronizedBuffer<Message> getReliableBuffer()
    {
        return _reliableBuffer;
    }
    
    public SynchronizedBuffer<Message> getCausalBuffer()
    {
        return _causalBuffer;
    }
    
    public SynchronizedBuffer<TotalAtomicMessage> getTotalBuffer()
    {
        return _totalBuffer;
    }
    
    public IIdentification getIdService()
    {
        return this.idService;
    }
}
