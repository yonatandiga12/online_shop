package BusinessLayer.NotificationSystem.Repositories;

import BusinessLayer.NotificationSystem.Mailbox;

import java.util.concurrent.ConcurrentHashMap;


//TODO: use DAO in future versions
public class MailboxesRepository {
    private final ConcurrentHashMap<Integer, Mailbox> mailboxes; // <ID, Mailbox>

    public MailboxesRepository(){
        mailboxes = new ConcurrentHashMap<>();
    }

    public void putIfAbsent(int userID, Mailbox mailbox){
        mailboxes.putIfAbsent(userID, mailbox);
    }

    public void remove(int ID){
        mailboxes.remove(ID);
    }

    public boolean containsKey(int key){
        return mailboxes.containsKey(key);
    }

    public Mailbox get(int userID){
        return mailboxes.get(userID);
    }
}
