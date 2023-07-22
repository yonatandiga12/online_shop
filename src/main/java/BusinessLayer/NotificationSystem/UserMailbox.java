package BusinessLayer.NotificationSystem;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Observer.NotificationObserver;
import BusinessLayer.Users.User;
import DataAccessLayer.NotificationsSystemDAOs.MailboxDAO;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
public class UserMailbox extends Mailbox {
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ownerID")
    @MapsId
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;
    @Transient
    private List<NotificationObserver> listeners;


    public UserMailbox(User _owner, NotificationHub _hub) throws Exception {
        owner = _owner;
        ownerID = owner.getId();
        chats = new ArrayList<>();
        hub = _hub;
        listeners = new ArrayList<>();

        mailboxDAO = new MailboxDAO();


//        notReadMessages = new NotReadMessagesRepository();
//        readMessages = new ReadMessagesRepository();
//        sentMessages = new SentMessagesRepository();
    }

    public UserMailbox() throws Exception {
        mailboxDAO = new MailboxDAO();
        listeners = new ArrayList<>();
        try{
            hub = Market.getInstance().getNotificationHub();
        }
        catch(Exception e){
            System.err.println("Error in UserMailbox empty constructor");
            System.err.println(e.getMessage());
        }
    }

    public void listen(NotificationObserver _listener){
        listeners.add(_listener);
    }

    @Override
    public void notifyOwner() {
        for(NotificationObserver listener : listeners){
            listener.notify("A new message is waiting for you!");
        }
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
