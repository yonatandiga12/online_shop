package BusinessLayer.NotificationSystem;

import DataAccessLayer.NotificationsSystemDAOs.ChatDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int mySideId;
    private int otherSideId;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "chatId")
    private List<Message> messages;

    @Transient
    private ChatDAO dao;

    public Chat(int _mySideId, int _otherSideId){
        mySideId = _mySideId;
        otherSideId = _otherSideId;
        messages = new ArrayList<>();
        dao = new ChatDAO();
    }

    public Chat(){
        dao = new ChatDAO();
    }

    public int getMySideId(){
        return mySideId;
    }

    public int getOtherSideId(){
        return otherSideId;
    }

    public List<Message> getMessages(){
        return new ArrayList<>(messages);
    }

    public void addMessage(Message message) throws Exception {
        messages.add(message);
        dao.addMessage(this, message);
    }

    public boolean contains(Message message){
        for(Message ms : messages){
            if(ms.equals(message)){
                return true;
            }
        }

        return false;
    }

    public void setMySideId(int mySideId) {
        this.mySideId = mySideId;
    }

    public void setOtherSideId(int otherSideId) {
        this.otherSideId = otherSideId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
