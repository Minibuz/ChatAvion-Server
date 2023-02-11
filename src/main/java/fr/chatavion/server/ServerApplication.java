package fr.chatavion.server;

import fr.chatavion.server.dns.MockDNS;
import fr.chatavion.server.dns.util.Community;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);

        new MockDNS(53).start();

        console();
	}

    public static void console() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            var input = scanner.next();
            if(input.startsWith("community")) {
                var communityName = scanner.next();
                Community.createCommunity(communityName);
            }
        }
    }
}
