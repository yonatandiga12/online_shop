package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public class BasketWeightLimitRule extends Rule
{
    private double basketWeightLimit;

    public BasketWeightLimitRule(double basketWeightLimit, int id, Store store) throws Exception
    {
        super(id, store);
        if (basketWeightLimit<=0)
            throw new Exception("Error: Basket weight limit must be positive");
        this.basketWeightLimit = basketWeightLimit;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        return getBasketTotalWeight(basketItems) <= basketWeightLimit;
    }

    private double getBasketTotalWeight(List<CartItemInfo> basketItems)
    {
        double totalWeight = 0;
        for (CartItemInfo item : basketItems)
        {
            totalWeight += item.getWeight() * item.getAmount();
        }
        return totalWeight;
    }

    @Override
    public String toString()
    {
        return  "(Basket's total weight is at most: " + basketWeightLimit + " KG)";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
