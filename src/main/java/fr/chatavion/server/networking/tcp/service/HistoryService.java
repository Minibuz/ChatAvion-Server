package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.util.Community;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class HistoryService implements HistoryInterface {

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
                messages.add(message);
            }
        } catch (IOException e) {
            //TODO : Clean exception
            throw new RuntimeException(e);
        }

        return messages;
    }

    private Message retrieveMessage(Community community, int i) throws IOException {
        Optional<String> optMessage = community.getMessage(i);
        if(optMessage.isEmpty()) {
            return null;
        }
        String[] userFollowByMessage = optMessage.get().split(":::");
        return new Message(i, userFollowByMessage[0], userFollowByMessage[1]);
    }
}
