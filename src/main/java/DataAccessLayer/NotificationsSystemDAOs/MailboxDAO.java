package DataAccessLayer.NotificationsSystemDAOs;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.Mailbox;
import BusinessLayer.NotificationSystem.Message;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

public class MailboxDAO {
    ConnectorConfigurations config;

    public MailboxDAO() throws Exception {
        config = Market.getConfigurations();
    }

    public void sendMessage(Mailbox mailbox, Chat chat/*, Message message, boolean newChat*/) throws Exception {
        addMessage(mailbox, chat/*, message, newChat*/);
    }

    public void receiveMessage(Mailbox mailbox, Chat chat/*, Message message, boolean newChat*/) throws Exception {
        addMessage(mailbox, chat/*, message, newChat*/);
    }

    private void addMessage(Mailbox mailbox, Chat chat/*, Message message, boolean newChat*/) throws Exception{
//        DBConnector<Message> messageConnector =
//                new DBConnector<>(Message.class, Market.getInstance().getConfigurations());
//        messageConnector.insert(message);
//
//        DBConnector<Chat> chatConnector =
//                new DBConnector<>(Chat.class, Market.getInstance().getConfigurations());
//
//        if(newChat){
//            chatConnector.insert(chat);
//        }
//        else{
//            chatConnector.saveState(chat);
//        }
//
//        DBConnector<Mailbox> mailboxConnector =
//                new DBConnector<>(Mailbox.class, Market.getInstance().getConfigurations());
//        mailboxConnector.saveState(mailbox);

        DBConnector<Chat> chatConnector =
                new DBConnector<>(Chat.class, Market.getConfigurations());
        chatConnector.insert(chat);

        DBConnector<Mailbox> mailboxConnector =
                new DBConnector<>(Mailbox.class, Market.getConfigurations());
        mailboxConnector.saveState(mailbox);

    }

    public void setMailboxAvailability(Mailbox mailbox) throws Exception {
        DBConnector<Mailbox> mailboxConnector =
                new DBConnector<>(Mailbox.class, Market.getConfigurations());
        mailboxConnector.saveState(mailbox);
    }


}
