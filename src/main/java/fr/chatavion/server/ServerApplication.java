package fr.chatavion.server;

import fr.chatavion.server.admin.Console;
import fr.chatavion.server.dns.MockDNS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);

        new MockDNS(53).start();

        new Console().start();
	}
}
