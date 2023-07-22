package BusinessLayer.Receipts.ReceiptItem;

import BusinessLayer.CollectionI;
import BusinessLayer.ItemCollectionI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiptItemCollection implements ItemCollectionI<ReceiptItem> {

    //each receipt composed of Id of user/store to the items bought.
    //for example: For user receipt the key is all the storeIds he bought from and the value are the items
    private HashMap<Integer,ArrayList<ReceiptItem>> items;

    public ReceiptItemCollection(){
        items = new HashMap<>();
    }

    @Override
    public ReceiptItem get(int userOrStoreId, int id) {
        for( ReceiptItem receiptItem : items.get(userOrStoreId)){
            if(receiptItem.getId() == id){
                return receiptItem;
            }
        }
        return null;
    }

    @Override
    public void add(int userOrStoreId, ReceiptItem item) {
        items.putIfAbsent(userOrStoreId, new ArrayList<>());
        items.get(userOrStoreId).add(item);
    }

    @Override
    public boolean delete(int userOrStoreId, int receiptItemId) {
        for( ReceiptItem receiptItem : items.get(userOrStoreId)){
            if(receiptItem.getId() == receiptItemId){
                items.get(userOrStoreId).remove(receiptItem);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(int userOrStoreId, int itemId, ReceiptItem item) {
        for( ReceiptItem receiptItem : items.get(userOrStoreId)){
            if(receiptItem.getId() == itemId){
                items.get(userOrStoreId).remove(receiptItem);
                items.get(userOrStoreId).add(item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exists(int userOrStoreId, int itemId) {
        for( ReceiptItem receiptItem : items.get(userOrStoreId)){
            if(receiptItem.getId() == itemId){
                return true;
            }
        }
        return false;
    }

    public boolean hasKeyId(int ownerId) {
        return items.containsKey(ownerId);
    }

    public Map<Integer, ArrayList<ReceiptItem>> getAllItems() {
        return items;
    }
}
