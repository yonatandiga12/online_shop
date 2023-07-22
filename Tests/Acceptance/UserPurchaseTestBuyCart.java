package Acceptance;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import ServiceLayer.Objects.CartService;
import ServiceLayer.Objects.ReceiptService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class UserPurchaseTestBuyCart extends ProjectTest{


    public static boolean doneSetUp = false;

    @Before
    public void setUp() {
        super.setUp();
        if(!doneSetUp) {
            setUpAllMarket();
            doneSetUp = true;
        }
    }


    @After
    public void tearDown() {
        //delete stores and delete users from DB
    }


    /**
     * Buy cart #15
     */
    @Test
    public void buyCart_Valid(){
        int userId = buyCartForTests();

        CartService afterCart = this.getCart(userId);
        assertTrue(afterCart.isEmpty());

        List<ReceiptService> receipts = this.getSellingHistoryOfStore(user7SystemManagerId, store2Id);

        assertTrue(receipts.get(0).hasItem(userId, item1Added));
        assertTrue(receipts.get(0).hasItem(userId, item2Added));

    }


    @Test
    @Ignore
    public void buyCart_WrongPaymentDetails(){
        //ccv = 984 should get the system stuck
        PurchaseInfo purchaseInfo = new PurchaseInfo("number", 1, 2020, "adasd", 984, user4LoggedInId, LocalDate.of(2000, 1, 1));
        SupplyInfo supplyInfo = new SupplyInfo("name", "address", "city", "country", "zip");
        boolean added = this.getBridge().buyCart(user4LoggedInId, purchaseInfo, supplyInfo);
        assertFalse(added);
    }



    @Test
    public void buyCart_UserNotLoggedIn(){
        logOut(user3NotLoggedInId);
        boolean added = this.buyCart(user3NotLoggedInId, "PaymentDetails");
        assertFalse(added);
    }

    @Test
    public void buyCart_NotEnoughItemsInStore(){
        int id = setUser("UserUserPutrchase2", "User44", MEMBER, LOGGED);
        int itemAdded1 = addItemToStoreForTests(store2Id, "Itemanme", 10, "Kitchen", 10);
        int itemAdded2 = addItemToStoreForTests(store2Id, "Itemname2", 10, "Kitchen", 10);
        //addItemsToUserForTests(id, store2Id, item1Added);
        addItemToBasket(id, store2Id, itemAdded2, 100);
        addItemToBasket(id, store2Id, itemAdded1, 100);
        boolean added = this.buyCart(id, "AA");
        assertFalse(added);
    }

    private int buyCartForTests() {
        int id = setUser("UserUserPutrchase", "User44", MEMBER, LOGGED);
        item1Added = addItemToStoreForTests(store2Id, "Itemanme", 10, "Kitchen", 10);
        item2Added = addItemToStoreForTests(store2Id, "Itemname2", 10, "Kitchen", 10);
        addItemsToUserForTests(id, store2Id, item1Added);
        addItemsToUserForTests(id, store2Id, item2Added);
        buyCart(id, "AA");
        return id;
    }




    protected static int user1GuestId = -1;         //guest - active
    protected static int user2LoggedInId = -1;
    protected static int user3NotLoggedInId = -1;   // registered, not logged in
    protected static int user4LoggedInId = -1;      //logged in, have items in carts
    protected static int user5ManagerOfStore2ToBeRemoved = -1; //Owner/Manager of store2, to be removed positioned  by user2
    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
    protected static int store2Id = -1;             //store is open
    protected static int store2ClosedId = -1;
    protected static int store4Id = -1;
    protected static int item1Id = -1;              //item1 in user1 basket
    protected static int item11Id = -1;             //item11 in store2 but not in basket
    protected static int item2Id = -1;              //item2 in store2
    protected static int item2ToBeRemovedId = -1;
    protected static int item4Id = -1;
    protected static int item1Added = -1;
    protected static int item2Added = -1;

    /**
     * User1: Guest, Not logged In
     */
    protected void setUpUser1(){
        user1GuestId = setUser("User1","User1!", GUEST, NOT_LOGGED);
    }

    /**
     * User2: Member, logged in, Store Owner and Manager of store2
     */
    protected void setUpUser2(){
        if(user2LoggedInId != -1){
            return;
        }
        user2LoggedInId = setUser("User2UserPurchase","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5UserPurchase", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6UserPurchase", "User6!", MEMBER, LOGGED);
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

    /**
     * User3: Member, Not logged in, Has a cart with items
     */
    protected void setUpUser3() {
        if(user3NotLoggedInId != -1)
            return;
        user3NotLoggedInId = setUser("User3UserPurchase","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4UserPurchase","User4!", MEMBER, LOGGED);
        if(user2LoggedInId == -1)
            user2LoggedInId = setUser("User2UserPurchase","User2!", MEMBER, LOGGED);   //created for the ownership of the store
        store4Id = createStore(user4LoggedInId, "Store4");  //user4 is founder, user2 is owner
        //add items
        item4Id = addItemToStoreForTests(store4Id, "Item4", 10, "Clothes", 10 );
    }



    /**
     * Set up all Users and Stores. user1 and user2 have carts with items in them
     */
    protected void setUpAllMarket() {
        setUpUser1();
        setUpUser2();
        setUpUser3();
        setUpUser4();
        addItemsToUserForTests(user4LoggedInId, store2Id, item1Id);
        addItemsToUserForTests(user4LoggedInId, store4Id, item4Id);
    }


}
