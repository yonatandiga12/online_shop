package ServiceLayer.Objects;

import BusinessLayer.Stores.Discounts.Discount;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Conditional;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Hidden;
import BusinessLayer.Stores.Discounts.DiscountsTypes.Visible;

public class DiscountService {

    private int discountId;
    private String type;

    public int getId(){
        return discountId;
    }
    public String getType() {
        return type;
    }

    public String getDiscountString() {
        return discountString;
    }

    private String discountString;


    public DiscountService(Discount discount){
        this.discountId = discount.getDiscountID();
        this.type = getDiscountTypeAsString(discount);
        this.discountString = discount.toString();
    }



    private String getDiscountTypeAsString(Discount discount) {
        if (discount instanceof Conditional)
            return "Conditional";
        else if(discount instanceof Hidden)
            return "Hidden";
        else if(discount instanceof Visible)
            return "Visible";
        return "";
    }



}
