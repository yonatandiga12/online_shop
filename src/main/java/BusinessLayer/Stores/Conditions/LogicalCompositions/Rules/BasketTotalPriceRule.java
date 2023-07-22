package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;


//Basket total price must be at least "minimumPrice" (without discounts)

public class BasketTotalPriceRule extends Rule
{
    double minimumPrice;
    public BasketTotalPriceRule(double minimumPrice, int id, Store store) throws Exception
    {
        super(id, store);
        if (minimumPrice<=0)
            throw new Exception("Error: minimum price must be positive");
        this.minimumPrice = minimumPrice;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        return (minimumPrice <= getBasketPrice(basketItems));
    }

    private double getBasketPrice(List<CartItemInfo> basketItems)
    {
        double price = 0;
        for (CartItemInfo item : basketItems)
        {
            price += item.getOriginalPrice()*item.getAmount(); //Price without discounts percent
        }
        return price;
    }

    @Override
    public String toString()
    {
        return "(Basket must have a total price of at least " + minimumPrice + " not including discounts)";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
