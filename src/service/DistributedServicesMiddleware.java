/* Coyright Eric Cariou, 2009 - 2011 */

package service;

import communication.CommunicationElement;
import communication.CommunicationException;
import communication.ReliabilitySetting;
import communication.ReliableCommElt;
import communication.UnreliableCommElt;
import java.util.HashMap;
import message.MessageType;
import message.TypedMessage;
import service.broadcast.BasicBroadcastService;
import service.broadcast.CausalReliableBroadcastService;
import service.broadcast.ReliableBroadcastService;
import service.broadcast.TotalAtomicBroadcastService;
import service.id.IdentificationService;

/** 
 * Access point to the middleware and associated services.
 */
public class DistributedServicesMiddleware implements IDistributedServices
{

    /**
     * The underlying communication element for communicating with other processes.
     */
    protected CommunicationElement commElt = null;
    
    /**
     * Availaible services
     */
    protected HashMap<IDistributedServices.ServiceSet, IService> _services;

    /**
     * Message dispatcher for dispatching received message to the associated service
     */
    protected MessageDispatcher dispatcher;
    
    private void _config(ReliabilitySetting setting, int localPort) throws CommunicationException
    {
        if (setting.isReliable())
        {
            commElt = (localPort > 0) ? new ReliableCommElt(localPort) : new ReliableCommElt();
        } else {
            commElt = (localPort > 0) ? new UnreliableCommElt(localPort) : new UnreliableCommElt();
        }

        commElt.setPacketLostLevel(setting.getPacketLostLevel());
        commElt.setTransmissionDelayLowerBound(setting.getTransmissionDelayLowerBound());
        commElt.setTransmissionDelayUpperBound(setting.getTransmissionDelayUpperBound());
        commElt.setDebugFault(setting.isDebugFault());
        commElt.setCrashLevel(setting.getCrashLevel());
        
    }
    
    @Override
    public void config(ReliabilitySetting setting, int localPort) throws CommunicationException
    {
        _config(setting, localPort);
    }
    
    @Override
    public void config(ReliabilitySetting setting) throws CommunicationException
    {
        _config(setting, -1);
    }

    @Override
    public void connect() throws CommunicationException
    {
        if(commElt == null)
        {
            commElt = new ReliableCommElt();
        }

        initServices();
    }

    @Override
    public void connect(ReliabilitySetting setting) throws CommunicationException
    {
        this.config(setting);

        initServices();
    }

    @Override
    public void connect(ReliabilitySetting setting, int localPort) throws CommunicationException
    {
        this.config(setting, localPort);

        initServices();
    }

    @Override
    public void disconnect()
    {
        ((IIdentification)_services.get(ServiceSet.Identification)).leaveSystem();
    }

    @Override
    public IService getService(IDistributedServices.ServiceSet serviceType)
    {
        return _services.get(serviceType);
    }

    /**
     * Initialize the services.
     */
    protected void initServices()
    {
        IService service, broadcaster;
        IIdentification idService;
        
        (new MessageReader(commElt, dispatcher)).start();

        service = _services.get(ServiceSet.Identification);
        service.initialize(dispatcher, commElt, MessageType.IDENTIFICATION);
        idService = (IIdentification)service;

        _services.get(ServiceSet.Communication).initialize(dispatcher, commElt, MessageType.NONE);

        broadcaster = _services.get(ServiceSet.BasicBroadcast);
        broadcaster.initialize(dispatcher, commElt, MessageType.BASIC_BROADCAST);
        ((IBroadcast)broadcaster).setIdentificationService(idService);

        broadcaster = _services.get(ServiceSet.ReliableBroadcast);
        broadcaster.initialize(dispatcher, commElt, MessageType.RELIABLE_BROADCAST);
        ((IBroadcast)broadcaster).setIdentificationService(idService);

        broadcaster = _services.get(ServiceSet.CausalReliableBroadcast);
        broadcaster.initialize(dispatcher, commElt, MessageType.CAUSAL_RELIABLE_BROADCAST);
        ((IBroadcast)broadcaster).setIdentificationService(idService);
        
        broadcaster = _services.get(ServiceSet.TotalAtomic);
        broadcaster.initialize(dispatcher, commElt, MessageType.TOTAL_ATOMIC_BROADCAST);
        ((IBroadcast)broadcaster).setIdentificationService(idService);
    }

    public DistributedServicesMiddleware()
    {
        dispatcher = new MessageDispatcher();
        
        _services = new HashMap();
        
        _services.put(ServiceSet.Identification, new IdentificationService());
        _services.put(ServiceSet.Communication, new ProxyCommunication());
        _services.put(ServiceSet.BasicBroadcast, new BasicBroadcastService());
        _services.put(ServiceSet.ReliableBroadcast, new ReliableBroadcastService());
        _services.put(ServiceSet.CausalReliableBroadcast, new CausalReliableBroadcastService());
        _services.put(ServiceSet.TotalAtomic, new TotalAtomicBroadcastService());
    }

    /**
     * Inner class that manage all received messages and dispatch them to
     * the required services thanks to a message dispatcher
     */
    protected class MessageReader extends Thread
    {

        /**
         * Underlying communication element
         */
        ICommunication commElt;
        /**
         * The message dispatcher
         */
        MessageDispatcher dispatcher;

        @Override
        public void run()
        {
            TypedMessage msg;
            // when a message is received, it is managed by the message dispatcher
            while (true)
            {
                msg = (TypedMessage) commElt.synchReceiveMessage();
                dispatcher.newEvent(msg);
            }
        }

        /**
         *
         * @param commElt
         * @param dispatcher
         */
        public MessageReader(ICommunication commElt, MessageDispatcher dispatcher)
        {
            this.commElt = commElt;
            this.dispatcher = dispatcher;
        }
    }
}
