package UnitTests.StoreTests;

import BusinessLayer.Market;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ItemsManagementTests {
    static StoreFacade storeFacade;
    static Store store1;
    static CatalogItem item1;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        storeFacade = new StoreFacade();
        store1 = new Store(1785, 2, "Name");
        store1 = spy(Store.class);
        doNothing().when(store1).addItemToStoreDAO(any());
    }

    @Test
    public void aAddCatalogItemSuccessfully(){
        try {
            item1 = store1.addCatalogItem(1, "Harry Potter Book", 79.90, "Books", 0.8);
            assertNotNull("Item added to store",item1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bAddCatalogItemUnsuccessfully(){
        try {
            CatalogItem item = storeFacade.addCatalogItem(-1, "Blabla Book", 100, "Books", 0.3);
            fail("Should throw an error for store not exist");
        } catch (Exception e) {
            assertTrue("Store not exist", ("No store with ID: " + -1).equals(e.getMessage()));
        }
    }

    @Test
    public void cRemoveCatalogItemSuccessfully(){
        try {
            assertNotNull("Item should be found here", store1.getItem(item1.getItemID()));
            CatalogItem item = store1.removeItemFromStore(item1.getItemID());
            assertNull("Item shouldn't be found here", store1.getItem(item1.getItemID()));
            assertNotNull("Should return non null object", item);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void dRemoveCatalogItemUnsuccessfully(){
        try {
            assertNull("Item shouldn't be found here", store1.getItem(item1.getItemID()));
            CatalogItem item = store1.removeItemFromStore(item1.getItemID());
            assertNull("Returned item should be null" ,item);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
