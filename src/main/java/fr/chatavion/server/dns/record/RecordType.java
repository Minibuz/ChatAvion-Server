package fr.chatavion.server.dns.record;

import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;

import java.io.IOException;

public interface RecordType {

    boolean sendHistorique(Message response, Name msg, byte[] rsp) throws IOException;

    boolean connection(Message response, Name msg) throws IOException;

    static boolean sendHistorique(int type, Message response, Name msg, byte[] rsp) throws IOException {
        return switch (type) {
            case Type.A -> new RecordA().sendHistorique(response, msg, rsp);
            case Type.AAAA -> new RecordAAAA().sendHistorique(response, msg, rsp);
            case Type.TXT -> new RecordTXT().sendHistorique(response, msg, rsp);
            default -> false;
        };
    }

    static boolean typeConnection(int type, Message response, Name msg) throws IOException {
        return switch (type) {
            case Type.A -> new RecordA().connection(response, msg);
            case Type.AAAA -> new RecordAAAA().connection(response, msg);
            case Type.TXT -> new RecordTXT().connection(response, msg);
            default -> false;
        };
    }
}
