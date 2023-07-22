package BusinessLayer.Stores.Discounts.DiscountScopes;

import BusinessLayer.CartAndBasket.CartItemInfo;

import java.util.List;

//@Entity
public class CategoryDiscount implements DiscountScope {
    //    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
    private Integer id;

    private String category;

    public CategoryDiscount(String category) {
        this.category = category;
    }

    public CategoryDiscount() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setItemsPercents(List<CartItemInfo> copyBasket, double percent) //ByCategory
    {
        for (CartItemInfo item : copyBasket) {
            if (item.getCategory().equals(category)) {
                item.setPercent(percent);
            } else {
                item.setPercent(0);
            }
        }
    }

    @Override
    public String toString() {
        return "Discount is applied on the category: " + category;
    }

    public boolean isDiscountApplyForItem(int itemID, String category) {
        return this.category.equals(category);
    }

    @Override
    public void removeItem(int itemID) {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
