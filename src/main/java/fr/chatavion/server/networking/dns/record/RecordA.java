package fr.chatavion.server.networking.dns.record;

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
        var count = 0;
        for (int i = 0; i < part.length - part.length%3; i += 3, count += 1) {
            response.addRecord(
                    Record.fromString(msg, Type.A, DClass.IN, 300,
                            count + "." + part[i] + "." + part[i + 1] + "." + part[i + 2],
                            Name.root),
                    Section.ANSWER);
        }
        if(part.length%3 == 1) {
            response.addRecord(
                    Record.fromString(msg, Type.A, DClass.IN, 300,
                            count + "." + part[part.length-1] + ".0.0",
                            Name.root),
                    Section.ANSWER);
        }
        if(part.length%3 == 2) {
            response.addRecord(
                    Record.fromString(msg, Type.A, DClass.IN, 300,
                            count + "." + part[part.length-2] + "." + part[part.length-1] + ".0",
                            Name.root),
                    Section.ANSWER);
        }
        return true;
    }

    @Override
    public boolean connection(Message response, Name msg, int id) throws IOException {
        int realId = 0;
        if(id != -1) {
            realId = id;
        }
        response.addRecord(
                Record.fromString(msg, Type.A, DClass.IN, 300, "0.0.0." + realId, Name.root),
                Section.ANSWER
        );
        return true;
    }
}
