package Acceptance;

import ServiceLayer.Objects.CatalogItemService;
import ServiceLayer.Objects.ReceiptService;
import ServiceLayer.Objects.StoreService;
import ServiceLayer.Objects.UserStaffInfoService;
import org.junit.*;
import org.junit.runners.MethodSorters;


import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreOwnerManagerTests extends ProjectTest{



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
     * Manage Inventory - Add #27
     */
    @Test
    public void AddItemToStore_Valid(){
        int itemId = addItemToStoreForTests(store2Id, "itemNameValid", 10, "Kitchen", 1);
        assertTrue(itemId > 0);

        StoreService store = this.getStoreInfo(store2Id);
        CatalogItemService item = store.getItem(itemId);
        assertEquals(item.getItemID(), itemId);
    }


    @Test
    public void AddItemToStore_StoreNotExisting(){
        int itemId = this.addCatalogItem(-1, "itemName2", 1, "Kitchen");
        assertTrue(itemId < 0);
    }


    /**
     * Manage Inventory - Remove #28
     */
    @Test
    public void removeItemFromStore_Valid(){
        boolean removed = this.removeItemFromStore(store2Id, item2ToBeRemovedId);
        assertTrue(removed);

        StoreService store = this.getStoreInfo(store2Id);
        CatalogItemService item = store.getItem(item2ToBeRemovedId);
        assertNull(item);
    }

    @Test
    public void removeItemFromStore_StoreNotExisting(){
        boolean removed = this.removeItemFromStore(-1, item2ToBeRemovedId);
        assertFalse(removed);
    }

    @Test
    public void removeItemFromStore_ItemNotInStore(){
        boolean removed = this.removeItemFromStore(store2Id, -1);
        assertFalse(removed);
    }

    /**
     * Manage Inventory - Change #29
     */
    @Test
    public void changeItemDetails_Valid(){
        boolean changed = this.changeItemName(store2Id, item2Id, "newName");
        assertTrue(changed);
    }

    @Test
    public void changeItemDetails_StoreNotExisting(){
        boolean changed = this.changeItemName(-1, item2Id, "newName" );
        assertFalse(changed);
    }

    @Test
    public void changeItemDetails_ItemNotInStore(){
        boolean changed = this.changeItemName(store2Id, -1, "newName");
        assertFalse(changed);
    }


    /**
     * Define Store Owner #32
     */
    @Test
    public void a3defineStoreOwner_Valid(){
        boolean defined = this.defineStoreOwner(store2Id, user2LoggedInId, user3NotLoggedInId);
        assertTrue(defined);
        assertTrue(checkIfStoreOwner(user3NotLoggedInId, store2Id));

    }

    @Test    //Checks circle of owners!
    public void a2defineStoreOwner_DefineMyOwner(){
        boolean defined = this.defineStoreOwner(store2Id, user6OwnerOfStore2, user2LoggedInId);
        assertFalse(defined);
    }

    @Test
    public void a1defineStoreOwner_AlreadyStoreOwner(){
        boolean defined = this.defineStoreOwner(store2Id, user2LoggedInId, user6OwnerOfStore2);
        assertFalse(defined);
    }

    @Test
    public void a4defineStoreOwner_UserNotStoreOwner(){
        boolean defined = this.defineStoreOwner(store4Id, user2LoggedInId, user6OwnerOfStore2);
        assertFalse(defined);
    }


    /**
     * Define Store Manager #33
     */
    @Test
    public void defineStoreManager_Valid(){
        boolean changed = this.defineStoreManager(store4Id, user4LoggedInId, user3NotLoggedInId);
        assertTrue(changed);

        assertTrue(checkIfStoreManager(user3NotLoggedInId, store4Id));
    }

    @Test    //Checks circle of Managers!
    public void defineStoreManager_DefineMyManager(){
        boolean defined = this.defineStoreManager(store2Id, user6OwnerOfStore2, user2LoggedInId);
        assertFalse(defined);
    }

    @Test
    public void defineStoreManager_AlreadyStoreManager(){
        boolean changed = this.defineStoreManager(store2Id, user2LoggedInId, user5ManagerOfStore2ToBeRemoved);
        //If its failing, check if the user4 is owner or manager of store2!!
        assertFalse(changed);
    }

    @Test
    public void defineStoreManager_UserNotThisStoreManager(){
        boolean changed = this.defineStoreManager(store4Id, user2LoggedInId, user6OwnerOfStore2);
        assertFalse(changed);

        assertFalse(checkIfStoreManager(user6OwnerOfStore2, store4Id));
    }


    /**
     * Remove Store Manager #33.5  NotForVersion1
     */
    @Test
    public void removeStoreManager_Valid(){
        int userToRemove = setUser("UsertoRemovee", "UsertoRemovee!!", MEMBER, LOGGED);
        defineStoreManager(store2Id, user2LoggedInId, userToRemove);
        assertTrue(checkIfStoreManager(userToRemove, store2Id));

        boolean removed = this.removeStoreManager(store2Id, user2LoggedInId, userToRemove);
        assertTrue(removed);

        boolean check = this.checkIfStoreManager(userToRemove, store2Id);
        assertFalse(check);
    }

    @Test
    public void removeStoreManager_NotByTheRightManager(){
        //User5 was positioned by User2 and he is removed by User6, error!
        boolean removed = this.removeStoreManager(store2Id, user6OwnerOfStore2, user5ManagerOfStore2ToBeRemoved);
        assertFalse(removed);
    }

    @Test
    public void removeStoreManager_NotByTheStoreManager(){
        boolean removed = this.removeStoreManager(store2Id, user1GuestId, user6OwnerOfStore2);
        assertFalse(removed);

    }


    /**
     * Remove Store Owner #34   NotForVersion1
     */
    @Test
    public void removeStoreOwner_Valid(){
        int userToRemove = setUser("UsertoRemoveeee", "UsertoRemoveeee!!", MEMBER, LOGGED);
        defineStoreOwner(store2Id, user2LoggedInId, userToRemove);
        assertTrue(checkIfStoreOwner(userToRemove, store2Id));

        boolean removed = this.removeStoreOwner(store2Id, user2LoggedInId, userToRemove);
        assertTrue(removed);

        boolean check = this.checkIfStoreOwner(user5ManagerOfStore2ToBeRemoved, store2Id);
        assertFalse(check);
    }

    @Test
    public void a13removeStoreOwner_NotByTheRightManager(){
        //User5 was positioned by User2 and he is removed by User6, error!
        boolean removed = this.removeStoreOwner(store2Id, user6OwnerOfStore2, user5ManagerOfStore2ToBeRemoved);
        assertFalse(removed);
    }

    @Test
    public void a12removeStoreOwner_NotByStoreOwner(){
        boolean removed = this.removeStoreOwner(store2Id, user1GuestId, user6OwnerOfStore2);
        assertFalse(removed);
    }

    /**
     * Close Store #35
     */
    @Test
    public void closeStore_Valid(){
        boolean closed = this.closeStore(user4LoggedInId, store4Id);
        assertTrue(closed);

        //user4 is still owner of this store!
        boolean stillOwner = this.checkIfStoreOwner(user4LoggedInId, store4Id);
        assertTrue(stillOwner);
    }

    @Test
    public void closeStore_OwnerNotFounder(){
        boolean closed = this.closeStore(user2LoggedInId, store4Id);
        assertFalse(closed);
    }

    @Test
    public void closeStore_StoreIsAlreadyClosed(){
        boolean closed = this.closeStore(user2LoggedInId, store2ClosedId);
        assertFalse(closed);
    }

    /**
     * Reopen Store #36 NotForVersion1
     */
    @Test
    public void reopenStore_Valid(){
        int storeId = openAndCloseStoreForUser2();

        boolean reopened = this.reopenStore(user2LoggedInId, storeId);
        assertTrue(reopened);

        //List<String> notifications = this.getNotifications(user2LoggedInId);
        //assertTrue(notifications.contains("Reopened Store!"));
    }

    private int openAndCloseStoreForUser2() {
        int storeId = this.createStore(user2LoggedInId, "SuperStore");
        this.closeStore(user2LoggedInId, storeId);
        return storeId;
    }

    @Test
    public void reopenStore_StoreOpened(){
        boolean reopened = this.reopenStore(user2LoggedInId, store2Id);
        assertFalse(reopened);
    }

    @Test
    public void reopenStore_NotStoreOwner(){
        int storeId = openAndCloseStoreForUser2();
        boolean reopened = this.reopenStore(user1GuestId, storeId);
        assertFalse(reopened);
    }

    /**
     * Show information of store staff  #37
     */
//    @Test
//    public void showStaffInfo_Valid(){
//        List<UserStaffInfoService> staff = this.showStaffInfo(store2Id, user2LoggedInId);
//        //
//        assertTrue(false);
//    }

    @Test
    public void showStaffInfo_UserNotManagerOrOwner(){
        List<UserStaffInfoService> staffInfo = this.showStaffInfo(store2Id, user1GuestId);
        assertNull(staffInfo);
    }

    /**
     * Get store info and answer request as Store manager #38  NotForVersion1
     */

    @Test
    public void getStoreInformation_Valid(){
        StoreService result = this.getStoreInformationAsStoreManager(store2Id, user2LoggedInId);
        assertEquals(result.getStoreId(), store2Id);
    }

    @Test
    public void getStoreInformation_StoreNotExist(){
        StoreService result = this.getStoreInformationAsStoreManager(-1, user2LoggedInId);
        assertNull(result);
    }
    /*
        @Test
    public void getStoreInformation_UserNotManagerOfThisStore(){
        StoreService result = this.getStoreInformationAsStoreManager(store2Id, user1GuestId);
        assertNull(result);
    }

    @Test
    public void getStoreInformation_getRequests_Valid(){
        List<String> requests = this.getRequestsOfStore_AsStoreOwnerManager(user2LoggedInId, store2Id);
        //get the requests from this object
        assertTrue(false);
    }
    */

    /**
     * Get Selling History #39
     */
    @Test
    public void getSellingHistory_Valid(){
        int userId = buyCartForTests();
        int newManagerId = setUser("Userrr", "AAAAAAA", MEMBER, LOGGED);
        boolean added = defineStoreManager(store2Id, user2LoggedInId, newManagerId);
        if(added){
            List<ReceiptService> receipts = this.getSellingHistoryOfStore(newManagerId, store2Id);
            assertNotNull(receipts);
            assertTrue(receipts.get(0).hasItem(userId, item11Added));
            assertTrue(receipts.get(0).hasItem(userId, item22Added));
        }
    }


    @Test
    public void getSellingHistory_UserNotManagerOrOwner(){
        List<ReceiptService> history = this.getSellingHistoryOfStore(store2Id, user1GuestId);
        assertNull(history);
    }

    @Test
    public void getSellingHistory_StoreNotExist(){
        List<ReceiptService> history = this.getSellingHistoryOfStore(-1, user2LoggedInId);
        assertNull(history);
    }



    /**
     * Approve Owner #53
     */
    @Test
    public void a11ApproveStoreOwner_NoOneConfirmed(){
        int ownerId = setUser("UserNameasdasd", "pass34asA", MEMBER, LOGGED);

        appointOwner(store2Id, user2LoggedInId, ownerId);
        boolean approved = checkIfStoreOwner(ownerId, store2Id);
        assertFalse(approved);

        approveOwner(store2Id, user6OwnerOfStore2, ownerId);

        approved = checkIfStoreOwner(ownerId, store2Id);
        assertTrue(approved);

    }

    @Test
    public void a12ApproveStoreOwner_UserNotOwner(){
        int ownerId = setUser("UserNameasdasdas", "pass34asA", MEMBER, LOGGED);

        boolean res = appointOwner(store2Id, -1, ownerId);

        assertFalse(res);

        boolean approved = checkIfStoreOwner(ownerId, store2Id);
        assertFalse(approved);
    }


    @Test
    public void a11rejectOwner_Valid(){
        int ownerId = setUser("UserNameasdassdda", "pass34asA", MEMBER, LOGGED);

        boolean res = defineStoreOwner(store2Id, user2LoggedInId, ownerId);
        assertTrue(res);

        rejectOwner(store2Id, ownerId);

        boolean approved = checkIfStoreOwner(ownerId, store2Id);
        assertTrue(approved);

    }




    private int buyCartForTests() {
        int id = setUser("User44StoreOwnerManagerTestsaaa", "User44", MEMBER, LOGGED);
        item11Added = addItemToStoreForTests(store2Id, "Itemanme", 10, "Kitchen", 10);
        item22Added = addItemToStoreForTests(store2Id, "Itemname2", 10, "Kitchen", 10);
        addItemsToUserForTests(id, store2Id, item11Added );
        addItemsToUserForTests(id, store2Id, item22Added);
        buyCart(id, "AA");
        return id;
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
    protected static int item11Added = -1;
    protected static int item22Added = -1;
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
        user2LoggedInId = setUser("User2StoreOwnerManagerTests","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5StoreOwnerManagerTests", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6StoreOwnerManagerTests", "User6!", MEMBER, LOGGED);
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
        user3NotLoggedInId = setUser("User3StoreOwnerManagerTests","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4StoreOwnerManagerTests","User4!", MEMBER, LOGGED);
        if(user2LoggedInId == -1)
            user2LoggedInId = setUser("User2StoreOwnerManagerTests","User2!", MEMBER, LOGGED);   //created for the ownership of the store
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
