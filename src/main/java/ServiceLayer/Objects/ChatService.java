package ServiceLayer.Objects;

import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.Message;
import BusinessLayer.NotificationSystem.Repositories.MessageRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatService {
    private final int mySideId;
    private final int otherSideId;
    private final String myName;
    private final String otherName;
    private final List<MessageService> messages;

    public ChatService(Chat chat, String _myName, String _otherName){
        mySideId = chat.getMySideId();
        otherSideId = chat.getOtherSideId();
        myName = _myName;
        otherName = _otherName;
        messages = new ArrayList<>();
        copyMessages(chat);
    }

    private void copyMessages(Chat chat){
        for(Message m : chat.getMessages()){
            messages.add(new MessageService(m));
        }
    }

    public List<MessageService> getMessages(){
        return messages;
    }

    public int getOtherSideId(){
        return otherSideId;
    }

    public String getMyName(){
        return myName;
    }

    public String getOtherName(){
        return otherName;
    }
}
