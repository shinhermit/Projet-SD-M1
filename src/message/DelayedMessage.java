/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import communication.ProcessIdentifier;

/**
 *
 * @author josuah
 */
public class DelayedMessage extends Message
{

    public DelayedMessage(ProcessIdentifier processId, Object data)
    {
        super(processId, data);
    }
    
}
