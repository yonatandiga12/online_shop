package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public class ForbiddenCategoryRule extends Rule {
    private String forbiddenCategory;
    public ForbiddenCategoryRule(String forbiddenCategory, int id, Store store) {
        super(id, store);
        this.forbiddenCategory = forbiddenCategory;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age) {
        for (CartItemInfo item : basketItems)
        {
            if (item.getCategory() == forbiddenCategory)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        return  "(Basket does not contain items from category: " + forbiddenCategory + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return forbiddenCategory.equals(category);
    }

    @Override
    public void removeItem(int itemID) {
    }
}
