package BusinessLayer.Stores.Discounts.DiscountsTypes;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Stores.Discounts.DiscountScopes.DiscountScope;

import javax.persistence.Entity;
import java.util.Calendar;
import java.util.List;

//@Entity
public class Visible extends DiscountType {

    public Visible(int discountID, double percent, Calendar endOfSale, DiscountScope discountScope){
        super(discountID, percent, endOfSale, discountScope);
    }

    protected boolean checkConditions(List<CartItemInfo> basketItems, List<Coupon> coupons)
    {
        return true;
    }
}