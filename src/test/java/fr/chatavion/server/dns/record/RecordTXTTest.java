package fr.chatavion.server.dns.record;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RecordTXTTest {

    private static final Base32 converter32 = new Base32();

    private static Message createMessage() throws IOException {
        Message message = new Message();
        message.addRecord(
                Record.fromString(
                        Name.fromString("unit.test."),
                        Type.TXT,
                        DClass.IN,
                        3600,
                        "0",
                        Name.root),
                Section.QUESTION);
        return message;
    }

    @Test
    void sendHistorique() throws IOException {
        Message msg = createMessage();
        var rsp = converter32.encode("work".getBytes());
        assertTrue(RecordType.sendHistorique(Type.TXT, msg, msg.getQuestion().getName(), rsp));
        assertEquals(1, msg.getSection(Section.ANSWER).size());
    }

    @Test
    void connection() throws IOException {
        Message msg = createMessage();
        assertTrue(RecordType.typeConnection(Type.TXT, msg, msg.getQuestion().getName()));
        assertEquals(1, msg.getSection(Section.ANSWER).size());
    }
}