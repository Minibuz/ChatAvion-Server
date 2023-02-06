package fr.chatavion.server.dns.record;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

public class RecordTXT implements RecordType {

    @Override
    public void sendHistorique(Message response, Name msg, byte[] rsp) throws IOException {
        StringBuilder rspTxt = new StringBuilder();
        for (byte code : rsp) {
            rspTxt.append(code);
        }
        response.addRecord(
                Record.fromString(msg, Type.TXT, DClass.IN, 86400, rspTxt.toString(), Name.root),
                Section.ANSWER);
    }

    @Override
    public void connection(Message response, Name msg) throws IOException {
        response.addRecord(
                Record.fromString(msg, Type.TXT, DClass.IN, 86400, "0", Name.root),
                Section.ANSWER
        );
    }
}
