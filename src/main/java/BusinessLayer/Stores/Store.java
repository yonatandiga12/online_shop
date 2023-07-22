package BusinessLayer.Stores;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.ExternalSystems.ESPurchaseManager;
import BusinessLayer.ExternalSystems.Purchase.PurchaseClient;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.Supply.SupplyClient;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Log;
import BusinessLayer.Market;
import BusinessLayer.MarketMock;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.StoreMailbox;
import BusinessLayer.Pair;
import BusinessLayer.Receipts.ReceiptHandler;
import BusinessLayer.StorePermissions.StoreEmployees;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Conditions.LogicalCompositions.*;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.*;
import BusinessLayer.Stores.Conditions.NumericCompositions.*;
import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountScopes.CategoryDiscount;
import BusinessLayer.Stores.Discounts.DiscountScopes.DiscountScope;
import BusinessLayer.Stores.Discounts.DiscountScopes.ItemsDiscount;
import BusinessLayer.Stores.Discounts.DiscountScopes.StoreDiscount;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Conditional;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Hidden;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Visible;
import BusinessLayer.Stores.Pairs.DiscountPair;
import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Policies.PurchasePolicy;
import DataAccessLayer.AppointmentDAO;
import DataAccessLayer.StoreDAO;
import Globals.FilterValue;
import Globals.SearchBy;
import Globals.SearchFilter;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static BusinessLayer.Stores.StoreStatus.*;

@Entity
@Table(name = "stores")
public class Store {
    private static final Logger log = Log.log;
    private int founderID;
    private String storeName;
    @Id
    private int storeID;
    private int bidsIDs;
    private int lotteriesIDs;
    private int auctionsIDs;
    private int discountsIDs;
    private int policiesIDs;
    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus;
    @OneToMany(mappedBy = "store", fetch = FetchType.EAGER)
    private Set<CatalogItem> items;
    @OneToMany(mappedBy = "store", fetch = FetchType.EAGER)
    private Set<StoreOwner> storeOwners;
    @OneToMany(mappedBy = "store", fetch = FetchType.EAGER)
    private Set<StoreManager> storeManagers;
    @OneToMany(mappedBy = "storeId", fetch = FetchType.EAGER)
    private Set<Appointment> appointments;
    @Transient
    private List<DiscountPair> discounts;
    @Transient
    private Map<Integer, PurchasePolicy> purchasePolicies;
    @Transient
    private Map<Integer, DiscountPolicy> discountPolicies;
    @Transient
    private Map<Integer, Bid> bids;
    @Transient
    private Map<Integer, Auction> auctions;
    @Transient
    private Map<Integer, Lottery> lotteries;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiptHandlerId")
    private ReceiptHandler receiptHandler;
    @OneToOne(cascade = CascadeType.ALL)
    private StoreMailbox mailbox;
    @Transient
    private StoreDAO storeDAO;
    @Transient
    private AppointmentDAO appointmentDAO;

    public Store(int storeID, int founderID, String name) throws Exception {
        this.storeID = storeID;
        this.storeName = name;
        this.storeDAO = new StoreDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.items = new HashSet<>();
        this.storeStatus = OPEN;
        this.founderID = founderID;
        this.discounts = new ArrayList<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.auctions = new HashMap<>();
        this.lotteries = new HashMap<>();
        this.bids = new HashMap<>();
        this.appointments = new HashSet<>();
        this.receiptHandler = new ReceiptHandler();
        this.bidsIDs = 0;
        this.lotteriesIDs = 0;
        this.auctionsIDs = 0;
        this.discountsIDs = 0;
        this.policiesIDs = 0;
        this.storeManagers = new HashSet<>();
        this.storeOwners = new HashSet<>();
        this.mailbox = Market.getInstance().getNotificationHub().registerToMailService(this);
        log.info("Store " + storeID + " created with name: " + storeName);
    }
    public Store(int storeID, int founderID, String name, MarketMock marketMock) throws Exception {
        this.storeID = storeID;
        this.storeName = name;
        this.discounts = new ArrayList<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.items = new HashSet<>();
        this.auctions = new HashMap<>();
        this.lotteries = new HashMap<>();
        this.bids = new HashMap<>();
        this.appointments = new HashSet<>();
        this.receiptHandler = new ReceiptHandler();
        this.bidsIDs = 0;
        this.lotteriesIDs = 0;
        this.auctionsIDs = 0;
        this.discountsIDs = 0;
        this.policiesIDs = 0;
        this.storeStatus = OPEN;
        this.storeManagers = new HashSet<>();
        this.founderID = founderID;
        this.storeOwners = new HashSet<>();
        this.mailbox = marketMock.getNotificationHub().registerToMailService(this);
        log.info("Store " + storeID + " created with name: " + storeName);
    }

    public Store() throws Exception {
        this.discounts = new ArrayList<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.storeDAO = new StoreDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.items = new HashSet<>();
        this.auctions = new HashMap<>();
        this.lotteries = new HashMap<>();
        this.bids = new HashMap<>();
        this.appointments = new HashSet<>();
        this.receiptHandler = new ReceiptHandler();
        this.bidsIDs = 0;
        this.lotteriesIDs = 0;
        this.auctionsIDs = 0;
        this.discountsIDs = 0;
        this.policiesIDs = 0;
        this.storeStatus = OPEN;
        this.storeManagers = new HashSet<>();
        this.storeOwners = new HashSet<>();
    }

    public Set<Appointment> getAppointments() {
        return appointmentDAO.getAppointments();
    }

    public void setAppointments(Set<Appointment> appointmentsList) {
        this.appointments = appointmentsList;
    }

    public Set<CatalogItem> getItems() {
        return items;
    }

    public int getBidsIDs() {
        return bidsIDs;
    }

    public void setBidsIDs(int bidsIDs) {
        this.bidsIDs = bidsIDs;
    }

    public int getLotteriesIDs() {
        return lotteriesIDs;
    }

    public void setLotteriesIDs(int lotteriesIDs) {
        this.lotteriesIDs = lotteriesIDs;
    }

    public int getAuctionsIDs() {
        return auctionsIDs;
    }

    public void setAuctionsIDs(int auctionsIDs) {
        this.auctionsIDs = auctionsIDs;
    }

    public int getDiscountsIDs() {
        return discountsIDs;
    }

    public void setDiscountsIDs(int discountsIDs) {
        this.discountsIDs = discountsIDs;
    }

    public int getPoliciesIDs() {
        return policiesIDs;
    }

    public void setPoliciesIDs(int policiesIDs) {
        this.policiesIDs = policiesIDs;
    }

    public Set<StoreOwner> getStoreOwners() {
        return storeOwners;
    }

    public void setStoreOwners(Set<StoreOwner> storeOwners) {
        this.storeOwners = storeOwners;
    }

    public Set<StoreManager> getStoreManagers() {
        return storeManagers;
    }

    public void setStoreManagers(Set<StoreManager> storeManagers) {
        this.storeManagers = storeManagers;
    }

    public CatalogItem getItem(int itemID) {
        for (CatalogItem item : items)
            if (item.getItemID() == itemID)
                return item;
        return null;
    }

    public int getItemAmount(int itemID) {
        for (CatalogItem item : items) {
            if (item.getItemID() == itemID)
                return item.getAmount();
        }
        return -1;
    }

    public int getStoreID() {
        return storeID;
    }

    public void setStoreID(int storeID) {
        this.storeID = storeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Discount getDiscount(int discountID) throws Exception {
        DiscountPair pair = (DiscountPair) Pair.searchPair(discounts, discountID);
        if (pair == null) {
            throw new Exception("Error: discount ID " + discountID + " does not exist in store " + storeName);
        }

        return pair.getValue();
    }

    public PurchasePolicy getPurchasePolicy(int purchasePolicyID) {
        return purchasePolicies.get(purchasePolicyID);
    }

    public DiscountPolicy getDiscountPolicy(int discountPolicyID) {
        return discountPolicies.get(discountPolicyID);
    }

    public ReceiptHandler getReceiptHandler() {
        return receiptHandler;
    }

    public int getFounderID() {
        return founderID;
    }

    public void setFounderID(int founderID) {
        this.founderID = founderID;
    }

    public Map<CatalogItem, Boolean> getCatalog() {
        for (CatalogItem item : items) {
            updateItemDiscounts(item.getItemID());
            updateItemPurchasePolicies(item.getItemID());
            updateItemDiscountPolicies(item.getItemID());
        }
        Map<CatalogItem, Boolean> res = new HashMap<>();
        CatalogItem valueFromA;
        boolean valueFromB;
        for (CatalogItem entry : items) {
            valueFromA = entry;
            valueFromB = entry.getAmount() > 0;

            // Put the value from map A as the key and the value from map B as the value in map res
            res.put(valueFromA, valueFromB);
        }
        return res;
    }

    public Map<CatalogItem, Boolean> getCatalog(String keywords, SearchBy searchBy, Map<SearchFilter, FilterValue> filters) throws Exception {
        for (CatalogItem item : items) {
            updateItemDiscounts(item.getItemID());
            updateItemPurchasePolicies(item.getItemID());
            updateItemDiscountPolicies(item.getItemID());
        }
        Map<CatalogItem, Boolean> res = new HashMap<>();
        CatalogItem valueFromA;
        boolean valueFromB;
        boolean filterResult;
        for (CatalogItem item : items) {
            valueFromA = item;
            valueFromB = item.getAmount() > 0;
            if (belongsToSearch(valueFromA, keywords, searchBy)) {
                filterResult = true;
                for (FilterValue filterValue : filters.values()) {
                    filterResult = filterResult && filterValue.filter();
                }
                if (filterResult) {
                    res.put(valueFromA, valueFromB);
                }
            }
        }
        return res;
    }

    private boolean sameCategory(CatalogItem item, String keywords) {
        return (item.getCategory()).equalsIgnoreCase(keywords);
    }

    private boolean sameName(CatalogItem item, String keywords) {
        return (item.getItemName()).equalsIgnoreCase(keywords);
    }

    private boolean belongsToSearch(CatalogItem item, String keywords, SearchBy searchBy) throws Exception {
        switch (searchBy) {
            case CATEGORY -> {
                return sameCategory(item, keywords);
            }
            case ITEM_NAME -> {
                return sameName(item, keywords);
            }
            case KEY_WORD -> {
                {
                    String[] keys = keywords.split(",");
                    for (String key : keys) {
                        key = key.strip();
                        if (sameCategory(item, key) || sameName(item, key)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        throw new Exception("Search by " + searchBy + "is invalid");
    }

    public int addVisibleItemsDiscount(List<Integer> itemsIDs, double percent, Calendar endOfSale) {
        DiscountScope discountScope = new ItemsDiscount(itemsIDs);
        Discount discount = new Visible(discountsIDs, percent, endOfSale, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new visible discount at store " + storeID);
        return discountsIDs++;
    }

    public int addVisibleCategoryDiscount(String category, double percent, Calendar endOfSale) {
        DiscountScope discountScope = new CategoryDiscount(category);
        Discount discount = new Visible(discountsIDs, percent, endOfSale, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new visible discount at store " + storeID);
        return discountsIDs++;
    }

    public int addVisibleStoreDiscount(double percent, Calendar endOfSale) {
        DiscountScope discountScope = new StoreDiscount();
        Discount discount = new Visible(discountsIDs, percent, endOfSale, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new visible discount at store " + storeID);
        return discountsIDs++;
    }

    public int addConditionalItemsDiscount(double percent, Calendar endOfSale, List<Integer> itemsIDs) {
        DiscountScope discountScope = new ItemsDiscount(itemsIDs);
        Discount discount = new Conditional(discountsIDs, percent, endOfSale, discountScope, this);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new conditional discount at store " + storeID);
        return discountsIDs++;
    }

    public int addConditionalCategoryDiscount(double percent, Calendar endOfSale, String category) {
        DiscountScope discountScope = new CategoryDiscount(category);
        Discount discount = new Conditional(discountsIDs, percent, endOfSale, discountScope, this);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new conditional discount at store " + storeID);
        return discountsIDs++;
    }

    public int addConditionalStoreDiscount(double percent, Calendar endOfSale) {
        DiscountScope discountScope = new StoreDiscount();
        Discount discount = new Conditional(discountsIDs, percent, endOfSale, discountScope, this);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new conditional discount at store " + storeID);
        return discountsIDs++;
    }

    public int addHiddenItemsDiscount(List<Integer> itemsIDs, double percent, String coupon, Calendar endOfSale) {
        DiscountScope discountScope = new ItemsDiscount(itemsIDs);
        Discount discount = new Hidden(discountsIDs, percent, endOfSale, coupon, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new hidden discount at store " + storeID);
        return discountsIDs++;
    }

    public int addHiddenCategoryDiscount(String category, double percent, String coupon, Calendar endOfSale) {
        DiscountScope discountScope = new CategoryDiscount(category);
        Discount discount = new Hidden(discountsIDs, percent, endOfSale, coupon, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new hidden discount at store " + storeID);
        return discountsIDs++;
    }

    public int addHiddenStoreDiscount(double percent, String coupon, Calendar endOfSale) {
        DiscountScope discountScope = new StoreDiscount();
        Discount discount = new Hidden(discountsIDs, percent, endOfSale, coupon, discountScope);
        discounts.add(new DiscountPair(discountsIDs, discount));
        log.info("Added new hidden discount at store " + storeID);
        return discountsIDs++;
    }

    public String addDiscountBasketTotalPriceRule(int discountID, double minimumPrice) throws Exception {
        Conditional discount = (Conditional) getDiscount(discountID);
        return discount.addBasketTotalPriceRule(minimumPrice);
    }

    public String addDiscountQuantityRule(int discountID, Map<Integer, Integer> itemsAmounts) throws Exception {
        Conditional discount = (Conditional) getDiscount(discountID);
        for (Integer i : itemsAmounts.keySet()) {
            if (getItem(i) == null) {
                throw new Exception("Error: One or more of the items IDs you entered are not exist in store " + storeName);
            }
        }
        return discount.addQuantityRule(itemsAmounts);
    }

    public String addDiscountComposite(int discountID, LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs) throws Exception {
        Conditional discount = (Conditional) getDiscount(discountID);
        return discount.addComposite(logicalComposite, logicalComponentsIDs);
    }

    public String finishConditionalDiscountBuilding(int discountID) throws Exception {
        Conditional discount = (Conditional) getDiscount(discountID);
        return discount.finish();
    }

    public int wrapDiscounts(List<Integer> discountsIDsToWrap, NumericComposites numericCompositeEnum) throws Exception {
        List<Discount> discountsToWrap = new ArrayList<>();
        for (Integer discountID : discountsIDsToWrap) {
            discountsToWrap.add(getDiscount(discountID));
        }
        NumericComposite myNumericComposite = null;
        switch (numericCompositeEnum) {
            case ADD: {
                myNumericComposite = new Add(discountsIDs, discountsToWrap);
                break;
            }
            case MAX: {
                myNumericComposite = new Max(discountsIDs, discountsToWrap);
                break;
            }
            case MIN: {
                myNumericComposite = new Min(discountsIDs, discountsToWrap);
                break;
            }
        }
        if (myNumericComposite == null)
            throw new Exception("The numeric composite is unrecognized");

        DiscountPair pair;

        for (Integer discountID : discountsIDsToWrap) {
            pair = (DiscountPair) Pair.searchPair(discounts, discountID);
            discounts.remove(pair);
        }
        discounts.add(new DiscountPair(discountsIDs, myNumericComposite));
        return discountsIDs++;
    }

    public int removeDiscount(Integer discountID) {
        DiscountPair pair = (DiscountPair) Pair.searchPair(discounts, discountID);
        discounts.remove(pair);
        return pair.getValue().getDiscountID();
    }

    public int removePolicy(Integer policyID) {
        return purchasePolicies.remove(policyID).getRoot().getID();
    }

    public int removeDiscountPolicy(Integer policyID) {
        return discountPolicies.remove(policyID).getRoot().getID();
    }

    public String addPurchasePolicyBasketWeightLimitRule(double basketWeightLimit) throws Exception {
        BasketWeightLimitRule basketWeightLimitRule = new BasketWeightLimitRule(basketWeightLimit, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(basketWeightLimitRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyBuyerAgeRule(int minimumAge) throws Exception {
        BuyerAgeRule buyerAgeRule = new BuyerAgeRule(minimumAge, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(buyerAgeRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyForbiddenCategoryRule(String forbiddenCategory) {
        ForbiddenCategoryRule forbiddenCategoryRule = new ForbiddenCategoryRule(forbiddenCategory, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(forbiddenCategoryRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyForbiddenDatesRule(List<Calendar> forbiddenDates) {
        ForbiddenDatesRule forbiddenDatesRule = new ForbiddenDatesRule(forbiddenDates, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(forbiddenDatesRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyForbiddenHoursRule(int startHour, int endHour) {
        ForbiddenHoursRule forbiddenHoursRule = new ForbiddenHoursRule(startHour, endHour, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(forbiddenHoursRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyMustDatesRule(List<Calendar> mustDates) {
        MustDatesRule mustDatesRule = new MustDatesRule(mustDates, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(mustDatesRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyItemsWeightLimitRule(Map<Integer, Double> weightsLimits) throws Exception {
        if (!new HashSet<>(items.stream().map(CatalogItem::getItemID).toList()).containsAll(weightsLimits.keySet())) {
            throw new Exception("Error: One or more of the items IDs you entered are not exist in store " + storeName);
        }
        ItemsWeightLimitRule itemsWeightLimitRule = new ItemsWeightLimitRule(weightsLimits, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(itemsWeightLimitRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyBasketTotalPriceRule(double minimumPrice) throws Exception {
        BasketTotalPriceRule basketTotalPriceRule = new BasketTotalPriceRule(minimumPrice, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(basketTotalPriceRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String addPurchasePolicyMustItemsAmountsRule(Map<Integer, Integer> itemsAmounts) throws Exception {
        if (!new HashSet<>(items.stream().map(CatalogItem::getItemID).toList()).containsAll(itemsAmounts.keySet())) {
            throw new Exception("Error: One or more of the items IDs you entered are not exist in store " + storeName);
        }
        MustItemsAmountsRule mustItemsAmountsRule = new MustItemsAmountsRule(itemsAmounts, policiesIDs, this);
        PurchasePolicy purchasePolicy = new PurchasePolicy(mustItemsAmountsRule);
        purchasePolicies.put(policiesIDs++, purchasePolicy);
        return (policiesIDs - 1) + ": " + purchasePolicy;
    }

    public String wrapPurchasePolicies(List<Integer> purchasePoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception {
        List<LogicalComponent> policiesRootsToWrap = new ArrayList<>();
        for (Integer policyID : purchasePoliciesIDsToWrap) {
            policiesRootsToWrap.add(getPurchasePolicy(policyID).getRoot());
        }
        LogicalComponent myLogicalComponent = null;
        switch (logicalCompositeEnum) {
            case AND: {
                myLogicalComponent = new And(policiesRootsToWrap, policiesIDs, this);
                break;
            }
            case OR: {
                myLogicalComponent = new Or(policiesRootsToWrap, policiesIDs, this);
                break;
            }
            case CONDITIONING: {
                if (policiesRootsToWrap.size() != 2)
                    throw new Exception("Conditioning logical component for purchase policy expect 2 purchase policies to wrap, but got " + policiesRootsToWrap.size());
                myLogicalComponent = new Conditioning(policiesRootsToWrap.get(0), policiesRootsToWrap.get(1), policiesIDs, this);
                break;
            }
        }
        if (myLogicalComponent == null)
            throw new Exception("The logical component is unrecognized");
        for (Integer purchasePolicyID : purchasePoliciesIDsToWrap) {
            purchasePolicies.remove(purchasePolicyID);
        }
        PurchasePolicy policy = new PurchasePolicy(myLogicalComponent);
        int policyId = policiesIDs;
        purchasePolicies.put(policyId, policy);
        policiesIDs++;
        return policyId + ":" + policy;
    }

    public String addDiscountPolicyBasketWeightLimitRule(double basketWeightLimit) throws Exception {
        BasketWeightLimitRule basketWeightLimitRule = new BasketWeightLimitRule(basketWeightLimit, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(basketWeightLimitRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyBuyerAgeRule(int minimumAge) throws Exception {
        BuyerAgeRule buyerAgeRule = new BuyerAgeRule(minimumAge, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(buyerAgeRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyForbiddenCategoryRule(String forbiddenCategory) {
        ForbiddenCategoryRule forbiddenCategoryRule = new ForbiddenCategoryRule(forbiddenCategory, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(forbiddenCategoryRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyForbiddenDatesRule(List<Calendar> forbiddenDates) {
        ForbiddenDatesRule forbiddenDatesRule = new ForbiddenDatesRule(forbiddenDates, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(forbiddenDatesRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyForbiddenHoursRule(int startHour, int endHour) {
        ForbiddenHoursRule forbiddenHoursRule = new ForbiddenHoursRule(startHour, endHour, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(forbiddenHoursRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyMustDatesRule(List<Calendar> mustDates) {
        MustDatesRule mustDatesRule = new MustDatesRule(mustDates, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(mustDatesRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyItemsWeightLimitRule(Map<Integer, Double> weightsLimits) throws Exception {
        if (!new HashSet<>(items.stream().map(CatalogItem::getItemID).toList()).containsAll(weightsLimits.keySet())) {
            throw new Exception("Error: One or more of the items IDs you entered are not exist in store " + storeName);
        }
        ItemsWeightLimitRule itemsWeightLimitRule = new ItemsWeightLimitRule(weightsLimits, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(itemsWeightLimitRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyBasketTotalPriceRule(double minimumPrice) throws Exception {
        BasketTotalPriceRule basketTotalPriceRule = new BasketTotalPriceRule(minimumPrice, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(basketTotalPriceRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String addDiscountPolicyMustItemsAmountsRule(Map<Integer, Integer> itemsAmounts) throws Exception {
        if (!new HashSet<>(items.stream().map(CatalogItem::getItemID).toList()).containsAll(itemsAmounts.keySet())) {
            throw new Exception("Error: One or more of the items IDs you entered are not exist in the store");
        }
        MustItemsAmountsRule mustItemsAmountsRule = new MustItemsAmountsRule(itemsAmounts, policiesIDs, this);
        DiscountPolicy discountPolicy = new DiscountPolicy(mustItemsAmountsRule);
        discountPolicies.put(policiesIDs++, discountPolicy);
        return (policiesIDs - 1) + ": " + discountPolicy;
    }

    public String wrapDiscountPolicies(List<Integer> discountPoliciesIDsToWrap, LogicalComposites logicalCompositeEnum) throws Exception {
        List<LogicalComponent> policiesRootsToWrap = new ArrayList<>();
        for (Integer policyID : discountPoliciesIDsToWrap) {
            policiesRootsToWrap.add(getDiscountPolicy(policyID).getRoot());
        }
        LogicalComponent myLogicalComponent = null;
        switch (logicalCompositeEnum) {
            case AND: {
                myLogicalComponent = new And(policiesRootsToWrap, policiesIDs, this);
                break;
            }
            case OR: {
                myLogicalComponent = new Or(policiesRootsToWrap, policiesIDs, this);
                break;
            }
            case CONDITIONING: {
                if (policiesRootsToWrap.size() != 2)
                    throw new Exception("Conditioning logical component for discount policy expect 2 discount policies to wrap, but got " + policiesRootsToWrap.size());
                myLogicalComponent = new Conditioning(policiesRootsToWrap.get(0), policiesRootsToWrap.get(1), policiesIDs, this);
                break;
            }
        }
        if (myLogicalComponent == null)
            throw new Exception("The logical component is unrecognized");
        for (Integer discountPolicyID : discountPoliciesIDsToWrap) {
            discountPolicies.remove(discountPolicyID);
        }
        DiscountPolicy policy = new DiscountPolicy(myLogicalComponent);
        int policyId = policiesIDs;
        discountPolicies.put(policyId, policy);
        policiesIDs++;
        return policyId + ":" + policy;
    }

    public StoreStatus getStoreStatus() {
        return storeStatus;
    }

    public void setStoreStatus(StoreStatus storeStatus) {
        this.storeStatus = storeStatus;
    }

    public CatalogItem addCatalogItem(int itemID, String itemName, double itemPrice, String itemCategory, double weight) throws Exception {
        if (storeStatus != OPEN) {
            throw new Exception("Can't add catalog item when store unopened");
        }
        CatalogItem newItem = new CatalogItem(itemID, itemName, itemPrice, itemCategory, this.storeName, this, weight);
        items.add(newItem);
        addItemToStoreDAO(newItem);
        log.info("Added new item: " + itemName + ", at store " + storeID);
        storeDAO.save(this);
        return newItem;
    }

    public void addItemToStoreDAO(CatalogItem newItem) {
        storeDAO.addItem(newItem);
    }

    public synchronized void buyBasket(List<CartItemInfo> basketItems, int userID) throws Exception {
        Map<CatalogItem, CartItemInfo> receiptItems = new HashMap<>();
        for (CartItemInfo cartItemInfo : basketItems) {
            int itemID = cartItemInfo.getItemID();
            CatalogItem item = getItem(itemID);
            receiptItems.put(item, cartItemInfo);
            item.setSavedAmount(item.getSavedAmount() - cartItemInfo.getAmount());
        }
        Map<Integer, Map<CatalogItem, CartItemInfo>> receiptInfo = new HashMap<>();
        receiptInfo.put(userID, receiptItems);
        receiptHandler.addReceipt(storeID, receiptInfo);
        List<Integer> sendToList = storeOwners.stream().map(StoreEmployees::getUserID).collect(Collectors.toList());

        sendMsgToList(sendToList, "User " + userID + " made a purchase in store " + storeName + " where you are one of the owners");
        log.info("A basket was bought at store " + storeID);
        storeDAO.save(this);
    }

    public void sendMsgToList(List<Integer> sendToList, String s) {
        mailbox.sendMessageToList(sendToList, s);
    }

    public synchronized void saveItemsForUpcomingPurchase(List<CartItemInfo> basketItems, List<Coupon> coupons, int userID, int age) throws Exception {
        if (storeStatus == OPEN) {
            if (checkIfItemsInStock(basketItems)) {
                if (checkIfBasketPriceChanged(basketItems, coupons)) {
                    updateBasket(basketItems, coupons);
                    log.warning("Trying to buy a basket in store: " + storeName + ", but item price or discount or discount policy changed/removed/added");
                    throw new Exception("One or more of the items or discounts or discounts policies in store : " + storeName + " that affect the basket have been changed");
                }
                try {
                    checkIfPurchaseIsValid(basketItems, age);
                } catch (IllegalStateException msg) {
                    sendMsg(userID, msg.getMessage());
                    log.warning("Trying to buy a basket in store: " + storeName + ", but you don't comply with the purchase policies");
                    throw new IllegalStateException(msg);
                }
                int itemID;
                int itemAmountToSave;
                for (CartItemInfo cartItemInfo : basketItems) {
                    itemID = cartItemInfo.getItemID();
                    itemAmountToSave = cartItemInfo.getAmount();
                    saveItemAmount(itemID, itemAmountToSave);
                }
                log.info("Items was saved for upcoming purchase at store " + storeID);
            } else {
                log.warning("Items wasn't saved for upcoming purchase at store " + storeID + " due to lack of items");
                throw new Exception("Not enough items in stock");
            }
        } else if (storeStatus == CLOSE) {
            throw new Exception("You can't buy items from a closed store");
        } else {
            throw new Exception("You can't buy items from a permanently-closed store");
        }
        storeDAO.save(this);
    }

    public void sendMsg(int userID, String message) {
        mailbox.sendMessage(userID, message);
    }

    public boolean checkIfPurchaseIsValid(List<CartItemInfo> basketItems, int age) throws Exception {
        for (Map.Entry<Integer, PurchasePolicy> purchasePolicy : purchasePolicies.entrySet()) {
            if (!purchasePolicy.getValue().isValidForPurchase(basketItems, age)) {
                throw new IllegalStateException("You don't comply with the following purchase policy:\n" + purchasePolicy);
            }
        }
        return true;
    }


    private boolean checkIfDiscountsAreValid(List<CartItemInfo> basketItems) throws Exception {
        for (Map.Entry<Integer, DiscountPolicy> discountPolicy : discountPolicies.entrySet()) {
            if (!discountPolicy.getValue().isValidForDiscount(basketItems, -1)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfBasketPriceChanged(List<CartItemInfo> basketItems, List<Coupon> coupons) throws Exception {
        List<CartItemInfo> copyBasketItems = new ArrayList<>();
        for (CartItemInfo item : basketItems) {
            copyBasketItems.add(new CartItemInfo(item));
        }
        updateBasket(copyBasketItems, coupons);
        for (int i = 0; i < basketItems.size(); i++) {
            CartItemInfo item = basketItems.get(i);
            CartItemInfo copyItem = copyBasketItems.get(i);
            if ((item.getOriginalPrice() != copyItem.getOriginalPrice()) || (item.getPercent() != copyItem.getPercent())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfItemsInStock(List<CartItemInfo> basketItems) {
        int itemID;
        int itemAmountToSave;
        int itemCurrentAmount;
        for (CartItemInfo cartItemInfo : basketItems) {
            itemID = cartItemInfo.getItemID();
            itemAmountToSave = cartItemInfo.getAmount();
            itemCurrentAmount = getItemAmount(itemID);
            if (itemCurrentAmount < itemAmountToSave) {
                return false;
            }
        }
        return true;
    }

    public void updateBasket(List<CartItemInfo> basketItems, List<Coupon> coupons) throws Exception {
        updateBasketPrices(basketItems);
        if (discounts.size() == 0 || basketItems.size() == 0 || !checkIfDiscountsAreValid(basketItems)) {
            for (CartItemInfo item : basketItems) {
                item.setPercent(0);
            }
            return;
        }
        List<List<CartItemInfo>> tempBaskets = new ArrayList<>();

        Discount discount;
        for (DiscountPair pair : discounts) {
            discount = pair.getValue();
            tempBaskets.add(discount.updateBasket(basketItems, coupons));
        }

        for (int i = 0; i < basketItems.size(); i++) //set the original basket to the first temp basket
        {
            basketItems.get(i).setPercent(tempBaskets.get(0).get(i).getPercent());
        }
        if (tempBaskets.size() > 1) {
            for (int i = 1; i < tempBaskets.size(); i++) //skipping the first temp basket and apply all discount together in the original basket
            {
                List<CartItemInfo> tempBasket = tempBaskets.get(i);
                for (int j = 0; j < basketItems.size(); j++) {
                    double originalItemPercent = basketItems.get(j).getPercent();
                    double tempItemPercent = tempBasket.get(j).getPercent();
                    basketItems.get(j).setPercent(tempItemPercent * (100 - originalItemPercent) / 100 + originalItemPercent);
                    /// 40% discount + 30% discount = 58% discount (30% from (100-40=60) is 18, plus 40% = 58%)
                }
            }
        }
        storeDAO.save(this);
    }

    private void updateBasketPrices(List<CartItemInfo> basketItems) {
        for (CartItemInfo item : basketItems) {
            item.setOriginalPrice(getItem(item.getItemID()).getPrice());
        }
    }


    public void saveItemAmount(int itemID, int amountToSave) {
        CatalogItem item = getItem(itemID);
        int itemNewAmount = item.getAmount() - amountToSave;
        int itemNewSavedAmount = item.getSavedAmount() + amountToSave;
        item.setAmount(itemNewAmount);
        item.setSavedAmount(itemNewSavedAmount);
        storeDAO.save(this);
    }

    public void reverseSavedItems(List<CartItemInfo> basketItems) throws Exception {
        boolean success = checkIfItemsSaved(basketItems);
        if (success) {
            for (CartItemInfo cartItemInfo : basketItems) {
                saveItemAmount(cartItemInfo.getItemID(), -cartItemInfo.getAmount());
            }
        } else {
            throw new Exception("Somehow the amounts of items to unsave exceed the amounts saved before");
        }
    }

    public boolean checkIfItemsSaved(List<CartItemInfo> basketItems) {
        CatalogItem item;
        int itemID;
        int itemAmountToRemoveFromSaved;
        int itemCurrentSavedAmount;
        for (CartItemInfo cartItemInfo : basketItems) {
            itemID = cartItemInfo.getItemID();
            item = getItem(itemID);
            itemAmountToRemoveFromSaved = cartItemInfo.getAmount();
            itemCurrentSavedAmount = item.getSavedAmount();
            if (itemCurrentSavedAmount < itemAmountToRemoveFromSaved) {
                return false;
            }
        }
        return true;
    }

    public List<Bid> getUserBidsToReply(int userID) {
        List<Bid> bidsToReply = new ArrayList<>();
        for (Bid bid : bids.values()) {
            if (bid.isUserNeedToReply(userID)) {
                bidsToReply.add(bid);
            }
        }
        return bidsToReply;
    }

    public Map<Integer, Bid> getBids() {
        return bids;
    }

    public Bid addBid(int itemID, int userID, double offeredPrice) throws Exception {
        if (getItem(itemID) == null) {
            throw new Exception("Item ID: " + itemID + " does not exist");
        }
        saveItemAmount(itemID, 1);
        double originalPrice = getItem(itemID).getPrice();
        Bid newBid = new Bid(bidsIDs, itemID, getItem(itemID).getItemName(), userID, offeredPrice, originalPrice, getStoreID());

        List<Integer> sendToList = addContactsToBid();
        newBid.setRepliers(sendToList);
        bids.put(bidsIDs++, newBid);
        sendMsgToList(sendToList, "User " + userID + " offered new bid for item " + getItem(itemID).getItemName() + " at store " + storeName + " with price of " + offeredPrice + " while the original price is " + getItem(itemID).getPrice());
        log.info("Added new bid for item " + itemID + " at store " + storeID);
        storeDAO.save(this);
        return newBid;
    }

    public List<Integer> addContactsToBid() {
        List<StoreEmployees> storeOwnersAndManagers = new ArrayList<>();
        storeOwnersAndManagers.addAll(storeOwners);
        //storeOwnersAndManagers.addAll(storeManagers.stream().filter(manager -> manager.hasPermission(BID_MANAGEMENT)).toList());
        return storeOwnersAndManagers.stream().map(StoreEmployees::getUserID).collect(Collectors.toList());
    }

    public List<Bid> getUserBids(int userID) {
        return bids.values().stream().filter(bid -> bid.getUserID() == userID).toList();
    }

    public void addLottery(int itemID, double price, int lotteryPeriodInDays) {
        saveItemAmount(itemID, 1);
        lotteries.put(lotteriesIDs, new Lottery(this, lotteriesIDs++, itemID, price, lotteryPeriodInDays));
        log.info("Added new lottery for item " + itemID + " at store " + storeID);
    }

    public void addAuction(int itemID, double initialPrice, int auctionPeriodInDays) {
        saveItemAmount(itemID, 1);
        auctions.put(auctionsIDs, new Auction(this, auctionsIDs++, itemID, initialPrice, auctionPeriodInDays));
        log.info("Added new auction for item " + itemID + " at store " + storeID);
    }

    public void addItemAmount(int itemID, int amountToAdd) throws Exception {
        if (storeStatus != OPEN) {
            throw new Exception("Can't add item amount to unopened store");
        }
        getItem(itemID).addAmount(amountToAdd);
        log.info("Added amount by " + amountToAdd + " for item " + itemID + " at store " + storeID);
    }

    public void addSavedItemAmount(int itemID, int amountToRemove) {
        CatalogItem item = getItem(itemID);
        int currentAmountSaved = item.getSavedAmount();
        item.setSavedAmount(currentAmountSaved + amountToRemove);
    }

    private void removeBid(int bidID) {
        bids.remove(bidID);
    }

    private void removeAuction(int auctionID) {
        auctions.remove(auctionID);
    }

    private void removeLottery(int lotteryID) {
        lotteries.remove(lotteryID);
    }

    public BidReplies finishBidSuccessfully(int bidID) {
        Bid bid = bids.get(bidID);
        int itemID = bid.getItemID();
        int userID = bid.getUserID();
        addSavedItemAmount(itemID, -1);
        if (bid.getHighestCounterOffer() == -1) {
            sendMsg(userID, "Hi, your bid for the item: " + getItem(itemID).getItemName() + ", was approved by the store, and the item will be sent to you soon");
            log.info("Bid " + bidID + " was fully approved");
            return BidReplies.APPROVED;
        } else {
            sendMsg(userID, "Hi, your bid for the item: " + getItem(itemID).getItemName() + ", was countered by the store with counter-offer of: " + bid.getHighestCounterOffer() + " while the original price is: " + getItem(itemID).getPrice());
            log.info("Bid " + bidID + " was counter-offered with price of " + bid.getHighestCounterOffer());
            return BidReplies.COUNTERED;
        }
    }

    public void finishBidUnsuccessfully(int bidID) throws Exception {
        Bid bid = bids.get(bidID);
        int itemID = bid.getItemID();
        int userID = bid.getUserID();
        addItemAmount(itemID, 1);
        addSavedItemAmount(itemID, -1);
        removeBid(bidID);
        sendMsg(userID, "Hi, we apologize for the inconvenience, but your bid for the item: " + getItem(itemID).getItemName() + ", was rejected by the store");
        log.info("Bid " + bidID + " was rejected");
    }

    public void finishAuctionSuccessfully(int auctionID) {
        System.out.println("The item is sold to user");
        Auction myAuction = auctions.get(auctionID);
        int winnerID = myAuction.getCurrentWinningUserID();
        int itemID = myAuction.getItemID();
        addSavedItemAmount(myAuction.getItemID(), -1);
        myAuction.getAuctionTimer().cancel();
        myAuction.getAuctionTimer().purge();
        removeAuction(auctionID);
        sendMsg(winnerID, "Congratulations, you are the winner in our auction in store " + storeName + " of item " + getItem(itemID).getItemName() + " with an offer of " + myAuction.getCurrentPrice() + " while the original price is " + getItem(itemID).getPrice());
        log.info("Auction " + auctionID + " finished successfully and item was sold");
    }

    public void finishAuctionUnsuccessfully(int auctionID) throws Exception {
        Auction myAuction = auctions.get(auctionID);
        int itemID = myAuction.getItemID();
        addItemAmount(itemID, 1);
        addSavedItemAmount(itemID, -1);
        myAuction.getAuctionTimer().cancel();
        myAuction.getAuctionTimer().purge();
        removeAuction(auctionID);
        log.info("Auction " + auctionID + " finished unsuccessfully and item was not sold");
    }

    public void finishLotterySuccessfully(int lotteryID) {
        Lottery myLottery = lotteries.get(lotteryID);
        int winnerID = myLottery.getWinnerID();
        int itemID = myLottery.getItemID();
        addSavedItemAmount(itemID, -1);
        myLottery.getLotteryTimer().cancel();
        myLottery.getLotteryTimer().purge();
        removeLottery(lotteryID);
        sendMsg(winnerID, "Congratulations, you are the winner in our lottery in store " + storeName + " of item " + getItem(itemID).getItemName());
        List<Integer> losers = myLottery.getParticipants();
        losers.remove(winnerID);
        sendMsgToList(losers, "We are sorry, but you lost the lottery in store " + storeName + " of item " + getItem(itemID).getItemName());
        log.info("Lottery " + lotteryID + " finished successfully and item was sold to user " + winnerID);
    }

    public void finishLotteryUnsuccessfully(int lotteryID) throws Exception {
        Lottery myLottery = lotteries.get(lotteryID);
        int itemID = myLottery.getItemID();
        addItemAmount(itemID, 1);
        addSavedItemAmount(itemID, -1);
        myLottery.getLotteryTimer().cancel();
        myLottery.getLotteryTimer().purge();
        removeLottery(lotteryID);
        List<Integer> participants = myLottery.getParticipants();
        if (participants.size() > 0) {
            sendMsgToList(participants, "We are sorry, but the lottery in store " + storeName + " of item " + getItem(itemID).getItemName() + " has canceled due to lack of demand. Your money will be returned.");
        }
        log.info("Lottery " + lotteryID + " finished unsuccessfully and item was not sold");
    }

    public boolean participateInLottery(int lotteryID, int userID, double offerPrice) {
        Lottery myLottery = lotteries.get(lotteryID);
        boolean participateSuccessfully = myLottery.participateInLottery(userID, offerPrice);
        if (participateSuccessfully) {
            if (myLottery.isLotteryFinished()) {
                finishLotterySuccessfully(lotteryID);
            }
            log.info("User " + userID + " is participating in lottery " + lotteryID);
            return true;
        }
        log.warning("User " + userID + " failed to participate in lottery " + lotteryID);
        return false;
    }

    public boolean offerToAuction(int auctionID, int userID, double offerPrice) {
        Auction myAuction = auctions.get(auctionID);
        double bestOfferBefore = myAuction.getCurrentPrice();
        int winnerBefore = myAuction.getCurrentWinningUserID();
        String itemName = getItem(myAuction.getItemID()).getItemName();
        boolean result = myAuction.offerToAuction(userID, offerPrice);
        double bestOfferNow = myAuction.getCurrentPrice();
        if (result)
            sendMsg(winnerBefore, "Hi, we want to inform you that other user passed your offer of " + bestOfferBefore + " with an offer of " + bestOfferNow + " at the auction of item " + itemName + " at store " + storeName);
        log.info("User " + userID + " offered to auction " + auctionID + " with price of " + offerPrice);
        return result;
    }

    public BidReplies approve(int bidID, int replierUserID) throws Exception {
        if (!bids.containsKey(bidID)) {
            throw new Exception("Bid ID: " + bidID + " does not exist");
        }
        boolean finishedBid = bids.get(bidID).approve(replierUserID);
        log.info("User " + replierUserID + " approved bid " + bidID);
        if (finishedBid) {
            if (finishBidSuccessfully(bidID) == BidReplies.APPROVED)
                return BidReplies.APPROVED;
            return BidReplies.COUNTERED;
        }
        return BidReplies.REJECTED; //simulates not done
    }

    public boolean replyToCounterOffer(int bidID, boolean accepted) throws Exception {
        if (!bids.containsKey(bidID)) {
            throw new Exception("Bid ID: " + bidID + " does not exist");
        }
        boolean finishedBid = bids.get(bidID).replyToCounterOffer(accepted);
        if (finishedBid) {
            finishBidSuccessfully(bidID);
            return true;
        }
        return false;
    }

    public boolean reject(int bidID, int replierUserID) throws Exception {
        if (!bids.containsKey(bidID)) {
            throw new Exception("Bid ID: " + bidID + " does not exist");
        }
        boolean finishedBid = bids.get(bidID).reject(replierUserID);
        log.info("User " + replierUserID + " rejected bid " + bidID);
        if (finishedBid) {
            finishBidUnsuccessfully(bidID);
            return true;
        }
        return false;
    }

    public boolean counterOffer(int bidID, int replierUserID, double counterOffer) throws Exception {
        if (!bids.containsKey(bidID)) {
            throw new Exception("Bid ID: " + bidID + " does not exist");
        }
        boolean finishedBid = bids.get(bidID).counterOffer(replierUserID, counterOffer);
        log.info("User " + replierUserID + " counter-offered bid " + bidID);
        if (finishedBid) {
            finishBidSuccessfully(bidID);
            return true;
        }
        return false;
    }

    public boolean reopenStore(int userID) throws Exception {
        if (!isFounder(userID))
            throw new Exception("Only the founder of the store can open it");
        if (storeStatus == OPEN) {
            return false;
        } else if (storeStatus == PERMANENTLY_CLOSE) {
            throw new Exception("Store is permanently close and cannot change its status to open");
        } else {
            storeStatus = OPEN;
            List<StoreEmployees> storeOwnersAndManagers = new ArrayList<>();
            storeOwnersAndManagers.addAll(storeOwners);
            storeOwnersAndManagers.addAll(storeManagers);
            List<Integer> sendToList = storeOwnersAndManagers.stream().map(StoreEmployees::getUserID).collect(Collectors.toList());

            sendMsgToListAndAvailable(sendToList, "Store " + storeName + " has opened");

            log.info("Store " + storeID + " opened");
            return true;
        }
    }

    public void sendMsgToListAndAvailable(List<Integer> sendToList, String s) throws Exception {
        mailbox.sendMessageToList(sendToList, s);
        mailbox.setMailboxAsAvailable();
    }

    private List<Integer> getOwnerIDs() {
        return storeOwners.stream().map(StoreEmployees::getUserID).toList();
    }

    private List<Integer> getManagerIDs() {
        return storeManagers.stream().map(StoreEmployees::getUserID).toList();
    }

    public boolean closeStore(int userID) throws Exception {
        if (!isFounder(userID))
            throw new Exception("Only the founder of the store can close it");
        if (storeStatus == CLOSE) {
            return false;
        } else if (storeStatus == PERMANENTLY_CLOSE) {
            throw new Exception("Store is permanently close and cannot change its status to close");
        } else {
            storeStatus = CLOSE;
            List<StoreEmployees> storeOwnersAndManagers = new ArrayList<>();
            storeOwnersAndManagers.addAll(storeOwners);
            storeOwnersAndManagers.addAll(storeManagers);
            List<Integer> sendToList = storeOwnersAndManagers.stream().map(StoreEmployees::getUserID).collect(Collectors.toList());

            sendMsgToListAndUnavailable(sendToList, "Store " + storeName + " has closed");

            log.info("Store " + storeID + " closed");
            storeDAO.save(this);
            return true;
        }
    }

    public void sendMsgToListAndUnavailable(List<Integer> sendToList, String s) throws Exception {
        mailbox.sendMessageToList(sendToList, s);
        mailbox.setMailboxAsUnavailable();
    }

    public boolean isFounder(int userId) {
        return founderID == userId;
    }


    public boolean closeStorePermanently() throws Exception {
        if (storeStatus == PERMANENTLY_CLOSE) {
            return false;
        } else {
            storeStatus = PERMANENTLY_CLOSE;
            List<StoreEmployees> storeOwnersAndManagers = new ArrayList<>();
            storeOwnersAndManagers.addAll(storeOwners);
            storeOwnersAndManagers.addAll(storeManagers);
            List<Integer> sendToList = storeOwnersAndManagers.stream().map(StoreEmployees::getUserID).collect(Collectors.toList());
            sendMsgToListAndUnavailable(sendToList, "Store " + storeName + " has closed permanently");
            storeOwners = new HashSet<>();
            storeManagers = new HashSet<>();
            log.info("Store " + storeID + " is permanently closed");
            storeDAO.save(this);
            return true;
        }
    }

    public void addManager(StoreManager manager) {
        this.storeManagers.add(manager);
        storeDAO.save(this);
    }

    public void addOwner(StoreOwner user) {
        this.storeOwners.add(user);
        storeDAO.save(this);
    }

    //Integer instead of int so that it removes by object not index
    public void removeManager(StoreManager manager) {
        this.storeManagers.remove(manager);
        storeDAO.save(this);
    }

    //Integer instead of int so that it removes by object not index
    public void removeOwner(StoreOwner owner) {
        this.storeOwners.remove(owner);
        storeDAO.save(this);
    }

    public CatalogItem removeItemFromStore(int itemID) throws Exception {
        if (getItem(itemID) == null)
            return null;
        if (haveActiveNonEmptyLottery(itemID))
            throw new Exception("Someone participates in a lottery for this item, please try again when the lottery ends");
        else
            removeItemLotteries(itemID);
        if (haveActiveNonEmptyAuction(itemID))
            throw new Exception("Someone participates in an auction for this item, please try again when the auction ends");
        else
            removeItemAuctions(itemID);
        if (haveActiveBids(itemID))
            throw new Exception("Someone participates in an bid for this item, please try again when the bid ends");
        removeItemFromDiscountsAndPolicies(itemID);
        storeDAO.save(this);
        return removeItem(itemID);
    }

    private void removeItemFromDiscountsAndPolicies(int itemID) {
        Discount discount;
        for (DiscountPair pair : discounts) {
            discount = pair.getValue();
            discount.removeItem(itemID);
        }
        for (PurchasePolicy purchasePolicy : purchasePolicies.values()) {
            purchasePolicy.removeItem(itemID);
        }
        for (DiscountPolicy discountPolicy : discountPolicies.values()) {
            discountPolicy.removeItem(itemID);
        }
    }

    private void removeItemAuctions(int itemID) {
        List<Integer> auctionsIDsToRemove = new ArrayList<>();
        for (Auction auction : auctions.values()) {
            if (auction.getItemID() == itemID)
                auctionsIDsToRemove.add(auction.getAuctionID());
        }
        for (Integer auctionID : auctionsIDsToRemove) {
            auctions.remove(auctionID);
        }
    }

    private void removeItemLotteries(int itemID) {
        List<Integer> lotteriesIDsToRemove = new ArrayList<>();
        for (Lottery lottery : lotteries.values()) {
            if (lottery.getItemID() == itemID)
                lotteriesIDsToRemove.add(lottery.getLotteryID());
        }
        for (Integer lotteryID : lotteriesIDsToRemove) {
            lotteries.remove(lotteryID);
        }
    }

    private boolean haveActiveBids(int itemID) {
        for (Bid bid : bids.values()) {
            if (bid.getItemID() == itemID)
                return true;
        }
        return false;
    }

    private boolean haveActiveNonEmptyAuction(int itemID) {
        for (Auction auction : auctions.values()) {
            if (auction.getItemID() == itemID && auction.getCurrentWinningUserID() != -1)
                return true;
        }
        return false;
    }

    private boolean haveActiveNonEmptyLottery(int itemID) {
        for (Lottery lottery : lotteries.values()) {
            if (lottery.getItemID() == itemID && !lottery.getParticipants().isEmpty())
                return true;
        }
        return false;
    }

    private CatalogItem removeItem(int itemID) {
        CatalogItem item = getItem(itemID);
        items.remove(item);
        storeDAO.removeItem(item);
        return item;
    }

    public String updateItemName(int itemID, String newName) throws Exception {
        if (isItemInCatalog(itemID)) {
            String name = getItem(itemID).setName(newName);
            storeDAO.updateItemName(getItem(itemID));
            return name;
        }
        throw new Exception("Item with ID " + itemID + " is not exist in store " + storeName);
    }

    public Boolean checkIfStoreOwner(int userID) {
        return getOwnerIDs().contains(userID);
    }

    public Boolean checkIfStoreManager(int userID) {
        return getManagerIDs().contains(userID);
    }

    public StoreMailbox getMailBox() {
        return mailbox;
    }

    public void sendMessage(int receiverID, String content) {
        mailbox.sendMessage(receiverID, content);
    }

    public ConcurrentHashMap<Integer, Chat> getChats() {
        return mailbox.getChatsAsMap();
    }

    public void setMailboxAsUnavailable() throws Exception {
        mailbox.setMailboxAsUnavailable();
    }

    public void setMailboxAsAvailable() throws Exception {
        mailbox.setMailboxAsAvailable();
    }

    public Map<Integer, Discount> getStoreDiscounts() {
        Map<Integer, Discount> discountsMap = new HashMap<>();

        for (DiscountPair pair : discounts) {
            discountsMap.put(pair.getKey(), pair.getValue());
        }

        return discountsMap;
    }

    public Map<Integer, Visible> getStoreVisibleDiscounts() {
        Map<Integer, Visible> visibleDiscounts = new HashMap<>();
        for (DiscountPair pair : discounts) {
            if (pair.getValue() instanceof Visible) {
                visibleDiscounts.put(pair.getKey(), (Visible) pair.getValue());
            }
        }
        return visibleDiscounts;
    }

    public Map<Integer, PurchasePolicy> getStorePurchasePolicies() {
        return purchasePolicies;
    }

    public Map<Integer, DiscountPolicy> getStoreDiscountPolicies() {
        return discountPolicies;
    }

    public void updateItemDiscounts(int itemID) {
        CatalogItem item = getItem(itemID);
        String category = item.getCategory();
        List<Discount> result = new ArrayList<>();

        Discount discount;
        for (DiscountPair pair : discounts) {
            discount = pair.getValue();
            if (discount.isDiscountApplyForItem(itemID, category)) {
                result.add(discount);
            }
        }

        item.setDiscounts(result);
    }

    public void updateItemPurchasePolicies(int itemID) {
        CatalogItem item = getItem(itemID);
        String category = item.getCategory();
        List<PurchasePolicy> result = new ArrayList<>();
        for (PurchasePolicy purchasePolicy : purchasePolicies.values()) {
            if (purchasePolicy.isPurchasePolicyApplyForItem(itemID, category)) {
                result.add(purchasePolicy);
            }
        }
        item.setPurchasePolicies(result);
    }

    public void updateItemDiscountPolicies(int itemID) {
        CatalogItem item = getItem(itemID);
        String category = item.getCategory();
        List<DiscountPolicy> result = new ArrayList<>();
        for (DiscountPolicy discountPolicy : discountPolicies.values()) {
            if (discountPolicy.isDiscountPolicyApplyForItem(itemID, category)) {
                result.add(discountPolicy);
            }
        }
        item.setDiscountPolicies(result);
    }

    public boolean isItemInCatalog(int id) {
        return getItem(id) != null;
    }

    public HashMap<Integer, Integer> getItemsAmount() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (CatalogItem item : items) {
            map.put(item.getItemID(), item.getAmount());
        }
        return map;
    }

    public boolean payForBid(int bidID, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        Bid bid = bids.get(bidID);
        HashMap<Integer, Map<CatalogItem, CartItemInfo>> receiptData = new HashMap<>(); //TODO

        CatalogItem item = getItem(bid.getItemID());
        List<CartItemInfo> cartItems = new ArrayList<>();
        cartItems.add(new CartItemInfo(item.getItemID(), 1, bid.getOfferedPrice(), item.getCategory(), item.getItemName(), item.getWeight()));

        //check bid validity
        try {
            checkIfPurchaseIsValid(cartItems, purchaseInfo.getAge());
        } catch (IllegalStateException msg) {
            sendMsg(bid.getUserID(), msg.getMessage());
            log.warning("Trying to buy a Item from Bid with store: " + storeName + ", but you don't comply with the purchase policies");
            throw new IllegalStateException(msg);
        }

        ESPurchaseManager purchaseManager = new ESPurchaseManager(new PurchaseClient(), new SupplyClient(), purchaseInfo, supplyInfo);
        if (!purchaseManager.handShake()) {
            throw new Exception("Problem with connection to external System");
        }

        saveItemAmount(item.getItemID(), 1);

        int purchaseTransId = purchaseManager.pay();

        purchaseManager.chooseSupplyService();
        int supplyTransId = purchaseManager.supply();

        if (purchaseTransId == -1 || supplyTransId == -1) {
            reverseSavedItems(cartItems);
            purchaseManager.cancelSupply(supplyTransId);
            purchaseManager.cancelPay(purchaseTransId);
            throw new Exception("Problem with Supply or Purchase");
        } else {
            Log.log.info("Bid  payment completed");
            Log.log.info("Bid delivery is scheduled");
        }
//        receiptData.putIfAbsent(basket.getStore().getStoreID(), basket.buyBasket(userID));
        removeBid(bidID);
        return true;
    }

    public void cancelBid(int id) {
        removeBid(id);
    }

    public Appointment addAppointment(Integer creatorId, Integer newOwnerId) throws Exception {
        //maybe check if store owners contains key
        if (getOwnerIDs().contains(newOwnerId))
            throw new Exception("This User is already a store Owner");
        if (getAppointmentByNewOwnerId(newOwnerId)!=null)
            throw new Exception("This User is already a candidate in this store!");
        Appointment appointment = new Appointment(getOwnerIDs(), creatorId, storeID, newOwnerId);
        appointments.add(appointment);
        List<Integer> sendToList = getOwnerIDs();
        sendMsgToList(sendToList, "User " + creatorId + " offered new appointment for user " + newOwnerId + " at store " + storeName);
        storeDAO.save(this);
        appointmentDAO.addAppointment(appointment);
        return appointment;
    }

    public void removeAppointment(int userId) throws Exception {
        Appointment a = getAppointmentByNewOwnerId(userId);
        if(a!=null){
            appointmentDAO.removeAppointment(a);
            appointments.remove(a);
            storeDAO.save(this);
        }
    }

    private Appointment getAppointmentByNewOwnerId(int newOwnerId) {
        List<Appointment> appointmentList=appointments.stream().filter(appointment -> appointment.getNewOwnerId()==newOwnerId).toList();
        if (!appointmentList.isEmpty())
            return appointmentList.get(0);
        else
            return null;
    }

    public void rejectAppointment(int theOwnerId) throws Exception {
        removeAppointment(theOwnerId);
        List<Integer> sendToList = getOwnerIDs();
        sendMsgToList(sendToList, "Appointment of " + theOwnerId + " at store " + storeName + " was rejected");
        storeDAO.save(this);
    }

    private void accept(int myId, int theNewOwnerId) throws Exception {
        Appointment appointment=getAppointmentByNewOwnerId(theNewOwnerId);
        if (appointment==null)
            throw new Exception("cant find this appointment");
        appointment.accept(myId);
        storeDAO.save(this);
    }

    private boolean isAllAccepted(int newOwnerId) throws Exception {
        Appointment appointment=getAppointmentByNewOwnerId(newOwnerId);
        if (appointment==null)
            throw new Exception("cant find this appointment");
        else
            return !appointment.getAcceptMap().containsValue(false);//at least one not yet accepted
    }

    public boolean acceptAppointment(int myId, int theOwnerId) throws Exception {
        accept(myId, theOwnerId);
        if (isAllAccepted(theOwnerId)) {
            return true;
        }
        return false;
    }
}
