package UnitTests.NotificationSystemUnitTests;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.*;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.UserFacade;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;


public class NotificationHubTests {
    static NotificationHub hub;
    static Market market;
    static UserFacade userFacade;
    static StoreFacade storeFacade;
    static RegisteredUser user1;
    static RegisteredUser user2;
    static RegisteredUser user3;
    static RegisteredUser user4;
    static RegisteredUser user5;
    static Store store1;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        hub = market.getNotificationHub();
        userFacade = market.getUserFacade();
        storeFacade = market.getStoreFacade();
        String addressOk="addressOk";
        LocalDate bDayOk=LocalDate.of(2022, 7, 11);

        int user1ID = market.register("user1C", "123456",addressOk,bDayOk);
        market.login("user1C", "123456");
        user1 = userFacade.getRegisteredUser(user1ID);

        int user2ID = market.register("user2C", "123456",addressOk,bDayOk);
        market.login("user2C", "123456");
        user2 = userFacade.getRegisteredUser(user2ID);

        int user3ID = market.register("user3C", "123456",addressOk,bDayOk);
        market.login("user3C", "123456");
        user3 = userFacade.getRegisteredUser(user3ID);

        int user4ID = market.register("user4C", "123456",addressOk,bDayOk);
        market.login("user4C", "123456");
        user4 = userFacade.getRegisteredUser(user4ID);

        int user5ID = market.register("user5C", "123456",addressOk,bDayOk);
        market.login("user5C", "123456");
        user5 = userFacade.getRegisteredUser(user5ID);

        int store1ID = market.addStore(user1.getId(), "store1C");
        store1 = market.getStoreInfo(store1ID);
    }

    @Test
    public void registerUserToService() {
        try{
            assertNotNull(user2.getMailbox());
            hub.removeFromService(user2.getId());
            assertFalse("The user is still registered", hub.isRegistered(user2.getId()));
        }
        catch(Exception e){
            fail(e.getMessage());
        }



    }

    @Test
    public void registerStoreToService(){
        try{
            StoreMailbox mailbox = store1.getMailBox();

            assertNotNull(mailbox);

            hub.removeFromService(store1.getStoreID());
            hub.removeFromService(user1.getId());

            assertFalse("The store is still registered", hub.isRegistered(store1.getStoreID()));

        }
        catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    public void passMessage() throws Exception {
        Mailbox mailbox1 = user3.getMailbox();
        Mailbox mailbox2 = user4.getMailbox();
        Mailbox mailbox3 = user5.getMailbox();

        Message message1 = new Message(user3.getId(), user4.getId(), "message1");

        mailbox1.sendMessage(message1.getReceiverID(), message1.getContent());

        // Good case
        assertTrue("The message was not sent properly! mailbox1 does not contain: " + message1,
                mailbox1.getChatsAsMap().get(user4.getId()).contains(message1));
        assertTrue("The message was not sent properly!", mailbox2.getChatsAsMap().get(user3.getId()).contains(message1));
        // Bad case
        //assertFalse("The message was sent to different mailbox!", mailbox3.watchNotReadMessages().contains(message1));

        hub.removeFromService(user3.getId());
        hub.removeFromService(user4.getId());
        hub.removeFromService(user5.getId());
    }


}
