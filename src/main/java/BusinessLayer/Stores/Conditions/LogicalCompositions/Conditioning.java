package BusinessLayer.Stores.Conditions.LogicalCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public class Conditioning extends LogicalComponent {    // if you break first -> you must apply second
    private LogicalComponent firstCondition;
    private LogicalComponent secondCondition;

    public Conditioning(LogicalComponent firstCondition, LogicalComponent secondCondition, int id, Store store)
    {
        super(id, store);
        this.firstCondition = firstCondition;
        this.secondCondition = secondCondition;
    }

    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        if (!firstCondition.checkConditions(basketItems, age) && !secondCondition.checkConditions(basketItems, age))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "(" + firstCondition.toString() + " unless " + secondCondition.toString() + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return (firstCondition.isApplyForItem(itemID, category) || secondCondition.isApplyForItem(itemID, category));
    }

    public void removeItem(int itemID)
    {
        firstCondition.removeItem(itemID);
        secondCondition.removeItem(itemID);
    }
}