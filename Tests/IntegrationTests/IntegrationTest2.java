package IntegrationTests;

import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Market;
import BusinessLayer.MarketMock;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.UserFacade;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;



// Focused on actions with login/logout
public class IntegrationTest2 {
    static StoreFacade storeFacade;
    static UserFacade userFacade;
    static RegisteredUser founder;
    static RegisteredUser storeOwner;
    static RegisteredUser storeManager;
    static RegisteredUser noRole;
    static CatalogItem item;
    static Store store;
    static Market market;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        storeFacade = market.getStoreFacade();
        userFacade = market.getUserFacade();
        String address = "address";
        LocalDate bday = LocalDate.now();
        int id1 = market.register("userName1", "password1", address, bday);
        founder = userFacade.getRegisteredUser(id1);
        int id2 = market.register("userName2", "password2", address, bday);
        storeOwner = userFacade.getRegisteredUser(id2);
        int id3 = market.register("userName3", "password3", address, bday);
        storeManager = userFacade.getRegisteredUser(id3);
        int id4 = market.register("userName4", "password4", address, bday);
        noRole = userFacade.getRegisteredUser(id4);

        market.login("userName1", "password1");
        market.login("userName2", "password2");
        market.login("userName3", "password3");
        market.login("userName4", "password4");

        int storeID = market.addStore(founder.getId(), "storeName1");
        store = market.getStoreInfo(storeID);

        market.addOwner(founder.getId(), id2, storeID);
        market.addManager(founder.getId(), id3, storeID);
        item = market.addItemToStore(storeID, "item1", 49.90, "Clothing", 0.2);
        market.addItemAmount(storeID, item.getItemID(), 20);

    }


    @Test
    public void integrationTest2(){
        int storeID = store.getStoreID();
        int founderID = founder.getId();
        int ownerID = storeOwner.getId();
        int managerID = storeManager.getId();
        int noRoleID = noRole.getId();
        int itemID = item.getItemID();

        //close store - SUCCESS
        try {
            boolean closeResult = market.closeStore(founderID, storeID);
            assertTrue("returned false, because store was already closed for some reason", closeResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //logout all - SUCCESS
        try {
            boolean logoutResult = market.logout(founderID);
            assertTrue("returned false, because logout failed", logoutResult);
            logoutResult = market.logout(ownerID);
            assertTrue("returned false, because logout failed", logoutResult);
            logoutResult = market.logout(managerID);
            assertTrue("returned false, because logout failed", logoutResult);
            //logoutResult = market.logout(noRoleID);
            //assertTrue("returned false, because logout failed", logoutResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //open store while logout - FAIL
        try {
            int id = market.login("userName1", "password1");
            assertTrue("returned false, because login failed", id == founderID);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //open store - SUCCESS
        try {
            boolean openResult = market.reopenStore(founderID, storeID);
            assertTrue("returned false, because store was already opened for some reason", openResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //logout founder - SUCCESS
        try {
            boolean founderLogoutResult = market.logout(founderID);
            assertTrue("returned false, because logout failed", founderLogoutResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //close store while logout - FAIL
//        try {
//            market.closeStore(founderID, storeID);
//            fail("ERROR: should have thrown an exception");
//        } catch (Exception e) {
//            assertTrue(e.getMessage(), e.getMessage().equals("User is logout and can't close store"));
//        }

        //store manager try to add new store while logout - FAIL
//        try {
//            int store2ID = market.addStore(founderID, "store2");
//            fail("ERROR: should have thrown an exception");
//        } catch (Exception e) {
//            assertTrue(e.getMessage(), e.getMessage().equals("User is logout and can't add store"));
//        }

        //add item to cart while logout - FAIL
        try {
            market.addItemToCart(founderID, storeID, itemID, 3);
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().equals("User " + founderID + " is not logged in"));
        }
        //buy cart while logout - FAIL
        try {
            Cart noRoleCart = noRole.getCart();
            noRoleCart = market.addItemToCart(noRoleID, storeID, itemID, 3);
            assertTrue(noRoleCart.getItemsInBasket("storeName1").size() == 1);
            boolean logoutResult1 = market.logout(noRoleID);
            assertTrue("returned false, because logout failed", logoutResult1);
            market.buyCart(noRoleID, getPurchaseInfo(), getSupplyInfo());
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "User " + noRoleID + " is not logged in");
        }
        //add new item to store while logout - FAIL
//        try {
//            market.addItemToStore(storeID,"book", 100,"Books", 10);
//            fail("ERROR: should have thrown an exception");
//        } catch (Exception e) {
//            assertTrue(e.getMessage(), e.getMessage().equals("Can't add catalog item when store unopened"));
//        }
    }


    public PurchaseInfo getPurchaseInfo(){
        return new PurchaseInfo("123", 1, 2222, "asd", 1222, 1, LocalDate.of(2000, 1, 1));
    }

    public SupplyInfo getSupplyInfo(){
        return new SupplyInfo("Name", "address", "city", "counyrt", "asd");
    }
}