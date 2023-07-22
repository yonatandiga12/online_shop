package BusinessLayer;

import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.NotificationHub;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.StorePermissions.StoreActionPermissions;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Visible;
import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Policies.PurchasePolicy;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.SystemManager;
import BusinessLayer.Users.UserFacade;

import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static BusinessLayer.Stores.StoreStatus.OPEN;


public class MarketMock {
    private UserFacade userFacade;
    private StoreFacade storeFacade;
    private Map<Integer, SystemManager> systemManagerMap;
    private NotificationHub notificationHub;

    public MarketMock() throws Exception {
        systemManagerMap = new HashMap<>();
        userFacade = new UserFacade();
        storeFacade = new StoreFacade();
        notificationHub = new NotificationHub();
        createFirstAdmin();
        userFacade.setGuest();
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
        userFacade.createAdmin(this);
    }

    public void addAdmin(int newAdmin, SystemManager systemManager) {
        systemManagerMap.put(newAdmin, systemManager);
    }

    public int register(String username, String pass) throws Exception {
        return userFacade.registerUser(username, pass, this);
    }

    public int login(String username, String pass) throws Exception {
        return userFacade.logIn(username, pass);
    }

    public boolean logout(int userID) throws Exception {

        return userFacade.logout(userID);
    }

    public void addOwner(int userID, int userToAddID, int storeID) throws Exception {
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

    public boolean closeStorePermanently(int userID, int storeID) throws Exception
    {
        if (isAdmin(userID)) {
            SystemManager systemManager = systemManagerMap.get(userID);
            systemManager.closeStorePermanently(storeFacade.getStore(storeID));
            return true;
        }
        else {
            throw new RuntimeException("Only admin can close stores permanently");
        }
    }

    public boolean isAdmin(int userID) {
        return systemManagerMap.get(userID) != null;
    }

    public void removeUser(int userID, int userToRemove) throws Exception {

        if (isAdmin(userID)) {
            if(!systemManagerMap.containsKey(userID))
                throw new Exception("systemManagerMap cant find userID");
            SystemManager systemManager = systemManagerMap.get(userID);
            if (systemManager==null)
                throw new Exception("systemManager is Null");
            if (userFacade.getRegisteredUser(userToRemove)==null)
                throw new Exception("userToRemove is null!");
            systemManager.removeUser(userFacade.getRegisteredUser(userToRemove));
        }
        else
            throw new RuntimeException("Only System admin can remove a user");
    }

    public int addStore(int founderID, String name) throws Exception {
        //bc of two-way dependency: store is created with only founder ID then when founder receives store pointer he adds himself to owner list
        Store store = storeFacade.addStore(founderID, name, this);
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

    /**
     * empties the cart
     */
    public Cart emptyCart(int userID) throws Exception {
        return userFacade.emptyCart(userID);
    }

    public Store getStoreInfo(int storeID) throws Exception
    {
        return storeFacade.getStore(storeID);
    }

    public CatalogItem addItemToStore(int storeID, String itemName, double itemPrice, String itemCategory, double weight) throws Exception
    {
        return storeFacade.addCatalogItem(storeID, itemName, itemPrice, itemCategory, weight);
    }

    public CatalogItem removeItemFromStore(int storeID, int itemID) throws Exception
    {
        return storeFacade.removeItemFromStore(storeID, itemID);
    }

    public String updateItemName(int storeID, int itemID, String newName) throws Exception
    {
        return storeFacade.updateItemName(storeID, itemID, newName);
    }

    public Boolean checkIfStoreOwner(int userId, int storeID) throws Exception
    {
        return storeFacade.checkIfStoreOwner(userId, storeID);
    }

    public Boolean checkIfStoreManager(int userID, int storeID) throws Exception
    {
        return storeFacade.checkIfStoreManager(userID, storeID);
    }

    public Boolean reopenStore(int userID, int storeID) throws Exception
    {
        return storeFacade.reopenStore(userID, storeID);
    }

    public Boolean closeStore(int userID, int storeID) throws Exception
    {
        return storeFacade.closeStore(userID, storeID);
    }

    public boolean sendMessage(int senderID, int receiverID, String content) throws Exception{
        if(storeFacade.isStoreExists(senderID)){
            storeFacade.sendMessage(senderID, receiverID, content);
            return true;
        }
        if(userFacade.userExists(senderID)){
            userFacade.sendMessage(senderID, receiverID, content);
            return true;
        }

        return false;
    }

//    public void markMessageAsRead(int ID, Message message) throws Exception {
//        if(storeFacade.isStoreExists(ID)){
//            storeFacade.markMessageAsRead(ID, message);
//        }
//        if(userFacade.userExists(ID)){
//            userFacade.markMessageAsRead(ID, message);
//        }
//    }
//
//    public void markMessageAsNotRead(int ID, Message message) throws Exception {
//        if(storeFacade.isStoreExists(ID)){
//            storeFacade.markMessageAsNotRead(ID, message);
//        }
//        if(userFacade.userExists(ID)){
//            userFacade.markMessageAsNotRead(ID, message);
//        }
//    }
//
//    public List<Message> watchNotReadMessages(int ID) throws Exception
//    {
//        if(storeFacade.isStoreExists(ID)){
//            return storeFacade.watchNotReadMessages(ID);
//        }
//        if(userFacade.userExists(ID)){
//            return userFacade.watchNotReadMessages(ID);
//        }
//
//        return null;
//    }
//
//    public List<Message> watchReadMessages(int ID) throws Exception
//    {
//        if(storeFacade.isStoreExists(ID)){
//            return storeFacade.watchNotReadMessages(ID);
//        }
//        if(userFacade.userExists(ID)){
//            return userFacade.watchReadMessages(ID);
//        }
//
//        return null;
//    }
//
//    public List<Message> watchSentMessages(int ID) throws Exception
//    {
//        if(storeFacade.isStoreExists(ID)){
//            return storeFacade.watchSentMessages(ID);
//        }
//        if(userFacade.userExists(ID)){
//            return userFacade.watchSentMessages(ID);
//        }
//
//        return null;
//    }

    public ConcurrentHashMap<Integer, Chat> getChats(int id) throws Exception {
        if(storeFacade.isStoreExists(id)){
            return storeFacade.getChats(id);
        }
        if(userFacade.userExists(id)){
            return userFacade.getChats(id);
        }

        throw new Exception("The given id is invalid!");
    }

    public boolean setMailboxAsUnavailable(int storeID) throws Exception
    {
        if(storeFacade.isStoreExists(storeID)){
            storeFacade.setMailboxAsUnavailable(storeID);
            return true;
        }

        return false;
    }

    public boolean setMailboxAsAvailable(int storeID) throws Exception
    {
        if(storeFacade.isStoreExists(storeID)){
            storeFacade.setMailboxAsAvailable(storeID);
            return true;
        }

        return false;
    }


    //Yonatan added boolean, don't delete
    public boolean addItemAmount(int storeID, int itemID, int amountToAdd) throws Exception
    {
        return storeFacade.addItemAmount(storeID, itemID, amountToAdd);
    }

    public List<Receipt> getSellingHistoryOfStoreForManager(int storeId, int userId) throws Exception {
        if(storeFacade.checkIfStoreManager(userId, storeId) || isAdmin(userId))
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
        for(Integer storeId: storesIds){
            result.put(storeId, storeFacade.getStore(storeId));
        }
        return result;
    }

    public Map<Integer, Store> getStoresIManage(int managerID) throws Exception {
        List<Integer> storesIds = userFacade.getStoresIdsIManage(managerID);
        Map<Integer, Store> result = new HashMap<>();
        for(Integer storeId: storesIds){
            result.put(storeId, storeFacade.getStore(storeId));
        }
        return result;
    }

    public int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale) throws Exception
    {
        return storeFacade.addVisibleItemsDiscount(storeID, itemsIDs, percent, endOfSale);
    }
    public int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale) throws Exception
    {
        return storeFacade.addVisibleCategoryDiscount(storeID, category, percent, endOfSale);
    }
    public int addVisibleStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception
    {
        return storeFacade.addVisibleStoreDiscount(storeID, percent, endOfSale);
    }
    public int addConditionalItemsDiscount(int storeID, double percent, Calendar endOfSale, List<Integer> itemsIDs) throws Exception
    {
        return storeFacade.addConditionalItemsDiscount(storeID, percent, endOfSale, itemsIDs);
    }
    public int addConditionalCategoryDiscount(int storeID, double percent, Calendar endOfSale, String category) throws Exception
    {
        return storeFacade.addConditionalCategoryDiscount(storeID, percent, endOfSale, category);
    }
    public int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception
    {
        return storeFacade.addConditionalStoreDiscount(storeID, percent, endOfSale);
    }
    public int addHiddenItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        return storeFacade.addHiddenItemsDiscount(storeID, itemsIDs, percent, coupon, endOfSale);
    }
    public int addHiddenCategoryDiscount(int storeID, String category, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        return storeFacade.addHiddenCategoryDiscount(storeID, category, percent, coupon, endOfSale);
    }
    public int addHiddenStoreDiscount(int storeID, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        return storeFacade.addHiddenStoreDiscount(storeID, percent, coupon, endOfSale);
    }


    public String addDiscountBasketTotalPriceRule(int storeID, int discountID, double minimumPrice) throws Exception
    {
        return storeFacade.addDiscountBasketTotalPriceRule(storeID, discountID, minimumPrice);
    }
    public String addDiscountQuantityRule(int storeID, int discountID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        return storeFacade.addDiscountQuantityRule(storeID, discountID, itemsAmounts);
    }
    public String addDiscountComposite(int storeID, int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs) throws Exception
    {
        return storeFacade.addDiscountComposite(storeID, discountID, logicalComposite, logicalComponentsIDs);
    }
    public String finishConditionalDiscountBuilding(int storeID, int discountID) throws Exception
    {
        return storeFacade.finishConditionalDiscountBuilding(storeID, discountID);
    }
    public int wrapDiscounts(int storeID, List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum) throws Exception
    {
        return storeFacade.wrapDiscounts(storeID, discountsIDsToWrap, numericCompositeEnum);
    }

    public String addPurchasePolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception
    {
        return storeFacade.addPurchasePolicyBasketWeightLimitRule(storeID, basketWeightLimit);
    }
    public String addPurchasePolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception
    {
        return storeFacade.addPurchasePolicyBuyerAgeRule(storeID, minimumAge);
    }
    public String addPurchasePolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception
    {
        return storeFacade.addPurchasePolicyForbiddenCategoryRule(storeID, forbiddenCategory);
    }
    public String addPurchasePolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception
    {
        return storeFacade.addPurchasePolicyForbiddenDatesRule(storeID, forbiddenDates);
    }
    public String addPurchasePolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception
    {
        return storeFacade.addPurchasePolicyForbiddenHoursRule(storeID, startHour, endHour);
    }
    public String addPurchasePolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception
    {
        return storeFacade.addPurchasePolicyMustDatesRule(storeID, mustDates);
    }
    public String addPurchasePolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception
    {
        return storeFacade.addPurchasePolicyItemsWeightLimitRule(storeID, weightsLimits);
    }
    public String addPurchasePolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception
    {
        return storeFacade.addPurchasePolicyBasketTotalPriceRule(storeID, minimumPrice);
    }
    public String addPurchasePolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        return storeFacade.addPurchasePolicyMustItemsAmountsRule(storeID, itemsAmounts);
    }
    public String wrapPurchasePolicies(int storeID, List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception
    {
        return storeFacade.wrapPurchasePolicies(storeID, purchasePoliciesIDsToWrap, logicalCompositeEnum);
    }

    public String addDiscountPolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception
    {
        return storeFacade.addDiscountPolicyBasketWeightLimitRule(storeID, basketWeightLimit);
    }
    public String addDiscountPolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception
    {
        return storeFacade.addDiscountPolicyBuyerAgeRule(storeID, minimumAge);
    }
    public String addDiscountPolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception
    {
        return storeFacade.addDiscountPolicyForbiddenCategoryRule(storeID, forbiddenCategory);
    }
    public String addDiscountPolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception
    {
        return storeFacade.addDiscountPolicyForbiddenDatesRule(storeID, forbiddenDates);
    }
    public String addDiscountPolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception
    {
        return storeFacade.addDiscountPolicyForbiddenHoursRule(storeID, startHour, endHour);
    }
    public String addDiscountPolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception
    {
        return storeFacade.addDiscountPolicyMustDatesRule(storeID, mustDates);
    }
    public String addDiscountPolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception
    {
        return storeFacade.addDiscountPolicyItemsWeightLimitRule(storeID, weightsLimits);
    }
    public String addDiscountPolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception
    {
        return storeFacade.addDiscountPolicyBasketTotalPriceRule(storeID, minimumPrice);
    }
    public String addDiscountPolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        return storeFacade.addDiscountPolicyMustItemsAmountsRule(storeID, itemsAmounts);
    }
    public String wrapDiscountPolicies(int storeID, List<Integer> discountPoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception
    {
        return storeFacade.wrapDiscountPolicies(storeID, discountPoliciesIDsToWrap, logicalCompositeEnum);
    }

    public Map<Integer, Discount> getStoreDiscounts(int storeID) throws Exception
    {
        return storeFacade.getStoreDiscounts(storeID);
    }

    public Map<Integer, Visible> getStoreVisibleDiscounts(int storeID) throws Exception
    {
        return storeFacade.getStoreVisibleDiscounts(storeID);
    }

    public Map<Integer, PurchasePolicy> getStorePurchasePolicies(int storeID) throws Exception
    {
        return storeFacade.getStorePurchasePolicies(storeID);
    }
    public Map<Integer, DiscountPolicy> getStoreDiscountPolicies(int storeID) throws Exception
    {
        return storeFacade.getStoreDiscountPolicies(storeID);
    }

    public Map<RegisteredUser, Set<Integer>> getAllOwnersIDefined(int ownerId) throws Exception {
        return userFacade.getAllOwnersIDefined(ownerId);
    }

    public Map<RegisteredUser, Set<Integer>> getAllManagersIDefined(int ownerId) throws Exception {
        return userFacade.getAllManagersIDefined(ownerId);
    }

    public NotificationHub getNotificationHub(){
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

    public void removeCouponFromCart(int userId, String coupon) throws Exception {
        userFacade.removeCouponFromCart(userId, coupon);
    }
}