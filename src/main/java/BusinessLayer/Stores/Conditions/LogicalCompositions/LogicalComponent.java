package BusinessLayer.Stores.Conditions.LogicalCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public abstract class LogicalComponent {
    private int id;
    protected Store store;
    public LogicalComponent(int id, Store store)
    {
        this.id = id;
        this.store = store;
    }
    public abstract boolean checkConditions(List<CartItemInfo> basketItems, int age);
    public int getID() { return id; }

    public abstract boolean isApplyForItem(int itemID, String category);

    public abstract void removeItem(int itemID);
}