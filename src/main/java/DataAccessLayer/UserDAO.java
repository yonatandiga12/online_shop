package DataAccessLayer;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Mailbox;
import BusinessLayer.Receipts.ReceiptHandler;
import BusinessLayer.Users.Guest;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.UserFacade;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//DB mock
public class UserDAO {
    private static HashMap<Integer, RegisteredUser> userMap = new HashMap<>();
    private static UserDAO instance;
    ConnectorConfigurations config;

    private UserDAO() {
        config = Market.getConfigurations();
    }
    private DBConnector<RegisteredUser> getConnector() {
        return new DBConnector<>(RegisteredUser.class, config);
    }

    private DBConnector<Guest> getGuestConnector() {
        return new DBConnector<>(Guest.class, config);
    }

    public static synchronized UserDAO getUserDao() {
        if (instance==null) {
            instance = new UserDAO();
        }
        return instance;
    }

    public void removeUser(RegisteredUser user) throws Exception {
        getConnector().delete(user.getId());
        if (userMap.remove(user.getId()) == null)
            throw new Exception("Fail to remove user in UserDAO");
    }

    public void removeManagership(RegisteredUser user) throws Exception {
        getConnector().saveState(user);
    }

    public void removeOwnership(RegisteredUser user) {
        getConnector().saveState(user);
    }

    public Map<Integer, RegisteredUser> getUsers() {
        List<RegisteredUser> users = getConnector().getAll();
        Map<Integer, RegisteredUser> res = new HashMap<>();
        for (RegisteredUser user : users) {
            res.put(user.getId(), user);
        }
        return res;
    }

    public void save(RegisteredUser user) throws Exception {
        getConnector().saveState(user);
    }

    public void save(Guest user) throws Exception {
        getGuestConnector().saveState(user);
    }

    public void addUser(RegisteredUser user){
        DBConnector<ReceiptHandler> handlerDBConnector = new DBConnector<>(ReceiptHandler.class, config);
        handlerDBConnector.insert(user.getReceiptHandler());
        DBConnector<Mailbox> mailboxDBConnector = new DBConnector<>(Mailbox.class, config);
        getConnector().insert(user);
    }
}
