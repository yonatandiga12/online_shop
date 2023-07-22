package BusinessLayer.Receipts.Receipt;

import BusinessLayer.Pair;
import BusinessLayer.Receipts.Pairs.ItemsPair;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import DataAccessLayer.ReceiptsDAOs.ReceiptDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;

@Entity
public class Receipt {

    //each receipt composed of Id of user/store to the items bought.
    //for example: For user receipt the key is all the storeIds he bought from and the value are the items
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int receiptId;
    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "receiptId")
    private List<ItemsPair> items;
    private int ownerId;
    private Date date;

    @Transient
    private ReceiptDAO dao;

    public Receipt(int ownerId, Calendar instance) throws Exception {
        items = new ArrayList<>();
        this.ownerId = ownerId;
        this.date = instance.getTime();

        dao = new ReceiptDAO();
    }

    public Receipt() throws Exception {
        dao = new ReceiptDAO();
        items = new ArrayList<>();
    }

    public int getId() {
        return receiptId;
    }

    public void addItems(int id, List<ReceiptItem> _items) throws Exception {
        boolean newPair;

        for(ReceiptItem item: _items){
            ItemsPair pair = (ItemsPair) Pair.searchPair(items, id);

            if(pair == null){
                pair = new ItemsPair(id, new ArrayList<>());
                items.add(pair);
                addItemPairTODAO(pair);
            }

            pair.getReceiptItems().add(item);
            addItemToPAirTODAO(pair, item);
        }
    }

    public void addItemPairTODAO(ItemsPair pair){
        dao.addItemPair(this, pair);
    }

    public void addItemToPAirTODAO(ItemsPair pair, ReceiptItem item){
        dao.addItemToPAir(pair, item);
    }

//    public void addItemsTODAO(boolean newPair, ReceiptItem item, ItemsPair pair) throws Exception {
//        dao.addItems(this, pair, item, newPair);
//    }

    public boolean itemExists(int userStoreId, int id){
        ItemsPair pair = (ItemsPair) Pair.searchPair(items, userStoreId);

        if(pair == null){
            return false;
        }

        for( ReceiptItem receiptItem : pair.getValue()){
            if(receiptItem.getId() == id){
                return true;
            }
        }
        return false;
    }

    public boolean deleteItem(int userStoreId, int id) throws Exception {
        ItemsPair pair = (ItemsPair) Pair.searchPair(items, userStoreId);

        if(pair == null){
            return false;
        }

        for( ReceiptItem receiptItem : pair.getValue()){
            if(receiptItem.getId() == id){
                pair.getValue().remove(receiptItem);

                dao.deleteItem(receiptItem);
                return true;
            }
        }
        return false;
    }

    public boolean update(int userStoreId, int id, ReceiptItem item) throws Exception {
        ItemsPair pair = (ItemsPair) Pair.searchPair(items, userStoreId);

        if(pair == null){
            return false;
        }

        for( ReceiptItem receiptItem : pair.getValue()){
            if(receiptItem.getId() == id){
                pair.getValue().remove(receiptItem);
                pair.getValue().add(item);

                dao.update(this, pair, receiptItem, item);
                return true;
            }
        }
        return false;
    }

    public ReceiptItem getItem(int userStoreId, int id){
        ItemsPair pair = (ItemsPair) Pair.searchPair(items, userStoreId);

        if(pair == null){
            return null;
        }

        for( ReceiptItem receiptItem : pair.getValue()){
            if(receiptItem.getId() == id){
                return receiptItem;
            }
        }
        return null;
    }

    public boolean hasKeyId(int ownerId) {
        return Pair.searchPair(items, ownerId) != null;
    }

    public Date getDate() {
        return date;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Map<Integer, ArrayList<ReceiptItem>> getAllItems(){
        Map<Integer, ArrayList<ReceiptItem>> map = new HashMap<>();

        for(ItemsPair pair : items){
            map.put(pair.getKey(), new ArrayList<>(pair.getValue()));
        }

        return map;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public List<ItemsPair> getItems() {
        return items;
    }

    public void setItems(List<ItemsPair> items) {
        this.items = items;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
