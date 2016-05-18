package net.caseif.flint.common.util.agent.chat;

import java.util.UUID;

/**
 * Utility interface for native chat integration.
 */
public interface IChatAgent {

    /**
     * Converts the given serialized message and sends it as a native chat
     * message to the player with the given UUID.
     *
     * @param recipient The UUID of the player to receive the chat message
     * @param message The chat message to send
     * @throws IllegalArgumentException If no player with the given UUID is
     *     online
     */
    void processAndSend(UUID recipient, String message) throws IllegalArgumentException;

    /**
     * Converts the given serialized messages and sends them as native chat
     * messages to the player with the given UUID.
     *
     * @param recipient The UUID of the player to receive the chat message
     * @param message The chat messages to send
     * @throws IllegalArgumentException If no player with the given UUID is
     *     online
     */
    void processAndSend(UUID recipient, String... message) throws IllegalArgumentException;

}
