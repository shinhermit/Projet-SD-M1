/* Coyright Eric Cariou, 2009 - 2011 */

package service.broadcast;

import communication.CommunicationException;
import communication.CompoundException;
import message.Message;
import communication.ProcessIdentifier;
import java.util.Iterator;
import service.IIdentification;
import service.Service;
import message.TypedMessage;
import service.IBroadcast;

/**
 * Implementation of a basic broadcast algorithm
 */
public class BasicBroadcastService extends Service implements IBroadcast
{

    protected IIdentification idService;

    @Override
    public void setIdentificationService(IIdentification idService)
    {
        this.idService = idService;
    }
    
    @Override
    public void broadcast(Object data)  throws CommunicationException
    {
        ProcessIdentifier id;
        Iterator it;

        CompoundException exceptions = null;
        CommunicationException firstException = null;

        // send the data to all the processes
        it = idService.getAllIdentifiers().iterator();
        while (it.hasNext())
        {
            id = (ProcessIdentifier) it.next();
            try
            {
                // simulate the crash of the process during the broadcast
                commElt.crashProcess();
                
                commElt.sendMessage(new TypedMessage(id, data, this.myType));
            }
            catch (CommunicationException e)
            {
                if (firstException == null) firstException = e;
                else
                {
                    if (exceptions == null)
                    {
                        exceptions = new CompoundException();
                        exceptions.addException(firstException);
                    }
                    exceptions.addException(e);
                }
            }
        }

        if (exceptions != null) throw exceptions;
        if (firstException != null) throw firstException;
    }

    @Override
    public Message synchDeliver()
    {
        return serviceBuffer.removeElement(true);
    }

    @Override
    public Message asynchDeliver()
    {
        return serviceBuffer.removeElement(false);
    }

    @Override
    public boolean availableMessage()
    {
        return serviceBuffer.available() > 0;
    }
}
