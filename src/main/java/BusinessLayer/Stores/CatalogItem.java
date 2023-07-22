package BusinessLayer.Stores;

import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Policies.PurchasePolicy;
import DataAccessLayer.ItemDAO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CatalogItem {
    @Transient
    private ItemDAO itemDAO;
    private String itemName;
    private double price;
    @Id
    private int itemID;
    private String category;
    private String storeName;
    @ManyToOne
    @JoinColumn(name = "storeID")
    private Store store;
    private double weight;
    private int amount;
    private int savedAmount;
    @Transient
    private List<Discount> discounts;
    @Transient
    private List<PurchasePolicy> purchasePolicies;
    @Transient
    private List<DiscountPolicy> discountPolicies;
    public CatalogItem() {
        this.itemDAO = new ItemDAO();
    }
    public CatalogItem(int itemID, String itemName, double price, String category, String storeName, Store store, double weight) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.storeName = storeName;
        this.store = store;
        this.weight = weight;
        this.amount = 0;
        this.savedAmount = 0;
        this.discounts = new ArrayList<>();
        this.purchasePolicies = new ArrayList<>();
        this.discountPolicies = new ArrayList<>();
        this.itemDAO = new ItemDAO();
    }

    public int getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(int savedAmount) {
        this.savedAmount = savedAmount;
        itemDAO.save(this);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        itemDAO.save(this);
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getStoreID() {
        return store.getStoreID();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<Discount> discounts) {
        this.discounts = discounts;
    }

    public List<PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }

    public void setPurchasePolicies(List<PurchasePolicy> purchasePolicies) {
        this.purchasePolicies = purchasePolicies;
    }

    public List<DiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }

    public void setDiscountPolicies(List<DiscountPolicy> discountPolicies) {
        this.discountPolicies = discountPolicies;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CatalogItem item)) {
            return false;
        }

        return itemName.equals(item.itemName)
                && price == item.price
                && itemID == item.itemID
                && category.equals(item.category)
                && weight == item.weight;
    }

    public String setName(String newName) {
        String oldName = itemName;
        itemName = newName;
        return oldName;
    }

    public void addAmount(int amountToAdd) {
        setAmount(this.amount + amountToAdd);
    }
}
