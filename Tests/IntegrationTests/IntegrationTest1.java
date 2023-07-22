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



// Focused on actions with store status
public class IntegrationTest1 {
    static StoreFacade storeFacade;
    static UserFacade userFacade;
    static RegisteredUser founder;
    static RegisteredUser storeOwner;
    static RegisteredUser storeManager;
    static RegisteredUser noRole;
    static CatalogItem item;
    static Store store;
    static Market market;
    static String userName1 = "userName1_IT1";
    static String userName2 = "userName2_IT1";
    static String userName3 = "userName3_IT1";
    static String userName4 = "userName4_IT1";

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        storeFacade = market.getStoreFacade();
        userFacade = market.getUserFacade();
        String address = "address";
        LocalDate bday = LocalDate.now();
        int id1 = market.register(userName1, "password1", address, bday);
        founder = userFacade.getRegisteredUser(id1);
        int id2 = market.register(userName2, "password2", address, bday);
        storeOwner = userFacade.getRegisteredUser(id2);
        int id3 = market.register(userName3, "password3", address, bday);
        storeManager = userFacade.getRegisteredUser(id3);
        int id4 = market.register(userName4, "password4", address, bday);
        noRole = userFacade.getRegisteredUser(id4);

        market.login(userName1, "password1");
        market.login(userName2, "password2");
        market.login(userName3, "password3");
        market.login(userName4, "password4");

        int storeID = market.addStore(founder.getId(), "storeName1");
        store = market.getStoreInfo(storeID);

        market.addOwner(founder.getId(), id2, storeID);
        market.addManager(founder.getId(), id3, storeID);
        item = market.addItemToStore(storeID, "item1", 49.90, "Clothing", 0.2);
        market.addItemAmount(storeID, item.getItemID(), 20);
    }


    @Test
    public void integrationTest1(){
        int storeID = store.getStoreID();
        int founderID = founder.getId();
        int noRoleID = noRole.getId();
        int itemID = item.getItemID();

        //close store - SUCCESS
        try {
            boolean closeResult = market.closeStore(founderID, storeID);
            assertTrue("returned false, because store was already closed for some reason", closeResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //add item of closed store to store - FAIL
        try {
            market.addItemAmount(storeID, itemID, 5);
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().equals("Can't add item amount to unopened store"));
        }
        //add item of closed store to cart - FAIL
        try {
            market.addItemToCart(noRoleID, storeID, itemID, 3);
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().equals("Error: Can't add item to cart from unopened store"));
        }
        //open store - SUCCESS
        try {
            boolean openResult = market.reopenStore(founderID, storeID);
            assertTrue("returned false, because store was already opened for some reason", openResult);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //add non-existing item to cart - FAIL
        try {
            market.addItemToCart(noRoleID, storeID, -1, 3);
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().equals("ERROR: Basket:: the item ID you entered does not exist in the given store"));
        }
        //add item to cart - SUCCESS
        try {
            Cart noRoleCart = noRole.getCart();
            assertTrue(noRoleCart.getItemsInBasket("storeName1").size() == 0);
            noRoleCart = market.addItemToCart(noRoleID, storeID, itemID, 3);
            assertTrue(noRoleCart.getItemsInBasket("storeName1").size() == 1);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
        //buy cart with basket of closed store - FAIL
        try {
            market.closeStore(founderID, storeID);
            market.buyCart(noRoleID, getPurchaseInfo(), getSupplyInfo());
            fail("ERROR: should have thrown an exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "You can't buy items from a closed store");
        }
        //buy cart - SUCCESS
        try {
            market.reopenStore(founderID, storeID);
            market.buyCart(noRoleID, getPurchaseInfo(), getSupplyInfo());
            assertTrue("Item amount hasn't decreased from 20 to 17 after buying 3", store.getItemAmount(itemID) == 17);
        } catch (Exception e) {
            fail("ERROR: " + e.getMessage());
        }
    }



    public PurchaseInfo getPurchaseInfo(){
        return new PurchaseInfo("123", 1, 2222, "asd", 1222, 1, LocalDate.of(2000, 1, 1));
    }

    public SupplyInfo getSupplyInfo(){
        return new SupplyInfo("Name", "address", "city", "counyrt", "asd");
    }


}