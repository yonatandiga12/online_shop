package ServiceLayer.Objects;

import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;

import java.util.*;

public class ReceiptService {

    private int id;
    private Date date;
    private int ownerId;
    //private Receipt receipt;

    //if the receipt is for store, the integers will be usersId's and the items they bought
    private HashMap<Integer, ArrayList<ReceiptItemService>> items;


    public ReceiptService(Receipt receipt) {
        this.id = receipt.getId();
        this.date = receipt.getDate();
        this.ownerId = receipt.getOwnerId();
        items = new HashMap<>();

        loadReceiptFromBusiness(receipt);
    }

    private void loadReceiptFromBusiness(Receipt receipt) {
        Map<Integer, ArrayList<ReceiptItem>> receiptItems = receipt.getAllItems();
        for(Map.Entry<Integer, ArrayList<ReceiptItem>> curr: receiptItems.entrySet()){
            ArrayList<ReceiptItemService> currItems = new ArrayList<>();
            for(ReceiptItem item : curr.getValue()){
                currItems.add(new ReceiptItemService(item));
            }
            items.put(curr.getKey(), currItems);
        }
    }

    public boolean hasItem(int userOrStoreId, int itemId){
        for( ReceiptItemService receiptItem : items.get(userOrStoreId)){
            if(receiptItem.getId() == itemId){
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public HashMap<Integer, ArrayList<ReceiptItemService>> getItems() {
        return items;
    }


    public List<ReceiptItemService> getItemsInList() {
        List<ReceiptItemService> result = new ArrayList<>();
        for(Map.Entry<Integer, ArrayList<ReceiptItemService>> entry : items.entrySet()){
            for(ReceiptItemService itemService : entry.getValue()){
                itemService.setOwnerId(entry.getKey());
                result.add(itemService);
            }
        }
        return result;
    }
}
