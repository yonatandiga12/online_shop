package BusinessLayer.Stores;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.MarketMock;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.StorePermissions.StoreActionPermissions;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Visible;
import BusinessLayer.Stores.Policies.PurchasePolicy;
import BusinessLayer.Users.RegisteredUser;
import DataAccessLayer.AppointmentDAO;
import DataAccessLayer.StoreDAO;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static Globals.SearchFilter.STORE_RATING;

public class StoreFacade {
    private Map<Integer, Store> stores;
    private final Set<String> categoryPool;
    private int storesIDs;
    private int itemsIDs;
    StoreDAO storeDAO;

    public StoreFacade() throws Exception {
        this.categoryPool = new HashSet<>();
        this.storesIDs = 0;
        this.itemsIDs = 0;
        this.stores = new HashMap<>();
    }

    public Store addStore(int founderID, String name) throws Exception
    {
        Store newStore = new Store(storesIDs, founderID, name);
        stores.put(storesIDs++, newStore);
        storeDAO.save(newStore);
        return newStore;
    }
    public Store addStore(int founderID, String name, MarketMock marketMock) throws Exception
    {
        Store newStore = new Store(storesIDs, founderID, name, marketMock);
        stores.put(storesIDs++, newStore);
        return newStore;
    }

    public void setStoreName(int storeID, String storeName) throws Exception
    {
        Store store = getStore(storeID);
        store.setStoreName(storeName);
    }

    public Store getStore(int storeID) throws Exception
    {
        Store store = stores.get(storeID);
        if (store == null)
            throw new Exception("No store with ID: " + storeID);
        return store;
    }

    public CatalogItem getItem(int storeID, int itemID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getItem(itemID);
    }
    public CatalogItem addCatalogItem(int storeID, String itemName, double itemPrice, String itemCategory, double weight) throws Exception
    {
        Store store = getStore(storeID);
        if (itemPrice <= 0)
            throw new Exception("Item price has to be positive but is " + itemPrice);
        if (weight <= 0)
            throw new Exception("Item weight has to be positive but is " + weight);
        categoryPool.add(itemCategory);
        return store.addCatalogItem(itemsIDs++, itemName, itemPrice, itemCategory, weight);
    }
    public int getItemAmount(int storeID, int itemID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getItemAmount(itemID);
    }
    public boolean addItemAmount(int storeID, int itemID, int amountToAdd) throws Exception
    {
        Store store = getStore(storeID);
        if (amountToAdd <= 0)
        {
            return false;
        }
        store.addItemAmount(itemID, amountToAdd);

        //Yonatan added it, don't delete
        return true;
    }

    public Map<Integer, List<Bid>> getUserBidsToReply(int userID) throws Exception
    {
        Map<Integer, List<Bid>> result = new HashMap<>();
        List<Bid> bids;
        for (Store store : stores.values())
        {
            bids = store.getUserBidsToReply(userID);
            if (bids.size() > 0)
                result.put(store.getStoreID(), bids);
        }
        return result;
    }

    public Bid addBid(int storeID, int itemID, int userID, double offeredPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.addBid(itemID, userID, offeredPrice);
    }

    public List<Bid> getUserBids(int userID)
    {
        List<Bid> bids = new ArrayList<>();
        for (Store store : stores.values()) {
            bids.addAll(store.getUserBids(userID));
        }
        return bids;
    }

    public void addLottery(int storeID, int itemID, double price, int lotteryPeriodInDays) throws Exception
    {
        Store store = getStore(storeID);
        store.addLottery(itemID, price, lotteryPeriodInDays);
    }
    public void addAuction(int storeID, int itemID, double initialPrice, int auctionPeriodInDays) throws Exception
    {
        Store store = getStore(storeID);
        store.addAuction(itemID, initialPrice, auctionPeriodInDays);
    }
    public boolean participateInLottery(int storeID, int lotteryID, int userID, double offerPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.participateInLottery(lotteryID, userID, offerPrice);
    }
    public boolean offerToAuction(int storeID, int auctionID, int userID, double offerPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.offerToAuction(auctionID, userID, offerPrice);
    }
    public BidReplies approve(int storeID, int bidID, int replierUserID) throws Exception
    {
        Store store = getStore(storeID);
        return store.approve(bidID, replierUserID);
    }

    public boolean replyToCounterOffer(int storeID, int bidID, boolean accepted) throws Exception
    {
        Store store = getStore(storeID);
        return store.replyToCounterOffer(bidID, accepted);
    }

    public boolean reject(int storeID, int bidID, int replierUserID) throws Exception
    {
        Store store = getStore(storeID);
        return store.reject(bidID, replierUserID);
    }
    public boolean counterOffer(int storeID, int bidID, int replierUserID, double counterOffer) throws Exception
    {
        Store store = getStore(storeID);
        return store.counterOffer(bidID, replierUserID, counterOffer);
    }

    public boolean reopenStore(int userID, int storeID) throws Exception {
        return getStore(storeID).reopenStore(userID);
    }

    public boolean closeStore(int userID, int storeID) throws Exception {
        return getStore(storeID).closeStore(userID);
    }

    public boolean closeStorePermanently(int storeID) throws Exception {
        return getStore(storeID).closeStorePermanently();
    }
    public int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addVisibleItemsDiscount(itemsIDs, percent, endOfSale);
    }
    public int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (!categoryPool.contains(category))
        {
            throw new Exception("Error: Category " + category + " does not exists");
        }
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addVisibleCategoryDiscount(category, percent, endOfSale);
    }
    public int addVisibleStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addVisibleStoreDiscount(percent, endOfSale);
    }
    public int addConditionalItemsDiscount(int storeID, double percent, Calendar endOfSale, List<Integer> itemsIDs) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addConditionalItemsDiscount(percent, endOfSale, itemsIDs);
    }
    public int addConditionalCategoryDiscount(int storeID, double percent, Calendar endOfSale, String category) throws Exception
    {
        Store store = getStore(storeID);
        if (!categoryPool.contains(category))
        {
            throw new Exception("Error: Category " + category + " does not exists");
        }
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addConditionalCategoryDiscount(percent, endOfSale, category);
    }
    public int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addConditionalStoreDiscount(percent, endOfSale);
    }
    public int addHiddenItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addHiddenItemsDiscount(itemsIDs, percent, coupon, endOfSale);
    }
    public int addHiddenCategoryDiscount(int storeID, String category, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (!categoryPool.contains(category))
        {
            throw new Exception("Error: Category " + category + " does not exists");
        }
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addHiddenCategoryDiscount(category, percent, coupon, endOfSale);
    }
    public int addHiddenStoreDiscount(int storeID, double percent, String coupon, Calendar endOfSale) throws Exception
    {
        Store store = getStore(storeID);
        if (percent < 0)
        {
            throw new Exception("Error: Percent " + percent + " is negative");
        }
        if (percent >100)
        {
            throw new Exception("Error: Percent " + percent + " is over 100");
        }
        if (!endOfSale.after(Calendar.getInstance()))
        {
            throw new Exception("Error: End of sale date " + getDateString(endOfSale) + " has already passed");
        }
        return store.addHiddenStoreDiscount(percent, coupon, endOfSale);
    }


    public String addDiscountBasketTotalPriceRule(int storeID, int discountID, double minimumPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountBasketTotalPriceRule(discountID, minimumPrice);
    }
    public String addDiscountQuantityRule(int storeID, int discountID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountQuantityRule(discountID, itemsAmounts);
    }
    public String addDiscountComposite(int storeID, int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountComposite(discountID, logicalComposite, logicalComponentsIDs);
    }
    public String finishConditionalDiscountBuilding(int storeID, int discountID) throws Exception
    {
        Store store = getStore(storeID);
        return store.finishConditionalDiscountBuilding(discountID);
    }
    public int wrapDiscounts(int storeID, List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum) throws Exception
    {
        Store store = getStore(storeID);
        return store.wrapDiscounts(discountsIDsToWrap, numericCompositeEnum);
    }

    public String addPurchasePolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception
    {
        Store store = getStore(storeID);
        return store.addPurchasePolicyBasketWeightLimitRule(basketWeightLimit);
    }
    public String addPurchasePolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception
    {
        Store store = getStore(storeID);
        return store.addPurchasePolicyBuyerAgeRule(minimumAge);
    }
    public String addPurchasePolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception
    {
        Store store = getStore(storeID);
        if (!categoryPool.contains(forbiddenCategory))
        {
            categoryPool.add(forbiddenCategory);
            //throw new Exception("Error: Category " + forbiddenCategory + " does not exists");
        }
        return store.addPurchasePolicyForbiddenCategoryRule(forbiddenCategory);
    }
    public String addPurchasePolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception
    {
        Store store = getStore(storeID);
        if (checkDatesDidNotPassed(forbiddenDates))
        {
            throw new Exception("One or more of the dates you chose have already passed");
        }
        return store.addPurchasePolicyForbiddenDatesRule(forbiddenDates);
    }
    public String addPurchasePolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception
    {
        Store store = getStore(storeID);
        return store.addPurchasePolicyForbiddenHoursRule(startHour, endHour);
    }
    public String addPurchasePolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception
    {
        Store store = getStore(storeID);
        if (checkDatesDidNotPassed(mustDates))
        {
            throw new Exception("One or more of the dates you chose have already passed");
        }
        return store.addPurchasePolicyMustDatesRule(mustDates);
    }
    public String addPurchasePolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception
    {
        Store store = getStore(storeID);
        for (Double weight : weightsLimits.values())
        {
            if (weight <= 0) {
                throw new Exception("Error: One or more of the weights limits you entered are not positive");
            }
        }
        return store.addPurchasePolicyItemsWeightLimitRule(weightsLimits);
    }
    public String addPurchasePolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.addPurchasePolicyBasketTotalPriceRule(minimumPrice);
    }
    public String addPurchasePolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        Store store = getStore(storeID);
        for (Integer amount : itemsAmounts.values())
        {
            if (amount <= 0) {
                throw new Exception("Error: One or more of the amounts you entered are not positive");
            }
        }
        return store.addPurchasePolicyMustItemsAmountsRule(itemsAmounts);
    }
    public String wrapPurchasePolicies(int storeID, List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception
    {
        Store store = getStore(storeID);
        return store.wrapPurchasePolicies(purchasePoliciesIDsToWrap, logicalCompositeEnum);
    }

    public String addDiscountPolicyBasketWeightLimitRule(int storeID, double basketWeightLimit) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountPolicyBasketWeightLimitRule(basketWeightLimit);
    }
    public String addDiscountPolicyBuyerAgeRule(int storeID, int minimumAge) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountPolicyBuyerAgeRule(minimumAge);
    }
    public String addDiscountPolicyForbiddenCategoryRule(int storeID, String forbiddenCategory) throws Exception
    {
        Store store = getStore(storeID);
        if (!categoryPool.contains(forbiddenCategory))
        {
            throw new Exception("Error: Category " + forbiddenCategory + " does not exists");
        }
        return store.addDiscountPolicyForbiddenCategoryRule(forbiddenCategory);
    }
    public String addDiscountPolicyForbiddenDatesRule(int storeID, List<Calendar> forbiddenDates) throws Exception
    {
        Store store = getStore(storeID);
        if (checkDatesDidNotPassed(forbiddenDates))
        {
            throw new Exception("One or more of the dates you chose have already passed");
        }
        return store.addDiscountPolicyForbiddenDatesRule(forbiddenDates);
    }
    public String addDiscountPolicyForbiddenHoursRule(int storeID, int startHour, int endHour) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountPolicyForbiddenHoursRule(startHour, endHour);
    }
    public String addDiscountPolicyMustDatesRule(int storeID, List<Calendar> mustDates) throws Exception
    {
        Store store = getStore(storeID);
        if (checkDatesDidNotPassed(mustDates))
        {
            throw new Exception("One or more of the dates you chose have already passed");
        }
        return store.addDiscountPolicyMustDatesRule(mustDates);
    }
    public String addDiscountPolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) throws Exception
    {
        Store store = getStore(storeID);
        for (Double weight : weightsLimits.values())
        {
            if (weight <= 0) {
                throw new Exception("Error: One or more of the weights limits you entered are not positive");
            }
        }
        return store.addDiscountPolicyItemsWeightLimitRule(weightsLimits);
    }
    public String addDiscountPolicyBasketTotalPriceRule(int storeID, double minimumPrice) throws Exception
    {
        Store store = getStore(storeID);
        return store.addDiscountPolicyBasketTotalPriceRule(minimumPrice);
    }
    public String addDiscountPolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) throws Exception
    {
        Store store = getStore(storeID);
        for (Integer amount : itemsAmounts.values())
        {
            if (amount <= 0) {
                throw new Exception("Error: One or more of the amounts you entered are not positive");
            }
        }
        return store.addDiscountPolicyMustItemsAmountsRule(itemsAmounts);
    }

    public int removeDiscount(int storeID, int discountID) throws Exception {
        Store store = getStore(storeID);
        return store.removeDiscount(discountID);
    }

    public int removePolicy(int storeID, int policyID) throws Exception {
        Store store = getStore(storeID);
        return store.removePolicy(policyID);
    }

    public int removeDiscountPolicy(int storeID, int policyID) throws Exception {
        Store store = getStore(storeID);
        return store.removeDiscountPolicy(policyID);
    }

    public String wrapDiscountPolicies(int storeID, List<Integer> discountPoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception
    {
        Store store = getStore(storeID);
        return store.wrapDiscountPolicies(discountPoliciesIDsToWrap, logicalCompositeEnum);
    }


    public Map<CatalogItem, Boolean> getCatalog() {
        Map<CatalogItem, Boolean> res = new HashMap<>();
        for (Store store : stores.values()) {
            if (store.getStoreStatus()!=StoreStatus.PERMANENTLY_CLOSE)
                res.putAll(store.getCatalog());
        }
        return res;
    }

    public Map<CatalogItem, Boolean> getCatalog(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) throws Exception {
        Map<CatalogItem, Boolean> res = new HashMap<>();
        Collection<Store> storesToSearch = stores.values();
        if (filters.containsKey(STORE_RATING)) {
            storesToSearch.removeIf(store -> filters.get(STORE_RATING).filter());
            filters.remove(STORE_RATING);
        }
        for (Store store : storesToSearch) {
            res.putAll(store.getCatalog(keywords, searchBy, filters));
        }
        return res;
    }

    public CatalogItem removeItemFromStore(int storeID, int itemID) throws Exception
    {
        Store store = getStore(storeID);
        return store.removeItemFromStore(itemID);
    }

    public String updateItemName(int storeID, int itemID, String newName) throws Exception
    {
        Store store = getStore(storeID);
        return store.updateItemName(itemID, newName);
    }

    public Boolean checkIfStoreOwner(int userID, int storeID) throws Exception {
        return getStore(storeID).checkIfStoreOwner(userID);
    }

    public Boolean checkIfStoreManager(int userID, int storeID) throws Exception {
        return getStore(storeID).checkIfStoreManager(userID);
    }

    public void sendMessage(int storeID, int receiverID, String content) throws Exception
    {
        Store store = getStore(storeID);
        store.sendMessage(receiverID, content);
    }

    public ConcurrentHashMap<Integer, Chat> getChats(int storeID) throws Exception {
        Store store = getStore(storeID);
        return store.getChats();
    }

    public void setMailboxAsUnavailable(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        store.setMailboxAsUnavailable();
    }

    public void setMailboxAsAvailable(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        store.setMailboxAsAvailable();
    }

    public Map<Integer, Store> getAllStores() {
        return stores;
    }

    public Map<Integer, Discount> getStoreDiscounts(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getStoreDiscounts();
    }

    public Map<Integer, Visible> getStoreVisibleDiscounts(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getStoreVisibleDiscounts();
    }
    public Map<Integer, PurchasePolicy> getStorePurchasePolicies(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getStorePurchasePolicies();
    }
    public Map<Integer, DiscountPolicy> getStoreDiscountPolicies(int storeID) throws Exception
    {
        Store store = getStore(storeID);
        return store.getStoreDiscountPolicies();
    }

    public boolean isStoreExists(int storeID) {
        return stores.containsKey(storeID);
    }

    public List<String> possibleManagerPermissions() {
        return parsePermissions(Arrays.stream(StoreActionPermissions.values()).toList());
    }

    public List<String> getManagerInfo(int userID, int storeId) {
        try {
            return parsePermissions(getStoreManager(userID, storeId).getStoreActionPermissions());
        }
        catch (Exception e) {
            return null;
        }
    }

    public Boolean managerHasPermission(int managerID, int storeID, StoreActionPermissions permission) {
        try {
            return getStoreManager(managerID, storeID).hasPermission(permission);
        }
        catch (Exception e) {
            return false;
        }
    }



    private String getDateString(Calendar date) //dd.mm.yyyy
    {
        return  date.get(5) + "." + date.get(2) + "." + date.get(1);
    }
    private boolean checkDatesDidNotPassed(List<Calendar> forbiddenDates)
    {
        Calendar today = Calendar.getInstance();
        today.set(11 ,0);
        today.set(12 ,0);
        today.set(13 ,0);
        today.add(Calendar.DATE, -1);
        for (Calendar date : forbiddenDates)
        {
            int difference = date.compareTo(today);
            if (difference < 0)
            {
                return true;
            }
        }
        return false;
    }
    private List<String> parsePermissions(Collection<StoreActionPermissions> values) {
        return Arrays.stream(values.toArray()).map(permissions -> permissions.toString().replace('_', ' ')).toList();
    }


    private StoreManager getStoreManager(int userID, int storeId) throws Exception {
        Store store = getStore(storeId);
        Set<StoreManager> permissions = store.getStoreManagers();
        for (StoreManager manager : permissions) {
            if (manager.getUserID() == userID) {
                return manager;
            }
        }
        return null;
    }

    public int getIdByStoreName(String name)
    {
        for (Store store : stores.values())
        {
            if (store.getStoreName().equals(name))
            {
                return store.getStoreID();
            }
        }
        return -1;
    }

    public String getStoreNameById(int id)
    {
        for (Store store : stores.values())
        {
            if (store.getStoreID() == id)
            {
                return store.getStoreName();
            }
        }
        return null;
    }

    public boolean payForBid(int storeID, int bidID, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        Store store = getStore(storeID);
        return store.payForBid(bidID, purchaseInfo, supplyInfo);
    }

    public void cancelBid(int storeId, int id) throws Exception {
        Store store = getStore(storeId);
        store.cancelBid(id);
    }

    public void loadStores() {
        this.storeDAO = new StoreDAO();
        this.stores = storeDAO.getStores();
        if (!stores.isEmpty())
            this.storesIDs = Collections.max(stores.keySet()) + 1;
    }

    public Appointment addAppointment(int storeID, int creatorId, int newOwnerId) throws Exception {
        if (storeID<0|creatorId<0|newOwnerId<0)
            throw new Exception("id cant be negative");
        Store store = getStore(storeID);
        if (!checkIfStoreOwner(creatorId,store.getStoreID()))
            throw new Exception("creator must be this store owner");
        return store.addAppointment(creatorId, newOwnerId);
    }

    public void removeAppointment(int storeID, int userId) throws Exception {
        Store store = getStore(storeID);
        store.removeAppointment(userId);
    }

    public Set<Appointment> getUserAppointments(RegisteredUser user) {
        Set<Appointment> appointmentList = new HashSet<>();
        Set<Appointment> appointmentMap;
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        appointmentMap = appointmentDAO.getAppointments();
        for (Appointment appointment : appointmentMap) {
            Map<Integer, Boolean> acceptMap = appointment.getAcceptMap();
            if (acceptMap.containsKey(user.getId())) {
                appointmentList.add(appointment);
            }
        }
        return appointmentList;
    }

    public void rejectAppointment(int storeID, int theOwnerId) throws Exception {
        Store store = getStore(storeID);
        store.rejectAppointment(theOwnerId);
    }

    public boolean acceptAppointment(int storeID, int myId, int theOwnerId) throws Exception {
        Store store = getStore(storeID);
        if (store.acceptAppointment(myId, theOwnerId))
            return true;
        return false;
    }
}