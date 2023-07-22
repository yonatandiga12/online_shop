package DataAccessLayer;

import BusinessLayer.Market;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Users.RegisteredUser;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.HashMap;

//DB mock
public class StoreEmployeesDAO {
    ConnectorConfigurations config;

    public StoreEmployeesDAO() {
        config = Market.getConfigurations();
    }

    private DBConnector<StoreOwner> getSOConnector() {
        try {
            return new DBConnector<>(StoreOwner.class, config);
        }
        catch (Exception e) {
            return null;
        }
    }

    private DBConnector<StoreManager> getSMConnector() {
        return new DBConnector<>(StoreManager.class, config);

    }

    public void addOwner(StoreOwner owner) {
        getSOConnector().saveState(owner);
    }

    public void removeOwner(StoreOwner owner) {
        getSOConnector().delete(owner.getUserID()); //TODO Change
    }

    public void removeOwnership(StoreOwner owner) {
        getSOConnector().saveState(owner);
    }

    public void addManager(StoreManager manager) {
        getSMConnector().saveState(manager);
    }

    public void removeManager(StoreManager manager) {
        getSMConnector().delete(manager.getUserID()); //TODO Change
    }

    public void removeManagership(StoreManager manager) {
        getSMConnector().saveState(manager);
    }

    public void save(StoreOwner storeOwner) {
        getSOConnector().saveState(storeOwner);
    }

    public void save(StoreManager storeOwner) {
        getSMConnector().saveState(storeOwner);
    }
}
