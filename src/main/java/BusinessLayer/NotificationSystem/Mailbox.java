package BusinessLayer.NotificationSystem;

import BusinessLayer.Log;
import DataAccessLayer.NotificationsSystemDAOs.MailboxDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Mailbox {
    @Id
    protected int ownerID;
    protected boolean available;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "mailboxOwner")
    protected List<Chat> chats; // <otherSideId, Chat>

    @Transient
    protected NotificationHub hub;

    @Transient
    protected MailboxDAO mailboxDAO;


//    protected NotReadMessagesRepository notReadMessages;
//    protected ReadMessagesRepository readMessages;
//    protected SentMessagesRepository sentMessages;

    public void sendMessage(int receiverID, String content){
        Message message = new Message(ownerID, receiverID, content);
//        boolean newChat = false;

        try{
            Chat chat = chats_searchChat(receiverID);

            if(chat == null){
                chat = new Chat(ownerID, receiverID);
                chats.add(chat);
                mailboxDAO.sendMessage(this, chat/*, message, newChat*/);
//                newChat = true;
            }

            chat.addMessage(message);

            hub.passMessage(message);

//            mailboxDAO.sendMessage(this, chat, message, newChat);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            // LOG: ERROR: Mailbox::sendMessage:e.getMessage()
            Log.log.severe("ERROR: Mailbox::sendMessage: " + e.getMessage());
            return;
        }

        //sentMessages.add(message);
    }

    public void receiveMessage(Message _message) throws Exception{
        if(_message == null){
            Log.log.warning("ERROR: Mailbox::receiveMessage: the given message is null");
            throw new Exception("Mailbox::receiveMessage: the given message is null");
        }

        if(ownerID != _message.getReceiverID()){
            Log.log.severe("ERROR: Mailbox::receiveMessage: A message for "
                    + _message.getReceiverID()
                    + "was sent to " + ownerID);
            throw new Exception("Mailbox::receiveMessage: A message for " + _message.getReceiverID() +
                    "was sent to " + ownerID);
        }

        Message message = new Message(_message);

        Chat chat = chats_searchChat(message.getSenderID());
//        boolean newChat = false;

        if(chat == null){
            chat = new Chat(ownerID, message.getSenderID());
            chats.add(chat);
            mailboxDAO.receiveMessage(this, chat/*, message, newChat*/);
//            newChat = true;
        }

        chat.addMessage(message);

//        mailboxDAO.receiveMessage(this, chat/*, message, newChat*/);
        //notReadMessages.add(message);
        notifyOwner();
    }

    /**
     * notify the owner of the mailbox about a new message,
     * using observer pattern
     */
    abstract public void notifyOwner() throws Exception;

//    public void markMessageAsRead(Message message) throws Exception {
//
//        if(message == null || !notReadMessages.contains(message)){
//            Log.log.warning("ERROR: Mailbox::markMessageAsRead: given message is invalid");
//            throw new Exception("Mailbox::markMessageAsRead: given message is invalid");
//        }
//
//        notReadMessages.remove(message);
//        readMessages.add(message);
//
//    }
//
//    public void markMessageAsNotRead(Message message) throws Exception {
//
//        if(message == null || !readMessages.contains(message)){
//            Log.log.warning("ERROR: Mailbox::markMessageAsNotRead: given message is invalid");
//            throw new Exception("Mailbox::markMessageAsNotRead: given message is invalid");
//        }
//
//        readMessages.remove(message);
//        notReadMessages.add(message);
//
//    }
//
//    public List<Message> watchNotReadMessages(){
//        return new ArrayList<>(notReadMessages.getMessages());
//    }
//
//    public List<Message> watchReadMessages(){
//        return new ArrayList<>(readMessages.getMessages());
//    }
//
//    public List<Message> watchSentMessages(){
//        return new ArrayList<>(sentMessages.getMessages());
//    }

    public ConcurrentHashMap<Integer, Chat> getChatsAsMap(){
        ConcurrentHashMap<Integer, Chat> _chats = new ConcurrentHashMap<>();
        for(Chat chat : chats){
            _chats.putIfAbsent(chat.getOtherSideId(), chat);
        }

        return _chats;
    }

    public void setMailboxAsUnavailable() throws Exception {
        available = false;
        mailboxDAO.setMailboxAvailability(this);

        Log.log.info("The mailbox of " + ownerID + " was marked as unavailable.");
    }

    public void setMailboxAsAvailable() throws Exception {
        available = true;
        mailboxDAO.setMailboxAvailability(this);

        Log.log.info("The mailbox of " + ownerID + " was marked as available.");
    }

    public boolean isAvailable(){
        return available;
    }

    protected Chat chats_searchChat(int receiverId){
        for(Chat chat : chats){
            if(chat.getOtherSideId() == receiverId){
                return chat;
            }
        }

        return null;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }


}
