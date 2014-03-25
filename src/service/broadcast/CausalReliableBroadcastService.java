/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service.broadcast;

import communication.CommunicationException;
import communication.Message;
import service.IBroadcast;
import service.IIdentification;
import service.Service;

/**
 *
 * @author josuah
 */
public class CausalReliableBroadcastService  extends Service implements IBroadcast
{

    protected IIdentification idService;

    @Override
    public void setIdentificationService(IIdentification idService)
    {
        this.idService = idService;
    }

    @Override
    public void broadcast(Object data) throws CommunicationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message synchDeliver()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message asynchDeliver()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean availableMessage()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
