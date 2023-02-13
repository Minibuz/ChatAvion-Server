package fr.chatavion.server.dns.record;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

public class RecordTXT implements RecordType {

    @Override
    public boolean sendHistorique(Message response, Name msg, byte[] rsp) throws IOException {
        StringBuilder rspTxt = new StringBuilder();
        for (byte code : rsp) {
            rspTxt.append(code);
        }
        response.addRecord(
                Record.fromString(msg, Type.TXT, DClass.IN, 300, rspTxt.toString(), Name.root),
                Section.ANSWER);
        return true;
    }

    @Override
    public boolean connection(Message response, Name msg) throws IOException {
        response.addRecord(
                Record.fromString(msg, Type.TXT, DClass.IN, 86400, "0", Name.root),
                Section.ANSWER
        );
        return true;
    }
}
