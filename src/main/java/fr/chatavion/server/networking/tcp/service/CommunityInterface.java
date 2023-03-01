package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;

import java.util.List;

/**
 * This interface defines methods for interacting with a community and performing various operations related to it.
 */
public interface CommunityInterface {

    /**
     * Retrieves a list of messages from the history of a specified community.
     * The idStart parameter specifies the ID of the first message to retrieve.
     * The cmtName parameter specifies the name of the community to retrieve the messages from.
     * The amount parameter specifies the maximum number of messages to retrieve.
     *
     * @param idStart
     *          Id of the first message to retrieve
     * @param cmtName
     *          Name of the community from which the message will be return
     * @param amount
     *          Number of message to be return, default value is 10
     * @return
     *          {@link List} of {@link Message}
     */
    List<Message> retrieveMessagesFromHistory(int idStart, String cmtName, int amount);

    /**
     * Registers a new message in a specified community.
     * The communityName parameter specifies the name of the community to register the message in.
     * The message parameter contains the details of the message to register.
     *
     * @param communityName
     *          Name of the community from which the message will be return
     * @param message
     *          Message that is getting send to the server
     * @return
     *          {@link Boolean}
     */
    Boolean registerMessage(String communityName, PostMessage message);

    /**
     * Determines whether a specified community exists.
     * The communityName parameter specifies the name of the community to check.
     * Returns true if the community exists, false otherwise.
     *
     * @param communityName
     *          Name of the community from which the message will be return
     * @return
     *          {@link Boolean}
     */
    Boolean isCommunityExisting(String communityName);

    /**
     * Returns the ID of the last message in a specified community.
     * The communityName parameter specifies the name of the community to check.
     *
     * @param communityName
     *          Name of the community from which the message will be return
     * @return
     *          {@link Integer}
     */
    Integer lastMessageId(String communityName);
}
