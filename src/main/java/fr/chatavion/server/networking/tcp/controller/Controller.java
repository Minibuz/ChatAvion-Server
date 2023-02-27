package fr.chatavion.server.networking.tcp.controller;


import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;
import fr.chatavion.server.networking.tcp.service.CommunityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Controller {

    private final CommunityInterface communityInterface;

    private Controller(
            @Autowired CommunityInterface communityInterface) {
        this.communityInterface = communityInterface;
    }

    @GetMapping(path = "/")
    public String test() {
        return "It works";
    }

    @GetMapping(path = "/history/{communityName}/{idStr}")
    public @ResponseBody ResponseEntity<List<Message>> retrieveHistory(
            @PathVariable String idStr,
            @PathVariable String communityName,
            @RequestParam(defaultValue = "10") int amount) {
        // Check on user given parameters
        Objects.requireNonNull(idStr);
        Objects.requireNonNull(communityName);
        int id = Integer.parseInt(idStr);

        // Send treatment to service
        List<Message> messages =
                communityInterface.retrieveMessagesFromHistory(id, communityName, amount);

        // Return the result
        return new ResponseEntity<>(messages.stream().filter(Objects::nonNull).toList(), HttpStatus.OK);
    }

    @GetMapping(path = "/community/{communityName}")
    public @ResponseBody ResponseEntity<Integer> chooseCommunity(
            @PathVariable String communityName) {
        // Check on user given parameters
        Objects.requireNonNull(communityName);

        // Send treatment to service
        Boolean result = communityInterface.isCommunityExisting(communityName);

        // Return the result
        return new ResponseEntity<>(result?communityInterface.lastMessageId(communityName):-1, HttpStatus.OK);
    }

    @PostMapping(path = "/message/{communityName}")
    public @ResponseBody ResponseEntity<Boolean> sendMessage(
            @PathVariable String communityName,
            @RequestBody PostMessage message
    ) {
        // Check on user given parameters
        Objects.requireNonNull(communityName);

        // Send treatment to service
        Boolean result = communityInterface.registerMessage(communityName, message);

        // Return the result
        return new ResponseEntity<>(result, result?HttpStatus.CREATED:HttpStatus.BAD_REQUEST);
    }
}
