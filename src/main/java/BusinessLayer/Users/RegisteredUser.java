package BusinessLayer.Users;

import BusinessLayer.Market;
import BusinessLayer.MarketMock;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.StorePermissions.StoreActionPermissions;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Store;
import DataAccessLayer.StoreEmployeesDAO;
import DataAccessLayer.UserDAO;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "users")
public class RegisteredUser extends User {
    private String username;
    private String password;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user")
    private Set<StoreOwner> storesIOwn;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user")
    private Set<StoreManager> storesIManage;
    @Transient
    private SystemManager systemManager;
    private boolean isSystemManager;
    @Transient
    private boolean isLoggedIn;
    @Transient
    private UserDAO userDAO;
    @Transient
    private StoreEmployeesDAO employeesDAO;
    public RegisteredUser(String username, String pass, int id, String address, LocalDate bDay) throws Exception {
        super(id);
        this.username = username;
        this.password = Password.hashPassword(pass);
        this.bDay = bDay;
        this.address = address;
        this.storesIOwn = new HashSet<>();
        this.storesIManage = new HashSet<>();
        this.isLoggedIn = false;
        this.isSystemManager = false;
        this.userDAO = UserDAO.getUserDao();
        this.employeesDAO = new StoreEmployeesDAO();
    }
    public RegisteredUser(String username, String pass, int id, boolean isAdmin) throws Exception {
        super(id);
        this.username = username;
        this.password = Password.hashPassword(pass);
        this.storesIOwn = new HashSet<>();
        this.storesIManage = new HashSet<>();
        this.isLoggedIn = false;
        this.isSystemManager = isAdmin;
        if (isAdmin) {
            systemManager = new SystemManager(this);
        }
    }

    public RegisteredUser(String username, String pass, int id, boolean isAdmin, MarketMock marketMock) throws Exception {
        super(id);
        this.username = username;
        this.password = Password.hashPassword(pass);
        this.storesIOwn = new HashSet<>();
        this.storesIManage = new HashSet<>();
        this.userDAO = UserDAO.getUserDao();
        this.employeesDAO = new StoreEmployeesDAO();
        this.isLoggedIn = false;
        this.isSystemManager = isAdmin;
        if (isAdmin) {
            systemManager = new SystemManager(this);
        }
    }

    public RegisteredUser(String username, String pass, int id, MarketMock marketMock) throws Exception {
        super(id);
        this.username = username;
        this.password = Password.hashPassword(pass);
        this.storesIOwn = new HashSet<>();
        this.storesIManage = new HashSet<>();
        this.userDAO = UserDAO.getUserDao();
        this.employeesDAO = new StoreEmployeesDAO();
        this.isLoggedIn = false;
        this.isSystemManager = false;
    }

    public RegisteredUser() throws Exception {
        super();
        this.employeesDAO = new StoreEmployeesDAO();
    }

    public boolean isSystemManager() {
        return isSystemManager;
    }

    public void setSystemManager(boolean systemManager) throws Exception {
        isSystemManager = systemManager;
    }

    @PostLoad
    private void adminInit() throws Exception {
        if (isSystemManager) {
            Market.getInstance().addAdmin(id, makeAdmin());
        }
    }

    public SystemManager makeAdmin() throws Exception {
        systemManager = new SystemManager(this);
        this.isSystemManager = true;
        return systemManager;
    }

    public Set<StoreOwner> getStoresIOwn() {
        return storesIOwn;
    }

    public void setStoresIOwn(Set<StoreOwner> storesIOwn) {
        this.storesIOwn = storesIOwn;
    }

    public Set<StoreManager> getStoresIManage() {
        return storesIManage;
    }

    public void setStoresIManage(Set<StoreManager> storesIManage) {
        this.storesIManage = storesIManage;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public StoreOwner getStoreIOwn(int storeID) {
        for (StoreOwner storeOwner : storesIOwn) {
            if (storeOwner.getStoreID() == storeID)
                return storeOwner;
        }
        return null;
    }

    private boolean ownsStore(int storeID) {
        for (StoreOwner storeOwner : storesIOwn) {
            if (storeOwner.getStoreID() == storeID)
                return true;
        }
        return false;
    }

    public StoreManager getStoreIManage(int storeID) {
        for (StoreManager storeManager : storesIManage) {
            if (storeManager.getStoreID() == storeID)
                return storeManager;
        }
        return null;
    }

    private boolean managesStore(int storeID) {
        return (getStoreIManage(storeID) != null);
    }

    public void addStore(Store store) throws Exception {
        StoreOwner ownership = new StoreOwner(this.getId(), store);
        storesIOwn.add(ownership);
        employeesDAO.addOwner(ownership);
    }

    public void addOwner(RegisteredUser newOwner, int storeID) throws Exception {
        //ensure I am an owner
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        //check if newOwner is already an owner or manager//TODO can user be both?
        if (newOwner.ownsStore(storeID)) {
            throw new RuntimeException("User " + newOwner.username + " already owns store " + storeID);
        }
        if (newOwner.managesStore(storeID)) {
            throw new RuntimeException("User already manages store");
        }
        storeOwnership.addOwner(newOwner);
        employeesDAO.save(storeOwnership);
        mailbox.sendMessage(newOwner.getId(), "You have been appointed owner of Store: " + storeOwnership.getStore().getStoreName());
    }

    public StoreOwner addStoreOwnership(StoreOwner storeOwnership) throws Exception {
        int storeID = storeOwnership.getStoreID();
        StoreOwner ownership = new StoreOwner(this.getId(), storeOwnership);
        storesIOwn.add(ownership);
        employeesDAO.addOwner(ownership);
        mailbox.getChatsAsMap().putIfAbsent(storeID, new Chat(id, storeID));
        return ownership;
    }

    public void addManager(RegisteredUser newManager, int storeID) {
        //ensure I am an owner
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        if (newManager.ownsStore(storeID)) {
            throw new RuntimeException("User already owns store");
        }
        if (newManager.managesStore(storeID)) {
            throw new RuntimeException("User already manages store");
        }
        storeOwnership.addManager(newManager);
        mailbox.sendMessage(newManager.getId(), "You have been appointed owner of Store: " + storeOwnership.getStore().getStoreName());
    }

    public void addStoreManagership(StoreOwner storeOwnerShip) {
        StoreManager managership = new StoreManager(this.getId(), storeOwnerShip);
        storesIManage.add(managership);
        employeesDAO.addManager(managership);
    }

    public void removeOwner(RegisteredUser ownerToRemove, int storeID) throws Exception {
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        if (!ownerToRemove.ownsStore(storeID)) {
            throw new RuntimeException("Owner to remove doesn't own store");
        }
        storeOwnership.removeOwner(ownerToRemove);
    }

    public void closeStore(RegisteredUser founder, int storeID) throws Exception {
        StoreOwner storeOwnership = founder.getStoreIOwn(storeID);
        storeOwnership.closeStore();
        founder.removeOwnership(storeID);
    }

    public void removeManager(RegisteredUser managerToRemove, int storeID) throws Exception {
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        if (!managerToRemove.managesStore(storeID)) {
            throw new RuntimeException("Manager to remove doesn't manage store");
        }
        storeOwnership.removeManager(managerToRemove);
    }

    public void removeManagership(int storeID) throws Exception {
        StoreManager manager = getStoreIManage(storeID);
        employeesDAO.removeManagership(manager);
        removeStoreIManage(storeID);
        userDAO.removeManagership(this);
    }

    private void removeStoreIManage(int storeID) {
        for (StoreManager storeManager : storesIManage) {
            if (storeManager.getStoreID() == storeID) {
                storesIManage.remove(storeManager);
                return;
            }
        }
    }

    private void removeStoreIOwn(int storeID) {
        for (StoreOwner storeOwner : storesIOwn) {
            if (storeOwner.getStoreID() == storeID) {
                storesIOwn.remove(storeOwner);
                return;
            }
        }
    }

    public void removeOwnership(int storeID) {
        StoreOwner owner = getStoreIOwn(storeID);
        employeesDAO.removeOwnership(owner);
        removeStoreIOwn(storeID);
        userDAO.removeOwnership(this);
    }

    public void addManagerPermission(int storeID, RegisteredUser manager, Set<String> permission) {
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        if (!manager.managesStore(storeID)) {
            throw new RuntimeException("Manager doesn't manage store");
        }
        storeOwnership.addManagerPermission(manager, permission);
    }

    public void removeManagerPermission(int storeID, RegisteredUser manager, Set<String> permission) {
        StoreOwner storeOwnership = getStoreIOwn(storeID);
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        if (!manager.managesStore(storeID)) {
            throw new RuntimeException("Manager doesn't manage store");
        }
        storeOwnership.removeManagerPermission(manager, permission);
    }

    public void logIn() throws Exception {
        this.isLoggedIn = true;
        getUserDao().save(this);
    }

    private UserDAO getUserDao() {
        if (userDAO == null) {
            this.userDAO = UserDAO.getUserDao();
        }
        return userDAO;
    }

    public void logout() throws Exception {
        this.isLoggedIn = false;
        getUserDao().save(this);
    }

    @Transient
    public Map<RegisteredUser, Set<Integer>> getAllOwnersIDefined() {
        Set<StoreOwner> storeOwnership = getStoresIOwn();
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        Map<RegisteredUser, Set<Integer>> owners = new HashMap<>();
        for (StoreOwner storeOwner : storeOwnership) {
            Set<RegisteredUser> currUsers = storeOwner.getOwnersIDefined();
            for (RegisteredUser user : currUsers) {
                if (!owners.containsKey(user)) {
                    Set<Integer> set = new HashSet<>();
                    set.add(storeOwner.getStoreID());
                    owners.put(user, set);
                } else {
                    owners.get(user).add(storeOwner.getStoreID());
                }
            }
        }
        return owners;
    }

    @Transient
    public Map<RegisteredUser, Set<Integer>> getAllManagersIDefined() {
        Set<StoreOwner> storeOwnership = getStoresIOwn();
        if (storeOwnership == null) {
            throw new RuntimeException("User is not a store owner");
        }
        Map<RegisteredUser, Set<Integer>> managers = new HashMap<>();
        for (StoreOwner owner : storeOwnership) {
            Set<RegisteredUser> currUsers = owner.getManagersIDefined();
            for (RegisteredUser user : currUsers) {
                if (!managers.containsKey(user)) {
                    Set<Integer> set = new HashSet<>();
                    set.add(owner.getStoreID());
                    managers.put(user, set);
                } else {
                    managers.get(user).add(owner.getStoreID());
                }
            }
        }

        return managers;
    }
}
