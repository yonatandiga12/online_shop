package Acceptance;

import ServiceLayer.Objects.ReceiptService;
import ServiceLayer.Objects.UserInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SystemManagerTests extends ProjectTest{


    public static boolean doneSetUp = false;

    @Before
    public void setUp() {
        super.setUp();
        if(!doneSetUp) {
            setUpAllMarket();
            butCartUser2Store4();
            doneSetUp = true;
        }
    }


    private void butCartUser2Store4() {
        //user2 buy items from store4 so the selling history will be in store4
    }


    @After
    public void tearDown() {
        //delete stores and delete users from DB
    }

    /**
     * Close Store permanently #40
     */

    @Test
    public void closeStorePermanently_Valid(){
        boolean closed = this.closeStorePermanently(user7SystemManagerId, store2ClosedId);
        //check if user6ManagerOwnerOfStore2 is owner of store2
        assertTrue(closed);
        boolean stillOwner = this.checkIfStoreOwner(user2LoggedInId, store2ClosedId);
        assertFalse(stillOwner);
    }

    @Test
    public void closeStorePermanently_NonValidStore(){
        boolean closed = this.closeStorePermanently(user7SystemManagerId, -1);
        assertFalse(closed);
    }

    @Test
    public void closeStorePermanently_NotSystemManager(){
        boolean closed = this.closeStorePermanently(user1GuestId, store2ClosedId);
        assertFalse(closed);
    }

    /**
     * remove registeredUser #41
     */


    @Test
    public void removeRegisteredUser_Valid(){
        int id = setUser("Name11","passssssss", MEMBER, LOGGED);
        boolean removed = this.removeRegisterdUser(user7SystemManagerId, id);
        assertTrue(removed);
        boolean login = this.loginUser("User4", "User4!");
        assertFalse(login);
    }

    @Test
    public void removeRegisteredUser_UserIsStoreOwner(){
        boolean removed = this.removeRegisterdUser(user7SystemManagerId, user4LoggedInId);
        assertFalse(removed);
        boolean check = this.checkIfStoreOwner(user4LoggedInId, store4Id);
        assertTrue(check);
    }

    @Test
    public void removeRegisteredUser_NotValidUser(){
        boolean removed = this.removeRegisterdUser(user7SystemManagerId, -1);
        assertFalse(removed);
    }

    @Test
    public void removeRegisteredUser_NotValidManager(){
        boolean removed = this.removeRegisterdUser(user1GuestId, user4LoggedInId);
        assertFalse(removed);
    }

    /**
     * get information and answer request #42   NotForVersion1
     */
    /*@Test
    public void answerComplaints_Valid(){
        this.sendComplaint(user4LoggedInId, "Complaint! important Very");
        HashMap<Integer, String> complaint = new HashMap<>();
        complaint.put(user4LoggedInId, "Answer");
        boolean answered = this.answerComplaints(user7SystemManagerId, complaint);
        assertTrue(answered);

        HashMap<Integer, String> complaints = this.getComplaints(user7SystemManagerId);
        assertNull(complaints);
    }

    @Test
    public void answerComplaints_NotSystemManager(){
        HashMap<Integer, String> complaint = new HashMap<>();
        complaint.put(user4LoggedInId, "Answer");
        boolean answered = this.answerComplaints(user4LoggedInId, complaint);
        assertFalse(answered);
    }

    @Test
    public void sendMsg_Valid(){
        String msg = "Msg Important";
        boolean sent = this.sendMsg(user7SystemManagerId, user4LoggedInId, msg);
        assertTrue(sent);

        HashMap<Integer, List<String>> complaints = this.getMsgs(user4LoggedInId);
        assertEquals(complaints.get(user7SystemManagerId).get(0), msg);
    }

    @Test
    public void sendMsg_WrongUser(){
        String msg = "Msg Important";
        boolean sent = this.sendMsg(user7SystemManagerId, user1GuestId, msg);
        assertFalse(sent);

        HashMap<Integer, List<String>> complaints = this.getMsgs(user1GuestId);
        assertNull(complaints.get(user7SystemManagerId));
    }*/

    /**
     * Get Selling History #43
     */
    @Test
    public void getSellingHistoryOfStore_Valid(){
        int userId = buyCartForTests();
        List<ReceiptService> receipts = this.getSellingHistoryOfStore(user7SystemManagerId, store2Id);
        assertNotNull(receipts);
        assertTrue(receipts.get(0).hasItem(userId, item11Added));
        assertTrue(receipts.get(0).hasItem(userId, item22Added));

    }

    private int buyCartForTests() {
        int id = setUser("User44", "User44", MEMBER, LOGGED);
        item11Added = addItemToStoreForTests(store2Id, "Itemanme", 10, "Kitchen", 10);
        item22Added = addItemToStoreForTests(store2Id, "Itemname2", 10, "Kitchen", 10);
        addItemsToUserForTests(id, store2Id, item11Added );
        addItemsToUserForTests(id, store2Id, item22Added);
        buyCart(id, "AA");
        return id;
    }

    @Test
    public void getSellingHistoryOfStore_WrongStore(){
        List<ReceiptService> receipts = this.getSellingHistoryOfStore(user7SystemManagerId, -1);
        assertNull(receipts);
    }



    /**
     * get system activity #44   NotForVersion1
     */


    /**
     * get Users Inf0 #47
     */
    @Test
    public void getUsersLoggedOut_Valid(){
        int id = setUser("UserName123", "asdadasd", MEMBER, NOT_LOGGED);
        Map<Integer, UserInfoService> result = getBridge().getLoggedOutUsers();
        assertNotNull(result);
        boolean found = false;
        for(UserInfoService userInfoService: result.values()){
            if (userInfoService.getId() == id) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void getUsersLoggedIn_Valid(){
        int id = setUser("UserName1234", "asdadasd", MEMBER, LOGGED);
        Map<Integer, UserInfoService> result = getBridge().getLoggedInUsers();
        assertNotNull(result);
        boolean found = false;
        for(UserInfoService userInfoService: result.values()){
            if (userInfoService.getId() == id) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }



    /*@Test
    public void getUsersTraffic_Valid(){
        HashMap<Integer, String> result = this.getUsersTraffic(user7SystemManagerId);
        assertNotNull(result);
    }

    @Test
    public void getUsersTraffic_NotSystemManager(){
        HashMap<Integer, String> result = this.getUsersTraffic(user2LoggedInId);
        assertNull(result);
    }

    @Test
    public void getPurchaseTraffic_Valid(){
        HashMap<Integer, Integer> result = this.getPurchaseTraffic(user7SystemManagerId);
        assertNotNull(result);
    }

    @Test
    public void getNumberOfRegistrationForToady_Valid(){
        int result = this.getNumberOfRegistrationForToady(user7SystemManagerId);
        assertTrue(result > 0);
    }

    @Test
    public void getNumberOfRegistrationForToady_NotSystemManager(){
        int result = this.getNumberOfRegistrationForToady(user2LoggedInId);
        assertTrue(result < 0);
    }

//    @Test
//    public void getSellingHistoryOfUser_Valid(){
//
//        HashMap<Integer,List<ReceiptService>> receipts = this.getSellingHistoryOfUser(user7SystemManagerId, user2LoggedInId);
//        assertNotNull(receipts);
//        //assertEquals(receipts.get(0).getItemsAsMap().get(0).getName(), "Tomato");
//    }
    */





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
        user2LoggedInId = setUser("User2SystemManagerTests","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5SystemManagerTests", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6SystemManagerTests", "User6!", MEMBER, LOGGED);
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
        user3NotLoggedInId = setUser("User3SystemManagerTests","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4SystemManagerTests","User4!", MEMBER, LOGGED);
        if(user2LoggedInId == -1)
            user2LoggedInId = setUser("User2SystemManagerTests","User2!", MEMBER, LOGGED);   //created for the ownership of the store
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


    protected void setUpBuyUser4() {
        if(user2LoggedInId == -1)
            setUpUser2();
        if(user4LoggedInId == -1)
            setUpUser4();

        addItemsToUserForTests(user4LoggedInId, store2Id, item2Id);
        buyCart(user4LoggedInId, "paypal");
    }



}
