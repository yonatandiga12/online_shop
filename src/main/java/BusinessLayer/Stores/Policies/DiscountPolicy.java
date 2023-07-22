package BusinessLayer.Stores.Policies;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComponent;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.BuyerAgeRule;

import java.util.List;

public class DiscountPolicy
{
    private LogicalComponent root;

    public DiscountPolicy(LogicalComponent root)
    {
        this.root = root;
    }

    public LogicalComponent getRoot() {
        return root;
    }

    public boolean isValidForDiscount(List<CartItemInfo> basketItems, int age) throws Exception
    {
        if (root != null)
        {
            return root.checkConditions(basketItems, age);
        }
        else
        {
            throw new Exception("Discount policy has no logical component");
        }
    }
    @Override
    public String toString()
    {
        return "The discount policy condition is: " + root.toString();
    }

    public boolean isDiscountPolicyApplyForItem(int itemID, String category)
    {
        return root.isApplyForItem(itemID, category);
    }

    public void removeItem(int itemID)
    {
        root.removeItem(itemID);
    }
}
