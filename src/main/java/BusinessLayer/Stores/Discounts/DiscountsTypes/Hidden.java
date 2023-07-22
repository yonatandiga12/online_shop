package BusinessLayer.Stores.Discounts.DiscountsTypes;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Stores.Discounts.DiscountScopes.DiscountScope;

import javax.persistence.Entity;
import java.util.Calendar;
import java.util.List;

//@Entity
public class Hidden extends DiscountType {
    private String coupon;

    public Hidden(int discountID, double percent, Calendar endOfSale, String coupon, DiscountScope discountScope){
        super(discountID, percent, endOfSale, discountScope);
        this.coupon = coupon;
    }

    protected boolean checkConditions(List<CartItemInfo> basketItems, List<Coupon> coupons)
    {
        return coupons.contains(coupon);
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }
}
