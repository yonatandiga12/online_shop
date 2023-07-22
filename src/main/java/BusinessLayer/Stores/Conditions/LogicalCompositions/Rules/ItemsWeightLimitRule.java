package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;
import java.util.Map;

public class ItemsWeightLimitRule extends Rule
{
    private Map<Integer, Double> weightsLimits;

    public ItemsWeightLimitRule(Map<Integer, Double> weightsLimits, int id, Store store)
    {
        super(id, store);

        this.weightsLimits = weightsLimits;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        for (CartItemInfo item : basketItems)
        {
            if (checkIfItemIsOverWeightInBasket(item))
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfItemIsOverWeightInBasket(CartItemInfo item)
    {
        for (Map.Entry<Integer, Double> weightLimit : weightsLimits.entrySet())
        {
            if (weightLimit.getKey() == item.getItemID())
            {
                if (weightLimit.getValue() < item.getWeight()*item.getAmount())
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String result = "";
        for (Map.Entry<Integer, Double> weightLimit : weightsLimits.entrySet()) {
            result += "; " + weightLimit.getValue() + " KG of item " + store.getItem(weightLimit.getKey()).getItemName();
        }
        if (result.length()>1)
            result = result.substring(2);
        return  "(Basket must contain at most: " + result + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return weightsLimits.containsKey(itemID);
    }

    @Override
    public void removeItem(int itemID) {
        weightsLimits.remove(itemID);
    }
}
