package BusinessLayer.StorePermissions;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "storeManagers")
public class StoreManager extends StoreEmployees {
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "storeActionPermissions")
    @Column(name = "permission")
    private Set<StoreActionPermissions> storeActionPermissions;

    public StoreManager() {
        super();
    }

    public StoreManager(int id, StoreOwner storeOwnerShip) {
        super(id, storeOwnerShip.getUserID(), storeOwnerShip.getStore());
        storeActionPermissions = new HashSet<>();
        storeActionPermissions.add(StoreActionPermissions.INVENTORY);
    }

    public Set<StoreActionPermissions> getStoreActionPermissions() {
        return storeActionPermissions;
    }

    public void addPermission(List<StoreActionPermissions> permission) {
        this.storeActionPermissions.addAll(permission);
    }

    public void removePermission(List<StoreActionPermissions> permission) {
        this.storeActionPermissions.removeAll(permission);
    }

    public boolean hasPermission(StoreActionPermissions permission) {
        return this.storeActionPermissions.contains(permission);
    }
}
