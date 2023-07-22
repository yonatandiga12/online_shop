package BusinessLayer.Stores.Discounts.DiscountsTypes;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountScopes.DiscountScope;
import BusinessLayer.Stores.Store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DiscountType extends Discount {
    protected double percent;
    //    @Column(name = "endOfSale")
    protected Calendar endOfSale;
    //    @ManyToOne
//    @JoinColumn(name = "storeId")
    protected Store store;
    //    @Transient
    private DiscountScope discountScope;

    public DiscountType(int discountID, double percent, Calendar endOfSale, DiscountScope discountScope) {
        super(discountID);
        this.percent = percent;
        this.endOfSale = endOfSale;
        this.discountScope = discountScope;
    }

    @Override
    public String toString() {
        return "Percent is: " + percent + ", " +
                "End of sale is at: " + getEndOfSaleString(endOfSale) + ", " +
                discountScope.toString();
    }

    private String getEndOfSaleString(Calendar endOfSale) {
        return endOfSale.get(5) + "." + endOfSale.get(2) + "." + endOfSale.get(1);
    }

    public void removeItem(int itemID) {
        discountScope.removeItem(itemID);
    }

    public List<CartItemInfo> updateBasket(List<CartItemInfo> basketItems, List<Coupon> coupons) {
        List<CartItemInfo> copyBasket = new ArrayList<>();
        for (CartItemInfo item : basketItems) {
            copyBasket.add(new CartItemInfo(item));
        }
        if (checkConditions(basketItems, coupons)) {
            discountScope.setItemsPercents(copyBasket, percent);
        } else {
            discountScope.setItemsPercents(copyBasket, 0);
        }
        return copyBasket;
    }

    protected abstract boolean checkConditions(List<CartItemInfo> basketItems, List<Coupon> coupons);

    public boolean isDiscountApplyForItem(int itemID, String category) {
        return discountScope.isDiscountApplyForItem(itemID, category);
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public Calendar getEndOfSale() {
        return endOfSale;
    }

    public void setEndOfSale(Calendar endOfSale) {
        this.endOfSale = endOfSale;
    }

    public DiscountScope getDiscountScope() {
        return discountScope;
    }

    public void setDiscountScope(DiscountScope discountScope) {
        this.discountScope = discountScope;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
