package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Chat;

import java.util.concurrent.ConcurrentHashMap;

public class ChatRepository {

    protected ConcurrentHashMap<Integer, Chat> chats; // <otherSideId, Chat>

    public ChatRepository(){
        chats = new ConcurrentHashMap<>();
    }

    public void putIfAbsent(int otherSideId, Chat chat){
        chats.putIfAbsent(otherSideId, chat);
    }

    public Chat get(int otherSideId){
        return chats.get(otherSideId);
    }

    public ConcurrentHashMap<Integer, Chat> getChats(){
        return chats;
    }

}
