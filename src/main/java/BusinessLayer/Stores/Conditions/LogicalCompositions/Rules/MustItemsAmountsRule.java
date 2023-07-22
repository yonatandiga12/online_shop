package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;
import java.util.Map;

public class MustItemsAmountsRule extends Rule{

    private Map<Integer, Integer> itemsAmounts;
    public MustItemsAmountsRule(Map<Integer, Integer> itemsAmounts, int id, Store store)
    {
        super(id, store);
        this.itemsAmounts = itemsAmounts;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        for (Map.Entry<Integer, Integer> itemAmount : itemsAmounts.entrySet())
        {
            if (!checkIfEnoughItemAmountInBasket(itemAmount, basketItems))
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfEnoughItemAmountInBasket(Map.Entry<Integer, Integer> itemAmount, List<CartItemInfo> basketItems)
    {
        for (CartItemInfo item : basketItems)
        {
            if (itemAmount.getKey() == item.getItemID())
            {
                if (itemAmount.getValue() <= item.getAmount())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String result = "";
        for (Map.Entry<Integer, Integer> itemAmount : itemsAmounts.entrySet()) {
            result += "; " + itemAmount.getValue() + " of item ID " + store.getItem(itemAmount.getKey()).getItemName();
        }
        if (result.length()>1)
            result = result.substring(2);
        return  "(Basket contains at least: " + result + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return itemsAmounts.containsKey(itemID);
    }

    @Override
    public void removeItem(int itemID) {
        itemsAmounts.remove(itemID);
    }
}
