package BusinessLayer.Stores.Discounts;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;

import java.util.List;

//@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Discount {
    //    @Id
    private int discountID;

    public Discount(int discountID) {
        this.discountID = discountID;
    }

    public abstract List<CartItemInfo> updateBasket(List<CartItemInfo> basketItems, List<Coupon> coupons);

    public int getDiscountID() {
        return discountID;
    }

    public void setDiscountID(int discountID) {
        this.discountID = discountID;
    }

    public abstract boolean isDiscountApplyForItem(int itemID, String category);

    public abstract void removeItem(int itemID);
}
