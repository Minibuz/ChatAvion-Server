package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;
import fr.chatavion.server.utils.Community;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CommunityInterface.
 */
@Service
public class CommunityService implements CommunityInterface {

    @Override
    public Boolean isCommunityExisting(String communityName) {
        return Community.findCommunity(communityName) != null;
    }

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

    @Override
    public List<Message> retrieveMessagesFromHistory(int idStart, String cmtName, int amount) {
        Community community = Community.findCommunity(cmtName);

        if(community == null) {
            //TODO : Throw personnal exception to catch to send error to the client.
            return List.of();
        }

        List<Message> messages = new ArrayList<>();
        try {
            for(int i = idStart; i < idStart + amount; i++) {
                Message message = retrieveMessage(community, i);
                if(message != null) {
                    messages.add(message);
                }
            }
        } catch (IOException e) {
            //TODO : Clean exception
            throw new RuntimeException(e);
        }

        return messages;
    }

    private Message retrieveMessage(Community community, int i) throws IOException {
        Optional<String> optMessage = community.getMessage(i, -1);
        if(optMessage.isEmpty()) {
            return null;
        }
        String[] userFollowByMessage = optMessage.get().split(":::");
        return new Message(i, userFollowByMessage[0], userFollowByMessage[1]);
    }

    @Override
    public Integer lastMessageId(String communityName) {
        Community community = Community.findCommunity(communityName);

        try {
            return community.findLastIdOfCommunity();
        } catch (IOException e) {
            return -1;
        }
    }
}
