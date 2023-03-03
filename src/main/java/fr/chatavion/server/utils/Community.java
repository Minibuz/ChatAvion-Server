package fr.chatavion.server.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class represents the representation in Java of the communities.
 * It contains the method to access the file of the corresponding community.
 */
public class Community {

    private static final Logger logger = Logger.getLogger(Community.class.getName());

    private static final Map<String, Community> existingCommunities = new HashMap<>();

    /*
     * The two community getting created by default.
     * default is the base community and test is used for test as well as being a base community.
     */
    static {
        existingCommunities.put("default", new Community("default"));
        existingCommunities.put("test", new Community("test"));
    }

    private final String name;

    private final Object lock = new Object();

    Community(String name) {
        this.name = name.toLowerCase();

        try {
            Files.createFile(this.getPathLog());
        } catch (FileAlreadyExistsException e) {
            logger.info(() -> "Community " + this.name + " already exist.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retrieve the community associate with the name (non-case-sensitive).
     * If the community doesn't exist, return null.
     *
     * @param name {@link String}
     * @return {@link Community}
     */
    public static Community findCommunity(String name) {
        Objects.requireNonNull(name);
        return existingCommunities.get(name.toLowerCase());
    }

    /**
     * Admin command to create a new community. This command will also create the file
     * for the community.
     *
     * @param name {@link String}
     */
    public static void createCommunity(String name) {
        Objects.requireNonNull(name);
        Community community = new Community(name);
        existingCommunities.put(name, community);
        logger.info(() -> "Community " + name + " has been created.");
    }

    private Path getPathLog() {
        return Path.of(System.getProperty("user.dir"), name + ".log");
    }

    /**
     * Give the id of the latest message in the log of the community.
     *
     * @return int
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public int findLastIdOfCommunity() throws IOException {
        List<String> history;
        synchronized (lock) {
            history = Files.readAllLines(this.getPathLog());
        }
        return history.size() - 1;
    }

    /**
     * Return the message of the given id. It will divide the message in part and
     * return the exact part given.
     * It can also return the full message if partId is equal to -1.
     *
     * @param id     int
     * @param partId int
     * @return {@link Optional} of {@link String}
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public Optional<String> getMessage(int id, int partId) throws IOException {
        List<String> history;
        synchronized (lock) {
            history = Files.readAllLines(this.getPathLog());
        }
        if (history.size() <= id) {
            return Optional.empty();
        }

        var fullMessage = history.get(id).split("@", 2)[1];

        if (partId == -1) {
            return Optional.of(fullMessage);
        }

        var messagePart = new ArrayList<String>();
        int i;
        int index;
        var maxSize = 20;
        var pseudoAndMessage = fullMessage.split(":::");
        var pseudo = pseudoAndMessage[0];
        for (i = 0, index = 0;
             i < pseudo.getBytes(StandardCharsets.UTF_8).length - maxSize && i < pseudo.length() - maxSize;
             i += maxSize, index++) {
            messagePart.add("1" + pseudo.substring(maxSize * index, maxSize * (1 + index)));
        }
        messagePart.add("1" + pseudo.substring(i));
        messagePart.add("1:::");
        var message = pseudoAndMessage[1];
        for (i = 0, index = 0;
             i < message.getBytes(StandardCharsets.UTF_8).length - maxSize && i < message.length() - maxSize;
             i += maxSize, index++) {
            messagePart.add("1" + message.substring(maxSize * index, maxSize * (1 + index)));
        }
        messagePart.add("0" + message.substring(i));

        return Optional.of(messagePart.get(partId));
    }

    /**
     * Add the message with the username into the log of the community.
     * It will add it with the date of reception under the format:
     * date@username:::message
     *
     * @param username {@link String}
     * @param message  {@link String}
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public void addMessage(String username, String message) throws IOException {
        synchronized (lock) {
            Files.write(this.getPathLog(), (LocalDateTime.now() + "@" + username + ":::" + message + "\n").getBytes(), StandardOpenOption.APPEND);
        }
    }
}
