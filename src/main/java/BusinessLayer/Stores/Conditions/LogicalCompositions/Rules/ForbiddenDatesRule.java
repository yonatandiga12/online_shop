package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.Store;

import java.util.Calendar;
import java.util.List;

public class ForbiddenDatesRule extends Rule {
    private List<Calendar> forbiddenDates;
    public ForbiddenDatesRule(List<Calendar> forbiddenDates, int id, Store store) {
        super(id, store);
        this.forbiddenDates = forbiddenDates;
    }

    @Override
    public boolean checkConditions(List<CartItemInfo> basketItems, int age) {
        Calendar now = Calendar.getInstance();
        for (Calendar date : forbiddenDates) {
            if (now.get(6) == date.get(6)) //6 = DAY_OF_YEAR (1-365, or 1-366 on leap years)
                return false;
        }
        return true;
    }

    private String getForbiddenDateString(Calendar forbiddenDate)
    {
        return  forbiddenDate.get(5) + "." + (forbiddenDate.get(2) + 1) + "." + forbiddenDate.get(1);
    }

    @Override
    public String toString()
    {
        String result = "";
        for (Calendar forbiddenDate : forbiddenDates)
        {
            result += "; " + getForbiddenDateString(forbiddenDate);
        }
        if (result.length() > 1)
        {
            result = result.substring(2);
        }
        return  "(The purchase date is not one of the following dates: " + result + ")";
    }

    public boolean isApplyForItem(int itemID, String category)
    {
        return false;
    }

    @Override
    public void removeItem(int itemID) {
    }
}
