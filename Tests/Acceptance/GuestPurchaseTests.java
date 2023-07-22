package Acceptance;

import Globals.FilterValue;
import Globals.FilterValue.*;
import Globals.SearchBy;
import Globals.SearchFilter;
import ServiceLayer.Objects.CartService;
import ServiceLayer.Objects.CatalogItemService;
import ServiceLayer.Objects.StoreService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class GuestPurchaseTests extends ProjectTest{


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
     * Get Store info #11
     * #18 is the same
     */
    @Test
    public void getStoreInfoValid(){
        StoreService storeInfo = this.getStoreInfo(store2Id);
        assertEquals(storeInfo.getStoreName(), "Store2");
        assertTrue(storeInfo.getStoreId() >= 0);
    }


    @Test
    public void getStoreInfoWrongId(){
        StoreService storeInfo = this.getStoreInfo(-1);
        assertNull(storeInfo);
    }


    /**
     * Search items #12
     */
    @Test
    public void searchItemsByItemName_Valid() throws Exception
    {
        addItemToStoreForTests(store2Id, "Bread", 10, "Kitchen", 10);
        addItemToStoreForTests(store2Id, "Bread2", 10, "Kitchen", 10);
        addItemToStoreForTests(store2Id, "Meat2", 10, "Kitchen", 10);
        String keyWords = "Bread";

        List<CatalogItemService> itemsFound = this.searchItems(keyWords, SearchBy.ITEM_NAME, new HashMap<>());

        boolean breadExists = false;
        for(CatalogItemService item: itemsFound){
            if(item.getItemName().contains("Bread")){
                breadExists = true;
            }
        }
        assertTrue(breadExists);
    }

    @Test
    public void searchItemsByCategory_Valid(){
        addItemToStoreForTests(store2Id, "Beans", 10, "Kitchen", 10);
        addItemToStoreForTests(store2Id, "Buns", 10, "Kitchen", 10);
        addItemToStoreForTests(store2Id, "Screw driver", 10, "Tools", 10);
        String category = "Kitchen";

        List<CatalogItemService> itemsFound = this.searchItems(category, SearchBy.CATEGORY, new HashMap<>());

        boolean wrongCategory = true;
        for(CatalogItemService item: itemsFound){
            if (!item.getCategory().equals("Kitchen")) {
                wrongCategory = false;
                break;
            }
        }
        assertTrue(wrongCategory);
    }


    @Test
    public void searchItemsByKeyWord_Valid(){
        addItemToStoreForTests(store2Id, "pants", 10, "Clothes", 10);
        addItemToStoreForTests(store2Id, "shorts", 10, "Clothes", 10);
        addItemToStoreForTests(store2Id, "shirt", 10, "Clothes", 10);
        addItemToStoreForTests(store2Id, "Clothes", 10, "Tools", 10);

        String keyWords = "pants,shirt,Tools";

        List<CatalogItemService> itemsFound = this.searchItems(keyWords, SearchBy.KEY_WORD, new HashMap<>());

        boolean foundPants = false;
        boolean foundShirt = false;
        boolean foundTools = false;
        for(CatalogItemService item: itemsFound){
            if(item.getItemName().equals("pants")){
                foundPants = true;
            }
            if(item.getItemName().equals("shirt")){
                foundShirt = true;
            }
            if(item.getCategory().equals("Tools")){
                foundTools = true;
            }
        }
        assertTrue(foundPants);
        assertTrue(foundShirt);
        assertTrue(foundTools);
    }



    @Test
    public void searchItemsNoMatch(){
        String keyWords = "Shoe";
        List<CatalogItemService> itemsFound = this.searchItems(keyWords, SearchBy.ITEM_NAME, new HashMap<>());
        assertEquals(0, itemsFound.size());
    }

    /**
     * Add to Basket #13
     */
    @Test
    public void addToBasketValid(){
        CartService cart = this.addItemToBasket(user4LoggedInId, store2Id, item11Id, 10);
        boolean added = cart.getBasketOfStore(store2Id).hasItem(item11Id);
        assertTrue(added);
    }

    @Test
    public void addToBasketStoreClosed(){
        CartService cart = this.addItemToBasket(user4LoggedInId, store2ClosedId, item1Id, 19);
        if(cart == null)
            assertNull(cart);
        else{
            boolean added = cart.getBasketOfStore(store2ClosedId).hasItem(item1Id);
            assertFalse(added);
        }
    }

    @Test
    public void addToBasketNegativeAmount() throws Exception
    {
        int item12Id = addItemToStoreForTests(store2Id, "Name11", 10, "Kitchen", 100);
        CartService cart = this.addItemToBasket(user2LoggedInId, store2Id, item12Id, -9);
        if(cart == null)
            assertNull(cart);
        else {
            boolean added = cart.getBasketOfStore(store2Id).hasItem(item12Id);
            assertFalse(added);
        }
    }

    @Test
    public void addToBasketItemNotInStore() throws Exception
    {
        addItemToStoreForTests(store4Id, "NameDD",10, "Kitchen", 10);
        CartService cart = this.addItemToBasket(user2LoggedInId, store2Id, store4Id, 10);
        if(cart == null)
            assertNull(cart);
        else{
            boolean added = cart.getBasketOfStore(store2Id).hasItem(item2Id);
            assertFalse(added);
        }
    }

    @Test
    public void addToBasketStoreNotExists() throws Exception
    {
        CartService cart = this.addItemToBasket(user2LoggedInId, -1, 1, 10);
        if(cart == null)
            assertNull(cart);
        else{
            boolean added = cart.getBasketOfStore(store2Id).hasItem(item2Id);
            assertFalse(added);
        }
    }

//    @Test
//    public void addToBasket_AlreadyInCart(){
//        int newItemId = addItemToStoreForTests(store4Id, "NameDDD",10, "Kitchen", 10);
//        this.addItemToBasket(user4LoggedInId, store4Id, newItemId, 5);
//        CartService cart = this.addItemToBasket(user4LoggedInId, store4Id, newItemId, 5);
//
//        assertNull(cart);
//    }

    /**
     * Show cart #14
     */
    @Test
    public void showCartValid(){
        CartService cart = this.getCart(user4LoggedInId);
        assertTrue(cart.getBasketOfStore(store2Id).hasItem(item1Id));
    }

    @Test
    public void showCartUserNotExist(){
        CartService cart = this.getCart(userNotExistId);
        assertNull(cart);
    }

    @Test
    public void showCartNotLoggedInUser(){
        CartService cart = this.getCart(user3NotLoggedInId);
        assertTrue(cart.isEmpty());
    }







    protected static int user2LoggedInId = -1;
    protected static int user3NotLoggedInId = -1;   // registered, not logged in
    protected static int user4LoggedInId = -1;      //logged in, have items in carts
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
     * User2: Member, logged in, Store Owner and Manager of store2
     */
    protected void setUpUser2(){
        if(user2LoggedInId != -1){
            return;
        }
        user2LoggedInId = setUser("User2GuestPurchaseTest","User2!", MEMBER, LOGGED);
        store2Id = createStore(user2LoggedInId, "Store2"); //store is open
        store2ClosedId = createStore(user2LoggedInId, "Store22"); //store is close
        closeStore(user2LoggedInId, store2ClosedId);

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
        user3NotLoggedInId = setUser("User3GuestPurchaseTest","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4GuestPurchaseTest","User4!", MEMBER, LOGGED);
        store4Id = createStore(user4LoggedInId, "Store4");  //user4 is founder, user2 is owner
        //add items
        item4Id = addItemToStoreForTests(store4Id, "Item4", 10, "Clothes", 10 );
    }


    /**
     * Set up all Users and Stores. user1 and user2 have carts with items in them
     */
    protected void setUpAllMarket() {
        setUpUser2();
        setUpUser3();
        setUpUser4();
        addItemsToUserForTests(user4LoggedInId, store2Id, item1Id);
        addItemsToUserForTests(user4LoggedInId, store4Id, item4Id);
    }



}
