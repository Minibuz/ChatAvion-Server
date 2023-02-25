package fr.chatavion.server.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class Community {

    private static final Logger logger = Logger.getLogger(Community.class.getName());

    private static final Map<String, Community> existingCommunities = new HashMap<>();
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

    private Path getPathLog() {
        return Path.of(System.getProperty("user.dir"), name + ".log");
    }

    public static Community findCommunity(String name) {
        return existingCommunities.get(name.toLowerCase());
    }

    public int findLastIdOfCommunity() throws IOException {
        List<String> history;
        synchronized (lock) {
            history = Files.readAllLines(this.getPathLog());
        }
        return history.size()-1;
    }

    public Optional<String> getMessage(int id, int partId) throws IOException {
        List<String> history;
        synchronized (lock) {
            history = Files.readAllLines(this.getPathLog());
        }
        if(history.size() <= id) {
            return Optional.empty();
        }
        var message = history.get(id).split("@")[1];
        System.out.println(message.length());

        if(partId == -1) {
            return Optional.of(message);
        }

        var i = 0;
        var messagePart = new ArrayList<String>();
        for(i=0; i < message.length()-139; i+=139) {
            messagePart.add("1" + message.substring(139 * i, 139 * (1 + i)));
            System.out.println("1" + message.substring(139 * i, 139 * (1 + i)));
        }
        messagePart.add("0" + message.substring(i));
        System.out.println("0" + message.substring(i));
        return Optional.of(messagePart.get(partId));
    }

    public void addMessage(String username, String message) throws IOException {
        synchronized (lock) {
            Files.write(this.getPathLog(), (LocalDateTime.now() + "@" + username + ":::" + message + "\n").getBytes(), StandardOpenOption.APPEND);
        }
    }

    public static void createCommunity(String name) {
        Community community = new Community(name);
        existingCommunities.put(name, community);
        logger.info(() -> "Community " + name + " has been created.");
    }
}
