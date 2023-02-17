package fr.chatavion.server.networking.tcp.service;

import fr.chatavion.server.networking.tcp.dto.Message;

import java.util.List;

public interface HistoryInterface {

    List<Message> retrieveMessagesFromHistory(int idStart, String cmtName, int amount);

}
