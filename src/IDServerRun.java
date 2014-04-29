
import communication.CommunicationException;
import service.id.IdentificationServer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author josuah
 */
public class IDServerRun
{
    public static void main(String argv[])
    {
        try
        {
            IdentificationServer server = new IdentificationServer();
        }
        
        catch (CommunicationException ex)
        {
            System.err.println("IdentificationServer.main: [ERROR] while launching identification server: " + ex);
        }
    }
}
