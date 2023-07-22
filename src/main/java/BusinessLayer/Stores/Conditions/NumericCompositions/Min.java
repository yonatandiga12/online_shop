package BusinessLayer.Stores.Conditions.NumericCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Discounts.Discount;

import java.util.ArrayList;
import java.util.List;

public class Min extends Xor //apply only the worst discount for a given basket (currently seems as useless)
{
    public Min(int id, List<Discount> discounts)
    {
        super(id, discounts);
    }

    protected List<CartItemInfo> updateBasketByNumericComposite(List<List<CartItemInfo>> tempBaskets)
    {
        double currentHighestPrice = -1;
        List<CartItemInfo> currentHighestBasket = new ArrayList<>();

        double tempPrice;
        for (List<CartItemInfo> items : tempBaskets) {
            tempPrice = getBasketTotalPrice(items);
            if (currentHighestPrice == -1) {
                currentHighestBasket = items;
                currentHighestPrice = tempPrice;
            }
            else {
                currentHighestPrice = Math.max(tempPrice, currentHighestPrice);
                if (currentHighestPrice == tempPrice) {
                    currentHighestBasket = items;
                }
            }
        }
        return currentHighestBasket;
    }

    @Override
    public String toString()
    {
        String result = "";
        for (Discount discount : getDiscounts())
        {
            result += " MIN " + discount.toString();
        }
        if (result.length()>1)
            result = result.substring(3);
        return "(" + result + ")";
    }
}