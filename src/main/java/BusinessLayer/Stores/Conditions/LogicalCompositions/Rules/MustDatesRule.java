package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.Calendar;
import java.util.List;

public class MustDatesRule extends Rule {
    private List<Calendar> mustDates;
    public MustDatesRule(List<Calendar> mustDates, int id, Store store) {
        super(id, store);
        this.mustDates = mustDates;
    }
    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age) {
        Calendar now = Calendar.getInstance();
        for (Calendar date : mustDates) {
            if (now.get(6) == date.get(6)) //6 = DAY_OF_YEAR (1-365, or 1-366 on leap years)
                return true;
        }
        return false;
    }

    private String getMustDateString(Calendar mustDate)
    {
        return  mustDate.get(5) + "." + (mustDate.get(2) + 1) + "." + mustDate.get(1);
    }

    @Override
    public String toString()
    {
        String result = "";
        for (Calendar mustDate : mustDates)
        {
            result += "; " + getMustDateString(mustDate);
        }
        if (result.length() > 1)
        {
            result = result.substring(2);
        }
        return  "(The purchase date is one of the following dates: " + result + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
