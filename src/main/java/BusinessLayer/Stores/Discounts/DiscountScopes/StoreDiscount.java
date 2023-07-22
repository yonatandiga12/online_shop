package BusinessLayer.Stores.Discounts.DiscountScopes;

import BusinessLayer.CartAndBasket.CartItemInfo;

import java.util.List;

public class StoreDiscount implements DiscountScope {
    public StoreDiscount() {}

    public void setItemsPercents(List<CartItemInfo> copyBasket, double percent) //ByStore
    {
        for (CartItemInfo item: copyBasket)
        {
            item.setPercent(percent);
        }
    }
    @Override
    public String toString()
    {
        return "Discount is applied on the whole store";
    }

    public boolean isDiscountApplyForItem(int itemID, String category)
    {
        return true;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
