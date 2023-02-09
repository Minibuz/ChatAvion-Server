package fr.chatavion.server.dns;

import fr.chatavion.server.dns.util.Community;
import org.apache.commons.codec.binary.Base32;
import org.xbill.DNS.*;
import fr.chatavion.server.dns.record.RecordType;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

public class MockDNS {

    private static final Logger logger = Logger.getLogger(MockDNS.class.getName());

    private Thread thread = null;
    private volatile boolean running = false;
    private static final int UDP_SIZE = 512;

    private static final Base32 converter32 = new Base32();
    private final int port;
    private int requestCount = 0;

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
        ++requestCount;
        logger.info(() -> "processing... " + requestCount);

        // Build the response
        Message request = new Message(in);
        Message response = new Message(request.getHeader().getID());
        response.addRecord(request.getQuestion(), Section.QUESTION);

        var msg = request.getQuestion().getName();

        var treatment = msg.getLabelString(1).toLowerCase();
        if("connexion".equals(treatment)) {
            logger.info("Connexion");

            if(Community.findCommunity(msg.getLabelString(0).toLowerCase()) != null) {
                RecordType.typeConnection(request.getQuestion().getType(), response, msg);
            }
        }
        if(treatment.contains("historique")) {
            logger.info("Historique");

            getHistorique(request, response, msg);
        }
        if(msg.labels() > 4 && !"_".equals(msg.getLabelString(0)) && "message".equalsIgnoreCase(msg.getLabelString(3))) {
            logger.info("Message");

            registerMessage(response, msg);
        }

        System.out.println(request);
        System.out.println(indp.getAddress() + " : " + indp.getPort());
        response.getHeader().setFlag(Flags.QR);
        System.out.println(response);

        byte[] resp = response.toWire();
        DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
        logger.info(() -> "sending... " + requestCount);
        socket.send(outdp);
    }

    private static boolean getHistorique(Message request, Message response, Name msg) throws IOException {
        logger.info(msg.getLabelString(0));
        String[] cmAndId = msg.getLabelString(0).split("-");
        var cmB32 = cmAndId[1];
        var cm = new String(converter32.decode(cmB32.getBytes()));
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
            message = community.getMessage(Integer.parseInt(val.substring(0, findO)));
        } else if (findN != -1) {
            message = community.getMessage(Integer.parseInt(val.substring(0, findN)));
        } else {
            message = community.getMessage(Integer.parseInt(val));
        }
        if (message.isPresent()) {
            var rsp = converter32.encode(message.get().getBytes(StandardCharsets.UTF_8));
            return RecordType.sendHistorique(request.getQuestion().getType(), response, msg, rsp);
        }
        return false;
    }

    private static boolean registerMessage(Message response, Name msg) throws IOException {
        var cm = new String(converter32.decode(msg.getLabelString(0).getBytes()));
        var pseudo = new String(converter32.decode(msg.getLabelString(1).getBytes()));
        var message = new String(converter32.decode(msg.getLabelString(2).getBytes()));
        var community = Community.findCommunity(cm);
        if(community == null) {
            logger.warning(() -> "Someone try to access a non existing community : " + cm);
            return false;
        }
        community.addMessage(pseudo, message);
        logger.info(() -> "UTF-8 > " + cm + ": " + pseudo + " - " + message);
        response.addRecord(Record.fromString(msg, Type.A, DClass.IN, 3600, "42.42.42.42", Name.root), Section.ANSWER);
        return true;
    }
}
