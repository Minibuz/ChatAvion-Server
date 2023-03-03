package fr.chatavion.server.networking.tcp.dto;

import java.util.Objects;

public class Message {

    private final int id;
    private final String user;
    private final String msg;

    public Message(int id, String user, String message) {
        this.id = id;
        this.user = user;
        this.msg = message;
    }

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return id == message1.id && Objects.equals(user, message1.user) && Objects.equals(msg, message1.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, msg);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", message='" + msg + '\'' +
                '}';
    }
}
