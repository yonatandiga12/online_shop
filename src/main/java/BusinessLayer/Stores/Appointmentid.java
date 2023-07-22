package BusinessLayer.Stores;

import java.io.Serializable;

public class Appointmentid implements Serializable {
    private int creatorId;
    private int storeId;
    private int newOwnerId;

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(int newOwnerId) {
        this.newOwnerId = newOwnerId;
    }
}
