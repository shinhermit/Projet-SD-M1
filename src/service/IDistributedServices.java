/* Coyright Eric Cariou, 2009 - 2011 */

package service;

import communication.CommunicationException;
import communication.ReliabilitySetting;

/**
 * Definition of the global service access point.
 */
public interface IDistributedServices
{
    /**
     * Defines the available services
     */
    public enum ServiceSet {Communication, Identification, BasicBroadcast,
                            ReliableBroadcast, CausalReliableBroadcast, TotalAtomic};

    /**
     * Configures the process to the system using reliability settings and port number.
     * @param setting reliability settings to use for the current process
     * @param localPort TCP port to be used by the server socket of the communication layer
     * @throws CommunicationException in case of problem during the connection
     */
    void config(ReliabilitySetting setting, int localPort) throws CommunicationException;

    /**
     * Connect the process to the system using default value, that is, without
     * communication delays or errors.
     * @param setting reliability settings to use for the current process
     * @throws CommunicationException in case of problem during the connection
     */
    void config(ReliabilitySetting setting) throws CommunicationException;

    /**
     * Connect the process to the system.
     * Uses default values if the config method had not been called yet.
     * @throws CommunicationException in case of problem during the connection
     */
    void connect() throws CommunicationException;

    /**
     * Connect the process to the system.
     * Uses the given reliability settings, overriding any existing configuration.
     * @param setting reliability settings to use for the current process
     * @throws CommunicationException in case of problem during the connection
     */
    void connect(ReliabilitySetting setting) throws CommunicationException;

    /**
     * Connect the process to the system.
     * Uses the given reliability settings and port, overriding any existing configuration.
     * @param setting reliability settings to use for the current process
     * @param localPort TCP port to be used by the server socket of the communication layer
     * @throws CommunicationException in case of problem during the connection
     */
    void connect(ReliabilitySetting setting, int localPort) throws CommunicationException;

    /**
     * Disconnect the process from the system
     */
    void disconnect();

    /**
     * @param serviceType the type of service we want to get.
     * @return the communication service access point
     */
    public IService getService(ServiceSet serviceType);
}
