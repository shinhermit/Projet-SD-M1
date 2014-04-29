/* Coyright Eric Cariou, 2009 - 2011 */

import communication.CommunicationException;
import communication.FaultLevel;
import message.Message;
import communication.ReliabilitySetting;

import service.ICommunication;
import service.DistributedServicesMiddleware;
import service.IDistributedServices;
import service.IIdentification;
import service.IBroadcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Chat extends Thread {

    IDistributedServices services;
    ICommunication commService;
    IIdentification idService;
    IBroadcast broadcastService;

    public void init() throws CommunicationException
    {

        // setting of the simulated system
        ReliabilitySetting setting = new ReliabilitySetting();
        setting.setTransmissionDelayLowerBound(FaultLevel.NONE);
        setting.setTransmissionDelayUpperBound(FaultLevel.NONE);
        setting.setPacketLostLevel(FaultLevel.NONE);
        setting.setCrashLevel(FaultLevel.NONE);
        setting.setReliable(false);
        setting.setDebugFault(true);

        // connection to the system
        services = new DistributedServicesMiddleware();
        try
        {
            services.config(setting);
            services.connect();
        }
        
        catch (CommunicationException e)
        {
            System.err.println("Chat.init: impossible de se connecter. " + e);
            throw e;
        }

        // get the service access points
        commService = (ICommunication)services.getService(IDistributedServices.ServiceSet.Communication);
        idService = (IIdentification)services.getService(IDistributedServices.ServiceSet.Identification);
        broadcastService = (IBroadcast)services.getService(IDistributedServices.ServiceSet.ReliableBroadcast);

        // as we are not directly informed when the process id has been received, wait a short time
        // to be almost sure to have received it when printing the identifier
        try { Thread.sleep(200); } catch(Exception e) { }
        System.out.println("Chat.init: OK, connexion réalisée, je suis : " + idService.getMyIdentifier()+ "\n");
    }

    public void papoter() {
     
        String message = null;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            // read the user entry
            System.out.println("Votre message ('end' pour finir) :");
            try {
                message = input.readLine();
            } catch (IOException ex) {
                System.err.println("Erreur pendant la lecture clavier : " + ex);
                System.exit(2);
            }

            // if end, disconnect from the system and exit the JVM
            if (message.equals("end")) {
                System.out.println("A la prochaine !");
                services.disconnect();
                System.exit(0);
            }

            // brodcast the message
            try {
                System.out.print(" --> Envoi message ... ");
                broadcastService.broadcast(message);
                System.out.println("done");
            } catch (CommunicationException ex) {
                System.err.println(" *** communication problem: " + ex);
            }
        }
    }

    @Override
    public void run() {
        // wait in an infinite loop for a message to be received
        Message msg;
        while (true) {
            msg = broadcastService.synchDeliver();
            System.out.println("[" + msg.getProcessId().getId() + "] " + msg.getData());
        }
    }

    public void Chat() {
    }

    public static void main(String argv[])
    {
        boolean yep = true;
        
        Chat chat = new Chat();

        try
        {
            chat.init();
        }
        
        catch(CommunicationException e)
        {
            System.err.println("Chat.Chat: CommunicationException. " + e);
            yep = false;
        }
        
        if(yep)
        {
            chat.start();
            chat.papoter();
        }
    }
}
