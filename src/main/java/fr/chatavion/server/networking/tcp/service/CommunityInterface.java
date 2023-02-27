package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;

import java.util.List;

public interface CommunityInterface {

    List<Message> retrieveMessagesFromHistory(int idStart, String cmtName, int amount);

    Boolean registerMessage(String communityName, PostMessage message);

    Boolean isCommunityExisting(String communityName);

    Integer lastMessageId(String communityName);
}
