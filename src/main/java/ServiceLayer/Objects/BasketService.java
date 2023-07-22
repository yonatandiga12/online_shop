package ServiceLayer.Objects;

import BusinessLayer.CartAndBasket.Basket;

import java.util.LinkedList;
import java.util.List;

public class BasketService {
    private List<CartItemInfoService> items;
    private Basket basket;

    public BasketService(Basket basket) {
        this.basket = basket;
        this.items=getAllItems();
    }

    public boolean hasItem(int itemId){
        return basket.isItemInBasket(itemId);
    }

    public boolean isEmpty() {
        return basket.getItemsAsMap().isEmpty();
    }

    public int getStoreId(){
        return basket.getStore().getStoreID();
    }
    public String getStoreName(){
        return basket.getStore().getStoreName();
    }
    public void removeItem(CartItemInfoService item){
        items.remove(item);
    }
    public List<CartItemInfoService> getAllItems(){
        List<CartItemInfoService> serviceItemsList =new LinkedList<>();
        basket.getItemsAsMap().values().forEach(cartItemInfo -> serviceItemsList.add(new CartItemInfoService(cartItemInfo)));
        return serviceItemsList;
    }

}
