package UnitTests;

import BusinessLayer.Market;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.SystemManager;
import BusinessLayer.Users.UserFacade;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;

import static BusinessLayer.Stores.StoreStatus.OPEN;
import static BusinessLayer.Stores.StoreStatus.PERMANENTLY_CLOSE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SystemManagerTest {
    static Market market;
    static String adminName = "admin";
    static RegisteredUser user = spy(RegisteredUser.class);
    static RegisteredUser user2 = spy(RegisteredUser.class);
    static StoreFacade sf;
    static UserFacade uf;
    static int adminID;
    static SystemManager manager;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        sf = market.getStoreFacade();
        uf = market.getUserFacade();
        adminID = uf.getUserByName(adminName).getId();
        manager = new SystemManager();

        HashSet<StoreOwner> stores = new HashSet<>();
        stores.add(null);
        when(user.getStoresIOwn()).thenReturn(stores);

        HashSet<StoreManager> storesIManage = new HashSet<>();
        storesIManage.add(null);
        when(user.getStoresIManage()).thenReturn(storesIManage);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void startWithAnAdmin() {
        assertTrue("System should have created an admin", market.getSystemManagerMap().size()>0);
    }


    @Test
    public void removeUserShouldFailOwnsStore() {

        try {
            manager.removeUser(user);
            fail("Cannot remove User who owns or manages a store");
        }
        catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void removeUserShouldFailManagesStore() {

        try {
            manager.removeUser(user2);
            fail("Cannot remove User who owns or manages a store");
        }
        catch (Exception e) {
            assertTrue(true);
        }
    }



    @Test
    public void removeUserShouldUserNotExists() {

        try {
            manager.removeUser(null);
            fail("Cannot remove User who owns or manages a store");
        }
        catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void closeStorePermanently_notAdmin() {
        try {
            market.closeStorePermanently(-1, 1);
            fail("Only admin should be able to close store");
        }
        catch (Exception e) {
            assertTrue(true);
        }
    }



    @Test
    public void removeUserShouldFail() {
        try {
            market.removeUser(-1 , 10);
            fail("Only admin should be able to remove user");
        }
        catch (Exception e) {
            assertTrue(e.getMessage() + " caused us to fail to remove user",true);
        }

    }
}