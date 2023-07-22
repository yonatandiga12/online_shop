package Bridge;


import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.Rule;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;
import ServiceLayer.Objects.*;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealBridge implements Bridge{


    public UserService userService;
    public ShoppingService shoppingService;

    public RealBridge(){
        try {
            userService = new UserService();
            shoppingService = new ShoppingService();
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
    }


    @Override
    public int registerUser(String userName, String password, String address, LocalDate bDay) {
        Result<Integer> result = userService.register(userName, password, address,  bDay);
        return handleIntResult(result);
    }


    @Override
    public boolean loginUser(String name, String password) {
        Result<Integer> result = userService.login(name, password);
        int res = handleIntResult(result);
        /** */
        return res != -1;
    }


    @Override
    public StoreService getStoreInfo(int storeId) {
        Result<StoreService> result = shoppingService.getStoreInfo(storeId);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();
    }

    @Override
    public List<CatalogItemService> searchItems(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) {
        Result<List<CatalogItemService>> result =  shoppingService.searchCatalog(keywords, searchBy, filters);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();
    }


    @Override
    public CartService addItemToBasket(int userId, int storeId, int itemId, int amount) {
        /** can remove try catch when shoppingService will dump theirs*/

        try {
            Result<CartService> result = shoppingService.addItemToCart(userId, storeId, itemId, amount);
            if(result.isError()){
                System.out.println(result.getMessage());
                return null;
            }
            return result.getValue();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CartService getCart(int userId) {
        Result<CartService> result = shoppingService.getCart(userId);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();

    }

    @Override
    public boolean buyCart(int userId, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) {
        try {
            Result<Boolean> result = shoppingService.buyCart(userId, purchaseInfo, supplyInfo);
            return handleBoolResult(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public CatalogItemService addCatalogItem(int storeId, String itemName, int price, String category) {
        Result<CatalogItemService> result = shoppingService.addItemToStore(storeId, itemName, price, category, 1);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();
    }



    @Override
    public boolean removeItemFromStore(int storeId, int itemId) {
        Result<CatalogItemService> result =  shoppingService.removeItemFromStore(storeId, itemId);
        if(result.isError()){
            System.out.println(result.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String changeItemName(int storeId, int itemId, String newName) {
        Result<String> result = shoppingService.updateItemName(storeId, itemId, newName);
        if(result.isError()){
            System.out.println(result.getMessage());
        }
        return result.getMessage();
    }

    @Override
    public List<UserStaffInfoService> showStaffInfo(int storeId, int userId) {
        //return this.facade.showStaffInfo(storeId, userId);
        /** */
        return null;
    }

    @Override
    public List<ReceiptService> getSellingHistoryOfStoreForManager(int storeId, int userId) {
        Result<List<ReceiptService>> result = shoppingService.getSellingHistoryOfStoreForManager(storeId, userId);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();
    }

    @Override
    public StoreService getStoreInfoAsStoreManager(int storeId, int userId) {
        Result<StoreService> result = shoppingService.getStoreInfoAsStoreManager(storeId, userId);
        if(result.isError()){
            System.out.println(result.getMessage());
            return null;
        }
        return result.getValue();
    }

    @Override
    public boolean logOut(int userID) {
        Result<Boolean> result = this.userService.logout(userID);
        return handleBoolResult(result);
    }


    @Override
    public int createStore(int userId, String storeName) {
        Result<Integer>  result = shoppingService.createStore(userId, storeName);
        return handleIntResult(result);
    }

    @Override
    public boolean closeStore(int userId, int storeId) {
        Result<Boolean> result = shoppingService.closeStore(userId, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean defineStoreManager(int storeId, int storeOwner, int newStoreManager){
        Result<Boolean> result = userService.addManager(storeOwner, newStoreManager, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean defineStoreOwner(int storeId, int ownerId, int newCoOwnerId) {
        Result<Boolean> result = userService.addOwner(ownerId, newCoOwnerId, storeId);
        return handleBoolResult(result);
    }



    @Override
    public void addItemAmount(int storeId, int itemId, int amount) throws Exception
    {
        shoppingService.addItemAmount(storeId, itemId, amount);
    }

    @Override
    public void exitSystem(int id) {
        //facade.exitSystem(id);
        /** */
    }


    @Override
    public HashMap<Integer, List<ReceiptService>> getSellingHistoryOfUserForManager(int managerId, int userId) {
        //return this.facade.getSellingHistoryOfUserForManager(managerId, userId);
        /** */
        return null;
    }


    @Override
    public List<ReceiptService> getPersonalHistory(int userId) {
        //return this.facade.getPersonalHistory(userId);
        /** */
        return null;
    }




    @Override
    public boolean checkIfStoreOwner(int userId, int storeId) {
        Result<Boolean> result = shoppingService.checkIfStoreOwner(userId, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean checkIfStoreManager(int userId, int storeId) {
        Result<Boolean> result = shoppingService.checkIfStoreManager(userId, storeId);
        return handleBoolResult(result);
    }


    private boolean handleBoolResult(Result<Boolean> result) {
        if(result.isError()){
            System.out.println(result.getMessage());
            return false;
        }
        return result.getValue();
    }

    private int handleIntResult(Result<Integer> result) {
        if(result.isError()){
            System.out.println(result.getMessage());
            return -1;
        }
        return result.getValue();
    }


    @Override
    public int addVisibleItemsDiscount(int storeID, List<Integer> itemsIDs, double percent, Calendar endOfSale) {
        Result<Integer> result = shoppingService.addVisibleItemsDiscount(storeID, itemsIDs, percent, endOfSale);
        return handleIntResult(result);
    }

    public int addVisibleCategoryDiscount(int storeID, String category, double percent, Calendar endOfSale){
        Result<Integer> result = shoppingService.addVisibleCategoryDiscount(storeID, category, percent, endOfSale);
        return handleIntResult(result);
    }

    public int addConditionalStoreDiscount(int storeID, double percent, Calendar endOfSale){
        Result<Integer> result = shoppingService.addConditionalStoreDiscount(storeID, percent, endOfSale);
        return handleIntResult(result);
    }

    @Override
    public int addHiddenStoreDiscount(int storeId, double percent, String coupon, Calendar calender) {
        Result<Integer> result = shoppingService.addHiddenStoreDiscount(storeId, percent, coupon, calender);
        return handleIntResult(result);
    }

    @Override
    public RuleService addDiscountBasketTotalPriceRule(int storeID, int discountID, double minimumPrice){
        Result<RuleService> result = shoppingService.addDiscountBasketTotalPriceRule(storeID, discountID, minimumPrice);
        if(result != null){
            return result.getValue();
        }
        return null;
    }

    @Override
    public RuleService addDiscountQuantityRule(int storeID, int discountID, Map<Integer, Integer> itemsAmounts){
        Result<RuleService> result = shoppingService.addDiscountQuantityRule(storeID, discountID, itemsAmounts);
        if(result != null){
            return result.getValue();
        }
        return null;
    }

    @Override
    public int wrapDiscounts(int storeID, List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum){
        Result<Integer> result = shoppingService.wrapDiscounts(storeID, discountsIDsToWrap, numericCompositeEnum);
        return handleIntResult(result);
    }

    @Override
    public RuleService addDiscountComposite(int storeID, int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs){
        Result<RuleService> result = shoppingService.addDiscountComposite(storeID, discountID, logicalComposite, logicalComponentsIDs);
        if(result != null){
            return result.getValue();
        }
        return null;
    }


    @Override
    public boolean removeStoreManager(int storeId, int storeOwnerId, int removeUserId) {
        Result<Boolean> result = userService.removeManager(storeOwnerId, removeUserId, storeId);
        return handleBoolResult(result);
    }


    @Override
    public boolean removeStoreOwner(int storeId, int storeOwnerId, int newStoreOwnerId) {
        Result<Boolean> result = userService.removeOwner(storeOwnerId, newStoreOwnerId, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean closeStorePermanently(int storeManagerId, int storeId) {
        Result<Boolean> result = shoppingService.closeStorePermanently(storeManagerId, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean reopenStore(int userId, int storeId) {
        Result<Boolean> result = shoppingService.reopenStore(userId, storeId);
        return handleBoolResult(result);
    }

    @Override
    public boolean removeRegisterdUser(int systemManagerId, int userToRemoveId) {
        Result<Boolean> result = userService.removeUser(systemManagerId, userToRemoveId);
        return handleBoolResult(result);
    }

    @Override
    public Map<Integer, UserInfoService> getLoggedOutUsers(){
        Result<Map<Integer, UserInfoService>> users =  userService.getLoggedOutUsers();
        if(users == null)
            return null;
        return users.getValue();
    }

    @Override
    public Map<Integer, UserInfoService> getLoggedInUsers(){
        Result<Map<Integer, UserInfoService>> users =  userService.getLoggedInUsers();
        if(users == null)
            return null;
        return users.getValue();
    }

    @Override
    public RuleService addDiscountPolicyBuyerAgeRule(int storeID, int minimumAge) {
        Result<RuleService> result = shoppingService.addDiscountPolicyBuyerAgeRule(storeID, minimumAge);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public RuleService addDiscountPolicyMustItemsAmountsRule(int storeID, Map<Integer, Integer> itemsAmounts) {
        Result<RuleService> result = shoppingService.addDiscountPolicyMustItemsAmountsRule(storeID, itemsAmounts);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public boolean removeUser(int userID, int userToRemove) {
        Result<Boolean> result = userService.removeUser(userID, userToRemove);
        return handleBoolResult(result);
    }

    @Override
    public RuleService addPurchasePolicyItemsWeightLimitRule(int storeID, Map<Integer, Double> weightsLimits) {
        Result<RuleService> result = shoppingService.addPurchasePolicyItemsWeightLimitRule(storeID, weightsLimits);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public RuleService addPurchasePolicyMustDatesRule(int storeID, List<Calendar> mustDates) {
        Result<RuleService> result = shoppingService.addPurchasePolicyMustDatesRule(storeID, mustDates);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public RuleService wrapPurchasePolicies(int storeID, List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) {
        Result<RuleService> result = shoppingService.wrapPurchasePolicies(storeID, purchasePoliciesIDsToWrap, logicalCompositeEnum);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public boolean addBid(int storeID, int itemID, int userID, double offeredPrice) {
        Result<Boolean> result = shoppingService.addBid(storeID, itemID, userID, offeredPrice);
        return handleBoolResult(result);
    }

    @Override
    public List<BidService> getUserBids(int userID) {
        Result<List<BidService>> result = shoppingService.getUserBids(userID);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public boolean approve(int storeID, int bidID, int replierUserID) {
        Result<Boolean> result = shoppingService.approve(storeID, bidID, replierUserID);
        return handleBoolResult(result);
    }

    @Override
    public boolean replyToCounterOffer(int storeID, int bidID, boolean accepted) {
        Result<Boolean> result = shoppingService.replyToCounterOffer(storeID, bidID, accepted);
        return handleBoolResult(result);
    }

    @Override
    public boolean reject(int storeID, int bidID, int replierUserID) {
        Result<Boolean> result = shoppingService.reject(storeID, bidID, replierUserID);
        return handleBoolResult(result);
    }

    @Override
    public boolean counterOffer(int storeID, int bidID, int replierUserID, double counterOffer) {
        Result<Boolean> result = shoppingService.counterOffer(storeID, bidID, replierUserID, counterOffer);
        return handleBoolResult(result);
    }

    @Override
    public List<BidService> getUserBidsToReply(int userID) {
        Result<List<BidService>> result = shoppingService.getUserBidsToReply(userID);
        if(result == null)
            return null;
        return result.getValue();
    }

    @Override
    public boolean approveOwner(int store, int approvingId, int newOwnerId) {
        Result<Boolean> res = shoppingService.acceptAppointment(store, approvingId, newOwnerId);
        if(res == null)
            return false;
        return false;
    }

    @Override
    public boolean appointOwner(int storeId, int ownerId, int newCoOwnerId) {
        Result<Boolean> result = shoppingService.addAppointment(storeId, ownerId, newCoOwnerId);
        return handleBoolResult(result);
    }

    @Override
    public boolean rejectOwner(int store, int newOwnerId) {
        Result<Boolean> res = shoppingService.rejectAppointment(store, newOwnerId);
        if(res == null)
            return false;
        return false;
    }


    @Override
    public HashMap<Integer, ChatService> getChats(int id){
        Result<HashMap<Integer, ChatService>> result = userService.getChats(id);

        if(result.isError()){
            result = shoppingService.getChats(id);
        }

        return result.getValue();
    }









    @Override
    public boolean sendMsg(int senderId, int receiverId, String msg) {
        /** NotForVersion1 */
        return false;
    }

    @Override
    public HashMap<Integer, String> getComplaints(int managerId) {
        //return this.facade.getComplaints(managerId);
        /** NotForVersion1 */
        return null;
    }


    @Override
    public List<String> getPersonalInformation(int userId) {
        //return this.facade.getPersonalInformation(userId);
        /** NotForVersion1 */
        return null;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        //return this.facade.changePassword(userId, oldPassword, newPassword);
        /** NotForVersion1 */
        return false;
    }


    @Override
    public HashMap<Integer, List<String>> getMsgs(int userId) {
        //return this.facade.getMsgs(userId);
        /** NotForVersion1 */
        return null;
    }


    @Override
    public boolean answerComplaint(int userId, HashMap<Integer, String> complaintsAnswers) {
        /** NotForVersion1 */
        //return this.facade.answerComplaint(userId, complaintsAnswers);
        return false;
    }

    @Override
    public void postComplaint(int userId, String msg) {
        /** NotForVersion1 */
        //this.facade.sendComplaint(userId, msg);
    }

    @Override
    public HashMap<Integer, String> getUsersTraffic(int managerId) {
        /** NotForVersion1 */
        //return this.facade.getUsersTraffic(managerId);
        return null;
    }

    @Override
    public HashMap<Integer, Integer> getPurchaseTraffic(int managerId) {
        /** NotForVersion1 */
        //return this.facade.getPurchaseTraffic(managerId);
        return null;
    }

    @Override
    public int getNumberOfRegistrationForToady(int managerId) {
        /** NotForVersion1 */
        //return this.facade.getNumberOfRegistrationForToady(managerId);
        return 0;
    }


    @Override
    public List<String> getRequestsOfStore(int ownerManagerId, int storeId) {
        /** NotForVersion1 */
        //return this.facade.getRequestsOfStore(ownerManagerId, storeId);
        return null;
    }



    @Override
    public boolean checkIfLoggedIn(int userId) {
        //return this.facade.checkIfLoggedIn(userId);
        /** Don't implement*/
        return false;
    }



    @Override
    public boolean rankAStore(int userId, int storeId, int rank) {
        //return this.facade.rankAStore(userId, storeId, rank);
        /** NotForVersion1 */
        return false;
    }

    @Override
    public double getStoreRank(int userId, int storeId) {
        //return this.facade.getStoreRank(userId, storeId);
        /** NotForVersion1 */
        return 0;
    }

    @Override
    public double getItemRank(int userId, int storeId, int itemId) {
        //return this.facade.getItemRank(userId, storeId, itemId);
        /** NotForVersion1 */
        return 0;
    }

    @Override
    public boolean rankAnItemInStore(int userId, int storeId, int itemId, int rank) {
        //return this.facade.rankAnItemInStore(userId, storeId, itemId, rank);
        /** NotForVersion1 */
        return false;
    }


    public void loadSystem(){
        //facade.loadSystem();
        /** NotForVersion1 */

    }


    @Override
    public boolean payCart(int userId, String paymentDetails, String paymentService) {
        //return this.facade.payCart(userId, paymentDetails, paymentService);
        /** Not sure...*/
        return false;
    }

    @Override
    public boolean askForSupply(int userId, List<CatalogItemService> items, String supplyService) {
        //return this.facade.askForSupply(userId, items, supplyService);
        /** Not sure...*/
        return false;
    }


}
