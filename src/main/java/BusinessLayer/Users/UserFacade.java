package BusinessLayer.Users;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Log;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.MarketMock;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.Observer.NotificationObserver;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.UserDAO;
import initialize.ConfigReader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserFacade {
    private static final Logger log = Log.log;
    private final static int MIN_PASS_LENGTH = 6;
    private final static String adminName = new ConfigReader().getAdminUserName(); //"admin"
    private final static String adminPass = new ConfigReader().getAdminPassword(); //"adminpass";
    public static int userID = 1000000;
    //    private Map<String, RegisteredUser> users;
    private Map<Integer, RegisteredUser> users;
    private UserDAO userDAO;
    private Map<Integer, Guest> guests;

    public UserFacade() throws Exception {
        guests = new HashMap<>();
        users = new HashMap<>();
//        userID = userDAO.getMaxID() + 1;
//        setGuest();
    }

    private synchronized int getNewId() {
        return userID++;
    }

    public Guest setGuest() throws Exception {
        Guest guest = new Guest();
        guests.put(Guest.GUEST_USER_ID--, guest);
        if (userDAO == null)
            userDAO = UserDAO.getUserDao();
        userDAO.save(guest);
        return guest;
    }

    public void createAdmin() throws Exception {
        RegisteredUser admin = new RegisteredUser(adminName, adminPass, getNewId(), true);
        users.put(admin.getId(), admin);
        saveUserInDB(admin);
    }

    public SystemManager makeAdmin(int id) throws Exception {
        RegisteredUser user = getRegisteredUser(id);
        SystemManager sm = user.makeAdmin();
        saveUserInDB(user);
        return sm;
    }

    public void createAdmin(MarketMock marketMock) throws Exception {
        RegisteredUser admin = new RegisteredUser(adminName, adminPass, getNewId(), true, marketMock);
        users.put(admin.getId(), admin);
    }
    public RegisteredUser getUserByName(String userName) throws Exception {
        for (RegisteredUser user : users.values()) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        throw new Exception("No user exists with name " + userName);
    }

    public boolean userExists(int ID){
        return users.containsKey(ID) || guests.containsKey(ID);
    }

    public User getUser(int userID) {
        if (!isGuest(userID)) {
            return users.get(userID);
        }
        return guests.get(userID);
    }

    private boolean isGuest(int userID) {
        return userID<=Guest.MAX_GUEST_USER_ID && userID > Guest.GUEST_USER_ID;
    }

    public RegisteredUser getRegisteredUser(int userID) throws Exception {
        if (isGuest(userID)) {
            throw new Exception("This is the guest user ID, not registered user");
        }
        if (users==null)
            throw new Exception("users is Null");
        if (!users.containsKey(userID))
            throw new Exception("user "+userID+" not found");
        return users.get(userID);
    }

    public RegisteredUser getLoggedInUser(int userID) throws Exception {
        RegisteredUser user = users.get(userID);
        if (user!=null&&user.isLoggedIn()) {
            return user;
        }
        throw new Exception("User " + userID + " is not logged in");
    }

    public int registerUser(String username, String password, String address, LocalDate bDay) throws Exception {
        if (checkUserName(username) && checkPassword(password)&& checkAddress(address) && checkBDay(bDay)) {
            int id = getNewId();
            RegisteredUser tempUser = new RegisteredUser(username, password, id, address, bDay);
            saveUserInDB(tempUser);
            //add to cash
            users.put(id, tempUser);
            return id;
        }
        else {
            log.severe("Problem logging in. username or password check returned false but not error");
            throw new Exception("Problem logging in. username or password check returned false but not error");
        }
    }

    public void saveUserInDB(RegisteredUser tempUser) throws Exception {
        // add to DB
        userDAO.save(tempUser);
    }

    public int registerUser(String username, String password, MarketMock marketMock) throws Exception {
        if (checkUserName(username) && checkPassword(password)) {
            RegisteredUser tempUser = new RegisteredUser(username, password, getNewId(), marketMock);
            // add to DB
//            userDAO.addUser(tempUser);
            //add to cash
            users.put(tempUser.getId(), tempUser);
            return tempUser.getId();
        }
        else {
            log.severe("Problem logging in. username or password check returned false but not error");
            throw new Exception("Problem logging in. username or password check returned false but not error");
        }
    }


    public boolean checkPassword(String password) throws Exception {
        if (password == null)
            throw new Exception("Password can't be null");
        if (password.length() < MIN_PASS_LENGTH)
            throw new Exception("Password too short! Must be at least 6 chars");
        return true;
    }
    public boolean checkAddress(String address) throws Exception {
        if (address == null)
            throw new Exception("Address can't be null");
        return true;
    }
    public boolean checkBDay(LocalDate bDay) throws Exception {
        if (bDay == null)
            throw new Exception("bDay can't be null");
        return true;
    }

    public boolean checkUserName(String userName) throws Exception {
        if (userName == null) {
            throw new Exception("User name can't be null");
        }
        for (RegisteredUser user : users.values()) {
            if (user.getUsername().equals(userName)) {
                throw new Exception("Username " + userName + " already taken");
            }
        }
        return true;
    }

    public int logIn(String username, String password) throws Exception {
        //List<RegisteredUser> list = new ArrayList<RegisteredUser>(userIDs.values()).stream().filter((user)->user.getUsername().equals(username)).toList();
        String errormsg = "Login invalid: Check Username and Password";
        if (username == null || password == null) {
            log.info("username or password is Null");
            throw new Exception("username or password is Null");
        }
        RegisteredUser user = getUserByName(username);
        if (user == null) {
            log.info("incorrect user name");
            throw new Exception(errormsg);
        }
        if (!Password.verifyPassword(password,user.getPassword())) {
            log.info("incorrect password");
            throw new Exception(errormsg);
        }
        if (user.isLoggedIn()) {
            log.info("User is already logged in");
            throw new Exception(errormsg);
        }
        user.logIn();
        return user.getId();
    }

    public boolean logout(int userID) throws Exception {
        RegisteredUser user = getLoggedInUser(userID);
        if (user == null)
            throw new Exception("incorrect user name");
        if (!user.isLoggedIn())
            throw new Exception("User is not logged in");
        user.logout();
        return true;
    }

    public void loadUsers() {
        if (userDAO == null)
            userDAO = UserDAO.getUserDao();
        users = userDAO.getUsers();
        if (!users.isEmpty())
            userID = Collections.max(users.keySet()) + 1;
    }

    public void addOwner(int userID, int userToAddID, int storeID) throws Exception {
        RegisteredUser currUser = getRegisteredUser(userID);
        RegisteredUser newOwner = getRegisteredUser(userToAddID);
        if (currUser == null || newOwner == null) {
            throw new RuntimeException("User does not exist");
        }
        currUser.addOwner(newOwner, storeID);
    }

    public void addStore(int founderID, Store store) throws Exception {
        RegisteredUser currUser = getLoggedInUser(founderID);
        currUser.addStore(store);
    }

    public synchronized void addManager(int userID, int userToAdd, int storeID) throws Exception {
        RegisteredUser currUser = getLoggedInUser(userID);
        RegisteredUser newManager = getRegisteredUser(userToAdd);
        if (currUser == null || newManager == null) {
            throw new RuntimeException("User does not exist");
        }
        currUser.addManager(newManager, storeID);
    }

    public void removeOwner(int userID, int userToRemove, int storeID) throws Exception {
        RegisteredUser currUser = getLoggedInUser(userID);
        RegisteredUser ownerToRemove = getRegisteredUser(userToRemove);
        if (currUser == null || ownerToRemove == null) {
            throw new RuntimeException("User does not exist");
        }
        currUser.removeOwner(ownerToRemove, storeID);
    }

    public void removeManager(int userID, int userToRemove, int storeID) throws Exception {
        RegisteredUser currUser = getLoggedInUser(userID);
        RegisteredUser managerToRemove = getRegisteredUser(userToRemove);
        if (currUser == null || managerToRemove == null) {
            throw new RuntimeException("User does not exist");
        }
        currUser.removeManager(managerToRemove, storeID);
    }

    //only called from system manager after other user associations removed
    public void removeUser(RegisteredUser userToRemove) throws Exception {
        users.remove(userToRemove.getId());
//        userDAO.removeUser(userToRemove);
    }

    public Cart getCart(int userID) {
        return getUser(userID).getCart();
    }

    public Cart addItemToCart(int userID, Store store, CatalogItem item, int quantity) throws Exception {
        if (!isGuest(userID))
            getLoggedInUser(userID);
        return getUser(userID).addItemToCart(store, item, quantity);
    }

    public Cart removeItemFromCart(int userID, int storeID, int itemID) throws Exception {
        return getUser(userID).removeItemFromCart(storeID, itemID);
    }

    public Cart changeItemQuantityInCart(int userID, int storeID, int itemID, int quantity) throws Exception {
        return getUser(userID).changeItemQuantityInCart(storeID, itemID, quantity);
    }

    /**
     * this method is used to show the costumer all the stores he added,
     * he can choose one of them and see what is inside with getItemsInBasket
     *
     * @return List<String> @TODO maybe should be of some kind of object?
     */
    public List<String> getStoresOfBaskets(int userID) throws Exception {
        return getLoggedInUser(userID).getStoresOfBaskets();
    }

    public HashMap<CatalogItem, CartItemInfo> getItemsInBasket(int userID, String storeName) throws Exception {
        return getUser(userID).getItemsInBasket(storeName);
    }

    public Cart buyCart(int userID, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        if (isGuest(userID)) {
            return guests.get(userID).buyCart(purchaseInfo, supplyInfo);
        }
        return getLoggedInUser(userID).buyCart(purchaseInfo, supplyInfo);
    }

    /**
     * empties the cart
     */
    public Cart emptyCart(int userID) throws Exception {
        return getUser(userID).emptyCart();
    }

    public void addManagerPermission(int userID, int storeID, int managerID, Set<String> permission) throws Exception {
        RegisteredUser user = getLoggedInUser(userID);
        RegisteredUser manager = getRegisteredUser(managerID);
        user.addManagerPermission(storeID, manager, permission);
    }

    public void removeManagerPermission(int userID, int storeID, int managerID, Set<String> permission) throws Exception {
        RegisteredUser user = getLoggedInUser(userID);
        RegisteredUser manager = getRegisteredUser(managerID);
        user.removeManagerPermission(storeID, manager, permission);
    }

    public void sendMessage(int senderID, int receiverID, String content){
        users.get(senderID).sendMessage(receiverID, content);
    }

//    public void markMessageAsRead(int userID, Message message) throws Exception {
//        users.get(userID).markMessageAsRead(message);
//    }
//
//    public void markMessageAsNotRead(int userID, Message message) throws Exception {
//        users.get(userID).markMessageAsNotRead(message);
//    }
//
//    public List<Message> watchNotReadMessages(int userID){
//        return users.get(userID).watchNotReadMessages();
//    }
//
//    public List<Message> watchReadMessages(int userID){
//        return users.get(userID).watchReadMessages();
//    }
//
//    public List<Message> watchSentMessages(int userID){
//        return users.get(userID).watchSentMessages();
//    }

    public ConcurrentHashMap<Integer, Chat> getChats(int userID) throws Exception {
        if(!userExists(userID)){
            throw new Exception("The user was not found!");
        }
        return getUser(userID).getChats();
    }

    public Map<Integer, RegisteredUser> getAllRegisteredUsers() {
        return  users;
    }


    public ArrayList<Integer> getStoresIdsIOwn(int ownerId) throws Exception {
        return new ArrayList<>(getRegisteredUser(ownerId).getStoresIOwn().stream().map(StoreOwner::getStoreID).collect(Collectors.toList()));
    }

    public List<Integer> getStoresIdsIManage(int ownerId) throws Exception {
        return new ArrayList<>(getRegisteredUser(ownerId).getStoresIManage().stream().map(StoreManager::getStoreID).toList());
    }

    public boolean isOwnerOrManager(int currUserID) {
        try {
            RegisteredUser user = getRegisteredUser(currUserID);
            return !user.getStoresIOwn().isEmpty() || !user.getStoresIManage().isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }

    public Map<Integer, RegisteredUser> getLoggedInUsers() {
        return users.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isLoggedIn())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Integer, RegisteredUser> getLoggedOutUsers() {
        return users.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isLoggedIn())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<RegisteredUser, Set<Integer>> getAllOwnersIDefined(int ownerId) throws Exception {
        return getRegisteredUser(ownerId).getAllOwnersIDefined();
    }

    public Map<RegisteredUser, Set<Integer>> getAllManagersIDefined(int ownerId) throws Exception {
        return getRegisteredUser(ownerId).getAllManagersIDefined();
    }

    public void addCouponToCart(int userId, String coupon) throws Exception {
        if(!users.containsKey(userId)){
            throw new Exception("ERROR: UserFacade::addCouponToCart: no such user!");
        }

        getUser(userId).addCouponToCart(coupon);
    }

    public void removeCouponFromCart(int userId, String coupon) throws Exception {
        if(!users.containsKey(userId)){
            throw new Exception("ERROR: UserFacade::removeCouponFromCart: no such user!");
        }

        getUser(userId).removeCouponFromCart(coupon);
    }

    public List<String> getCoupons(int userId) throws Exception {
        if(!users.containsKey(userId)){
            throw new Exception("no such user!");
        }
        return getUser(userId).getCart().getCouponStrings();
    }

    public void listenToNotifications(int userId, NotificationObserver listener) throws Exception {
        if(!userExists(userId)){
            throw new Exception("No such user!");
        }

        getUser(userId).listenToNotifications(listener);
    }
  
    public Basket removeBasketFromCart(int userID, int storeID) throws Exception {
        return getUser(userID).removeBasketFromCart(storeID);
    }

    public int findUserByUsername(String username) throws Exception {
        for(RegisteredUser user : users.values()){
            if(user.getUsername().equals(username)){
                return user.getId();
            }
        }

        throw new Exception("The user " + username + "was not found!");
    }

    public String getUsernameById(int id){
        RegisteredUser user =  users.get(id);

        if(user != null){
            return user.getUsername();
        }

        return null;
    }


}
