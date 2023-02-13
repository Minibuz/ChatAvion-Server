package fr.chatavion.server.admin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleTest {

    @Test
    void consoleTestWithHelp() {
        InputStream in = new ByteArrayInputStream("help".getBytes());
        System.setIn(in);
        assertDoesNotThrow(() -> new Console().start());
    }

    @Test
    void consoleTestWithNewCommunity() throws IOException {
        InputStream in = new ByteArrayInputStream("community test".getBytes());
        System.setIn(in);
        assertDoesNotThrow(() -> new Console().start());
        Files.delete(Path.of("test.log"));
        Files.delete(Path.of("default.log"));
    }

    @Test
    void consoleTestThrow() {
        InputStream in = new ByteArrayInputStream("explosion".getBytes());
        System.setIn(in);
        assertThrows(IllegalStateException.class, () -> new Console().start());
    }
}