package BusinessLayer.StorePermissions;

import BusinessLayer.Stores.Store;

import javax.persistence.*;

@MappedSuperclass
public abstract class StoreEmployees {
    @EmbeddedId
    private StoreEmployeeId id;
    private int parentID;
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;
    public StoreEmployees(int userID, int parentID, Store store) {
        this.parentID = parentID;
        this.store = store;
        this.id = new StoreEmployeeId(userID, store.getStoreID());
    }

    public StoreEmployees() {

    }

    public int getUserID() {
        return id.getUser();
    }

    public void setUserID(int userID) {
        id.setUser(userID);
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public int getStoreID() {
        return store.getStoreID();
    }


    public abstract boolean hasPermission(StoreActionPermissions permission);

}
