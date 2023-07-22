package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Message;

import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageRepository {
    private final ConcurrentLinkedDeque<Message> messages;

    public MessageRepository(){
        messages = new ConcurrentLinkedDeque<>();
    }

    public ConcurrentLinkedDeque<Message> getMessages(){
        return messages;
    }

    public void add(Message message){
        messages.add(message);
    }

}
