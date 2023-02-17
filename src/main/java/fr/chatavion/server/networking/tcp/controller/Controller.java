package fr.chatavion.server.networking.tcp.controller;


import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;
import fr.chatavion.server.networking.tcp.service.HistoryInterface;
import fr.chatavion.server.networking.tcp.service.MessageInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Controller {

    private final HistoryInterface historyInterface;
    private final MessageInterface messageInterface;

    private Controller(
            @Autowired HistoryInterface historyInterface,
            @Autowired MessageInterface messageInterface) {
        this.historyInterface = historyInterface;
        this.messageInterface = messageInterface;
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
                historyInterface.retrieveMessagesFromHistory(id, communityName, amount);

        // Return the result
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping(path = "/message/{communityName}")
    public @ResponseBody ResponseEntity<Boolean> sendMessage(
            @PathVariable String communityName,
            @RequestBody PostMessage message
    ) {
        Objects.requireNonNull(communityName);

        Boolean result = messageInterface.registerMessage(communityName, message);
        return new ResponseEntity<>(result, result?HttpStatus.CREATED:HttpStatus.BAD_REQUEST);
    }
}
