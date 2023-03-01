package fr.chatavion.server.admin;

import fr.chatavion.server.utils.Community;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This class is the console dedicated to admin input and community creation.
 * It contains the method to create community.
 */
public class Console {

    private final static Logger logger = Logger.getLogger(Console.class.getName());

    public Console() {
    }

    /**
     * Main method of a console instance.
     * This is starting the scanner for admin input and will delegate the input
     * to make the corresponding action
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            var input = scanner.next();
            if(input.startsWith("community")) {
                var communityName = scanner.next();
                Community.createCommunity(communityName);
            } else {
                logger.info("That command isn't recognise");
            }
        }
    }
}
