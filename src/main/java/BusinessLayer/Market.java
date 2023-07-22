package BusinessLayer;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.NotificationHub;
import BusinessLayer.NotificationSystem.UserMailbox;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Stores.*;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Visible;
import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Policies.PurchasePolicy;
import BusinessLayer.Users.*;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;
import initialize.ConfigReader;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static BusinessLayer.Stores.StoreStatus.OPEN;

public class Market {
    private static final Object instanceLock = new Object();
    private static volatile Market instance;
    private static ConnectorConfigurations configurations;
    private UserFacade userFacade;
    private StoreFacade storeFacade;
    private Map<Integer, SystemManager> systemManagerMap;
    private NotificationHub notificationHub;
//    private AppointmentManager appointmentManager;

    private Market() throws Exception {
        synchronized (instanceLock) {
            readDBConfigurations();
            deleteGuestsFromDB(); //in case system did not shut down properly
            systemManagerMap = new HashMap<>();
            userFacade = new UserFacade();
            storeFacade = new StoreFacade();
            notificationHub = new NotificationHub();
//            appointmentManager = new AppointmentManager();
        }
    }

    public static Market getInstance() throws Exception {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new Market();
                instance.notificationHub.loadHub();
                instance.userFacade.loadUsers();
                instance.createFirstAdmin();
                instance.storeFacade.loadStores();
            }
            return instance;
        }
    }

    public static ConnectorConfigurations getConfigurations() {
        return configurations;
    }

    public void setConfigurations(ConnectorConfigurations conf) {
        configurations = conf;
    }

    public void readDBConfigurations() {
        ConfigReader configReader = new ConfigReader();
        String name = configReader.getDBName();
        String url = configReader.getDBUrl();
        String username = configReader.getDBUsername();
        String password = configReader.getDBPassword();
        String driver = configReader.getDBDriver();
        configurations = new ConnectorConfigurations(name, url, username, password, driver);
    }
    //Proper System shutdown. Only System Managers may do this
    public boolean system_shutdown(int userID) throws Exception {
        if (!isAdmin(userID)) {
            throw new Exception("Only System Admin may shut down the system");
        }
        deleteGuestsFromDB();
        return true;
    }

    private void deleteGuestsFromDB() {
        ConnectorConfigurations configurations = getConfigurations();
        DBConnector<UserMailbox> guestConnector = new DBConnector<>(UserMailbox.class, configurations);
        DBConnector<Guest> c = new DBConnector<>(Guest.class, configurations);
        DBConnector<Cart> cart = new DBConnector<>(Cart.class, configurations);
        c.emptyTable();
        guestConnector.noValueQuery("delete from UserMailbox where ownerID < " + (Guest.MAX_GUEST_USER_ID+1));
        cart.noValueQuery("delete from Cart where userID < " + (Guest.MAX_GUEST_USER_ID+1));
    }

    public User addGuest() throws Exception {
        return userFacade.setGuest();
    }

    public UserFacade getUserFacade() {
        return userFacade;
    }

    public StoreFacade getStoreFacade() {
        return storeFacade;
    }

    public Map<Integer, SystemManager> getSystemManagerMap() {
        return systemManagerMap;
    }

    private void createFirstAdmin() throws Exception {
        if (systemManagerMap.isEmpty())
            userFacade.createAdmin();
    }


    public void addAdmin(int newAdmin, SystemManager systemManager) {
        systemManagerMap.put(newAdmin, systemManager);
    }

    public void addAdmin(Integer curr, int newAdmin) throws Exception {
        if (!systemManagerMap.containsKey(curr)) {
            throw new RuntimeException("Only admin can add another admin");
        }
        if (getUserFacade().userExists(newAdmin)) {
            SystemManager systemManager = userFacade.makeAdmin(newAdmin);
            systemManagerMap.put(newAdmin, systemManager);
        }
    }

    public int register(String username, String pass, String address, LocalDate bDay) throws Exception {
        return userFacade.registerUser(username, pass, address, bDay);
    }

    public int login(String username, String pass) throws Exception {
        return userFacade.logIn(username, pass);
    }

    public boolean logout(int userID) throws Exception {

        return userFacade.logout(userID);
    }

    public void addOwner(int userID, int userToAddID, int storeID) throws Exception {
        removeAppointment(storeID, userToAddID);
        userFacade.addOwner(userID, userToAddID, storeID);
    }

    public void addManager(int userID, int userToAdd, int storeID) throws Exception {
        userFacade.addManager(userID, userToAdd, storeID);
    }

    public void removeOwner(int userID, int userToRemove, int storeID) throws Exception {
        userFacade.removeOwner(userID, userToRemove, storeID);
    }

    public void removeManager(int userID, int userToRemove, int storeID) throws Exception {
        userFacade.removeManager(userID, userToRemove, storeID);
    }

    public boolean closeStorePermanently(int userID, int storeID) throws Exception {
        if (isAdmin(userID)) {
            SystemManager systemManager = systemManagerMap.get(userID);
            systemManager.closeStorePermanently(storeFacade.getStore(storeID));
            return true;
        } else {
            throw new RuntimeException("Only admin can close stores permanently");
        }
    }

    public boolean isAdmin(int userID) {
        return systemManagerMap.get(userID) != null;
    }

    public void removeUser(int userID, int userToRemove) throws Exception {

        if (isAdmin(userID)) {
            if (!systemManagerMap.containsKey(userID))
                throw new Exception("systemManagerMap cant find userID");
            SystemManager systemManager = systemManagerMap.get(userID);
            if (systemManager == null)
                throw new Exception("systemManager is Null");
            if (userFacade.getRegisteredUser(userToRemove) == null)
                throw new Exception("userToRemove is null!");
            systemManager.removeUser(userFacade.getRegisteredUser(userToRemove));
        } else
            throw new RuntimeException("Only System admin can remove a user");
    }

    public int addStore(int founderID, String name) throws Exception {
        //bc of two-way dependency: store is created with only founder ID then when founder receives store pointer he adds himself to owner list
        Store store = storeFacade.addStore(founderID, name);
        userFacade.addStore(founderID, store);
        return store.getStoreID();
    }

    public Map<CatalogItem, Boolean> getCatalog() {
        return storeFacade.getCatalog();
    }

    public Map<CatalogItem, Boolean> searchCatalog(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) throws Exception {
        return storeFacade.getCatalog(keywords, searchBy, filters);
    }

    public Cart getCart(int userID) {
        return userFacade.getCart(userID);
    }

    public Cart addItemToCart(int userID, int storeID, int itemID, int quantity) throws Exception {
        Store store = storeFacade.getStore(storeID);
        if (store.getStoreStatus() != OPEN)
            throw new Exception("Error: Can't add item to cart from unopened store");
        CatalogItem item = store.getItem(itemID);
        return userFacade.addItemToCart(userID, store, item, quantity);
    }

    public Cart removeItemFromCart(int userID, int storeID, int itemID) throws Exception {
        return userFacade.removeItemFromCart(userID, storeID, itemID);
    }

    public Cart changeItemQuantityInCart(int userID, int storeID, int itemID, int quantity) throws Exception {
        return userFacade.changeItemQuantityInCart(userID, storeID, itemID, quantity);
    }

    /**
     * this method is used to show the costumer all the stores he added,
     * he can choose one of them and see what is inside with getItemsInBasket
     *
     * @return List<String> @TODO maybe should be of some kind of object?
     */
    public List<String> getStoresOfBaskets(int userID) throws Exception {
        return userFacade.getStoresOfBaskets(userID);
    }

    public HashMap<CatalogItem, CartItemInfo> getItemsInBasket(int userID, String storeName) throws Exception {
        return userFacade.getItemsInBasket(userID, storeName);
    }

    public Cart buyCart(int userID, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        return userFacade.buyCart(userID, purchaseInfo, supplyInfo);
    }

    public boolean payForBid(int storeID, int bidID, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        return storeFacade.payForBid(storeID, bidID, purchaseInfo, supplyInfo);
    }

    /**
     * empties the cart
     */
    public Cart emptyCart(int userID) throws Exception {
        return userFacade.emptyCart(userID);
    }

    public Store getStoreInfo(int storeID) throws Exception {
        return storeFacade.getStore(storeID);
    }

    public CatalogItem addItemToStore(int storeID, String itemName, double itemPrice, String itemCategory, double weight) throws Exception {
        return storeFacade.addCatalogItem(storeID, itemName, itemPrice, itemCategory, weight);
    }

    public CatalogItem removeItemFromStore(int storeID, int itemID) throws Exception {
        return storeFacade.removeItemFromStore(storeID, itemID);
    }

    public String updateItemName(int storeID, int itemID, String newName) throws Exception {
        return storeFacade.updateItemName(storeID, itemID, newName);
    }

    public Boolean checkIfStoreOwner(int userId, int storeID) throws Exception {
        return storeFacade.checkIfStoreOwner(userId, storeID);
    }

    public Boolean checkIfStoreManager(int userID, int storeID) throws Exception {
        return storeFacade.checkIfStoreManager(userID, storeID);
    }

    public Boolean reopenStore(int userID, int storeID) throws Exception {
        return storeFacade.reopenStore(userID, storeID);
    }

    public Boolean closeStore(int userID, int storeID) throws Exception {
        return storeFacade.closeStore(userID, storeID);
    }

    public void addManagerPermission(int userID, int storeID, int manager, Set<String> permission) throws Exception {
        userFacade.addManagerPermission(userID, storeID, manager, permission);
    }

    public void removeManagerPermission(int userID, int storeID, int manager, Set<String> permission) throws Exception {
        userFacade.removeManagerPermission(userID, storeID, manager, permission);
    }

    public boolean sendMessage(int senderID, int receiverID, String content) throws Exception {
        if (storeFacade.isStoreExists(senderID)) {
            storeFacade.sendMessage(senderID, receiverID, content);
            return true;
        }
        if (userFacade.userExists(senderID)) {
            userFacade.sendMessage(senderID, receiverID, content);
            return true;
        }

        return false;
    }


    public ConcurrentHashMap<Integer, Chat> getChats(int id) throws Exception {
        if (storeFacade.isStoreExists(id)) {
            return storeFacade.getChats(id);
        }
        if (userFacade.userExists(id)) {
            return userFacade.getChats(id);
        }

        throw new Exception("The given id is invalid!");
    }

    public boolean setMailboxAsUnavailable(int storeID) throws Exception {
        if (storeFacade.isStoreExists(storeID)) {
            storeFacade.setMailboxAsUnavailable(storeID);
            return true;
        }

        return false;
    }

    public boolean setMailboxAsAvailable(int storeID) throws Exception {
        if (storeFacade.isStoreExists(storeID)) {
            storeFacade.setMailboxAsAvailable(storeID);
            return true;
        }

        return false;
    }


    //Yonatan added boolean, don't delete
    public boolean addItemAmount(int storeID, int itemID, int amountToAdd) throws Exception {
        return storeFacade.addItemAmount(storeID, itemID, amountToAdd);
    }

    public List<Receipt> getSellingHistoryOfStoreForManager(int storeId, int userId) throws Exception {
        if (storeFacade.checkIfStoreManager(userId, storeId) || storeFacade.checkIfStoreOwner(userId, storeId) || isAdmin(userId))
            return storeFacade.getStore(storeId).getReceiptHandler().getAllReceipts();
        return null;
    }

    public Map<Integer, Store> getAllStores() {
        return storeFacade.getAllStores();
    }

    public Map<Integer, RegisteredUser> getAllRegisteredUsers() {
        return userFacade.getAllRegisteredUsers();
    }

    public Map<Integer, Store> getStoresIOwn(int ownerId) throws Exception {
        ArrayList<Integer> storesIds = userFacade.getStoresIdsIOwn(ownerId);
        Map<Integer, Store> result = new HashMap<>();
        for (Integer storeId : storesIds) {
            result.put(storeId, storeFacade.getStore(storeId));
        }
        return result;
    }

    public Map<Integer, Store> getStoresIManage(int managerID) throws Exception {
        List<Integer> storesIds = userFacade.getStoresIdsIManage(managerID);
        Map<Integer, Store> result = new HashMap<>();
        for (Integer storeId : storesIds) {
            result.put(storeId, storeFacade.getStore(storeId));
        }
        return result;
    }

    public int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale) throws Exception {
        return storeFacade.addVisibleItemsDiscount(storeID, itemsIDs, percent, endOfSale);
    }

    public int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale) throws Exception {
        return storeFacade.addVisibleCategoryDiscount(storeID, category, percent, endOfSale);
    }

    public int addVisibleStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception {
        return storeFacade.addVisibleStoreDiscount(storeID, percent, endOfSale);
    }

    public int addConditionalItemsDiscount(int storeID, double percent, Calendar endOfSale, List<Integer> itemsIDs) throws Exception {
        return storeFacade.addConditionalItemsDiscount(storeID, percent, endOfSale, itemsIDs);
    }

    public int addConditionalCategoryDiscount(int storeID, double percent, Calendar endOfSale, String category) throws Exception {
        return storeFacade.addConditionalCategoryDiscount(storeID, percent, endOfSale, category);
    }

    public int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception {
        return storeFacade.addConditionalStoreDiscount(storeID, percent, endOfSale);
    }

    public int addHiddenItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, String coupon, Calendar endOfSale) throws Exception {
        return storeFacade.addHiddenItemsDiscount(storeID, itemsIDs, percent, coupon, endOfSale);
    }

    public int addHiddenCategoryDiscount(int storeID, String category, double percent, String coupon, Calendar endOfSale) throws Exception {
        return storeFacade.addHiddenCategoryDiscount(storeID, category, percent, coupon, endOfSale);
    }

    public int addHiddenStoreDiscount(int storeID, double percent, String coupon, Calendar endOfSale) throws Exception {
        return storeFacade.addHiddenStoreDiscount(storeID, percent, coupon, endOfSale);
    }


    public String addDiscountBasketTotalPriceRule(int storeID, int discountID, double minimumPrice) throws Exception {
        return storeFacade.addDiscountBasketTotalPriceRule(storeID, discountID, minimumPrice);
    }

    public String addDiscountQuantityRule(int storeID, int discountID, Map<Integer, Integer> itemsAmounts) throws Exception {
        return storeFacade.addDiscountQuantityRule(storeID, discountID, itemsAmounts);
    }

    public String addDiscountComposite(int storeID, int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs) throws Exception {
        return storeFacade.addDiscountComposite(storeID, discountID, logicalComposite, logicalComponentsIDs);
    }

    public String finishConditionalDiscountBuilding(int storeID, int discountID) throws Exception {
        return storeFacade.finishConditionalDiscountBuilding(storeID, discountID);
    }

    public int wrapDiscounts(int storeID, List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum) throws Exception {
        return storeFacade.wrapDiscounts(storeID, discountsIDsToWrap, numericCompositeEnum);
    }

    public String addPurchasePolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception {
        return storeFacade.addPurchasePolicyBasketWeightLimitRule(storeID, basketWeightLimit);
    }

    public String addPurchasePolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception {
        return storeFacade.addPurchasePolicyBuyerAgeRule(storeID, minimumAge);
    }

    public String addPurchasePolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception {
        return storeFacade.addPurchasePolicyForbiddenCategoryRule(storeID, forbiddenCategory);
    }

    public String addPurchasePolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception {
        return storeFacade.addPurchasePolicyForbiddenDatesRule(storeID, forbiddenDates);
    }

    public String addPurchasePolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception {
        return storeFacade.addPurchasePolicyForbiddenHoursRule(storeID, startHour, endHour);
    }

    public String addPurchasePolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception {
        return storeFacade.addPurchasePolicyMustDatesRule(storeID, mustDates);
    }

    public String addPurchasePolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception {
        return storeFacade.addPurchasePolicyItemsWeightLimitRule(storeID, weightsLimits);
    }

    public String addPurchasePolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception {
        return storeFacade.addPurchasePolicyBasketTotalPriceRule(storeID, minimumPrice);
    }

    public String addPurchasePolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception {
        return storeFacade.addPurchasePolicyMustItemsAmountsRule(storeID, itemsAmounts);
    }

    public String wrapPurchasePolicies(int storeID, List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception {
        return storeFacade.wrapPurchasePolicies(storeID, purchasePoliciesIDsToWrap, logicalCompositeEnum);
    }

    public String addDiscountPolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception {
        return storeFacade.addDiscountPolicyBasketWeightLimitRule(storeID, basketWeightLimit);
    }

    public String addDiscountPolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception {
        return storeFacade.addDiscountPolicyBuyerAgeRule(storeID, minimumAge);
    }

    public String addDiscountPolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception {
        return storeFacade.addDiscountPolicyForbiddenCategoryRule(storeID, forbiddenCategory);
    }

    public String addDiscountPolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception {
        return storeFacade.addDiscountPolicyForbiddenDatesRule(storeID, forbiddenDates);
    }

    public String addDiscountPolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception {
        return storeFacade.addDiscountPolicyForbiddenHoursRule(storeID, startHour, endHour);
    }

    public String addDiscountPolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception {
        return storeFacade.addDiscountPolicyMustDatesRule(storeID, mustDates);
    }

    public String addDiscountPolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception {
        return storeFacade.addDiscountPolicyItemsWeightLimitRule(storeID, weightsLimits);
    }

    public String addDiscountPolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception {
        return storeFacade.addDiscountPolicyBasketTotalPriceRule(storeID, minimumPrice);
    }

    public String addDiscountPolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception {
        return storeFacade.addDiscountPolicyMustItemsAmountsRule(storeID, itemsAmounts);
    }

    public String wrapDiscountPolicies(int storeID, List<Integer> discountPoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception {
        return storeFacade.wrapDiscountPolicies(storeID, discountPoliciesIDsToWrap, logicalCompositeEnum);
    }

    public Map<Integer, Discount> getStoreDiscounts(int storeID) throws Exception {
        return storeFacade.getStoreDiscounts(storeID);
    }

    public int removeDiscount(int storeID, int discountID) throws Exception {
        return storeFacade.removeDiscount(storeID, discountID);
    }

    public int removePolicy(int storeID, int policyID) throws Exception {
        return storeFacade.removePolicy(storeID, policyID);
    }

    public int removeDiscountPolicy(int storeID, int policyID) throws Exception {
        return storeFacade.removeDiscountPolicy(storeID, policyID);
    }

    public Map<Integer, Visible> getStoreVisibleDiscounts(int storeID) throws Exception {
        return storeFacade.getStoreVisibleDiscounts(storeID);
    }

    public Map<Integer, PurchasePolicy> getStorePurchasePolicies(int storeID) throws Exception {
        return storeFacade.getStorePurchasePolicies(storeID);
    }

    public Map<Integer, DiscountPolicy> getStoreDiscountPolicies(int storeID) throws Exception {
        return storeFacade.getStoreDiscountPolicies(storeID);
    }

    public Map<Integer, List<Bid>> getUserBidsToReply(int userID) throws Exception {
        return storeFacade.getUserBidsToReply(userID);
    }

    public Bid addBid(int storeID, int itemID, int userID, double offeredPrice) throws Exception {
        if (!userFacade.userExists(userID)) {
            throw new Exception("User does not exist!");
        }
        return storeFacade.addBid(storeID, itemID, userID, offeredPrice);
    }

    public List<Bid> getUserBids(int userID) {
        return storeFacade.getUserBids(userID);
    }

    public boolean approve(int storeID, int bidID, int replierUserID) throws Exception {
        BidReplies reply = storeFacade.approve(storeID, bidID, replierUserID);
        switch (reply) {
            case REJECTED, COUNTERED -> {
                return false;
            }
            case APPROVED -> {
                //add to cart
                //
                return true;
            }
        }
        throw new RuntimeException("Illegeal Reply from approve");
    }

    public boolean replyToCounterOffer(int storeID, int bidID, boolean accepted) throws Exception {
        return storeFacade.replyToCounterOffer(storeID, bidID, accepted);
    }

    public boolean reject(int storeID, int bidID, int replierUserID) throws Exception {
        return storeFacade.reject(storeID, bidID, replierUserID);
    }

    public boolean counterOffer(int storeID, int bidID, int replierUserID, double counterOffer) throws Exception {
        return storeFacade.counterOffer(storeID, bidID, replierUserID, counterOffer);
    }

    public Map<RegisteredUser, Set<Integer>> getAllOwnersIDefined(int ownerId) throws Exception {
        return userFacade.getAllOwnersIDefined(ownerId);
    }

    public Map<RegisteredUser, Set<Integer>> getAllManagersIDefined(int ownerId) throws Exception {
        return userFacade.getAllManagersIDefined(ownerId);
    }

    public NotificationHub getNotificationHub() {
        return notificationHub;
    }

    public Map<Integer, RegisteredUser> getLoggedInUsers() {
        return userFacade.getLoggedInUsers();
    }

    public Map<Integer, RegisteredUser> getLoggedOutUsers() {
        return userFacade.getLoggedOutUsers();
    }

    public void addCouponToCart(int userId, String coupon) throws Exception {
        userFacade.addCouponToCart(userId, coupon);
    }

    public List<String> getCoupons(int userId) throws Exception {
        return userFacade.getCoupons(userId);
    }

    public void removeCouponFromCart(int userId, String coupon) throws Exception {
        userFacade.removeCouponFromCart(userId, coupon);
    }

    public String getStoreName(int storeId) throws Exception {
        return storeFacade.getStore(storeId).getStoreName();
    }

    public Basket removeBasketFromCart(int userID, int storeID) throws Exception {
        return userFacade.removeBasketFromCart(userID, storeID);
    }

    public int getUserIdByName(String name) throws Exception {
        return userFacade.findUserByUsername(name);
    }

    public int getStoreIdByName(String name) throws Exception {
        int id = storeFacade.getIdByStoreName(name);

        if (id == -1) {
            throw new Exception("ERROR: the searched store wa not found!");
        }

        return id;
    }

    public String getNameById(int id) {
        String name = storeFacade.getStoreNameById(id);

        if (name == null) {
            name = userFacade.getUsernameById(id);
        }

        return name;
    }

    public String getUserAddress(int id) {
        return userFacade.getUser(id).getAddress();
    }

    public LocalDate getUserBDay(int id) {
        return userFacade.getUser(id).getbDay();
    }


    public void cancelBid(int storeId, int id) throws Exception {
        storeFacade.cancelBid(storeId, id);
    }

    public void addAppointment(int storeID, int creatorId, int newOwnerId) throws Exception {
        if (!userFacade.userExists(newOwnerId)) {
            throw new Exception("User does not exist!");
        }
        storeFacade.addAppointment(storeID, creatorId, newOwnerId);
//        appointmentManager.addAppointment(new Appointment(getOwnersByStore(storeID), creatorId, storeID, newOwnerId));
    }

    public Set<Appointment> getUserAppointments(int userID) throws Exception {
        RegisteredUser user = userFacade.getRegisteredUser(userID);
        return storeFacade.getUserAppointments(user);
    }


    public void removeAppointment(int storeID, int userId) throws Exception {
        storeFacade.removeAppointment(storeID, userId);
    }


    public void acceptAppointment(int storeID, int myId, int theOwnerId) throws Exception {
        if (storeFacade.acceptAppointment(storeID, myId, theOwnerId))
            addOwner(myId, theOwnerId, storeID);
    }

    public void rejectAppointment(int storeID, int theOwnerId) throws Exception {
        storeFacade.rejectAppointment(storeID, theOwnerId);
    }
}
