package fr.chatavion.server.dns.record;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

public class RecordA implements RecordType {

    @Override
    public boolean sendHistorique(Message response, Name msg, byte[] rsp) throws IOException {
        StringBuilder rspIpv4 = new StringBuilder();
        for (byte code : rsp) {
            rspIpv4.append(code);
            rspIpv4.append('.');
        }
        String[] part = rspIpv4.substring(0, rspIpv4.length() - 1).split("\\.");
        for (int i = 0; i < part.length; i += 4) {
            response.addRecord(
                    Record.fromString(msg, Type.A, DClass.IN, 86400,
                            part[i] + "." + part[i + 1] + "." + part[i + 2] + "." + part[i + 3],
                            Name.root),
                    Section.ANSWER);
        }
        return true;
    }

    @Override
    public boolean connection(Message response, Name msg) throws IOException {
        response.addRecord(
                Record.fromString(msg, Type.A, DClass.IN, 86400, "0.0.0.0", Name.root),
                Section.ANSWER
        );
        return true;
    }
}
