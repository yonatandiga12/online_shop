package BusinessLayer.Stores.Conditions.LogicalCompositions;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.List;

public class Or extends LogicalComposite {

    public Or(List<LogicalComponent> components, int id, Store store)
    {
        super(components, id, store);
    }

    public boolean checkConditions(List<CartItemInfo> basketItems, int age)
    {
        for (LogicalComponent component : getComponents())
        {
            if (component.checkConditions(basketItems, age))
            {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString()
    {
        String result = "";
        for (LogicalComponent logicalComponent : getComponents())
        {
            result += " | " + logicalComponent.toString();
        }
        if (result.length()>1)
            result = result.substring(3);
        return "(" + result + ")";
    }
}