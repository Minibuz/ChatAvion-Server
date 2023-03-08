# Chatavion Server


Chatavion is a messaging system working over DNS. 
It can be used on every public network without any authentication or payment. This project was created in order to discuss while being connected on Wi-Fi on planes without paying.<br>
This version also include an HTTP version in order to make it more efficient for people using it on the ground and willing to chat with people in planes for example.

Initially, this was a NodeJS prototype made by @vincesafe at https://github.com/vincesafe/chatavion

Thee program is composed of a Spring project that provides a DNS server on port 53 and an HTTP server on port 80.<br>
The DNS server responds to queries for a specific domain with a pre-configured IP address, and the HTTP server responds to the same request on a REST API implementation.

# Prerequisites

Java 17 or later<br>
Maven

### Dns configuration

In order to make the program able to retrieve message, multiple steps are required :

- The server where the program is installed need to be set-up with a fix ipv4 address.<br>
- The server need to have a dns record associate with the fixed ipv4 address, for it to work, it need to start with **chat.** : <br>
For example : *chat.example.com A 1.1.1.12*
- Three domains need to be set up to redirect DNS queries to the server, in NS type, they need to start with **message**, **connexion**, **historique** as follows :
*message.example.com NS chat.example.com*
*connexion.example.com NS chat.example.com*
*historique.example.com NS chat.example.com* (This will be change to history in a future patch)

# Getting Started

Clone the repository: git clone https://github.com/yourusername/spring-dns-http-server.git <br>
Change directory: cd spring-dns-http-server <br>
Build the project: mvn clean install <br>
Run the server: java -jar target/spring-dns-http-server-0.0.1-SNAPSHOT.jar <br>
The server will start and listen for incoming DNS and HTTP requests on ports 53 and 80 respectively. <br>
An admin console is provided for managing the community. The console can be accessed directly from the application console.

### Network DNS
The server will receive three types of request.<br>
First of all is the *connexion* request. This request will be a DNS request as follows "*communityname*.connexion.example.com" with communityname being the community the user is trying to access.<br>
This request will have two possible output. First is the community exist. In that case, the server will add to the answer of the DNS message a record of type A containing the id of the last message of the community.<br>
```

```

### Network HTTP


### Admin console
Only one command is available in the console of the application.<br>
The command **community** followed by a name creates a new community.<br>
For example, " *community community* " will create a new community for every user to access named "community".<br>
Those community needs to be recreated every time the server is restarted. But the history of message will be keep in the log file corresponding to the name of the community as "community.log".

# License

This project is licensed under the MIT License. See the LICENSE file for details.

# Contact

If you have any questions or comments about this project, please feel free to contact me at your.email@example.com.