package ServiceLayer.Objects;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.Cart;
import ServiceLayer.ShoppingService;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    //service object for cart, includes a list of basketsService
    private List<BasketService> baskets;
    static ShoppingService shoppingService;

    public CartService(Cart cart) {
        baskets = new ArrayList<>();
        for (Basket basket : cart.getBasketsAsHashMap().values()) {
            baskets.add(new BasketService(basket));
        }
    }


    public BasketService getBasketOfStore(int storeId){
        for(BasketService basket: baskets){
            if(basket.getStoreId() == storeId)
                return basket;
        }
        return null;
    }

    public boolean isEmpty(){
        boolean found = true;
        if(baskets.isEmpty())
            return found;
        for(BasketService basketService : baskets){
            if(!basketService.isEmpty()){
                found = false;
            }
        }
        return found;
    }


    public List<BasketService> getAllBaskets() {
        return baskets;
    }
}
