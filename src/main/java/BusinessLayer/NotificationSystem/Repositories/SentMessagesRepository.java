package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * NOT USED!
 * FOR NOW, THE CLASS WILL STAY FOR ROLL-BACK OPTION.
 */
public class SentMessagesRepository {
    protected List<Message> sentMessages;

    public SentMessagesRepository(){
        sentMessages = new ArrayList<>();
    }

    public void add(Message message){
        sentMessages.add(message);
        //TODO: add to DAO
    }

    public List<Message> getMessages(){
        return sentMessages;
    }

    public void remove(Message message){
        sentMessages.remove(message);
        //TODO: DAO
    }

    public boolean contains(Message message){
        for(Message m : sentMessages){
            if(m.equals(message)){
                return true;
            }
        }
        return false;
        //TODO: DAO
    }

}
