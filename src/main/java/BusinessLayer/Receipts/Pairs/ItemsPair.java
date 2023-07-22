package BusinessLayer.Receipts.Pairs;

import BusinessLayer.Pair;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;

@Entity
public class ItemsPair implements Pair<Integer, List<ReceiptItem>> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    private int UserOrStoreId;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ItemsPairId")
    private List<ReceiptItem> receiptItems;

    public ItemsPair(int _UserOrStoreId, List<ReceiptItem> _receiptItems){
        UserOrStoreId = _UserOrStoreId;
        receiptItems = _receiptItems;
    }

    public ItemsPair(){

    }

    @Override
    public Integer getKey() {
        return UserOrStoreId;
    }

    @Override
    public List<ReceiptItem> getValue() {
        return receiptItems;
    }

    @Override
    public void setKey(Integer k) {
        UserOrStoreId = k;
    }

    @Override
    public void setValue(List<ReceiptItem> v) {
        receiptItems = v;
    }

    public int getId(){
        return id;
    }

    public void setId(int _id){
        id = _id;
    }

    public int getUserOrStoreId() {
        return UserOrStoreId;
    }

    public void setUserOrStoreId(int userOrStoreId) {
        UserOrStoreId = userOrStoreId;
    }

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public void setReceiptItems(List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
    }
}
