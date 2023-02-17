package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.PostMessage;

public interface MessageInterface {

    Boolean registerMessage(String communityName, PostMessage message);
}
