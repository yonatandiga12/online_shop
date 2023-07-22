package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public class BuyerAgeRule extends Rule { //you must be at the minimum age or older to pass the rule.
    private int minimumAge;
    public BuyerAgeRule(int minimumAge, int id, Store store) throws Exception
    {
        super(id, store);
        if (minimumAge<=0)
            throw new Exception("Error: Minimum age must be positive");
        this.minimumAge = minimumAge;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age) {
        return minimumAge <= age;
    }


    @Override
    public String toString()
    {
        return  "(Buyer must be at least: " + minimumAge + " years old)";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
