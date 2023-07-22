package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Message;

import java.util.ArrayList;
import java.util.List;


/**
 * NOT USED!
 * FOR NOW, THE CLASS WILL STAY FOR ROLL-BACK OPTION.
 */
public class NotReadMessagesRepository {
    private List<Message> notReadMessages;
    //TODO: add DAO in future versions

    public NotReadMessagesRepository(){
        notReadMessages = new ArrayList<>();
    }

    public void add(Message message){
        notReadMessages.add(message);
        //TODO: add to DAO
    }

    public List<Message> getMessages(){
        return notReadMessages;
    }

    public void remove(Message message){
        notReadMessages.remove(message);
        //TODO: DAO
    }

    public boolean contains(Message message){
        for(Message m : notReadMessages){
            if(m.equals(message)){
                return true;
            }
        }
        return false;
        //TODO: DAO
    }



}
