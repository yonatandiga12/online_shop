package BusinessLayer.StorePermissions;

import BusinessLayer.Stores.Store;
import BusinessLayer.Users.RegisteredUser;
import DataAccessLayer.StoreEmployeesDAO;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "storeOwners")
public class StoreOwner extends StoreEmployees {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "storeowners_ownersDefined")//,
    private Set<RegisteredUser> ownersIDefined;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "storeowners_managersDefined")
    private Set<RegisteredUser> managersIDefined;
    @Transient
    private StoreEmployeesDAO employeesDAO;
    /*
    founder calls this constructor
     */
    public StoreOwner(int userID, Store store) throws Exception {
        super(userID, userID, store);
        store.addOwner(this);
        this.ownersIDefined = new HashSet<>();
        this.managersIDefined = new HashSet<>();
        this.employeesDAO = new StoreEmployeesDAO();
    }

    public StoreOwner(int userID, StoreOwner parentStoreOwnership) throws Exception {
        super(userID, parentStoreOwnership.getUserID(), parentStoreOwnership.getStore());
        this.ownersIDefined = new HashSet<>();
        this.managersIDefined = new HashSet<>();
        this.employeesDAO = new StoreEmployeesDAO();
    }

    public StoreOwner() throws Exception {
        super();
        this.employeesDAO = new StoreEmployeesDAO();
    }

    public Set<RegisteredUser> getOwnersIDefined() {
        return ownersIDefined;
    }

    public void setOwnersIDefined(Set<RegisteredUser> ownersIDefined) {
        this.ownersIDefined = ownersIDefined;
    }

    public Set<RegisteredUser> getManagersIDefined() {
        return managersIDefined;
    }

    public void setManagersIDefined(Set<RegisteredUser> managersIDefined) {
        this.managersIDefined = managersIDefined;
    }

    public boolean isFounder() {
        return this.getUserID() == this.getParentID();
    }

    public void addOwner(RegisteredUser newOwner) throws Exception {
        ownersIDefined.add(newOwner);
        StoreOwner owner = newOwner.addStoreOwnership(this);
        this.getStore().addOwner(owner);
        employeesDAO.save(this);
    }

    public void addManager(RegisteredUser newManager) {
        managersIDefined.add(newManager);
        employeesDAO.save(this);
        newManager.addStoreManagership(this);
        this.getStore().addManager(newManager.getStoreIManage(getStoreID()));
    }

    public void removeOwner(RegisteredUser ownerToRemove) throws Exception {
        if (!ownersIDefined.contains(ownerToRemove)) {
            throw new RuntimeException("This user is not the one who defined this owner");
        }
        if (ownerToRemove.getId() == this.getUserID() && this.isFounder()) {
            throw new RuntimeException("This user is Founder of the store and cannot remove himself");
        }
        StoreOwner ownership = ownerToRemove.getStoreIOwn(this.getStoreID());
        ownership.destruct();
        this.ownersIDefined.remove(ownerToRemove);
        this.getStore().removeOwner(ownership);
        ownerToRemove.removeOwnership(this.getStoreID());
        employeesDAO.save(this);
    }

    private void destruct() throws Exception {
        StoreOwner ownership;
        for (RegisteredUser manager : managersIDefined) {
            this.getStore().removeManager(manager.getStoreIManage(getStoreID()));
            manager.removeManagership(this.getStoreID());
        }
        for (RegisteredUser owner : ownersIDefined) {
            ownership = owner.getStoreIOwn(this.getStoreID());
            ownership.destruct();
            this.getStore().removeOwner(owner.getStoreIOwn(getStoreID()));
            owner.removeOwnership(this.getStoreID());
        }

    }

    public void removeManager(RegisteredUser managerToRemove) throws Exception {
        if (!managersIDefined.contains(managerToRemove)) {
            throw new RuntimeException("This user is not the one who defined this owner");
        }
        this.getStore().removeManager(managerToRemove.getStoreIManage(getStoreID()));
        managerToRemove.removeManagership(this.getStoreID());
        managersIDefined.remove(managerToRemove);
        employeesDAO.save(this);
    }

    public void closeStore() throws Exception {
        if (!this.isFounder()) {
            throw new RuntimeException("process to initiate store closing must be through founder");
        }
        destruct();
    }

    public int findChild(RegisteredUser child) {
        Integer res = null;
        if (this.getUserID() == child.getId()) {
            return child.getId();
        }
        //can be same bc managers dont define other managers
        if (this.ownersIDefined.contains(child) || this.managersIDefined.contains(child)) {
            return this.getUserID();
        }
        //DFS on owners
        for (RegisteredUser owner : ownersIDefined) {
            res = owner.getStoreIOwn(this.getStoreID()).findChild(child);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    public void addManagerPermission(RegisteredUser manager, Set<String> permission) {
        if (!managersIDefined.contains(manager)) {
            throw new RuntimeException("This user is not the one who defined this owner");
        }
        StoreManager m = manager.getStoreIManage(this.getStoreID());
        List<StoreActionPermissions> permissions = invParsePermissions(permission);
        m.addPermission(permissions);
        employeesDAO.save(m);
    }

    public void removeManagerPermission(RegisteredUser manager, Set<String> permission) {
        if (!managersIDefined.contains(manager)) {
            throw new RuntimeException("This user is not the one who defined this owner");
        }
        StoreManager m = manager.getStoreIManage(this.getStoreID());
        List<StoreActionPermissions> permissions = invParsePermissions(permission);
        m.removePermission(permissions);
        employeesDAO.save(m);
    }

    public boolean hasPermission(StoreActionPermissions permission) {
        return true;
    }

    private List<StoreActionPermissions> invParsePermissions(Collection<String> values) {
        return values.stream().map(v->StoreActionPermissions.valueOf(v.replace(' ', '_'))).toList();
    }
}
