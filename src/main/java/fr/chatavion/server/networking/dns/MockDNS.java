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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
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

    public void stop() {
        running = false;
        thread.interrupt();
        thread = null;
    }

    private void serve() throws IOException {
        logger.info("Creating socket...");
        try (DatagramSocket socket = new DatagramSocket(port)) {
            logger.info("Socket created...");
            while (running) {
                process(socket);
            }
        }
    }

    private void process(DatagramSocket socket) throws IOException {
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);
        logger.info(() -> "Processing entry...");

        try {
            // Build the response
            Message request = new Message(in);
            Message response = new Message(request.getHeader().getID());
            response.addRecord(request.getQuestion(), Section.QUESTION);
            response.getHeader().setFlag(Flags.QR);

            // Treat the request corresponding to the given type of request
            var question = request.getQuestion();
            if(question == null) {
                return;
            }
            var msg = question.getName();
            if(msg.labels()<4) {
                return;
            }
            var treatment = msg.getLabelString(1).toLowerCase();
            if ("connexion".equals(treatment)) {
                logger.info("Connexion");
                communityConnexionValidation(request, response, msg);
            } else if (treatment.contains("historique")) {
                logger.info("Historique");
                getHistorique(request, response, msg);
            } else if (msg.labels() > 4 && !"_".equals(msg.getLabelString(0)) && "message".equalsIgnoreCase(msg.getLabelString(3))) {
                logger.info("Message");
                registerMessage(response, msg);
            }

            // Send the response as a packet
            byte[] resp = response.toWire();
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            logger.info(() -> "sending output...");
            socket.send(outdp);
        } catch (WireParseException e) {
            logger.warning(() -> "Something went wrong - " + e.getMessage());
        }
    }

    private static void communityConnexionValidation(Message request, Message response, Name msg) throws IOException {
        var cmt = Community.findCommunity(msg.getLabelString(0).trim().toLowerCase());
        if(cmt != null) {
            RecordType.typeConnection(request.getQuestion().getType(), response, msg, cmt.findLastIdOfCommunity());
        }
    }

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
            message = community.getMessage(Integer.parseInt(val.substring(0, findO)), Integer.parseInt(val.substring(idO)));
        } else if (findN != -1) {
            var idN = findN + 1;
            message = community.getMessage(Integer.parseInt(val.substring(0, findN)), Integer.parseInt(val.substring(idN)));
        } else {
            message = community.getMessage(Integer.parseInt(val), -1);
        }
        if (message.isPresent()) {
            var rsp = converter32.encode(message.get().getBytes(StandardCharsets.UTF_8));
            return RecordType.sendHistorique(request.getQuestion().getType(), response, msg, rsp);
        }
        return false;
    }

    private static boolean registerMessage(Message response, Name msg) throws IOException {
        var cm = new String(converter32.decode(msg.getLabelString(0).getBytes())).trim();
        var pseudo = new String(converter32.decode(msg.getLabelString(1).getBytes())).trim();

        var community = Community.findCommunity(cm);
        if(community == null) {
            logger.warning(() -> "Someone try to access a non existing community : " + cm);
            return false;
        }

        var messageCoded = msg.getLabelString(2);
        // idnm"msg" -> 4 bytes "-" 1 byte 1 byte "-"
        var partMessage = messageCoded.split("-",3);
        if(partMessage.length != 3) {
            logger.warning(() -> "Malform message : " + messageCoded);
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
