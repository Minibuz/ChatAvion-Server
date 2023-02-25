package integration;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.ArrayUtils;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class MockDNSTest {

    private static final Logger logger = Logger.getLogger(MockDNSTest.class.getName());

    private static final int NUMBER_OF_RETRIES = 1;

    private final InetAddress local;

    private final Base32 converter32;

    private final SimpleResolver resolver;

    // This can be modified to test multiple type of DNS message.
    private final int type = Type.A;

    private int id = 0;

    private final List<String> list = new ArrayList<>();

    MockDNSTest() throws UnknownHostException {
        this.converter32 = new Base32();
        this.resolver = new SimpleResolver(InetAddress.getLocalHost());
        this.local = InetAddress.getLocalHost();
    }

    public static void main(String[] args) throws UnknownHostException {
        // TEST
        Properties props = System.getProperties();
        props.setProperty("dnsjava.lookup.max_iterations", "1");


        MockDNSTest sender = new MockDNSTest();

        try (var scanner = new Scanner(System.in)) {
            String userB32 = sender.converter32.encodeAsString("test".getBytes(StandardCharsets.UTF_8));
            String cmtB32 = sender.converter32.encodeAsString("test".getBytes(StandardCharsets.UTF_8));
            Response response = DnsUtils.forNameType(sender.resolver, "test.connexion." + sender.local, Type.A);
            if(response.results().isEmpty()) {
                logger.severe(() -> "Problem with the server.");
                return;
            }
            System.out.println(response.results().get(0));

            System.out.println("\nEnter : ");
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();

                String type = msg.substring(0,3);
                switch (type) {
                    case "msg" ->
                            sender.sendMessage(cmtB32, userB32, msg.substring(4));
                    case "hst" ->
                            sender.requestHistorique(cmtB32, msg.substring(4));
                    case "dsp" ->
                            sender.list.forEach(System.out::println);
                    default -> logger.info(() -> "Use msg follow by your message.\nUse hst follow by a number to get the historique.\nUse dsp to display the history.\n");
                }
                System.out.println("\nEnter : ");
            }
        }
    }

    private void sendMessage(String cmtB32, String userB32, String msg) {
        byte[] msgAsBytes = msg.getBytes(StandardCharsets.UTF_8);
        if(msgAsBytes.length > 70) {
            logger.warning("Message cannot be more than 70 character as UTF_8 byte array.");
            return;
        }
        String msgB32 = this.converter32.encodeAsString(msgAsBytes);

        // TODO Generate random id for the messages
        Random r = new Random();
        int randomId = r.nextInt(65536);
        int maxSplit = (short) (msgB32.length()/35);

        int index = 0;
        List<String> listPart = new ArrayList<>();
        for(int i = 0; i < msgB32.length()-35; i+=35, index++ ) {
            String partSend = randomId + "-" +
                    maxSplit +
                    index + "-" +
                    msgB32.substring(i, i+35);
            listPart.add(partSend);
        }
        listPart.add(randomId + "-" +
                maxSplit +
                maxSplit + "-" +
                msgB32.substring(35 * index));

        for(var element : listPart) {
            System.out.println(element);
        }

        for(String msgPart : listPart) {
            for (int retries = 0; retries < NUMBER_OF_RETRIES; retries++) {
                Response results = DnsUtils.forNameType(this.resolver, cmtB32 + "." + userB32 + "." + msgPart + ".message." + this.local, Type.A);

                if (results.results().isEmpty()) {
                    System.out.println(results.statut());
                } else if ("42.42.42.42".equals(results.results().get(0))) {
                    System.out.println("\nServer received the message\n");
                } else {
                    System.out.println("\nServer hasn't received the message\n");
                }
            }
        }
    }

    private void requestHistorique(String cmtB32, String number) {
        int nbMsgHistorique = 1;
        try {
            nbMsgHistorique = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            // don't do anything
        }

        var doRetrieve = true;
        var part = 0;
        String message = "";
        do {
            for (int i = 0; i < nbMsgHistorique; i++) {
                String request = type == Type.TXT ? "m" + id : type == Type.AAAA ? "m" + id + "o" + part : "m" + id + "n" + part;

                Response results = DnsUtils.forNameType(this.resolver, request + "-" + cmtB32 + ".historique." + this.local, type);
                if (results.statut() == Lookup.UNRECOVERABLE) {
                    System.err.println("Error");
                    return;
                }
                if (results.results().isEmpty()) {
                    System.out.println("No message to retrieve");
                    return;
                }

                List<Byte> msg = new ArrayList<>();
                if (type == Type.A) {
                    mergeResultTypeA(results.results(), msg);
                } else if (type == Type.AAAA) {
                    mergeResultTypeAAAA(results.results(), msg);
                } else {
                    mergeResultTypeTXT(results.results(), msg);
                }
                var messagePart = new String(converter32.decode(ArrayUtils.toPrimitive(msg.toArray(new Byte[0]))));
                if(messagePart.startsWith("0")) {
                    message += messagePart.substring(1);
                    if ("".equals(message)) {
                        return;
                    }
                    id++;
                    list.add(message);
                    doRetrieve = false;
                } else if (messagePart.startsWith("1")) {
                    message += messagePart.substring(1);
                    part++;
                }
            }
        } while(doRetrieve);
    }

    private static void mergeResultTypeA(List<String> results, List<Byte> msg) {
        // Sort results based on first byte
        HashMap<Integer, List<Byte>> map = new HashMap<>();
        for (var result : results) {
            var part = result.split("\\.");
            map.put(Integer.parseInt(part[0]),
                    List.of(Byte.parseByte(part[1]),
                            Byte.parseByte(part[2]),
                            Byte.parseByte(part[3])));
        }
        List<Integer> keys = map.keySet().stream().sorted().toList();
        for (var key : keys) {
            msg.addAll(Objects.requireNonNull(map.get(key)));
        }
    }

    private static void mergeResultTypeAAAA(List<String> results, List<Byte> msg) {
        for (var result : results) {
            var part = result.split(":");
            msg.add(Byte.parseByte(part[0]));
            msg.add(Byte.parseByte(part[1]));
            msg.add(Byte.parseByte(part[2]));
            msg.add(Byte.parseByte(part[3]));
            msg.add(Byte.parseByte(part[4]));
            msg.add(Byte.parseByte(part[5]));
            msg.add(Byte.parseByte(part[6]));
            msg.add(Byte.parseByte(part[7]));
        }
    }

    private static void mergeResultTypeTXT(List<String> results, List<Byte> msg) {
        for (var result : results) {
            var element = result.replace("\"", "");
            for (int i = 0; i < result.length() - 2; i += 2) {
                msg.add(Byte.parseByte(element.substring(i, i + 2)));
            }
        }
    }
}
