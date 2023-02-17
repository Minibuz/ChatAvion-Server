package fr.chatavion.server.admin;

import fr.chatavion.server.networking.util.Community;

import java.util.Scanner;
import java.util.logging.Logger;

public class Console {

    private final static Logger logger = Logger.getLogger(Console.class.getName());

    public Console() {
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            var input = scanner.next();
            if(input.startsWith("help")) {
                logger.info(() -> "No help given.");
            } else if(input.startsWith("community")) {
                var communityName = scanner.next();
                Community.createCommunity(communityName);
            } else {
                // Add more branch here.
                throw new IllegalStateException();
            }
        }
    }
}
