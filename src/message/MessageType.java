/* Coyright Eric Cariou, 2009 - 2011 */

package message;

/**
 * Define the types of messages: for each service, a given type has to be defined
 */
public enum MessageType
{
    NONE,
    IDENTIFICATION,
    BASIC_BROADCAST,
    RELIABLE_BROADCAST,
    CAUSAL_RELIABLE_BROADCAST
}
