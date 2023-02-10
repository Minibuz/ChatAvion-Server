package fr.chatavion.server.dns.record;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;

public class RecordAAAA implements RecordType {


    @Override
    public boolean sendHistorique(Message response, Name msg, byte[] rsp) throws IOException {
        StringBuilder rspIpv6 = new StringBuilder();
        for (byte code : rsp) {
            rspIpv6.append(code);
            rspIpv6.append(':');
        }
        String[] part = rspIpv6.substring(0, rspIpv6.length() - 1).split(":");
        int rest = part.length%7;
        for (int i = 0; i < part.length - rest; i += 7) {
            response.addRecord(
                    Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                            i + ":" + part[i] + ":" + part[i + 1] + ":" + part[i + 2] +
                                    ":" + part[i + 3] + ":" + part[i + 4] + ":" + part[i + 5] + ":" + part[i + 6],
                            Name.root),
                    Section.ANSWER);
        }

        switch (rest) {
            case 1 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-1 + ":" + part[part.length-1] + ":0:0:0:0:0:0",
                                Name.root),
                        Section.ANSWER);
            case 2 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-2 + ":" + part[part.length-2] + ":" + part[part.length-1] + ":0:0:0:0:0",
                                Name.root),
                        Section.ANSWER);
            case 3 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-3 + ":" + part[part.length-3] + ":" + part[part.length-2] + ":" + part[part.length-1] + ":0:0:0:0",
                                Name.root),
                        Section.ANSWER);
            case 4 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-4 + ":" + part[part.length-4] + ":" + part[part.length-3] + ":" + part[part.length-2] + ":" + part[part.length-1] + ":0:0:0",
                                Name.root),
                        Section.ANSWER);
            case 5 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-5 + ":" + part[part.length-5] + ":" + part[part.length-4] + ":" + part[part.length-3] + ":" + part[part.length-2] + ":" + part[part.length-1] + ":0:0",
                                Name.root),
                        Section.ANSWER);
            case 6 ->
                response.addRecord(
                        Record.fromString(msg, Type.AAAA, DClass.IN, 3600,
                                part.length-6 + ":" + part[part.length-6] + ":" + part[part.length-5] + ":" + part[part.length-4] + ":" + part[part.length-3] + ":" + part[part.length-2] + ":" + part[part.length-1] + ":0",
                                Name.root),
                        Section.ANSWER);
        }

        return true;
    }

    @Override
    public boolean connection(Message response, Name msg) throws IOException {
        response.addRecord(
                Record.fromString(msg, Type.AAAA, DClass.IN, 3600, "0:0:0:0:0:0:0:0", Name.root),
                Section.ANSWER
        );
        return true;
    }
}
