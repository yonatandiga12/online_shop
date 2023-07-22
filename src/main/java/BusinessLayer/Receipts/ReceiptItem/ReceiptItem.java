package BusinessLayer.Receipts.ReceiptItem;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int generatedId;
    private int id;
    private String name;
    private double priceBeforeDiscount;
    private int amount;
    private double finalPrice;

    public ReceiptItem(int id, String name, int amount, double priceBeforeDiscount, double finalPrice){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.priceBeforeDiscount = priceBeforeDiscount;
        this.finalPrice = finalPrice;
    }

    public ReceiptItem(){

    }

    public double getPrice() {
        return priceBeforeDiscount;
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public int getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(int generatedId) {
        this.generatedId = generatedId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPriceBeforeDiscount() {
        return priceBeforeDiscount;
    }

    public void setPriceBeforeDiscount(double priceBeforeDiscount) {
        this.priceBeforeDiscount = priceBeforeDiscount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
