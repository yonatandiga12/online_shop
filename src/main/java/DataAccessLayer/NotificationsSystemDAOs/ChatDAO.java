package DataAccessLayer.NotificationsSystemDAOs;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.Message;
import DataAccessLayer.Hibernate.DBConnector;

public class ChatDAO {

    public ChatDAO(){

    }

    public void addMessage(Chat chat, Message message) throws Exception {
        DBConnector<Message> messageConnector =
                new DBConnector<>(Message.class, Market.getConfigurations());
        messageConnector.insert(message);

        DBConnector<Chat> chatConnector =
                new DBConnector<>(Chat.class, Market.getConfigurations());
        chatConnector.saveState(chat);
    }


}
