package IntegrationTests;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Mailbox;
import BusinessLayer.StorePermissions.StoreEmployees;
import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.User;
import BusinessLayer.Users.UserFacade;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class StoreEmployeesTest {
    static RegisteredUser user1;
    static RegisteredUser user2;
    static RegisteredUser user3;
    static RegisteredUser user4;
    static RegisteredUser user5;
    static RegisteredUser user6;
    static StoreFacade storeFacade;
    static UserFacade userFacade;
    static Market market;
    static Store store1;
    static Store store2;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        Market market = Market.getInstance();
        storeFacade = market.getStoreFacade();
        userFacade = market.getUserFacade();
        String addressOk="addressOk";
        LocalDate bDayOk=LocalDate.of(1999, 7, 11);

        int id1 = market.register("testUser1", "testPass",addressOk,bDayOk);
        market.login("testUser1", "testPass");
        user1 = userFacade.getRegisteredUser(id1);
        int id2 = market.register("testUser2", "testPass",addressOk,bDayOk);
        market.login("testUser2", "testPass");
        user2 = userFacade.getRegisteredUser(id2);
        int id3 = market.register("testUser3", "testPass",addressOk,bDayOk);
        market.login("testUser3", "testPass");
        user3 = userFacade.getRegisteredUser(id3);
        int id4 = market.register("testUser4", "testPass",addressOk,bDayOk);
        market.login("testUser4", "testPass");
        user4 = userFacade.getRegisteredUser(id4);
        int id5 = market.register("testUser5", "testPass",addressOk,bDayOk);
        market.login("testUser5", "testPass");
        user5 = userFacade.getRegisteredUser(id5);
        int id6 = market.register("testUser6", "testPass",addressOk,bDayOk);
        market.login("testUser6", "testPass");
        user6 = userFacade.getRegisteredUser(id6);

        int store1ID = market.addStore(user1.getId(), "store1");
        store1 = market.getStoreInfo(store1ID);
        StoreOwner storeOwner1 = new StoreOwner(user1.getId(), store1); //Why new and not addOwner method???
        user1.getStoresIOwn().add(storeOwner1);
        int store2ID = market.addStore(user4.getId(), "store2");
        store2 = market.getStoreInfo(store2ID);
        StoreOwner storeOwner2 = new StoreOwner(user4.getId(), store2); //Why new and not addOwner method???
        user4.getStoresIOwn().add(storeOwner2);
    }

    private void ensureOwnershipWithParent(RegisteredUser child, RegisteredUser parent, Store store) {
        Set<StoreOwner> ownerSet = child.getStoresIOwn();
        StoreOwner ownership1 = (StoreOwner)getOwnership(ownerSet, store);
        assertEquals(child.getUsername() + " should have " + store.getStoreName() + "store1 as store he owns",
                store, ownership1.getStore());
        int parentId = parent.getId();
        assertEquals(child.getUsername() + " should have " + parent.getUsername() + " as parent in ownership",
                parentId, ownership1.getParentID());
        ownerSet = parent.getStoresIOwn();
        StoreOwner ownership2 = (StoreOwner)getOwnership(ownerSet, store);

        assertTrue(parent.getUsername() + " should have " + child.getUsername() + " as ownerIDefined",
                ownership2.getOwnersIDefined().contains(child));
        assertTrue(store.getStoreName() + " should have " + child.getUsername() + "'s id as in owners",
                store.getStoreOwners().contains(child.getStoreIOwn(store.getStoreID())));
    }

    private StoreEmployees getOwnership(Set<? extends StoreEmployees> ownerSet, Store store) {
        for (StoreEmployees storeOwner : ownerSet) {
            if (storeOwner.getStoreID() == store.getStoreID())
                return storeOwner;
        }
        return null;
    }

    private void ensureManagerWithParent(RegisteredUser child, RegisteredUser parent, Store store) {
        Set<StoreManager> managers = child.getStoresIManage();
        StoreManager manager = (StoreManager)getOwnership(managers, store);
        assertEquals(child.getUsername() + " should have " + store.getStoreName() + " as store he manages",
                store, manager.getStore());
        int parentId = parent.getId();
        assertEquals(child.getUsername() + " should have " + parent.getUsername() + " as parent in managing"
                , manager.getParentID(), parentId);
        StoreOwner ownership = (StoreOwner) getOwnership(parent.getStoresIOwn(), store);
        assertTrue(parent.getUsername() + " should have " + child.getUsername() + " as managerIDefined",
                ownership.getManagersIDefined().contains(child));
    }

    private void ensureManagerRemovedWithOwner(RegisteredUser manager, RegisteredUser owner, Store store) {
        assertNull(manager.getUsername() + " shouldnt have the store in stores i manage",
                manager.getStoreIManage(store.getStoreID()));
        assertFalse(owner.getUsername() + " should no longer contain user1 in managers i defined",
                owner.getStoreIOwn(store.getStoreID()).getManagersIDefined().contains(manager));
        assertFalse(manager.getUsername() + "'s ID shouldnt be in store manager list",
                store.getStoreManagers().contains(manager.getId()));
    }

    private void ensureManagerRemovedAfterOwnerDeletion(RegisteredUser manager, RegisteredUser owner, Store store) {
        assertNull(manager.getUsername() + " shouldnt have the store in stores i manage",
                manager.getStoreIManage(store.getStoreID()));
        assertNull(owner.getUsername() + " shouldnt have the store in stores i own",
                owner.getStoreIOwn(store.getStoreID()));
        assertFalse(manager.getUsername() + "'s ID shouldnt be in store manager list",
                store.getStoreManagers().contains(manager.getId()));
    }

    private void removeOwnerSetup() throws Exception {
        if (user2.getStoreIOwn(store2.getStoreID())==null) {
            user4.addOwner(user2, store2.getStoreID());
        }
        if (user5.getStoreIOwn(store2.getStoreID())==null) {
            user4.addOwner(user5, store2.getStoreID());
        }
        if (user3.getStoreIOwn(store2.getStoreID())==null) {
            user5.addOwner(user3, store2.getStoreID());
        }
        //if these fail then the test will be obsolete
        ensureOwnershipWithParent(user5, user4, store2);
        ensureOwnershipWithParent(user2, user4, store2);
        ensureOwnershipWithParent(user3, user5, store2);
    }

    private void removeManagerSetup(RegisteredUser parent) {
        parent.addManager(user1, store2.getStoreID());
        parent.addManager(user6, store2.getStoreID());
        //if these fail then the test will be obsolete
        ensureManagerWithParent(user6, parent, store2);
    }

    @Test
    public void addOwner() throws Exception {
        try {
            user2.addOwner(user1, store1.getStoreID());
            fail("Should not allow to add owner if you aren't owner");
        } catch (RuntimeException e) {
        }
        user1.addOwner(user2, store1.getStoreID());
        ensureOwnershipWithParent(user2, user1, store1);
        try {
            user1.addOwner(user2, store1.getStoreID());
            fail("Should not allow to add owner if user is already owner");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void addManager() throws Exception{
        try {
            user3.addManager(user1, store1.getStoreID());
            fail("Should not allow to add manager if you aren't owner");
        } catch (RuntimeException e) {
        }
        user1.addManager(user3, store1.getStoreID());
        ensureManagerWithParent(user3, user1, store1);
        try {
            user1.addOwner(user3, store1.getStoreID());
            fail("Should not allow to add manager if user is already manager");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void removeOwner() throws Exception {
        try {
            user1.removeOwner(user4, store2.getStoreID());
            fail("Should not allow to remove owner if you aren't owner");
        } catch (RuntimeException e) {
        }
        try {
            user4.removeOwner(user1, store2.getStoreID());
            fail("Should not allow to remove user that isnt owner");
        } catch (RuntimeException e) {
        }

        removeOwnerSetup();

        try {
            user4.removeOwner(user4, store2.getStoreID());
            fail("Should not allow to remove yourself if you are founder");
        } catch (RuntimeException e) {
        }
        try {
            user4.removeOwner(user1, store2.getStoreID());
            fail("Should not be able to remove owner you did not define, even if 'grandchild'");
        } catch (RuntimeException e) {
        } catch (Exception e) {
            fail("Should not allow to remove manager if you aren't owner");
        }
        try {
            user5.removeOwner(user2, store2.getStoreID());
            fail("Should not be able to remove owner you did not define");
        } catch (RuntimeException e) {

        }
        user4.removeOwner(user5, store2.getStoreID());
        //ensure user5 and user1 are no longer owners
        assertNull("user5 should no longer be an owner in stores he owns", user5.getStoreIOwn(store2.getStoreID()));
        assertFalse("user5 should no longer be an owner in stores owner list", store2.getStoreOwners().contains(user5.getId()));
        assertFalse("user4 should no longer contain user5 in owners i defined", user4.getStoreIOwn(store2.getStoreID()).getOwnersIDefined().contains(user5));
        assertNull("user1 should no longer be an owner in stores he owns", user5.getStoreIOwn(store2.getStoreID()));
        assertFalse("user1 should no longer be an owner in stores owner list", store2.getStoreOwners().contains(user1.getId()));
        //ensure user2 is still owner
        ensureOwnershipWithParent(user2, user4, store2);
    }

    @Test
    public void removeManager() throws Exception {
        try {
            user1.removeManager(user4, store2.getStoreID());
            fail("Should not allow to remove manager if you aren't owner");
        } catch (Exception e) {
        }
        removeManagerSetup(user4);
        try {
            user4.removeManager(user1, store2.getStoreID());
            //should only affect user1
            ensureManagerWithParent(user6, user4, store2);
            ensureManagerRemovedWithOwner(user1, user4, store2);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void removeManagerFromCascade() throws Exception {
        //test that remove owner also removes relevant managers
        removeOwnerSetup();
        removeManagerSetup(user3);
        user4.removeOwner(user5, store2.getStoreID());
        ensureManagerRemovedAfterOwnerDeletion(user1, user3, store2);
        ensureManagerRemovedAfterOwnerDeletion(user6, user3, store2);
    }
}