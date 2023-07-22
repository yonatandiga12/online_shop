package Acceptance;

import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import ServiceLayer.Objects.RuleService;
import ServiceLayer.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DiscountsTests extends ProjectTest{

    public static boolean doneSetUp = false;

    @Before
    public void setUp() {
        super.setUp();
        if(!doneSetUp) {
            setUpUser2();
            doneSetUp = true;
        }
    }


    @After
    public void tearDown() {
    }


    /**
     * Change discount 45
     */
    @Test
    public void addVisibleItemsDiscounts_Valid(){
        List<Integer> ids = new ArrayList<>();
        ids.add(item2Id);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addVisibleItemsDiscount(store2Id, ids, 50, calendar);
        assertTrue(id >= 0);
    }


    @Test
    public void addVisibleItemsDiscounts_ItemsNotExist(){
        List<Integer> ids = new ArrayList<>();
        ids.add(-1);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addVisibleItemsDiscount(store2Id, ids, 50, calendar);
        assertTrue(id >= 0);
    }


    @Test
    public void addVisibleItemsDiscounts_DateNotValid(){
        List<Integer> ids = new ArrayList<>();
        ids.add(item1Id);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        int id = this.addVisibleItemsDiscount(store2Id, ids, 50, calendar);
        assertTrue(id < 0);
    }


    @Test
    public void addVisibleCategoryDiscounts_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addVisibleCategoryDiscount(store2Id, "Books", 40, calendar);
        assertTrue(id >= 0);
    }

    @Test
    public void addVisibleCategoryDiscounts_NonValidStore(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addVisibleCategoryDiscount(-1, "NotValidCategory", 40, calendar);
        assertTrue(id < 0);
    }


    @Test
    public void addConditionalStoreDiscounts_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addConditionalStoreDiscount(store2Id,40, calendar);
        assertTrue(id >= 0);

    }

   @Test
   public void addHiddenStoreDiscount_Valid(){
       Calendar calendar = Calendar.getInstance();
       calendar.add(Calendar.HOUR_OF_DAY, 4);
       int id = this.addHiddenStoreDiscount(store2Id, 50, "GG", calendar);
       assertTrue(id >= 0);
   }

   @Test
   public void addDiscountBasketTotalPriceRule_Valid(){
       Calendar calendar = Calendar.getInstance();
       calendar.add(Calendar.HOUR_OF_DAY, 4);
       int id = this.addConditionalStoreDiscount(store2Id,40, calendar);

       RuleService rule = getBridge().addDiscountBasketTotalPriceRule(store2Id, id, 70);
       assertTrue(rule.getId() > 0);
       assertTrue(rule.getInfo().contains("total price"));
   }


    @Test
    public void wrap2DiscountsMAX_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addHiddenStoreDiscount(store2Id, 40, "GG", calendar);
        int id2 = this.addHiddenStoreDiscount(store2Id, 50, "GG", calendar);

        int newDiscountId = getBridge().wrapDiscounts(store2Id,Arrays.asList(id2, id) , NumericComposites.MAX);
        assertTrue(newDiscountId > 0);
    }


    @Test
    public void wrap2DiscountsMIN_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addHiddenStoreDiscount(store2Id, 40, "GG", calendar);
        int id2 = this.addHiddenStoreDiscount(store2Id, 50, "GG", calendar);

        int newDiscountId = getBridge().wrapDiscounts(store2Id,Arrays.asList(id2, id) , NumericComposites.MIN);
        assertTrue(newDiscountId > 0);
    }

    @Test
    public void wrap2DiscountsADD_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int id = this.addHiddenStoreDiscount(store2Id, 40, "GG", calendar);
        int id2 = this.addHiddenStoreDiscount(store2Id, 50, "GG", calendar);

        int newDiscountId = getBridge().wrapDiscounts(store2Id,Arrays.asList(id2, id) , NumericComposites.ADD);
        assertTrue(newDiscountId > 0);
    }


    @Test
    public void addRuleCompositeAND_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int discountId = this.addConditionalStoreDiscount(store2Id, 40, calendar);

        RuleService rule1 = getBridge().addDiscountBasketTotalPriceRule(store2Id, discountId, 30);
        Map<Integer, Integer> map = new HashMap<>();
        map.put(item1Id, 200);
        RuleService rule2 = getBridge().addDiscountQuantityRule(store2Id, discountId, map);
        assertTrue(rule1.getId() >= 0);
        assertTrue(rule2.getId() >= 0);

        RuleService newRule = getBridge().addDiscountComposite(store2Id, discountId, LogicalComposites.AND, Arrays.asList(rule2.getId(), rule1.getId()));
        assertTrue(newRule.getId() > 0);
        assertTrue(newRule.getInfo().contains(rule1.getInfo()));
        assertTrue(newRule.getInfo().contains(rule2.getInfo().strip()));
        assertTrue(newRule.getInfo().contains("&"));
    }


    @Test
    public void addRuleCompositeOR_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        int discountId = this.addConditionalStoreDiscount(store2Id, 40, calendar);

        RuleService rule1 = getBridge().addDiscountBasketTotalPriceRule(store2Id, discountId, 30);
        Map<Integer, Integer> map = new HashMap<>();
        map.put(item1Id, 200);
        RuleService rule2 = getBridge().addDiscountQuantityRule(store2Id, discountId, map);
        assertTrue(rule1.getId() >= 0);
        assertTrue(rule2.getId() >= 0);

        RuleService newRule = getBridge().addDiscountComposite(store2Id, discountId, LogicalComposites.OR, Arrays.asList(rule2.getId(), rule1.getId()));
        assertTrue(newRule.getId() > 0);
        assertTrue(newRule.getInfo().contains(rule1.getInfo()));
        assertTrue(newRule.getInfo().contains(rule2.getInfo().strip()));
        assertTrue(newRule.getInfo().contains("|"));
    }


    /**
     * Add discount policy #46
     */
    @Test
    public void addDiscountPolicyBuyerAgeRule_valid(){
        RuleService ruleService = getBridge().addDiscountPolicyBuyerAgeRule(store2Id, 10);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("10"));
    }


    @Test
    public void addDiscountPolicyBuyerAgeRule_StoreNotExists(){
        RuleService ruleService = getBridge().addDiscountPolicyBuyerAgeRule(-1, 10);
        assertNull(ruleService);
    }

    @Test
    public void addDiscountPolicyMustItemsAmountsRule_Valid(){
        Map<Integer, Integer> map = new HashMap<>();
        map.put(item1Id, 22); map.put(item2Id, 22);
        RuleService ruleService = getBridge().addDiscountPolicyMustItemsAmountsRule(store2Id, map);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("item1"));
        assertTrue(ruleService.getInfo().contains("item2"));
    }

    @Test
    public void addDiscountPolicyMustItemsAmountsRule_ItemsNotInStore(){
        Map<Integer, Integer> map = new HashMap<>();
        map.put(-1, 22);
        RuleService ruleService = getBridge().addDiscountPolicyMustItemsAmountsRule(store2Id, map);
        assertNull(ruleService);
    }

    /**
     * Add purchase policy #46
     */
    @Test
    public void addPurchasePolicyItemsWeightLimitRule_Valid(){
        Map<Integer, Double> map = new HashMap<>();
        map.put(item1Id, 22.5);
        RuleService ruleService = getBridge().addPurchasePolicyItemsWeightLimitRule(store2Id, map);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("item1"));
        assertTrue(ruleService.getInfo().contains("22.5"));
    }

    @Test
    public void addPurchasePolicyItemsWeightLimitRule_NegativeWeight(){
        Map<Integer, Double> map = new HashMap<>();
        map.put(item1Id, -22.5);
        RuleService ruleService = getBridge().addPurchasePolicyItemsWeightLimitRule(store2Id, map);
        assertNull(ruleService);
    }

    @Test
    public void addPurchasePolicyMustDatesRule_Valid(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        RuleService ruleService = getBridge().addPurchasePolicyMustDatesRule(store2Id, Arrays.asList(calendar));
        assertTrue(ruleService.getId() >= 0);
    }

    @Test
    public void addPurchasePolicyMustDatesRule_DatePassed(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);
        RuleService ruleService = getBridge().addPurchasePolicyMustDatesRule(store2Id, Arrays.asList(calendar));
        assertNull(ruleService);
    }

    @Test
    public void wrapPurchasePoliciesAND_Valid(){
        List<Integer> ids = setUp2Rules();
        RuleService ruleService = getBridge().wrapPurchasePolicies(store2Id, ids, LogicalComposites.AND);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("item1"));
        assertTrue(ruleService.getInfo().contains("&"));
    }


    @Test
    public void wrapPurchasePoliciesOr_Valid(){
        List<Integer> ids = setUp2Rules();
        RuleService ruleService = getBridge().wrapPurchasePolicies(store2Id, ids, LogicalComposites.OR);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("item1"));
        assertTrue(ruleService.getInfo().contains("|"));

    }

    @Test
    public void wrapPurchasePoliciesCONDITIONING_Valid(){
        List<Integer> ids = setUp2Rules();
        RuleService ruleService = getBridge().wrapPurchasePolicies(store2Id, ids, LogicalComposites.CONDITIONING);
        assertTrue(ruleService.getId() >= 0);
        assertTrue(ruleService.getInfo().contains("item1"));
        assertTrue(ruleService.getInfo().contains("unless"));
    }

    @Test
    public void wrapPurchasePoliciesCONDITIONING_NotExistingStore(){
        List<Integer> ids = setUp2Rules();
        RuleService ruleService = getBridge().wrapPurchasePolicies(-1, ids, LogicalComposites.CONDITIONING);
        assertNull(ruleService);
    }


    private List<Integer> setUp2Rules() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        RuleService rule1 = getBridge().addPurchasePolicyMustDatesRule(store2Id, Arrays.asList(calendar));

        Map<Integer, Double> map = new HashMap<>();
        map.put(item1Id, 10.0);
        RuleService rule2 = getBridge().addPurchasePolicyItemsWeightLimitRule(store2Id, map);

        return Arrays.asList(rule1.getId(), rule2.getId());
    }






    protected static int discountId = -1;
    protected static int user2LoggedInId = -1;
    protected static int user5ManagerOfStore2ToBeRemoved = -1; //Owner/Manager of store2, to be removed positioned  by user2
    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
    protected static int store2Id = -1;             //store is open
    protected static int store2ClosedId = -1;
    protected static int item1Id = -1;              //item1 in user1 basket
    protected static int item11Id = -1;             //item11 in store2 but not in basket
    protected static int item2Id = -1;              //item2 in store2
    protected static int item2ToBeRemovedId = -1;

    /**
     * User2: Member, logged in, Store Owner and Manager of store2
     */
    protected void setUpUser2(){
        if(user2LoggedInId != -1){
            return;
        }
        user2LoggedInId = setUser("User2DiscountClass","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5DiscountClass", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6DiscountClass", "User6!", MEMBER, LOGGED);
        store2Id = createStore(user2LoggedInId, "Store2"); //store is open
        store2ClosedId = createStore(user2LoggedInId, "Store22"); //store is close
        closeStore(user2LoggedInId, store2ClosedId);

        //Make user6 and user5 manager Owner
        defineStoreOwner(store2Id, user2LoggedInId, user6OwnerOfStore2);
        defineStoreManager(store2Id , user2LoggedInId, user5ManagerOfStore2ToBeRemoved);

        //add items
        item1Id = addItemToStoreForTests(store2Id, "item1", 10, "Books", 10);
        item11Id = addItemToStoreForTests(store2Id, "item11", 10, "Books", 10);
        item2Id = addItemToStoreForTests(store2Id, "item2", 10, "Kitchen", 10);
        item2ToBeRemovedId = addItemToStoreForTests(store2Id, "Name2", 10, "Kitchen", 10);
    }




}
