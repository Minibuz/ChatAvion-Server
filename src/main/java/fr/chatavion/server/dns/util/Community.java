package fr.chatavion.server.dns.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class Community {

    private static final Logger logger = Logger.getLogger(Community.class.getName());

    private static final Map<String, Community> existingCommunities = new HashMap<>();
    static {
        existingCommunities.put("Default", new Community("Default"));
    }

    private final String name;

    Community(String name) {
        this.name = name;

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
        return existingCommunities.get(name);
    }

    public Optional<String> getMessage(int id) throws IOException {
        List<String> history = Files.readAllLines(this.getPathLog());
        if(history.size() <= id) {
            return Optional.empty();
        }
        return Optional.of(history.get(id).split("@")[1]);
    }

    public void addMessage(String username, String message) {
        try {
            Files.write(this.getPathLog(),(LocalDateTime.now() + "@" + username + " : " + message + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            // TODO : Do something here
            throw new UncheckedIOException(e);
        }
    }
}
