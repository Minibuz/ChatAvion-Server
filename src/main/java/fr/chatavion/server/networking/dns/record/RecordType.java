package fr.chatavion.server.networking.dns.record;

import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;

import java.io.IOException;

public interface RecordType {

    /**
     * Fill a message response along with some additional information.
     *
     * @param response
     *          Response getting filled to be sent to the client
     * @param msg
     *          Name containing the request in the url
     * @param rsp
     *          The byte array to add to the response
     * @return
     *          True
     * @throws IOException
     */
    boolean sendHistorique(Message response, Name msg, byte[] rsp) throws IOException;

    /**
     *
     *
     * @param response
     *          Response getting filled to be sent to the client
     * @param msg
     *          Name containing the request in the url
     * @param id
     *          Id of the last message of the community
     * @return
     *          True
     * @throws IOException
     */
    boolean connection(Message response, Name msg, int id) throws IOException;

    static boolean sendHistorique(int type, Message response, Name msg, byte[] rsp) throws IOException {
        return switch (type) {
            case Type.A -> new RecordA().sendHistorique(response, msg, rsp);
            case Type.AAAA -> new RecordAAAA().sendHistorique(response, msg, rsp);
            case Type.TXT -> new RecordTXT().sendHistorique(response, msg, rsp);
            default -> false;
        };
    }

    static boolean typeConnection(int type, Message response, Name msg, int id) throws IOException {
        return switch (type) {
            case Type.A -> new RecordA().connection(response, msg, id);
            case Type.AAAA -> new RecordAAAA().connection(response, msg, id);
            case Type.TXT -> new RecordTXT().connection(response, msg, id);
            default -> false;
        };
    }
}
