package BusinessLayer.Stores.Conditions.NumericCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Discounts.Discount;

import java.util.List;

public abstract class Xor extends NumericComposite
{
    public Xor(int id, List<Discount> discounts)
    {
        super(id, discounts);
    }
    protected double getBasketTotalPrice(List<CartItemInfo> items)
    {
        double result = 0;
        for (CartItemInfo item : items) {
            result += item.getFinalPrice();
        }
        return result;
    }
}