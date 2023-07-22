package UnitTests.Receipts;

import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptHandler;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import BusinessLayer.Stores.CatalogItem;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReceiptHandlerTest {



    private static ReceiptHandler handler;
    private static int store1ID = 1;
    private static int store2ID = 2;
    private static int userId = 1;
    private static int receipt1Id = 1;
    private static Map<Integer, Map<CatalogItem, CartItemInfo>> items;
    private static Receipt receipt1;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        handler = new ReceiptHandler();

        items = new HashMap<>();
        HashMap<CatalogItem, CartItemInfo> itemsAndAmounts1 = new HashMap<>();
        CartItemInfo item1 = new CartItemInfo(11, 20, 10, "Books", "item11", 0.5);
        CartItemInfo item2 = new CartItemInfo(12, 20, 10, "Books", "item12", 0.5);
        CartItemInfo item3 = new CartItemInfo(21, 20, 10, "Books", "item21", 0.5);
        CartItemInfo item4 = new CartItemInfo(22, 20, 10, "Books", "item22", 0.5);
        item1.setPercent(0.2);
        item2.setPercent(0.2);
        item3.setPercent(0.2);
        item4.setPercent(0.2);

        HashMap<CatalogItem, CartItemInfo> itemsAndAmounts2 = new HashMap<>();
        items.put(store1ID, itemsAndAmounts1);
        items.put(store2ID, itemsAndAmounts2);

        handler = spy(ReceiptHandler.class);
        doNothing().when(handler).addItemsToReceipt(any(), any(), any());
        doNothing().when(handler).addReceiptToDAO(any());
    }

    @After
    public void tearDown() throws Exception {
    }



// public void addReceipt(int ownerId, HashMap<Integer,HashMap<CatalogItem, Integer>> storeOrUserIdToItems){
    @Test
    public void aAddReceipt() throws Exception {
        receipt1Id = handler.addReceipt(userId, items);
        receipt1 = handler.getReceipt(receipt1Id);

        assertEquals(receipt1.getId(), receipt1Id);
    }


    @Test
    public void bGetReceipt(){

        Receipt receipt = handler.getReceipt(receipt1Id);
        assertEquals(receipt.getId(), receipt1Id);
    }

    @Test
    public void cGetAllReceipts() throws Exception {
        receipt1Id = handler.addReceipt(userId, items);
        ArrayList<Receipt> receipts = handler.getAllReceipts();
        assertEquals(2, receipts.size());
    }

}