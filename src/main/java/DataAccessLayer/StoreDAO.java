package DataAccessLayer;

import BusinessLayer.Market;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//DB mock
public class StoreDAO {
    ConnectorConfigurations config;
    public StoreDAO() {
        config = Market.getConfigurations();
    }
    public DBConnector<Store> getStoreConnector() {
        return new DBConnector<>(Store.class, config);
    }

    public DBConnector<CatalogItem> getCatalogConnector() {
        return new DBConnector<>(CatalogItem.class, config);
    }

    public void addStore(Store store) {
        getStoreConnector().insert(store);
    }

    public void removeUser(Store store) throws Exception {
        getStoreConnector().delete(store.getStoreID());
    }

    public Map<Integer, Store> getStores() {
        Map<Integer, Store> storeMap = new HashMap<>();
        List<Store> stores = getStoreConnector().getAll();
        for (Store store : stores) {
            storeMap.put(store.getStoreID(), store);
        }
        return storeMap;
    }

    public void addItem(CatalogItem newItem) {
        getCatalogConnector().insert(newItem);
    }
    public void removeItem(CatalogItem item) {
        getCatalogConnector().delete(item.getItemID());
    }

    public List<CatalogItem> getItems() {
        return getCatalogConnector().getAll();
    }

    public void updateItemName(CatalogItem item){
        getCatalogConnector().saveState(item);
    }

    public void save(Store newStore) {
        getStoreConnector().saveState(newStore);
    }
}
