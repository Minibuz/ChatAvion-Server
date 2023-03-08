# Chatavion Server


Chatavion is a messaging system working over DNS. 
It can be used on every public network without any authentication or payment. This project was created in order to discuss while being connected on Wi-Fi on planes without paying.<br>
This version also include an HTTP version in order to make it more efficient for people using it on the ground and willing to chat with people in planes for example.

Initially, this was a NodeJS prototype made by @vincesafe at https://github.com/vincesafe/chatavion

The program is composed of a Spring project that provides a DNS server on port 53 and an HTTP server on port 80.<br>
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
  - *message.example.com NS chat.example.com*
  - *connexion.example.com NS chat.example.com*
  - *historique.example.com NS chat.example.com* (This will be change to history in a future patch)

# Getting Started

Clone the repository: ```git clone https://github.com/Minibuz/ChatAvion-Server ```<br>
Change directory: ```cd ChatAvion-Server```<br>
Build the project: ```mvn clean install``` <br>
Run the server: ```sudo java -jar target/server-x.x.x.jar``` <br>
The server will start and listen for incoming DNS and HTTP requests on ports 53 and 80 respectively. <br>
An admin console is provided for managing the community. The console can be accessed directly from the application console.

### Network DNS
The server will receive three types of request.<br>

First of all is the *connexion* request. This request will be a DNS request as follows "*communityname*.connexion.example.com" with communityname being the community the user is trying to access.<br>
This request will have two possible output.<br>
First is the community exist. In that case, the server will add to the answer of the DNS message a record of type A containing the id of the last message of the community.<br>
```
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 7777
;; flags: qr ; qd: 1 an: 1 au: 0 ad: 0 
;; QUESTIONS:
;;	test.connexion.example.com., type = A, class = IN

;; ANSWERS:
test.connexion.example.com.	300	IN	A	0.0.0.10

;; AUTHORITY RECORDS:

;; ADDITIONAL RECORDS:

;; Message size: 0 bytes
```
In the answer field, we can see a response which means the community test exists and the last message of the community is at id 10.<br>
The second case is if the community doesn't exist on the given server. In this case, we have the following response :<br>
```
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 7777
;; flags: qr ; qd: 1 an: 0 au: 0 ad: 0 
;; QUESTIONS:
;;	test.connexion.example.com., type = A, class = IN

;; ANSWERS:

;; AUTHORITY RECORDS:

;; ADDITIONAL RECORDS:

;; Message size: 0 bytes
```
This time, the answer field is empty.<br>

Second type of request the server is going to receive are the message request.<br>
They are formed as follows :
```
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 20630
;; flags: qr ; qd: 1 an: 1 au: 0 ad: 0 
;; QUESTIONS:
;;	ORSXG5A=.ORSXG5A=.25963-00-ORSXG5A=.message.example.com., type = A, class = IN

;; ANSWERS:
ORSXG5A=.ORSXG5A=.25963-00-ORSXG5A=.message.example.com.	3600	IN	A	42.42.42.42

;; AUTHORITY RECORDS:

;; ADDITIONAL RECORDS:

;; Message size: 0 bytes
```
Those request have a question formed as follows :<br>
*communityBase32.usernameBase32.randomId-totalPartpartId-messagePartBase32.message.example.com*<br>
In this question, we can see each part is a different element.<br>
communityBase32 is the community the message is getting send on. The name of the community is transform in Base32 for the request.<br>
usernameBase32 is the username of the user sending the message. The username is transform in Base32 for the request.<br>
The last part is the message part. This part is composed of multiple subpart, each subpart is being split with '-'. 
First is a random short generated in order to identify the message and recompose it. 
The second part is composed of two numbers from 0 to 9. The first one is the number of total part of the message. The second one is the id of the part following the next '-'.
When those two numbers are equals, it means that the whole message have been sent, the server can then decrypt and recompose the whole message before storing it in the community log.
The third part is the message subpart encrypted in Base32.

Third type of request are the history request. Those are the most complex request as they involved splitting message in multiple parts depending on ids received in the request.
```
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 4783
;; flags: qr ; qd: 1 an: 14 au: 0 ad: 0 
;; QUESTIONS:
;;	m0n2-ORSXG5A=.historique.example.com., type = A, class = IN

;; ANSWERS:
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	0.71.70.71
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	1.87.75.52
m0n2-ORSXG5A=.historique.example.com..	300	IN	A	2.51.84.77
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	3.70.84.87
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	4.75.77.74
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	5.83.71.65
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	6.90.68.71
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	7.76.74.81
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	8.71.77.87
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	9.84.65.78
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	10.50.85.71
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	11.69.61.61
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	12.61.61.61
m0n2-ORSXG5A=.historique.example.com.	300	IN	A	13.61.0.0

;; AUTHORITY RECORDS:

;; ADDITIONAL RECORDS:

;; Message size: 0 bytes
```
Those request have a question formed as follows :<br>
*m0n2-ORSXG5A=.historique.example.com* <br>
The first part is composed of two subpart, the id of the message and the id of the part followed by '-' and the community name in Base32.
The id *'m' number* with number being the id of the message the client is trying to retrieve.<br>
The id *'n' number* with number being the id of the part of the message the client is trying to retrieve.<br>
In the answers fields, we can see multiple records. Each of the start with an id from 0 in order for the
client to recompose the message in the good order. Then the other byte are the part of the message from the history that
has been transform to Base32 and separate byte by byte.

### Network HTTP

#### Connexion
```GET /community/communityName```<br>
```curl -i -H 'Accept: application/json' http://localhost:80/community/test``` <br>

**Response**
```http request
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Wed, 08 Mar 2023 13:49:37 GMT
Keep-Alive: timeout=60
Connection: keep-alive

1
```

#### Get history
```GET /history/communityName/idStr```<br>
```curl -i -H 'Accept: application/json' http://localhost:80/history/test/0``` <br>

**Response**
```http request
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Wed, 08 Mar 2023 13:50:08 GMT
Keep-Alive: timeout=60
Connection: keep-alive

[
  {
    "id": 0,
    "user": "User",
    "message": "Message test 1"
  },
  {
    "id": 1,
    "user": "User",
    "message": "Message test 2"
  }
]
```

#### Send message
```POST /message/communityName``` <br>

```POST http://localhost:80/message/test``` <br>
```{"id":0,"user":"User","message":"Message"}``` <br>

**Response**
```http request
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Wed, 08 Mar 2023 13:55:31 GMT
Keep-Alive: timeout=60
Connection: keep-alive

true
```

### Admin console
Only one command is available in the console of the application.<br>
The command **community** followed by a name creates a new community.<br>
For example, " *community community* " will create a new community for every user to access named "community".<br>
Those community needs to be recreated every time the server is restarted. But the history of message will be keep in the log file corresponding to the name of the community as "community.log".

### Using the server
If you are a server owner and want to start the server to let it run on a distant server, you can use the following commands :

- ```sudo java -jar server-\*.\*.\*.jar``` is used to start the server. If you are already using a root account,
u can remove the sudo.
- ```screen -S screenName``` with screenName being the name of the screen you want to create. This command allow the user to start
a screen to separate the logs of the server from the main screen. It also permits the server to run after leaving
the communication such as ssh.
- ```screen -r screenName``` with screenName being the name of the screen you want to create. This command allow the user to swap the
current screen to screenName screen.<br>
While being on a screen, you can return to the main one using ctrl+a+d.<br>

# License

This project is licensed under the MIT License. See the [LICENCE](LICENSE) for details.

# Contact

If you have any questions or comments about this project, please feel free to contact [me](https://github.com/minibuz/) or anyone who worked on the project.
