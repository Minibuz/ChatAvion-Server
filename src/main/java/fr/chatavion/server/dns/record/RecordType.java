package fr.chatavion.server.dns.record;

import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;

import java.io.IOException;

public interface RecordType {

    void sendHistorique(Message response, Name msg, byte[] rsp) throws IOException;

    void connection(Message response, Name msg) throws IOException;

    static void sendHistorique(int type, Message response, Name msg, byte[] rsp) throws IOException {
        switch (type) {
            case Type.A -> new RecordA().sendHistorique(response, msg, rsp);
            case Type.AAAA -> new RecordAAAA().sendHistorique(response, msg, rsp);
            case Type.TXT -> new RecordTXT().sendHistorique(response, msg, rsp);
        }
    }

    static void typeConnection(int type, Message response, Name msg) throws IOException {
        switch (type) {
            case Type.A -> new RecordA().connection(response, msg);
            case Type.AAAA -> new RecordAAAA().connection(response, msg);
            case Type.TXT -> new RecordTXT().connection(response, msg);
        }
    }
}
