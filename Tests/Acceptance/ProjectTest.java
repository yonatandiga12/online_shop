package Acceptance;

import Bridge.Bridge;
import Bridge.Driver;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;
import ServiceLayer.Objects.*;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ProjectTest {


    final int GUEST = 1;
    final int MEMBER = 2;
    final int LOGGED = 1;
    final int NOT_LOGGED = 2;

    private Bridge bridge;

    public void setUp() {
        if(bridge == null) {
            System.setProperty("env", "test");
            this.bridge = Driver.getBridge();
        }
        user7SystemManagerId = 1000000;
        //setUpExternalSystems();
    }
    protected static int user7SystemManagerId = -1;

    public Bridge getBridge(){
        return bridge;
    }
//
//
//    protected static int user1GuestId = -1;         //guest - active
//    protected static int user2LoggedInId = -1;
//    protected static int user3NotLoggedInId = -1;   // registered, not logged in
//    protected static int user4LoggedInId = -1;      //logged in, have items in carts
//    protected static int user5ManagerOfStore2ToBeRemoved = -1; //Owner/Manager of store2, to be removed positioned  by user2
//    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
//    protected static int userNotExistId = -1;
//    protected static int store2Id = -1;             //store is open
//    protected static int store2ClosedId = -1;
//    protected static int store4Id = -1;
//    protected static int item1Id = -1;              //item1 in user1 basket
//    protected static int item11Id = -1;             //item11 in store2 but not in basket
//    protected static int item2Id = -1;              //item2 in store2
//    protected static int item2ToBeRemovedId = -1;
//    protected static int item4Id = -1;





//
//    /**
//     * User1: Guest, Not logged In
//     */
//    protected void setUpUser1(){
//        user1GuestId = setUser("User1","User1!", GUEST, NOT_LOGGED);
//    }
//
//    /**
//     * User2: Member, logged in, Store Owner and Manager of store2
//     */
//    protected void setUpUser2(){
//        if(user2LoggedInId != -1){
//            return;
//        }
//        user2LoggedInId = setUser("User2","User2!", MEMBER, LOGGED);
//        user5ManagerOfStore2ToBeRemoved = setUser("User5", "User5!", MEMBER, NOT_LOGGED);
//        user6OwnerOfStore2 = setUser("User6", "User6!", MEMBER, LOGGED);
//        store2Id = createStore(user2LoggedInId, "Store2"); //store is open
//        store2ClosedId = createStore(user2LoggedInId, "Store22"); //store is close
//        closeStore(user2LoggedInId, store2ClosedId);
//
//        //Make user6 and user5 manager Owner
//        defineStoreOwner(store2Id, user2LoggedInId, user6OwnerOfStore2);
//        defineStoreManager(store2Id , user2LoggedInId, user5ManagerOfStore2ToBeRemoved);
//
//        //add items
//        item1Id = addItemToStoreForTests(store2Id, "item1", 10, "Books", 10);
//        item11Id = addItemToStoreForTests(store2Id, "item11", 10, "Books", 10);
//        item2Id = addItemToStoreForTests(store2Id, "item2", 10, "Kitchen", 10);
//        item2ToBeRemovedId = addItemToStoreForTests(store2Id, "Name2", 10, "Kitchen", 10);
//    }
//
//    /**
//     * User3: Member, Not logged in, Has a cart with items
//     */
//    protected void setUpUser3() {
//        if(user3NotLoggedInId != -1)
//            return;
//        user3NotLoggedInId = setUser("User3","User3!", MEMBER, NOT_LOGGED);
//    }
//
//    /**
//     * User4: Member, logged in, Store Owner and founder of store4
//     */
//    protected void setUpUser4(){
//        user4LoggedInId = setUser("User4","User4!", MEMBER, LOGGED);
//        if(user2LoggedInId == -1)
//            user2LoggedInId = setUser("User2","User2!", MEMBER, LOGGED);   //created for the ownership of the store
//        store4Id = createStore(user4LoggedInId, "Store4");  //user4 is founder, user2 is owner
//        //add items
//        item4Id = addItemToStoreForTests(store4Id, "Item4", 10, "Clothes", 10 );
//    }
//
//
//    /**
//     * Set up all Users and Stores. user1 and user2 have carts with items in them
//     */
//    protected void setUpAllMarket() {
//        setUpUser1();
//        setUpUser2();
//        setUpUser3();
//        setUpUser4();
//        addItemsToUserForTests(user4LoggedInId, store2Id, item1Id);
//        addItemsToUserForTests(user4LoggedInId, store4Id, item4Id);
//    }
//
//
//    protected void setUpBuyUser4() {
//        if(user2LoggedInId == -1)
//            setUpUser2();
//        if(user4LoggedInId == -1)
//            setUpUser4();
//
//        addItemsToUserForTests(user4LoggedInId, store2Id, item2Id);
//        buyCart(user4LoggedInId, "paypal");
//    }



    protected int setUser(String userName, String password, int GuestOrMember, int logged) {
        int id = -1;
        if(GuestOrMember == MEMBER){
            id = registerUser(userName, password);
            loginUser(userName, password);
        }
        else{
            //what to do If it is Guest?
        }
        if(logged == LOGGED){
            loginUser(userName, password);
        }
        else if(logged == NOT_LOGGED){
            logOut(id);
        }
        return id;
    }

    public int addItemToStoreForTests(int storeId, String name, int price, String category, int amount)
    {
        try {
            int id = addCatalogItem(storeId, name, price, category);
            addItemAmount(storeId, id, amount);
            return id;
        } catch (Exception e) { return -1; }
    }


    /**
     * add items to cart of user from store
     */
    void addItemsToUserForTests(int userId, int storeId, int itemId) {
        this.addItemToBasket(userId, storeId, itemId, 1);
    }


    public int registerUser(String userName, String password) {
        return this.bridge.registerUser(userName, password, "String address", LocalDate.of(1999,2,2));
    }

    protected boolean loginUser(String name, String password) {
        return this.bridge.loginUser(name, password);
    }

    protected void exitSystem(int id) {
        this.bridge.exitSystem(id);
    }

    protected void loadSystem() {
        this.bridge.loadSystem();
    }

    protected StoreService getStoreInfo(int storeId) {
        return this.bridge.getStoreInfo(storeId);
    }

    protected List<CatalogItemService> searchItems(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) {
        return this.bridge.searchItems(keywords, searchBy, filters);
    }

    protected CartService addItemToBasket(int userId, int storeId, int itemId, int amount) {
        return this.bridge.addItemToBasket(userId, storeId, itemId, amount);
    }

    protected CartService getCart(int userId) {
        return this.bridge.getCart(userId);
    }

    protected boolean buyCart(int userId, String deliveryAddress) {
        PurchaseInfo purchaseInfo = new PurchaseInfo("number", 1, 2020, "adasd", 23, userId, LocalDate.of(2000, 1, 1));
        SupplyInfo supplyInfo = new SupplyInfo("name", "address", "city", "country", "zip");
        return this.bridge.buyCart(userId, purchaseInfo, supplyInfo);
    }

    protected int addCatalogItem(int storeId, String itemName, int price, String category) {
        CatalogItemService item = this.bridge.addCatalogItem(storeId, itemName, price, category);
        if(item == null)
            return -1;
        return item.getItemID();
    }

    protected boolean removeItemFromStore(int storeId, int itemId) {
        return this.bridge.removeItemFromStore(storeId, itemId);
    }

    protected boolean changeItemName(int storeId, int itemId, String newName) {
        String res = this.bridge.changeItemName(storeId, itemId, newName);
        return res.contains("Changed item");
    }

    protected List<UserStaffInfoService> showStaffInfo(int storeId, int userId) {
        return this.bridge.showStaffInfo(storeId, userId);
    }

    protected boolean logOut(int userID) {
        return this.bridge.logOut(userID);
    }

    protected int createStore(int userId, String storeName) {
        return this.bridge.createStore(userId, storeName);
    }

    protected boolean closeStore(int userId, int storeId) {
        return bridge.closeStore(userId, storeId);
    }

    protected boolean defineStoreManager(int storeId, int storeManager, int newStoreManager) {
        return this.bridge.defineStoreManager(storeId, storeManager, newStoreManager);
    }

    protected boolean defineStoreOwner(int storeId, int ownerId, int newCoOwnerId) {
        return this.bridge.defineStoreOwner(storeId, ownerId, newCoOwnerId);
    }

    protected boolean appointOwner(int storeId, int ownerId, int newCoOwnerId) {
        return this.bridge.appointOwner(storeId, ownerId, newCoOwnerId);
    }

    protected List<ReceiptService> getSellingHistoryOfStore(int userId, int storeId) {
        return this.bridge.getSellingHistoryOfStoreForManager(storeId, userId);
    }


    protected HashMap<Integer, List<ReceiptService>> getSellingHistoryOfUser(int managerId, int userId) {
        return this.bridge.getSellingHistoryOfUserForManager(managerId, userId);
    }


    protected List<ReceiptService> getPersonalHistory(int userId) {
        return this.bridge.getPersonalHistory(userId);
    }

    protected void addItemAmount(int storeId, int itemId, int amount) throws Exception
    {
        this.bridge.addItemAmount(storeId, itemId, amount);
    }



    protected boolean checkIfStoreOwner(int userId, int storeId) {
        return this.bridge.checkIfStoreOwner(userId, storeId);
    }

    protected boolean checkIfStoreManager(int userId, int storeId) {
        return this.bridge.checkIfStoreManager(userId, storeId);
    }

    protected List<String> showPersonalInformation(int userId) {
        return this.bridge.getPersonalInformation(userId);
    }

    protected boolean changePassword(int userId, String oldPassword, String newPassword) {
        return this.bridge.changePassword(userId, oldPassword, newPassword);
    }

    protected boolean sendMsg(int senderId, int receiverId, String msg) {
        return this.bridge.sendMsg(senderId, receiverId, msg);
    }

    protected HashMap<Integer, List<String>> getMsgs(int userId) {
        return this.bridge.getMsgs(userId);
    }

    protected HashMap<Integer, String> getComplaints(int managerId) {
        return this.bridge.getComplaints(managerId);
    }

    protected boolean rankAStore(int userId, int storeId, int rank) {
        return this.bridge.rankAStore(userId, storeId, rank);
    }

    protected double getStoreRank(int userId, int storeId) {
        return this.bridge.getStoreRank(userId, storeId);
    }

    protected double getItemRank(int userId, int storeId, int itemId) {
        return this.bridge.getItemRank(userId, storeId, itemId);
    }

    protected boolean rankAnItemInStore(int userId, int storeId, int itemId, int rank) {
        return this.bridge.rankAnItemInStore(userId, storeId, itemId, rank);
    }

    protected boolean checkIfLoggedIn(int userId) {
        return this.bridge.checkIfLoggedIn(userId);
    }

    protected StoreService getStoreInformationAsStoreManager(int storeId, int userId) {
        return this.bridge.getStoreInfoAsStoreManager(storeId, userId);
    }

    protected List<String> getRequestsOfStore_AsStoreOwnerManager(int ownerManagerId, int storeId) {
        return this.bridge.getRequestsOfStore(ownerManagerId, storeId);
    }

    protected HashMap<Integer, String> getUsersTraffic(int managerId) {
        return this.bridge.getUsersTraffic(managerId);
    }

    protected HashMap<Integer, Integer> getPurchaseTraffic(int managerId) {
        return this.bridge.getPurchaseTraffic(managerId);
    }

    protected int getNumberOfRegistrationForToady(int managerId) {
        return this.bridge.getNumberOfRegistrationForToady(managerId);
    }

    protected boolean reopenStore(int userId, int storeId) {
        return this.bridge.reopenStore(userId, storeId);
    }

    protected boolean removeRegisterdUser(int systemManagerId, int userToRemoveId) {
        return this.bridge.removeRegisterdUser(systemManagerId, userToRemoveId);
    }

    protected boolean removeStoreManager(int storeId, int storeManagerId, int removeUserId) {
        return this.bridge.removeStoreManager(storeId, storeManagerId, removeUserId);
    }

    protected boolean removeStoreOwner(int storeId, int storeOwnerId, int newStoreOwnerId) {
        return this.bridge.removeStoreOwner(storeId, storeOwnerId, newStoreOwnerId);
    }

    protected boolean closeStorePermanently(int storeManagerId, int storeId) {
        return this.bridge.closeStorePermanently(storeManagerId, storeId);
    }

    protected void sendComplaint(int userId, String msg) {
        this.bridge.postComplaint(userId, msg);
    }

    protected boolean answerComplaints(int userId, HashMap<Integer, String> complaintsAnswers) {
        return this.bridge.answerComplaint(userId, complaintsAnswers);
    }

    protected boolean payCart(int userId, String paymentDetails, String paymentService) {
        return this.bridge.payCart(userId, paymentDetails, paymentService);
    }

    protected boolean askForSupply(int userId, List<CatalogItemService> items, String supplyService) {
        return this.bridge.askForSupply(userId, items, supplyService);
    }

    protected int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale){
        return this.bridge.addVisibleItemsDiscount(storeID, itemsIDs, percent, endOfSale);
    }

    protected  int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale){
        return this.bridge.addVisibleCategoryDiscount(storeID, category, percent, endOfSale);
    }


    protected int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale){
        return this.bridge.addConditionalStoreDiscount(storeID, percent, endOfSale);
    }


    protected int addHiddenStoreDiscount(int storeId, double percent, String coupon, Calendar calendar) {
        return this.bridge.addHiddenStoreDiscount(storeId, percent, coupon, calendar);
    }
    protected HashMap<Integer, ChatService> getChats(int id){
        return this.bridge.getChats(id);
    }

    protected boolean approveOwner(int store, int approvingId, int newOwnerId){
        return this.bridge.approveOwner(store, approvingId, newOwnerId);
    }

    protected boolean rejectOwner(int store, int newOwnerId){
        return this.bridge.rejectOwner(store, newOwnerId);
    }

}
