package DataAccessLayer.NotificationsSystemDAOs;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Mailbox;
import BusinessLayer.NotificationSystem.NotificationHub;
import BusinessLayer.NotificationSystem.StoreMailbox;
import BusinessLayer.NotificationSystem.UserMailbox;
import BusinessLayer.Receipts.Pairs.ItemsPair;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import BusinessLayer.Stores.Store;
import BusinessLayer.Users.Guest;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.User;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationHubDAO {
    ConnectorConfigurations config;
    public NotificationHubDAO() {
        config = Market.getConfigurations();
    }

    private DBConnector<Mailbox> mailboxDBConnector() {
        return new DBConnector<>(Mailbox.class, config);
    }

    private DBConnector<StoreMailbox> storeMailboxDBConnector() {
        return new DBConnector<>(StoreMailbox.class, config);
    }

    private DBConnector<UserMailbox> userMailboxDBConnector() {
        return new DBConnector<>(UserMailbox.class, config);
    }

    public ConcurrentHashMap<Integer, Mailbox> loadMailboxes(NotificationHub hub) {

        List<Mailbox> mailboxList = mailboxDBConnector().getAll();

        ConcurrentHashMap<Integer, Mailbox> mailboxes = hub.getMailboxes();

        for(Mailbox mailbox : mailboxList){
            if (mailbox.getOwnerID()> Guest.MAX_GUEST_USER_ID)
                mailboxes.put(mailbox.getOwnerID(), mailbox);
        }
        return mailboxes;
    }

    public void registerToMailService(UserMailbox mailbox) {
        userMailboxDBConnector().insert(mailbox);
    }

    public void registerToMailService(StoreMailbox mailbox) {
        storeMailboxDBConnector().insert(mailbox);
    }

    public void removeFromService(Mailbox mailbox){
//        DBConnector<RegisteredUser> userDBConnector = new DBConnector<>(RegisteredUser.class, config);
//        userDBConnector.delete(mailbox.getOwnerID());
//
//        DBConnector<Store> storeDBConnector = new DBConnector<>(Store.class, config);
//        storeDBConnector.delete(mailbox.getOwnerID());
//
//        mailboxDBConnector().delete(mailbox.getOwnerID());
    }
}
