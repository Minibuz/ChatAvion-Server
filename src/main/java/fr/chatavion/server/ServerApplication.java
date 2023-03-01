package fr.chatavion.server;

import fr.chatavion.server.admin.Console;
import fr.chatavion.server.networking.dns.MockDNS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class represents the main entry point for the server application.
 * It contains the main method that starts the Spring Boot application
 * and also starts a mock DNS server and a console for user input.
 * <br>
 * To use this class, simply run the main method.
 * The Spring Boot application, mock DNS server, and console will all start up.
 */
@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);

        new MockDNS(53).start();

        new Console().start();
	}
}
