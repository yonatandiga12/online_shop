package Bridge;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;
import ServiceLayer.Objects.*;
import ServiceLayer.Result;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyBridge implements Bridge {

    private Bridge real = null;

    public void setRealBridge(Bridge implementation) {
        if (real == null)
            real = implementation;
    }


    @Override
    public int registerUser(String userName, String password,String address,LocalDate bDay) {
        if(real != null){
            return real.registerUser(userName, password, "address", LocalDate.of(1999,2,2));
        }
        return 1;
    }

    @Override
    public boolean loginUser(String name, String password) {
        if(real != null){
            return real.loginUser(name, password);
        }
        return true;
    }

    @Override
    public void exitSystem(int id) {
        if(real != null){
            real.exitSystem(id);
        }
    }

    @Override
    public void loadSystem(){
        if(real != null){
            real.loadSystem();
        }
    }


    @Override
    public StoreService getStoreInfo(int storeId) {
        if(real != null){
            return real.getStoreInfo(storeId);
        }
        return null;
    }


    @Override
    public List<CatalogItemService> searchItems(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) {
        if(real != null){
            return real.searchItems(keywords, searchBy, filters);
        }
        return null;
    }


    @Override
    public CartService addItemToBasket(int userId, int storeId, int itemId, int amount) {
        if (real != null) {
            return real.addItemToBasket(userId, storeId, itemId, amount);
        }
        return null;
    }

    @Override
    public CartService getCart(int userId) {
        if(real != null){
            return real.getCart(userId);
        }
        return null;
    }

    @Override
    public boolean buyCart(int userId, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) {
        if(real != null){
            return real.buyCart(userId, purchaseInfo, supplyInfo);
        }
        return true;
    }

    @Override
    public  CatalogItemService addCatalogItem(int storeId, String itemName, int price, String category) {
        if(real != null){
            return real.addCatalogItem(storeId, itemName, price, category);
        }
        return null;
    }

    @Override
    public void addItemAmount(int storeId, int itemId, int amount) throws Exception {
        if (real != null) {
            real.addItemAmount(storeId, itemId, amount);
        }
    }

    @Override
    public boolean removeItemFromStore(int storeId, int itemId) {
        if (real != null) {
            return real.removeItemFromStore(storeId, itemId);
        }
        return false;
    }

    @Override
    public String changeItemName(int storeId, int itemId, String newName) {
        if (real != null) {
            return real.changeItemName(storeId, itemId, newName);
        }
        return "Not removed";
    }

    @Override
    public List<UserStaffInfoService> showStaffInfo(int storeId, int userId) {
        if (real != null) {
            return real.showStaffInfo(storeId, userId);
        }
        return null;
    }

    @Override
    public List<ReceiptService> getSellingHistoryOfStoreForManager(int storeId, int userId) {
        if (real != null) {
            return real.getSellingHistoryOfStoreForManager(storeId, userId);
        }
        return null;
    }

    @Override
    public StoreService getStoreInfoAsStoreManager(int storeId, int userId) {
        if(real != null){
            return real.getStoreInfoAsStoreManager(storeId, userId);
        }
        return null;
    }

    @Override
    public boolean logOut(int userID) {
        if(real != null){
            return real.logOut(userID);
        }
        return true;
    }

    @Override
    public int createStore(int userId, String name) {
        if(real != null){
            return real.createStore(userId, name);
        }
        return -1;
    }


    @Override
    public boolean closeStore(int userId, int storeId) {
        if(real != null){
            return real.closeStore(userId, storeId);
        }
        return true;
    }

    @Override
    public boolean defineStoreManager(int storeId, int storeOwner, int newStoreManager){
        if(real != null){
            return real.defineStoreManager(storeId, storeOwner, newStoreManager);
        }
        return true;
    }

    @Override
    public boolean removeStoreManager(int storeId, int storeOwnerId, int removeUserId) {
        if(real != null){
            return real.removeStoreManager(storeId, storeOwnerId, removeUserId);
        }
        return true;
    }

    @Override
    public boolean defineStoreOwner(int storeId, int ownerId, int newCoOwnerId) {
        if(real != null){
            return real.defineStoreOwner(storeId, ownerId, newCoOwnerId);
        }
        return false;
    }

    @Override
    public boolean appointOwner(int storeId, int ownerId, int newCoOwnerId) {
        if(real != null){
            return real.appointOwner(storeId, ownerId, newCoOwnerId);
        }
        return false;
    }

    @Override
    public boolean removeStoreOwner(int storeId, int storeOwnerId, int newStoreOwnerId) {
        if(real != null){
            return real.removeStoreOwner(storeId, storeOwnerId, newStoreOwnerId);
        }
        return false;
    }

    @Override
    public boolean payCart(int userId, String paymentDetails, String paymentService) {
        if(real != null){
            return real.payCart(userId, paymentDetails, paymentService);
        }
        return false;
    }

    @Override
    public boolean askForSupply(int userId, List<CatalogItemService> items, String supplyService) {
        if(real != null){
            return real.askForSupply(userId, items, supplyService);
        }
        return true;
    }

    @Override
    public boolean closeStorePermanently(int storeManagerId, int storeId) {
        if(real != null){
            return real.closeStorePermanently(storeManagerId, storeId);
        }
        return false;
    }

    @Override
    public boolean checkIfStoreOwner(int userId, int storeId) {
        if(real != null){
            return real.checkIfStoreOwner(userId, storeId);
        }
        return false;
    }

    @Override
    public boolean checkIfStoreManager(int userId, int storeId) {
        if(real != null){
            return real.checkIfStoreManager(userId, storeId);
        }
        return false;
    }

    @Override
    public boolean removeRegisterdUser(int systemManagerId, int userToRemoveId) {
        if(real != null){
            return real.removeRegisterdUser(systemManagerId, userToRemoveId);
        }
        return false;
    }

    @Override
    public HashMap<Integer, String> getComplaints(int managerId) {
        if(real != null){
            return real.getComplaints(managerId);
        }
        return null;
    }

    @Override
    public boolean answerComplaint(int userId, HashMap<Integer, String> complaintsAnswers) {
        if(real != null){
            return real.answerComplaint(userId, complaintsAnswers);
        }
        return false;
    }

    @Override
    public void postComplaint(int userId, String msg) {
        if(real != null){
            real.postComplaint(userId, msg);
        }
    }

    @Override
    public boolean sendMsg(int senderId, int receiverId, String msg) {
        if(real != null){
            return real.sendMsg(senderId, receiverId, msg);
        }
        return false;
    }

    @Override
    public HashMap<Integer, List<String>> getMsgs(int userId) {
        if(real != null){
            return real.getMsgs(userId);
        }
        return null;
    }

    @Override
    public HashMap<Integer, List<ReceiptService>> getSellingHistoryOfUserForManager(int managerId, int userId) {
        if(real != null){
            return real.getSellingHistoryOfUserForManager(managerId, userId);
        }
        return null;
    }

    @Override
    public HashMap<Integer, String> getUsersTraffic(int managerId) {
        if(real != null){
            return real.getUsersTraffic(managerId);
        }
        return null;
    }

    @Override
    public HashMap<Integer, Integer> getPurchaseTraffic(int managerId) {
        if(real != null){
            return real.getPurchaseTraffic(managerId);
        }
        return null;
    }

    @Override
    public int getNumberOfRegistrationForToady(int managerId) {
        if(real != null){
            return real.getNumberOfRegistrationForToady(managerId);
        }
        return 0;
    }

    @Override
    public boolean reopenStore(int userId, int storeId) {
        if(real != null){
            return real.reopenStore(userId, storeId);
        }
        return false;
    }


    @Override
    public boolean rankAStore(int userId, int storeId, int rank) {
        if(real != null){
            return real.rankAStore(userId, storeId, rank);
        }
        return false;
    }

    @Override
    public double getStoreRank(int userId, int storeId) {
        if(real != null){
            return real.getStoreRank(userId, storeId);
        }
        return 0;
    }

    @Override
    public double getItemRank(int userId, int storeId, int itemId) {
        if(real != null){
            return real.getItemRank(userId, storeId, itemId);
        }
        return 0;
    }

    @Override
    public boolean rankAnItemInStore(int userId, int storeId, int itemId, int rank) {
        if(real != null){
            return real.rankAnItemInStore(userId, storeId, itemId, rank);
        }
        return false;
    }

    @Override
    public List<ReceiptService> getPersonalHistory(int userId) {
        if(real != null){
            return real.getPersonalHistory(userId);
        }
        return null;
    }

    @Override
    public List<String> getPersonalInformation(int userId) {
        if(real != null){
            return real.getPersonalInformation(userId);
        }
        return null;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if(real != null){
            return real.changePassword(userId, oldPassword, newPassword);
        }
        return false;
    }

    @Override
    public List<String> getRequestsOfStore(int ownerManagerId, int storeId) {
        if(real != null){
            return real.getRequestsOfStore(ownerManagerId, storeId);
        }
        return null;
    }

    @Override
    public boolean checkIfLoggedIn(int userId) {
        if(real != null){
            return real.checkIfLoggedIn(userId);
        }
        return false;
    }


    @Override
    public int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale) {
        if (real != null) {
            return real.addVisibleItemsDiscount(storeID, itemsIDs, percent, endOfSale);
        }
        return -1;
    }

    public int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale) {
        if (real != null) {
            return real.addVisibleCategoryDiscount(storeID, category, percent, endOfSale);
        }
        return -1;
    }

    public int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale){
        if (real != null) {
            return real.addConditionalStoreDiscount(storeID, percent, endOfSale);
        }
        return -1;
    }

    @Override
    public int addHiddenStoreDiscount(int storeId, double percent, String coupon, Calendar calender) {
        if (real != null) {
            return real.addHiddenStoreDiscount(storeId, percent, coupon, calender);
        }
        return -1;
    }

    @Override
    public RuleService addDiscountBasketTotalPriceRule(int storeID, int discountID, double minimumPrice){
        if(real != null){
            return real.addDiscountBasketTotalPriceRule(storeID, discountID, minimumPrice);
        }
        return null;
    }

    @Override
    public RuleService addDiscountQuantityRule(int storeID, int discountID, Map<Integer, Integer> itemsAmounts){
        if(real != null){
            return real.addDiscountQuantityRule(storeID, discountID, itemsAmounts);
        }
        return null;
    }

    @Override
    public int wrapDiscounts(int storeID, List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum){
        if(real != null){
            return real.wrapDiscounts(storeID, discountsIDsToWrap, numericCompositeEnum);
        }
        return -1;
    }

    @Override
    public RuleService addDiscountComposite(int storeID, int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs){
        if(real != null){
            return real.addDiscountComposite(storeID, discountID, logicalComposite, logicalComponentsIDs);
        }
        return null;
    }

    @Override
    public Map<Integer, UserInfoService> getLoggedOutUsers(){
        if(real!=null){
            return real.getLoggedOutUsers();
        }
        return null;
    }

    @Override
    public Map<Integer, UserInfoService> getLoggedInUsers(){
        if(real!=null){
            return real.getLoggedInUsers();
        }
        return null;
    }

    @Override
    public RuleService addDiscountPolicyBuyerAgeRule(int storeID, int minimumAge) {
        if(real != null)
            return real.addDiscountPolicyBuyerAgeRule(storeID, minimumAge);
        return null;
    }

    @Override
    public RuleService addDiscountPolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) {
        if(real != null)
            return real.addDiscountPolicyMustItemsAmountsRule(storeID, itemsAmounts);
        return null;
    }

    @Override
    public boolean removeUser(int userID, int userToRemove) {
        if(real != null){
            return real.removeUser(userID, userToRemove);
        }
        return false;
    }

    @Override
    public RuleService addPurchasePolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) {
        if(real != null)
            return real.addPurchasePolicyItemsWeightLimitRule(storeID, weightsLimits);
        return null;
    }

    @Override
    public RuleService addPurchasePolicyMustDatesRule(int storeID, List<Calendar> mustDates) {
        if(real != null)
            return real.addPurchasePolicyMustDatesRule(storeID, mustDates);
        return null;
    }

    @Override
    public RuleService wrapPurchasePolicies(int storeID, List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) {
        if(real != null)
            return real.wrapPurchasePolicies(storeID, purchasePoliciesIDsToWrap, logicalCompositeEnum);
        return null;
    }

    @Override
    public boolean addBid(int storeID, int itemID, int userID, double offeredPrice) {
        if(real != null)
            return real.addBid(storeID, itemID, userID, offeredPrice);
        return false;
    }

    @Override
    public List<BidService> getUserBids(int userID) {
        if(real != null)
            return real.getUserBids(userID);
        return null;
    }

    @Override
    public boolean approve(int storeID, int bidID, int replierUserID) {
        if(real != null)
            return real.approve(storeID, bidID, replierUserID);
        return false;
    }

    @Override
    public boolean replyToCounterOffer(int storeID, int bidID, boolean accepted) {
        if(real != null)
            return real.replyToCounterOffer(storeID, bidID, accepted);
        return false;
    }

    @Override
    public boolean reject(int storeID, int bidID, int replierUserID) {
        if(real != null)
            return real.reject(storeID, bidID, replierUserID);
        return false;
    }

    @Override
    public boolean counterOffer(int storeID, int bidID, int replierUserID, double counterOffer) {
        if(real != null)
            return real.counterOffer(storeID, bidID, replierUserID, counterOffer);
        return false;
    }

    @Override
    public List<BidService> getUserBidsToReply(int userID) {
        if(real != null)
            return real.getUserBidsToReply(userID);
        return null;
    }

    @Override
    public boolean approveOwner(int store, int approvingId, int newOwnerId) {
        if(real != null)
            return real.approveOwner(store, approvingId, newOwnerId);
        return true;
    }

    @Override
    public boolean rejectOwner(int store, int newOwnerId) {
        if(real != null)
            return real.rejectOwner(store, newOwnerId);
        return true;
    }

    @Override
    public HashMap<Integer, ChatService> getChats(int id){
        if(real != null){
            return real.getChats(id);
        }

        return null;
    }

}
