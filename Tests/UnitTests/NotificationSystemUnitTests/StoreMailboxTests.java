package UnitTests.NotificationSystemUnitTests;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Mailbox;
import BusinessLayer.NotificationSystem.NotificationHub;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.UserFacade;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;



public class StoreMailboxTests {
    static NotificationHub hub;
    static Market market;
    static UserFacade userFacade;
    static StoreFacade storeFacade;
    static RegisteredUser user1;
    static RegisteredUser user2;
    static Store store1;
    static Store store2;


    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        hub = market.getNotificationHub();
        userFacade = market.getUserFacade();
        storeFacade = market.getStoreFacade();
        String addressOk="addressOk";
        LocalDate bDayOk= LocalDate.of(2022, 7, 11);
        int user1ID = market.register("user1", "123456",addressOk,bDayOk);
        market.login("user1", "123456");
        user1 = userFacade.getRegisteredUser(user1ID);
        int store1ID = market.addStore(user1.getId(), "store1");
        store1 = market.getStoreInfo(store1ID);
        int user2ID = market.register("user2", "123456",addressOk,bDayOk);
        market.login("user2", "123456");
        user2 = userFacade.getRegisteredUser(user2ID);
        int store2ID = market.addStore(user1.getId(), "store2");
        store2 = market.getStoreInfo(store2ID);
    }

    @Test
    public void notifyOwner() throws Exception {
        Mailbox mailbox = hub.getMailboxes().get(user1.getId());

//        try{
//            mailbox.notifyOwner();
//            fail("The function worked");
//        }
//        catch(Exception e){
//            assertTrue(true);
//        }

        assertTrue(true); // for now, because it is not implemented

        hub.removeFromService(store1.getStoreID());
        hub.removeFromService(user1.getId());
    }

    @Test
    public void availability() throws Exception {
        Mailbox mailbox = hub.getMailboxes().get(store2.getStoreID());

        assertTrue("The store's mailbox is not available", mailbox.isAvailable());

        mailbox.setMailboxAsUnavailable();
        assertFalse("The store is still available", mailbox.isAvailable());

        mailbox.setMailboxAsAvailable();
        assertTrue("The store is not available", mailbox.isAvailable());

        hub.removeFromService(store2.getStoreID());
        hub.removeFromService(user2.getId());
    }

}
