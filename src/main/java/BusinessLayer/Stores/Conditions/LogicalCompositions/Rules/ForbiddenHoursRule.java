package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.Calendar;
import java.util.List;

public class ForbiddenHoursRule extends Rule {
    private int startHour;
    private int endHour;
    public ForbiddenHoursRule(int startHour, int endHour, int id, Store store) {
        super(id, store);
        this.startHour = startHour%24;
        this.endHour = endHour%24;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age) {
        Calendar now = Calendar.getInstance();
        boolean isForbidden = true;
        if (endHour<startHour)
        {
            isForbidden = now.get(11) >= startHour || now.get(11) < endHour; //11 = 0 to 23
        }
        else
        {
            isForbidden = now.get(11) >= startHour && now.get(11) < endHour; //11 = 0 to 23
        }
        return !isForbidden;
    }

    @Override
    public String toString()
    {
        if (endHour<startHour)
        {
            return "(The purchase time is after " + endHour + ":00 and before " + startHour + ":00)";
        }
        return "(The purchase time is before " + startHour + ":00 or after " + endHour + ":00)";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
