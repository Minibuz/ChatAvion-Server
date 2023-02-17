package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.PostMessage;
import fr.chatavion.server.networking.util.Community;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MessageService implements MessageInterface {

    @Override
    public Boolean registerMessage(String communityName, PostMessage message) {
        Community community = Community.findCommunity(communityName);

        try {
            community.addMessage(message.getUsername(), message.getMessage());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
