package DataAccessLayer;

import BusinessLayer.Market;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.HashMap;
import java.util.Map;

//DB mock
public class ItemDAO {
    ConnectorConfigurations config;

    public ItemDAO() {
        try {

            config = Market.getConfigurations();
        }
        catch (Exception e) {

        }
    }

    public DBConnector<CatalogItem> getItemDBConnector() {
        return new DBConnector<>(CatalogItem.class, config);
    }
    public void addItem(CatalogItem newItem) {
        getItemDBConnector().insert(newItem);
    }
    public void removeItem(CatalogItem item) {
        getItemDBConnector().delete(item.getItemID());
    }
    public void save(CatalogItem item) {
        getItemDBConnector().saveState(item);
    }
}
