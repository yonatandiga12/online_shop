package BusinessLayer.Stores.Conditions.NumericCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Stores.Discounts.Discount;

import java.util.ArrayList;
import java.util.List;

public abstract class NumericComposite extends Discount
{
    private List<Discount> discounts;
    public NumericComposite(int discountID, List<Discount> discounts)
    {
        super(discountID);
        this.discounts = discounts;
    }
    public List<Discount> getDiscounts()
    {
        return discounts;
    }

    public List<CartItemInfo> updateBasket(List<CartItemInfo> basketItems, List<Coupon> coupons)
    {
        List<List<CartItemInfo>> tempBaskets = new ArrayList<>();
        if (getDiscounts().size() == 0)
        {
            List<CartItemInfo> emptyBasket = new ArrayList<>();
            for (CartItemInfo item : basketItems)
            {
                item.setPercent(0);
                emptyBasket.add(new CartItemInfo(item));
            }
            return emptyBasket;
        }
        for (Discount numericComponent: getDiscounts())
        {
            tempBaskets.add(numericComponent.updateBasket(basketItems, coupons));
        }
        return updateBasketByNumericComposite(tempBaskets);
    }
    protected abstract List<CartItemInfo> updateBasketByNumericComposite(List<List<CartItemInfo>> tempBaskets);

    public boolean isDiscountApplyForItem(int itemID, String category)
    {
        for (Discount discount : discounts)
        {
            if (discount.isDiscountApplyForItem(itemID, category))
            {
                return true;
            }
        }
        return false;
    }

    public void removeItem(int itemID)
    {
        for (Discount discount : discounts)
        {
            discount.removeItem(itemID);
        }
    }
}
