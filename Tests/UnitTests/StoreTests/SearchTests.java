package UnitTests.StoreTests;

import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import Globals.SearchBy;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SearchTests {
    static Store store1;
    static Store store2;
    static Store store3;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        store1 = spy(Store.class);
        store2 = spy(Store.class);
        store3 = spy(Store.class);

        doNothingToStore(store1);
        doNothingToStore(store2);
        doNothingToStore(store3);

        store1.addCatalogItem(1001, "item1K", 10, "category1k", 1);
        store1.addCatalogItem(1002, "item2K", 10, "category2k", 1);
        store1.addCatalogItem(1003, "item5K", 10, "category5k", 1);
        store2.addCatalogItem(1001, "item1K", 10, "category1k", 1);
        store2.addCatalogItem(1002, "item2K", 10, "category2k", 1);
        store2.addCatalogItem(1003, "item3K", 10, "category3k", 1);
        store2.addCatalogItem(1004, "item4K", 10, "category4k", 1);


    }

    private static void doNothingToStore(Store store) {
        doNothing().when(store).updateItemDiscounts(anyInt());
        doNothing().when(store).updateItemDiscountPolicies(anyInt());
        doNothing().when(store).updateItemPurchasePolicies(anyInt());
        doNothing().when(store).addItemToStoreDAO(any());
    }

    @Test
    public void getAllItems(){
        try {
            Map<CatalogItem, Boolean> items1 = store1.getCatalog();
            Map<CatalogItem, Boolean> items2 = store2.getCatalog();
            Map<CatalogItem, Boolean> items3 = store3.getCatalog();
            assertEquals("Store 1 should return 3 items", 3, items1.keySet().size());
            assertEquals("Store 2 should return 4 items", 4, items2.keySet().size());
            assertEquals("Store 3 should return 0 items", 0, items3.keySet().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getSearchedItemsByItemName(){
        try {
            Map<CatalogItem, Boolean> items1 = store1.getCatalog("item2K", SearchBy.ITEM_NAME, new HashMap<>());
            Map<CatalogItem, Boolean> items2 = store2.getCatalog("category2K", SearchBy.ITEM_NAME, new HashMap<>());
            Map<CatalogItem, Boolean> items3 = store3.getCatalog("item2K", SearchBy.ITEM_NAME, new HashMap<>());
            assertEquals("Store 1 should return 1 items", 1, items1.keySet().size());
            assertEquals("Store 2 should return 0 items", 0, items2.keySet().size());
            assertEquals("Store 3 should return 0 items", 0, items3.keySet().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void getSearchedItemsByItemCategory(){
        try {
            Map<CatalogItem, Boolean> items1 = store1.getCatalog("category2K", SearchBy.CATEGORY, new HashMap<>());
            Map<CatalogItem, Boolean> items2 = store2.getCatalog("item2K", SearchBy.CATEGORY, new HashMap<>());
            Map<CatalogItem, Boolean> items3 = store3.getCatalog("category2K", SearchBy.CATEGORY, new HashMap<>());
            assertEquals("Store 1 should return 1 items", 1, items1.keySet().size());
            assertEquals("Store 2 should return 0 items", 0, items2.keySet().size());
            assertEquals("Store 3 should return 0 items", 0, items3.keySet().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void getSearchedItemsByKeywords(){
        try {
            Map<CatalogItem, Boolean> items1 = store1.getCatalog("item2k", SearchBy.KEY_WORD, new HashMap<>());
            Map<CatalogItem, Boolean> items2 = store2.getCatalog("cateGORY2k, category3K", SearchBy.KEY_WORD, new HashMap<>());
            Map<CatalogItem, Boolean> items3 = store3.getCatalog("item1K, category1K", SearchBy.KEY_WORD, new HashMap<>());
            assertEquals("Store 1 should return 1 items", 1, items1.keySet().size());
            assertEquals("Store 2 should return 1 items", 2, items2.keySet().size());
            assertEquals("Store 3 should return 0 items", 0, items3.keySet().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
