package BusinessLayer.Stores.Pairs;

import BusinessLayer.Pair;
import BusinessLayer.Stores.Discounts.Discount;

public class DiscountPair implements Pair<Integer, Discount> {

    private int discountId;
    private Discount discount;

    public DiscountPair(int _discountId, Discount _discount){
        discountId = _discountId;
        discount = _discount;
    }

    public DiscountPair(){

    }

    @Override
    public Integer getKey() {
        return discountId;
    }

    @Override
    public Discount getValue() {
        return discount;
    }

    @Override
    public void setKey(Integer k) {
        discountId = k;
    }

    @Override
    public void setValue(Discount v) {
        discount = v;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }
}
