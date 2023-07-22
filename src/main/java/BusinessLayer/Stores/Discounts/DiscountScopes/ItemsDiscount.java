package BusinessLayer.Stores.Discounts.DiscountScopes;

import BusinessLayer.CartAndBasket.CartItemInfo;

import java.util.List;

//@Entity
public class ItemsDiscount implements DiscountScope {
    //    @Id
//    @Column(name = "id", nullable = false)
    private Integer id;
    //    @Transient //TODO
    private List<Integer> itemIDs;

    public ItemsDiscount(List<Integer> itemIDs) {
        this.itemIDs = itemIDs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setItemsPercents(List<CartItemInfo> copyBasket, double percent) //ByItemsList
    {
        for (CartItemInfo item : copyBasket) {
            if (itemIDs.contains(item.getItemID())) {
                item.setPercent(percent);
            } else {
                item.setPercent(0);
            }
        }
    }

    @Override
    public String toString() {
        return "Discount is applied on the items IDs: " + itemIDs;
    }

    public boolean isDiscountApplyForItem(int itemID, String category) {
        return itemIDs.contains(itemID);
    }

    @Override
    public void removeItem(int itemID) {
        itemIDs.remove(itemID);
    }

    public List<Integer> getItemIDs() {
        return itemIDs;
    }

    public void setItemIDs(List<Integer> itemIDs) {
        this.itemIDs = itemIDs;
    }
}
