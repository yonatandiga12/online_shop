package BusinessLayer.CartAndBasket;

import javax.persistence.*;

@Entity
@Table(name = "cartItemInfo")
public class CartItemInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int itemID;
    private int amount;
    private double percent;
    private double originalPrice;
    private String category;
    private String itemName;
    private double weight;

    public CartItemInfo(int itemID, int amount, double originalPrice, String category, String itemName, double weight){
        this.itemID = itemID;
        this.amount = amount;
        this.percent = 0;
        this.originalPrice = originalPrice;
        this.category = category;
        this.itemName = itemName;
        this.weight = weight;
    }

    public CartItemInfo(CartItemInfo other){
        this.id = other.id;
        itemID = other.itemID;
        amount = other.amount;
        percent = other.percent;
        originalPrice = other.originalPrice;
        category = other.category;
        itemName = other.itemName;
        weight = other.weight;
    }

    public CartItemInfo(){

    }

    public int getItemID() { return itemID; }

    public int getAmount() { return amount; }

    public double getPercent() { return percent; }

    public double getOriginalPrice() { return originalPrice; }

    public String getCategory() { return category; }

    public String getItemName() { return itemName; }

    public double getWeight() { return weight; }

    public void setPercent(double percent) { this.percent = percent; }

    public void setOriginalPrice(double newPrice) { originalPrice = newPrice; }

    public void setAmount(int _amount){
        amount = _amount;
    }

    public void setCategory(String newCategory) { category = newCategory; }

    public void setItemName(String newItemName) { itemName = newItemName; }

    public void setWeight(double newWeight) { weight = newWeight; }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof CartItemInfo item)){
            return false;
        }

        return     itemID == item.itemID
                && amount == item.amount
                && percent == item.percent
                && originalPrice == item.originalPrice
                && category.equals(item.category)
                && itemName.equals(item.itemName);
    }

    public double getFinalPrice() { return originalPrice*amount*(100-percent)/100; }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void steal(CartItemInfo other){
        itemID = other.itemID;
        amount = other.amount;
        percent = other.percent;
        originalPrice = other.originalPrice;
        category = other.category;
        itemName = other.itemName;
        weight = other.weight;
    }

    @Override
    public String toString() {
        return "CartItemInfo{" +
                "id=" + id +
                ", itemID=" + itemID +
                ", amount=" + amount +
                ", percent=" + percent +
                ", originalPrice=" + originalPrice +
                ", category='" + category + '\'' +
                ", itemName='" + itemName + '\'' +
                ", weight=" + weight +
                '}';
    }
}
