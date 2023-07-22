package ServiceLayer.Objects;

import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreService {

    public final int founderID;
    public final String storeName;
    public final int storeID;
    public final StoreStatus storeStatus;
    private Map<CatalogItemService, Integer> items = new HashMap<>();
    private Map<Integer, Integer> managers; //id -> person who defined me
    private Map<Integer, Integer> owners; //id -> person who defined me

    public StoreService(Store store) {
        storeID = store.getStoreID();
        storeName = store.getStoreName();
        founderID = store.getFounderID();
        storeStatus = store.getStoreStatus();
        managers = new HashMap<>();
        owners = new HashMap<>();
        setManagersAndOwners(store);
        loadItemsAndAmountsFromStore(store);

    }

    private void setManagersAndOwners(Store store) {
        for (StoreManager manager : store.getStoreManagers()) {
            managers.put(manager.getUserID(), manager.getParentID());
        }
        for (StoreOwner owner : store.getStoreOwners()) {
            owners.put(owner.getUserID(), owner.getParentID());
        }
    }

    public int getFounderID() {
        return founderID;
    }

    public Map<Integer, Integer> getManagers() {
        return managers;
    }

    public Map<Integer, Integer> getOwners() {
        return owners;
    }

    private Map<CatalogItemService, Integer> loadItemsAndAmountsFromStore(Store store) {
        Map<CatalogItem, Boolean> itemsInStore = store.getCatalog();
        for (Map.Entry<CatalogItem, Boolean> itemEntry : itemsInStore.entrySet()) {
            CatalogItemService item = new CatalogItemService(itemEntry.getKey(), itemEntry.getValue());
            int amount = 0;
            if (itemEntry.getValue()) {
                amount = store.getItemAmount(item.getItemID());
                item.setAmount(amount);
            }
            items.put(item, amount);
        }
        return items;
    }

    public boolean hasItem(int itemId) {
        return false;
    }

    public int getStoreId() {
        return storeID;
    }

    public CatalogItemService getItem(int id) {
        for (CatalogItemService catalogItemService : items.keySet()) {
            if (catalogItemService.getItemID() == id)
                return catalogItemService;
        }
        return null;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreStatus() {
        switch (storeStatus) {
            case OPEN -> {
                return "Open";
            }
            case CLOSE -> {
                return "Close";
            }
            case PERMANENTLY_CLOSE -> {
                return "Permanently closed";
            }
        }
        return "";
    }

    public List<CatalogItemService> getItems() {
        return new ArrayList<>(items.keySet());
    }


}
