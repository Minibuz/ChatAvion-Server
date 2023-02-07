package fr.chatavion.server.dns.error;

public class DnsException extends Exception {

    public DnsException(String errorMessage) {
        super(errorMessage);
    }
}
