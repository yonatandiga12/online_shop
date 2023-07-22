package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * NOT USED!
 * FOR NOW, THE CLASS WILL STAY FOR ROLL-BACK OPTION.
 */
public class ReadMessagesRepository {
    private List<Message> readMessages;

    public ReadMessagesRepository(){
        readMessages = new ArrayList<>();
    }

    public void add(Message message){
        readMessages.add(message);
        //TODO: add to DAO
    }

    public List<Message> getMessages(){
        return readMessages;
    }

    public void remove(Message message){
        readMessages.remove(message);
        //TODO: DAO
    }

    public boolean contains(Message message){
        for(Message m : readMessages){
            if(m.equals(message)){
                return true;
            }
        }
        return false;
        //TODO: DAO
    }



}
