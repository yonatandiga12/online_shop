package BusinessLayer.NotificationSystem;


import BusinessLayer.Log;
import BusinessLayer.NotificationSystem.Repositories.MailboxesRepository;
import BusinessLayer.Stores.Store;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.User;
import DataAccessLayer.NotificationsSystemDAOs.NotificationHubDAO;

import java.util.concurrent.ConcurrentHashMap;


/**
 * In order to register to the notification system:
 * Market.getInstance().getNotificationHub().registerToMailService(this);
 */
public class NotificationHub {

    //private static final Object instanceLock = new Object();
    // class attributes
    //private static NotificationHubImpl instance = null;

    // fields
    //private final ConcurrentHashMap<Integer, Mailbox> mailboxes; // <ID, Mailbox>
    private ConcurrentHashMap<Integer, Mailbox> mailboxes; // <ID, Mailbox>
    private final NotificationHubDAO dao;


    // object methods
    public NotificationHub() throws Exception {
        mailboxes = new ConcurrentHashMap<>();
        dao = new NotificationHubDAO();

        Log.log.info("The notification hub has started successfully.");
    }

    public void loadHub(){
        try{
            mailboxes = dao.loadMailboxes(this);
        }
        catch(Exception e){
            System.err.println("ERROR: notification hub load failed!");
            System.err.println(e.getMessage());
        }
    }
    // class methods
//    public static NotificationHubImpl getInstance() {
//        synchronized (instanceLock) {
//            if (instance == null) {
//                instance = new NotificationHubImpl();
//            }
//        }
//        return instance;
//    }

    public ConcurrentHashMap<Integer, Mailbox> getMailboxes() {
        return mailboxes;
    }

    public UserMailbox registerToMailService(int userID, User user) throws Exception {
        if (isRegistered(userID)) {
            Log.log.severe("ERROR: NotificationHub::registerToMailService: the user " + userID + " is already registered!");
            throw new Exception("NotificationHub::registerToMailService: the user " + userID + " is already registered!");
        }
        UserMailbox mailbox = new UserMailbox(user, this);
        mailboxes.putIfAbsent(userID, mailbox);
        dao.registerToMailService(mailbox);

        Log.log.info("NotificationHub::registerToMailService: user "
                + user.getId() + " is registered to notification service");
        return mailbox;
    }

    public StoreMailbox registerToMailService(Store store) throws Exception {
        int storeID = store.getStoreID();
        if (isRegistered(storeID)) {
            Log.log.warning("NotificationHub::registerToMailService: the store " + storeID + " is already registered!");
            throw new Exception("NotificationHub::registerToMailService: the store " + storeID + " is already registered!");
        }
        StoreMailbox mailbox = new StoreMailbox(store, this);
        mailboxes.putIfAbsent(storeID, mailbox);

        dao.registerToMailService(mailbox);

        Log.log.info("NotificationHub::registerToMailService: store " + store.getStoreID()
                + " is registered to notification service");
        return mailbox;
    }

    public void removeFromService(int ID) throws Exception {
        if (!isRegistered(ID)) {
            Log.log.warning("NotificationHub::removeFromService: the given ID " + ID + " is not of a registered user or store!");
            throw new Exception("NotificationHub::removeFromService: the given ID " + ID + " is not of a registered user or store!");
        }

        Mailbox mailbox = mailboxes.remove(ID);
        dao.removeFromService(mailbox);
    }

    public boolean isRegistered(int ID) {
        return mailboxes.containsKey(ID);
    }

    public void passMessage(Message message) throws Exception {
        validatePassedMessage(message);

        Mailbox mailbox = mailboxes.get(message.getReceiverID());
        mailbox.receiveMessage(message);

        Log.log.info("A message passed from " + message.getSenderID() + " to " + message.getReceiverID());
    }

    private void validatePassedMessage(Message message) throws Exception {
        if (message == null) {
            throw new Exception("NotificationHub::passMessage: given message is null");
        }

        if (!isRegistered(message.getSenderID())) {
            throw new Exception("NotificationHub::passMessage: the sender is not registered!");
        }

        if (!isRegistered(message.getReceiverID())) {
            throw new Exception("NotificationHub::passMessage: the receiver is not registered!");
        }

//        if (message.getTitle() == null || message.getTitle().isBlank()) {
//            throw new Exception("NotificationHub::passMessage: the title of the message is invalid!");
//        }

        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new Exception("NotificationHub::passMessage: the content of the message is invalid!");
        }
    }

}
