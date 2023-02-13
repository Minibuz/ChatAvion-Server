package fr.chatavion.server.dns.record;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecordTypeTest {

    private static final Base32 converter32 = new Base32();

    private static Message createMessage() throws IOException {
        Message message = new Message();
        message.addRecord(
                Record.fromString(
                        Name.fromString("unit.test."),
                        Type.NS,
                        DClass.IN,
                        3600,
                        "test.test.",
                        Name.root),
                Section.QUESTION);
        return message;
    }

    @Test
    void sendHistorique() throws IOException {
        Message msg = createMessage();
        var rsp = converter32.encode("work".getBytes());
        assertFalse(RecordType.sendHistorique(Type.NS, msg, msg.getQuestion().getName(), rsp));
    }

    @Test
    void typeConnection() throws IOException {
        Message msg = createMessage();
        assertFalse(RecordType.typeConnection(Type.NS, msg, msg.getQuestion().getName()));
    }
}
