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
        for (int i = 0; i < part.length; i += 8) {
            response.addRecord(
                    Record.fromString(msg, Type.AAAA, DClass.IN, 86400,
                            part[i] + ":" + part[i + 1] + ":" + part[i + 2] + ":" + part[i + 3] +
                                    ":" + part[i + 4] + ":" + part[i + 5] + ":" + part[i + 6] + ":" + part[i + 7],
                            Name.root),
                    Section.ANSWER);
        }
        return true;
    }

    @Override
    public boolean connection(Message response, Name msg) throws IOException {
        response.addRecord(
                Record.fromString(msg, Type.AAAA, DClass.IN, 86400, "0:0:0:0:0:0:0:0", Name.root),
                Section.ANSWER
        );
        return true;
    }
}
