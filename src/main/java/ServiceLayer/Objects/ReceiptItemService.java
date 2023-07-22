package ServiceLayer.Objects;

import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceiptItemService {


    private int id;
    private String name;
    private double priceBeforeDiscount;
    private int amount;
    private double finalPrice;
    private int ownerId;


    public ReceiptItemService(ReceiptItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.amount = item.getAmount();
        this.priceBeforeDiscount = item.getPrice() * amount;
        this.finalPrice = item.getFinalPrice();
    }


    public void  setOwnerId(int id){
        this.ownerId = id;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPriceBeforeDiscount() {
        return priceBeforeDiscount;
    }

    public int getAmount() {
        return amount;
    }


    public double getFinalPrice() {
        return finalPrice;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
