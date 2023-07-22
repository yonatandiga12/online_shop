package BusinessLayer.Receipts;


import BusinessLayer.Log;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.Receipt.ReceiptCollection;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.CatalogItem;
import DataAccessLayer.ReceiptsDAOs.ReceiptHandlerDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Entity //todo this doesn't need to be an entity, similar to facade
public class ReceiptHandler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private static final Logger log = Log.log;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ReceiptHandlerId")
    private List<Receipt> receipts;

    @Transient
    private ReceiptHandlerDAO dao;


    public ReceiptHandler() throws Exception {
        receipts = new ArrayList<>();
        dao = new ReceiptHandlerDAO();
    }

    /**
     * add receipts to the owner of this class
     * @param ownerId the id of the class owner (store or user)
     * @param storeOrUserIdToItems
     *        - If the ownerId is userId than the keys of this map would be store Ids he bought at.
     *          If the ownerId is storeId than the key of this map would be the user bought from this store.
     *
     *        - The value is a map of catalogItems to CartItemInfo
     *          CartItemInfo : (amount, percent, finalPrice)
     *
     *        for store: key = userId (you only have 1 key)
     *        for user: keys = storesId (you have multiple keys if he bought from several store)
     *
     */
    public int addReceipt(int ownerId, Map<Integer,Map<CatalogItem, CartItemInfo>> storeOrUserIdToItems) throws Exception {
        Receipt newReceipt = new Receipt(ownerId, Calendar.getInstance());
        receipts.add(newReceipt);
        addReceiptToDAO(newReceipt);

        for (Map.Entry<Integer,Map<CatalogItem, CartItemInfo>> set : storeOrUserIdToItems.entrySet()) {
            ArrayList<ReceiptItem> items = convertToReceiptItems(set.getValue());
            addItemsToReceipt(newReceipt, set, items);
        }
        log.info("Created receipt successfully.");
        log.info("Added receipt successfully.");
        return newReceipt.getId();
    }

    public void addReceiptToDAO(Receipt newReceipt) throws Exception {
        dao.addReceipt(this, newReceipt);
    }

    public void addItemsToReceipt(Receipt newReceipt, Map.Entry<Integer, Map<CatalogItem, CartItemInfo>> set, ArrayList<ReceiptItem> items) throws Exception {
        newReceipt.addItems(set.getKey(), items);
    }

    private ArrayList<ReceiptItem> convertToReceiptItems(Map<CatalogItem, CartItemInfo> catalogItemsToAmount){
        ArrayList<ReceiptItem> items = new ArrayList<>();
        for (Map.Entry<CatalogItem, CartItemInfo> set : catalogItemsToAmount.entrySet()) {
            CatalogItem catalogItem = set.getKey();
            CartItemInfo cartItemInfo = set.getValue();
            int amount = cartItemInfo.getAmount();
            double finalPrice = cartItemInfo.getFinalPrice();
            items.add(new ReceiptItem(catalogItem.getItemID(), catalogItem.getItemName(),amount, catalogItem.getPrice(), finalPrice));
            log.info("Added items to receipt successfully.");
        }
        return items;
    }

    public ArrayList<Receipt> getStoreReceiptsFromUser(int storeId){
        return getByOwnerId(storeId);
    }

    public ArrayList<Receipt> getUserReceiptsFromStore(int userId){
        return getByOwnerId(userId);
    }

    public ArrayList<Receipt> getAllReceipts(){
        return new ArrayList<>(receipts);
    }

    public Receipt getReceipt(int receiptId){
        for(Receipt receipt: receipts){
            if(receipt.getId() == receiptId){
                return receipt;
            }
        }
        return null;
    }

    private ArrayList<Receipt> getByOwnerId(int ownerId) {
        ArrayList<Receipt> result = new ArrayList<>();
        for(Receipt receipt : receipts){
            if(receipt.hasKeyId(ownerId))
                result.add(receipt);
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }
}

