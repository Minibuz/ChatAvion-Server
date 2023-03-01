package fr.chatavion.server.networking.dns;

import fr.chatavion.server.utils.Community;
import fr.chatavion.server.networking.dns.record.RecordType;
import org.apache.commons.codec.binary.Base32;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class MockDNS {

    private static final Logger logger = Logger.getLogger(MockDNS.class.getName());

    private Thread thread = null;
    private volatile boolean running = false;
    private static final int UDP_SIZE = 512;

    private static final Base32 converter32 = new Base32();
    private final int port;
    private static final HashMap<String, String> messages = new HashMap<>();

    public MockDNS(int port) {
        this.port = port;
    }

    /**
     * Main method of a mockdns instance.
     * This is starting the dns part of the server.
     */
    public void start() {
        logger.info(() -> "Server starting on port " + this.port);
        running = true;
        thread = new Thread(() -> {
            try {
                serve();
            } catch (IOException ex) {
                stop();
                throw new UncheckedIOException(ex);
            }
        });
        thread.start();
    }

    /**
     * Stop the dns server.
     */
    public void stop() {
        running = false;
        thread.interrupt();
        thread = null;
    }

    /**
     * Serve the incoming request and redirect them to the process.
     *
     * @throws IOException
     *          if the socket could not be opened, or the socket could not bind to the specified local port.
     */
    private void serve() throws IOException {
        logger.info("Creating socket...");
        try (DatagramSocket socket = new DatagramSocket(port)) {
            logger.info("Socket created...");
            while (running) {
                process(socket);
            }
        }
    }

    /**
     * Main process of the DNS server.
     *
     * @param socket
     *          contain the byte of the info send other network.
     * @throws IOException
     *          if an I/O error occurs.
     */
    private void process(DatagramSocket socket) throws IOException {
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);
        logger.info(() -> "Processing entry");

        try {
            // Build the response
            Message request = new Message(in);
            Message response = new Message(request.getHeader().getID());
            response.addRecord(request.getQuestion(), Section.QUESTION);
            response.getHeader().setFlag(Flags.QR);

            // Verify that the question is on the good format
            var question = request.getQuestion();
            // Question shouldn't be empty
            if(question == null) {
                logger.warning(() -> "Message doesn't contain a question");
                return;
            }
            var msg = question.getName();
            // The name should at least be x.x.x.x
            if(msg.labels()<4) {
                logger.warning(() -> "Message question isn't compose of 4 labels or more " + msg);
                return;
            }

            // Treat the request corresponding to the given type of request
            var treatment = msg.getLabelString(1).toLowerCase();
            if ("connexion".equals(treatment)) {
                logger.info(() -> "Connexion " + msg);
                communityConnexionValidation(request, response, msg);
            } else if (treatment.contains("historique")) {
                logger.info(() -> "Historic" + msg);
                getHistorique(request, response, msg);
            } else if (msg.labels() > 4 && !"_".equals(msg.getLabelString(0)) && "message".equalsIgnoreCase(msg.getLabelString(3))) {
                logger.info(() -> "Message " + msg);
                registerMessage(response, msg);
            }

            // Send the response as a packet
            byte[] resp = response.toWire();
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            logger.info(() -> "Sending output");
            socket.send(outdp);
        } catch (WireParseException e) {
            logger.warning(() -> "Something went wrong - " + e.getMessage());
        }
    }

    /**
     * Verify the community the client is trying to connect to.
     * And fill the response accordingly.
     *
     * @param request
     *          Message send by the client
     * @param response
     *          Response getting filled to be sent to the client
     * @param msg
     *          Name containing the request in the url
     * @throws IOException
     *
     */
    private static void communityConnexionValidation(Message request, Message response, Name msg) throws IOException {
        var cmt = Community.findCommunity(msg.getLabelString(0).trim().toLowerCase());
        if(cmt != null) {
            RecordType.typeConnection(request.getQuestion().getType(), response, msg, cmt.findLastIdOfCommunity());
        }
    }

    /**
     *  Get message from the historic based on a specific community and the request from the client.
     *  The function first extracts the community name, message id, and optional part number from the label of the DNS query contained in the msg parameter.
     *  It then searches for the message in the community by calling the getMessage method of the Community class.
     *  If the message is found, it is encoded using a Base32 encoder and added to the DNS response message using the sendHistorique method of the RecordType class,
     *  either type A, type AAAA or type TXT.
     *  The function returns true if the message was found and added to the response, false otherwise.
     *
     * @param request
     *          Message send by the client
     * @param response
     *          Response getting filled to be sent to the client
     * @param msg
     *          Name containing the request in the url
     * @return
     *          True if a message with a given id can be found, otherwise false
     * @throws IOException
     */
    private static boolean getHistorique(Message request, Message response, Name msg) throws IOException {
        String[] cmAndId = msg.getLabelString(0).split("-");
        var cmB32 = cmAndId[1];
        var cm = new String(converter32.decode(cmB32.getBytes())).trim();
        var community = Community.findCommunity(cm);
        if (community == null) {
            logger.warning("Someone try to access a non existing community.");
            return false;
        }

        var val = cmAndId[0].toLowerCase();
        val = val.replace("m", "");
        var findO = val.indexOf("o");
        var findN = val.indexOf("n");

        Optional<String> message;
        if (findO != -1) {
            var idO = findO + 1;
            var part = Integer.parseInt(val.substring(idO));
            message = community.getMessage(Integer.parseInt(val.substring(0, findO)), part);
        } else if (findN != -1) {
            var idN = findN + 1;
            message = community.getMessage(
                    Integer.parseInt(val.substring(0, findN)),
                    Integer.parseInt(val.substring(idN)));
        } else {
            message = community.getMessage(Integer.parseInt(val), -1);
        }
        if (message.isPresent()) {
            return RecordType.sendHistorique(
                    request.getQuestion().getType(),
                    response,
                    msg,
                    converter32.encode(message.get().getBytes(StandardCharsets.UTF_8)));
        }
        return false;
    }

    /**
     * Register a message in a specific community
     *
     * @param response
     *          Response getting filled to be sent to the client
     * @param msg
     *          Name containing the request in the url
     * @return
     *          Boolean indicating whether the registration was successful or not
     * @throws IOException
     *          If an IO exception occurs
     */
    private static boolean registerMessage(Message response, Name msg) throws IOException {
        var cm = new String(converter32.decode(msg.getLabelString(0).getBytes())).trim();
        var pseudo = new String(converter32.decode(msg.getLabelString(1).getBytes())).trim();

        var community = Community.findCommunity(cm);
        if(community == null) {
            logger.warning(() -> "Someone try to access a non existing community : " + cm);
            return false;
        }

        var messageCoded = msg.getLabelString(2);
        // id random "-" nmOfPart PartId "-" msgB32 -> 4 bytes "-" 1 byte 1 byte "-" msgB32
        var partMessage = messageCoded.split("-",3);
        if(partMessage.length != 3) {
            logger.warning(() -> "Malformed message : " + messageCoded);
            return false;
        }
        String id = partMessage[0] + cm + pseudo;
        int parts = partMessage[1].getBytes()[0];
        int index = partMessage[1].getBytes()[1];

        if(parts == index) {
            // Dernière partie du message
            var messageParts = messages.getOrDefault(id, "");
            var message = new String(converter32.decode(messageParts + partMessage[2])).trim();
            community.addMessage(pseudo, message);
            logger.info(() -> "UTF-8 > " + cm + ": " + pseudo + " - " + message);
        } else {
            // Stockée à la suite pour cet id
            messages.compute(id, (key, value) -> (value == null) ? partMessage[2] : value + partMessage[2]);
            logger.info(() -> "UTF-8 > " + cm + ": " + pseudo + " - part of message : " + partMessage[2]);
        }
        response.addRecord(Record.fromString(msg, Type.A, DClass.IN, 3600, "42.42.42.42", Name.root), Section.ANSWER);
        return true;
    }
}
