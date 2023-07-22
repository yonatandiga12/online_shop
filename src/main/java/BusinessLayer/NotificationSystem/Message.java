package BusinessLayer.NotificationSystem;

import ServiceLayer.Objects.MessageService;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int senderID;
    private int receiverID;
    private String content;
    private LocalDateTime sendingTime;

    public Message(int _senderID, int _receiverID, String _content){
        senderID = _senderID;
        receiverID = _receiverID;
        content = _content;
        sendingTime = LocalDateTime.now();
    }

    public Message(MessageService message){
        senderID = message.getSenderID();
        receiverID = message.getReceiverID();
        content = message.getContent();
        sendingTime = message.getSendingTime();
    }

    public Message(){

    }

    public Message(Message other){
        senderID = other.senderID;
        receiverID = other.receiverID;
        content = other.content;
        sendingTime = other.sendingTime;
    }

    public int getSenderID(){
        return senderID;
    }

    public int getReceiverID(){
        return receiverID;
    }

    public String getContent(){
        return content;
    }

    public String getDateAsString(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"); // NOTICE: the format may cause problems, if so, try dd-MM-yyyy, HH:mm
        return sendingTime.format(format);
    }

    public LocalDateTime getSendingTime(){return sendingTime;}

    public int getId(){
        return id;
    }

    public void setId(int _id){
        id = _id;
    }

    public void setSenderID(int id){
        senderID = id;
    }

    public void setReceiverID(int id){
        receiverID = id;
    }

    public void setContent(String _content){
        content = _content;
    }

    public void setSendingTime(LocalDateTime time){
        sendingTime = time;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof Message other)){
            return false;
        }

        return senderID == other.senderID
                && receiverID == other.receiverID
                && content.equals(other.content)
                && sendingTime.equals(other.sendingTime);
    }

}
