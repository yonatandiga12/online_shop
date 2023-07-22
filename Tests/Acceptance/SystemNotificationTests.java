package Acceptance;

import ServiceLayer.Objects.MessageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import static org.junit.Assert.*;

public class SystemNotificationTests extends ProjectTest{


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
    }


    /**
     * Live Notification #5
     */
    @Test
    public void getLiveNotification() throws Exception
    {
        closeStore(user2LoggedInId, store2Id);

        List<MessageService> notifications = getChats(user2LoggedInId).get(store2Id).getMessages();
        List<MessageService> notifications2 = getChats(user6OwnerOfStore2).get(store2Id).getMessages();

        boolean foundClosed1 = false;
        boolean foundClosed2 = false;
        for(MessageService msg: notifications) {
            if (msg.getContent().equals("Store Store2 has closed")){
                foundClosed1 = true;
                break;
            }
        }
        for(MessageService msg: notifications2) {
            if (msg.getContent().equals("Store Store2 has closed")){
                foundClosed2 = true;
                break;
            }
        }
        assertTrue(foundClosed1);
        assertTrue(foundClosed2);


    }

    /**
     * delayed Notifications #6
     */
    @Test
    public void delayedNotifications_Valid(){
        logOut(user4LoggedInId);

        this.closeStore(user4LoggedInId, store4Id);
        loginUser("User4", "User4!");

        List<MessageService> notifications = null;
        try {
            notifications = getChats(user4LoggedInId).get(store4Id).getMessages();
            boolean foundClosed = false;
            for(MessageService msg: notifications) {
                if (msg.getContent().equals("Store Store4 has closed")){
                    foundClosed = true;
                    break;
                }
            }
            assertTrue(foundClosed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


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
        user2LoggedInId = setUser("User2NotificationTests","User2!", MEMBER, LOGGED);
        user5ManagerOfStore2ToBeRemoved = setUser("User5NotificationTests", "User5!", MEMBER, NOT_LOGGED);
        user6OwnerOfStore2 = setUser("User6NotificationTests", "User6!", MEMBER, LOGGED);
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
        user3NotLoggedInId = setUser("User3NotificationTests","User3!", MEMBER, NOT_LOGGED);
    }

    /**
     * User4: Member, logged in, Store Owner and founder of store4
     */
    protected void setUpUser4(){
        user4LoggedInId = setUser("User4NotificationTests","User4!", MEMBER, LOGGED);
        if(user2LoggedInId == -1)
            user2LoggedInId = setUser("User2NotificationTests","User2!", MEMBER, LOGGED);   //created for the ownership of the store
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
