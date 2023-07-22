package ServiceLayer.Objects;

import BusinessLayer.Stores.CatalogItem;

public class CatalogItemService {

    private String itemName;
    private double price;
    private int itemID;
    private String category;

    public String getStoreName() {
        return storeName;
    }

    private String storeName;

    public int getStoreID() {
        return storeID;
    }

    private int storeID;
    private boolean inStock;
    private int amount;
    private double weight;

    public CatalogItemService(CatalogItem item, boolean inStock)
    {
        this.itemID = item.getItemID();
        this.itemName = item.getItemName();
        this.category = item.getCategory();
        this.price = item.getPrice();
        this.storeName = item.getStoreName();
        this.storeID = item.getStoreID();
        this.inStock = inStock;
        this.amount = 0;
        this.weight = item.getWeight();
    }

    public String getItemName(){
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public int getItemID() {
        return itemID;
    }

    public String getCategory() {
        return category;
    }

    public boolean isInStock() {
        return inStock;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getWeight() {
        return this.weight;
    }
}
