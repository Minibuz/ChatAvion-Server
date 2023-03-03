package fr.chatavion.server.networking.tcp.controller;


import fr.chatavion.server.networking.tcp.dto.Message;
import fr.chatavion.server.networking.tcp.dto.PostMessage;
import fr.chatavion.server.networking.tcp.service.CommunityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This class represents a REST controller that handles HTTP requests and responses.
 * It interacts with a CommunityInterface object to perform various operations related to a community,
 * such as retrieving history, choosing a community, and sending a message.
 */
@RestController
public class Controller {

    private final CommunityInterface communityInterface;

    private Controller(@Autowired CommunityInterface communityInterface) {
        this.communityInterface = communityInterface;
    }

    @GetMapping(path = "/")
    public String test() {
        return "It works";
    }

    /**
     * Handles HTTP GET requests to the "/history/{communityName}/{idStr}" path.
     * Retrieves a list of messages from the history of a specified community.
     * The idStr and communityName path variables specify the community and the ID of the message to retrieve history from.
     * The amount parameter specifies the maximum number of messages to retrieve.
     *
     * @param idStr         ID of the first message to retrieve
     * @param communityName Name of the community from which the message will be return
     * @param amount        Number of message to be return, default value is 10
     * @return {@link ResponseEntity} of {@link List} of {@link Message}
     */
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

    /**
     * Handles HTTP GET requests to the "/community/{communityName}" path.
     * Determines whether a specified community exists.
     * The communityName path variable specifies the community to check.
     * Returns the ID of the last message in the community, or -1 if the community does not exist.
     *
     * @param communityName Name of the community from which the message will be return
     * @return {@link ResponseEntity} of {@link Integer}
     */
    @GetMapping(path = "/community/{communityName}")
    public @ResponseBody ResponseEntity<Integer> chooseCommunity(
            @PathVariable String communityName) {
        // Check on user given parameters
        Objects.requireNonNull(communityName);

        // Send treatment to service
        boolean result = communityInterface.isCommunityExisting(communityName);

        // Return the result
        return new ResponseEntity<>(result ? communityInterface.lastMessageId(communityName) : -1, HttpStatus.OK);
    }

    /**
     * Handles HTTP POST requests to the "/message/{communityName}" path. `
     * Sends a message to a specified community.
     * The communityName path variable specifies the community to send the message to.
     * The message parameter contains the details of the message to send.
     *
     * @param communityName Name of the community from which the message will be return
     * @param message       Message that is getting send to the server
     * @return {@link ResponseEntity} of {@link Boolean}
     */
    @PostMapping(path = "/message/{communityName}")
    public @ResponseBody ResponseEntity<Boolean> sendMessage(
            @PathVariable String communityName,
            @RequestBody PostMessage message
    ) {
        // Check on user given parameters
        Objects.requireNonNull(communityName);

        // Send treatment to service
        boolean result = communityInterface.registerMessage(communityName, message);

        // Return the result
        return new ResponseEntity<>(result, result ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }
}
