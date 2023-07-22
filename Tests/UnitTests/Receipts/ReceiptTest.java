package UnitTests.Receipts;

import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

public class ReceiptTest {


    Receipt receipt;
    int receiptId;
    ReceiptItem item1FromStore1;
    ReceiptItem item2FromStore1;
    ReceiptItem item2FromStore2;
    int store1Id = 1;
    int store2Id = 2;
    @Before
    public void setUp() throws Exception {
        System.setProperty("env", "test");
        receipt = new Receipt(1000, Calendar.getInstance());
        receiptId = receipt.getId();
        item1FromStore1 = new ReceiptItem(1121, "item1", 20, 100, 15);
        item2FromStore1 = new ReceiptItem(2121, "item12", 20, 100, 15);
        item2FromStore2 = new ReceiptItem(2122, "item2", 20, 1000, 900);

        ArrayList<ReceiptItem> items = new ArrayList<>();
        items.add(item2FromStore1);

        receipt = spy(Receipt.class);

        doNothing().when(receipt).addItemPairTODAO(any());
        doNothing().when(receipt).addItemToPAirTODAO(any(), any());

        receipt.addItems(store1Id, items);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testAddItems() throws Exception {
        ArrayList<ReceiptItem> items = new ArrayList<>();
        items.add(item1FromStore1);
        ArrayList<ReceiptItem> items2 = new ArrayList<>();
        items2.add(item2FromStore2);
        receipt.addItems(store1Id, items);
        receipt.addItems(store2Id, items2);

        assertTrue(receipt.itemExists(store1Id, 1121));
        assertTrue(receipt.itemExists(store2Id, 2122));
        assertFalse(receipt.itemExists(store2Id, 1121));

    }

    @Test
    public void testDeleteItem() throws Exception {

        int removedId = item2FromStore1.getId();
        receipt.deleteItem(store1Id, removedId);

        assertFalse(receipt.itemExists(store1Id, removedId));
    }


    @Test
    public void testGetItem() {
        int removedId = item2FromStore1.getId();
        ReceiptItem item = receipt.getItem(store1Id, removedId);
        assertEquals(item.getName(), "item12");
    }
}