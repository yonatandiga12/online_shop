package Acceptance;

import ServiceLayer.Objects.BasketService;
import ServiceLayer.Objects.CartService;
import ServiceLayer.Objects.StoreService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConcurrencyTests extends ProjectTest{

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

    boolean user2Bought = true, user4Bought = true, user2Erased = true;

    /**
     * 2 Users trying to buy the last item available
     */
    @Test
    public void twoUsersBuyingLastItem() throws Exception
    {
        //Store2 as only 1 item21 !!
        int item21Id = addItemToStoreForTests(store2Id, "Item2222222", 10, "Kitchen", 1);

        addItemToBasket(user2LoggedInId, store2Id, item21Id, 1);
        addItemToBasket(user4LoggedInId, store2Id, item21Id, 1);


        Thread thread1 = new Thread("User2") {
            public void run(){
                user2Bought = buyCart(user2LoggedInId, "Details");
            }
        };

        Thread thread2 = new Thread("User4") {
            public void run(){
                user4Bought = buyCart(user4LoggedInId, "Details");
            }
        };

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNotEquals(user4Bought, user2Bought);
        StoreService info = getStoreInfo(store2Id);
        boolean exists = info.hasItem(item21Id);
        assertFalse(exists);


        CartService cart2 = getCart(user2LoggedInId);
        CartService cart4 = getCart(user4LoggedInId);
        boolean cart2Empty ,cart4Empty;
        cart2Empty = cart2.isEmpty();
        cart4Empty = cart4.isEmpty();
        assertNotEquals(cart2Empty, cart4Empty);
    }


    /**
     * user trying to buy an item when a Store Owner deletes it
     */
    @Test
    public void buyingAndDeletingTheSameTime(){
        int item22Id = addItemToStoreForTests(store2Id, "Item221", 10, "Kitchen", 10);
        addItemToBasket(user4LoggedInId, store2Id, item22Id, 1);
        Thread thread1 = new Thread("User2") {
            public void run(){
                user4Bought = buyCart(user4LoggedInId, "Details");
            }
        };

        Thread thread2 = new Thread("User4") {
            public void run(){
                user2Erased = removeItemFromStore(store2Id, item22Id);
            }
        };

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //This test could fail sometimes, because purchase then remove is legal
        //And the test checks the case where trying to remove then purchase.
        //assertNotEquals(user4Bought, user2Erased);

        StoreService info = getStoreInfo(store2Id);
        boolean itemExists = info.hasItem(item22Id);

        CartService cart4 = getCart(user4LoggedInId);
        BasketService basket = cart4.getBasketOfStore(store2Id);
        if(basket != null){  //there is no basket for this store, the item was bought
            boolean stillInCart = cart4.getBasketOfStore(store2Id).hasItem(item22Id);
            assertNotEquals(stillInCart, itemExists);
        }

    }


    boolean manager5 = false, manager6 = false;
    /**
     * 2 Store Owners try to put same user as manager
     */
    @Test
    public void simultaneouslyAddManager(){
        int secondOwnerForStore2 = setUser("user9ConcurrencyTests", "User8!", MEMBER, LOGGED);
        boolean ownerDefined = defineStoreOwner(store2Id, user6OwnerOfStore2, secondOwnerForStore2);
        assertTrue(ownerDefined);

        int newManagerId = setUser("user8ConcurrencyTests", "User8!", MEMBER, LOGGED);
        Thread thread1 = new Thread("User2") {
            public void run(){
                manager5 = defineStoreManager(store2Id, user6OwnerOfStore2, newManagerId);
            }
        };

        Thread thread2 = new Thread("User4") {
            public void run(){
                manager6 = defineStoreManager(store2Id, secondOwnerForStore2, newManagerId);
            }
        };

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNotEquals(manager5, manager6);

    }





    protected static int user1GuestId = -1;         //guest - active
    protected static int user2LoggedInId = -1;
    protected static int user3NotLoggedInId = -1;   // registered, not logged in
    protected static int user4LoggedInId = -1;      //logged in, have items in carts
    protected static int user5ManagerOfStore2ToBeRemoved = -1; //Owner/Manager of store2, to be removed positioned  by user2
    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
    protected static int userNotExistId = -1;
    protected static int store2Id = -1;             //store is open
    protected static int store2ClosedId = -1;
    protected static int store4Id = -1;
    protected static int item1Id = -1;              //item1 in user1 basket
    protected static int item11Id = -1;             //item11 in store2 but not in basket
    protected static int item2Id = -1;              //item2 in store2
    protected static int item2ToBeRemovedId = -1;
    protected static int item4Id = -1;




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
        user2LoggedInId = setUser("User2ConcurrencyTests","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5ConcurrencyTests", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6ConcurrencyTests", "User6!", MEMBER, LOGGED);
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
        user3NotLoggedInId = setUser("User3ConcurrencyTests","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4ConcurrencyTests","User4!", MEMBER, LOGGED);
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
