package UnitTests.StoreTests;

import BusinessLayer.Market;
import BusinessLayer.StorePermissions.StoreEmployees;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreStatus;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreStatusTests {
    static int noRoleId = 4;
    static Store store1;
    static int storeOwnerId = 1;
    static int storeManagerId = 2;
    static int storeFounderId = 3;


    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        store1 = spy(Store.class);

        when(store1.isFounder(storeOwnerId)).thenReturn(false);
        when(store1.isFounder(storeManagerId)).thenReturn(false);
        when(store1.isFounder(noRoleId)).thenReturn(false);
        when(store1.isFounder(storeFounderId)).thenReturn(true);

        doNothing().when(store1).sendMessage(isA(Integer.class), isA(String.class));
        doNothing().when(store1).sendMsgToListAndUnavailable(isA(List.class), isA(String.class));
        doNothing().when(store1).sendMsgToList(isA(List.class), isA(String.class));
        doNothing().when(store1).sendMsgToListAndAvailable(isA(List.class), isA(String.class));

    }

    @Test
    public void aCloseStoreWithStoreOwner(){
        try {
            store1.closeStore(storeOwnerId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Only the founder of the store can close it", "Only the founder of the store can close it".equals(e.getMessage()));
        }
    }

    @Test
    public void bCloseStoreWithStoreManager(){
        try {
            store1.closeStore(storeManagerId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Store should remain OPEN", store1.getStoreStatus()==StoreStatus.OPEN);
            assertTrue("Only the founder of the store can close it", "Only the founder of the store can close it".equals(e.getMessage()));
        }
    }

    @Test
    public void cCloseStoreWithNoRoleUser(){
        try {
            store1.closeStore(noRoleId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Store should remain OPEN", store1.getStoreStatus()==StoreStatus.OPEN);
            assertTrue("Only the founder of the store can close it", "Only the founder of the store can close it".equals(e.getMessage()));
        }
    }

    @Test
    public void dCloseStoreSuccessfully(){
        try {
            if (store1.getStoreStatus() != StoreStatus.OPEN)
                throw new Exception("store should start as OPEN");
            Set<Integer> ownersAndManagersBefore = new HashSet<>();
            store1.addOwner(new StoreOwner(5, store1));
            ownersAndManagersBefore.addAll(store1.getStoreOwners().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            ownersAndManagersBefore.addAll(store1.getStoreManagers().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            boolean success = store1.closeStore(storeFounderId);
            Set<Integer> ownersAndManagersAfter = new HashSet<>();
            ownersAndManagersAfter.addAll(store1.getStoreOwners().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            ownersAndManagersAfter.addAll(store1.getStoreManagers().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            if (!(ownersAndManagersBefore.equals(ownersAndManagersAfter)))
                success = false;
            assertTrue("Store should be CLOSE", store1.getStoreStatus()==StoreStatus.CLOSE);
            assertTrue("Store closed successfully",success);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void eCloseStoreWhileAlreadyClosed(){
        try {
            boolean success = store1.closeStore(storeFounderId);
            assertFalse("Close store while already closed",success);
            assertTrue("Store should remain CLOSE",store1.getStoreStatus()==StoreStatus.CLOSE);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void fOpenStoreWithStoreOwner(){
        try {
            store1.reopenStore(storeOwnerId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Store should remain CLOSE", store1.getStoreStatus()==StoreStatus.CLOSE);
            assertTrue("Only the founder of the store can open it", "Only the founder of the store can open it".equals(e.getMessage()));
        }
    }

    @Test
    public void gOpenStoreWithStoreManager(){
        try {
            store1.reopenStore(storeManagerId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Store should remain CLOSE", store1.getStoreStatus()==StoreStatus.CLOSE);
            assertTrue("Only the founder of the store can open it", "Only the founder of the store can open it".equals(e.getMessage()));
        }
    }

    @Test
    public void hOpenStoreWithNoRoleUser(){
        try {
            store1.reopenStore(noRoleId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Store should remain CLOSE", store1.getStoreStatus()==StoreStatus.CLOSE);
            assertTrue("Only the founder of the store can open it", "Only the founder of the store can open it".equals(e.getMessage()));
        }
    }

    @Test
    public void iOpenStoreSuccessfully(){
        try {
            if (store1.getStoreStatus() != StoreStatus.CLOSE)
                throw new Exception("store should closed by now");
            Set<Integer> ownersAndManagersBefore = new HashSet<>();
            ownersAndManagersBefore.addAll(store1.getStoreOwners().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            ownersAndManagersBefore.addAll(store1.getStoreManagers().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            boolean success = store1.reopenStore(storeFounderId);
            Set<Integer> ownersAndManagersAfter = new HashSet<>();
            ownersAndManagersAfter.addAll(store1.getStoreOwners().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            ownersAndManagersAfter.addAll(store1.getStoreManagers().stream().map(StoreEmployees::getUserID).collect(Collectors.toList()));
            if (!(ownersAndManagersBefore.equals(ownersAndManagersAfter)))
                success = false;
            assertTrue("Store should be OPEN", store1.getStoreStatus()==StoreStatus.OPEN);
            assertTrue("Store opened successfully", success);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void jOpenStoreWhileAlreadyOpened(){
        try {
            boolean success = store1.reopenStore(storeFounderId);
            assertTrue("Store should remain OPEN", store1.getStoreStatus()==StoreStatus.OPEN);
            assertTrue("Open store while already opened", !success);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void kCloseStoreWhilePermanentlyClosed(){
        try {
            store1.closeStorePermanently();
            store1.closeStore(storeFounderId);
            fail("Should have throw an error");
        } catch (Exception e) {
            assertTrue("Can't close store while permanently closed", "Store is permanently close and cannot change its status to close".equals(e.getMessage()));
        }
    }

    @Test
    public void lOpenStoreWhilePermanentlyClosed(){
        try {
            store1.reopenStore(storeFounderId);
            fail("Should have throw an error");
        } catch (Exception e) {
            assertTrue("Can't open store while permanently closed", "Store is permanently close and cannot change its status to open".equals(e.getMessage()));
        }
    }


    @Test
    public void mCloseStorePermanentlyWhileAlreadyClosedPermanently(){
        try {
            boolean success = store1.closeStorePermanently();
            assertFalse("Can't close store permanently while already permanently closed", success);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
