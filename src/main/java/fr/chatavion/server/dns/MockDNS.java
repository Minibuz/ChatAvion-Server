package fr.chatavion.server.dns;

import fr.chatavion.server.dns.util.Community;
import org.apache.commons.codec.binary.Base32;
import org.xbill.DNS.*;
import fr.chatavion.server.dns.record.RecordType;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

public class MockDNS {

    private final static Logger logger = Logger.getLogger(MockDNS.class.getName());

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
        logger.info("Server starting on port " + this.port);
        running = true;
        thread = new Thread(() -> {
            try {
                serve();
            } catch (IOException ex) {
                stop();
                throw new RuntimeException(ex);
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
        DatagramSocket socket = new DatagramSocket(port);
        logger.info("Socket created...");
        while (running) {
            process(socket);
        }
    }

    private void process(DatagramSocket socket) throws IOException {
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);
        ++requestCount;
        logger.info(String.format("processing... %d", requestCount));

        // Build the response
        Message request = new Message(in);
        Message response = new Message(request.getHeader().getID());
        response.addRecord(request.getQuestion(), Section.QUESTION);

        var msg = request.getQuestion().getName();

        // Add answers as needed (depending on the type of request)
        if("connection".equals(msg.getLabelString(0))) {
            logger.info("Type requête");

            // TODO add id dynamic per community ? Idk how tho
            RecordType.typeConnection(request.getQuestion().getType(), response, msg);

        }
        else if("m".equals(msg.getLabelString(1).charAt(0) + "")) {// starts with m: assume recv request
            logger.info("Historique");

            String cm = new String(converter32.decode(msg.getLabelString(0).getBytes()));
            var community = Community.findCommunity(cm);
            if(community == null) {
                logger.warning("Someone try to access a non existing community.");
                return;
            }

            var val = msg.getLabelString(1);
            val = val.replace("m", "");
            var findO = val.indexOf("o");
            var findN = val.indexOf("n");

            Optional<String> message;
            if(findO != -1) {
                message = community.getMessage(Integer.parseInt(val.substring(0, findO)));
            } else if (findN != -1) {
                message = community.getMessage(Integer.parseInt(val.substring(0, findN)));
            } else {
                message = community.getMessage(Integer.parseInt(val));
            }
            if(message.isPresent()) {
                var rsp = converter32.encode(message.get().getBytes(StandardCharsets.UTF_8));
                RecordType.sendHistorique(request.getQuestion().getType(), response, msg, rsp);
            }
        }
        else {
            logger.info("Message");
            registerMessage(response, msg);
        }

        byte[] resp = response.toWire();
        DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
        logger.info("sending... " + requestCount);
        socket.send(outdp);
    }

    private static void registerMessage(Message response, Name msg) throws IOException {
        var cm = new String(converter32.decode(msg.getLabelString(0).getBytes()));
        var pseudo = new String(converter32.decode(msg.getLabelString(1).getBytes()));
        var message = new String(converter32.decode(msg.getLabelString(2).getBytes()));
        var community = Community.findCommunity(cm);
        if(community == null) {
            logger.warning("Someone try to access a non existing community.");
            return;
        }
        try {
            community.addMessage(pseudo, message);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        logger.info("UTF-8 > " + cm + ": " + pseudo + " - " + message);
        response.addRecord(Record.fromString(msg, Type.A, DClass.IN, 86400, "42.42.42.42", Name.root), Section.ANSWER);
    }
}
