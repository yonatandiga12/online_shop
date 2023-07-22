package DataAccessLayer.CartAndBasketDAOs;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Market;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the class will be changed in next versions
 */
public class CartDAO {
    ConnectorConfigurations config;
    public CartDAO() throws Exception {
        config = Market.getConfigurations();
    }

    private DBConnector<Cart> cartDBConnector() {
        return new DBConnector<>(Cart.class, config);
    }

    private DBConnector<CartItemInfo> cartItemInfoDBConnector() {
        return new DBConnector<>(CartItemInfo.class, config);
    }

    private DBConnector<Basket.ItemWrapper> itemWrapperDBConnector() {
        return new DBConnector<>(Basket.ItemWrapper.class, config);
    }

    private DBConnector<Basket> basketDBConnector() {
        return new DBConnector<>(Basket.class, config);
    }

    public void addItem(Cart cart, Basket basket){
        basketDBConnector().insert(basket);

        cartDBConnector().saveState(cart);
    }

    public void removeBasket(Basket basket) {

        for(Basket.ItemWrapper wrapper : basket.getItems()){
            itemWrapperDBConnector().delete(wrapper.getId());
        }


        for(Basket.ItemWrapper wrapper : basket.getItems()){
            cartItemInfoDBConnector().delete(wrapper.info.getId());
        }
        basketDBConnector().delete(basket.getId());
    }

    public void empty(List<Basket> baskets, List<Coupon> coupons) throws Exception {
        for(Basket basket : baskets){
            removeBasket(basket);
        }

        DBConnector<Coupon> couponConnector =
                new DBConnector<>(Coupon.class, Market.getConfigurations());

        for(Coupon coupon : coupons){
            couponConnector.delete(coupon.getId());
        }
    }

    public void addCoupon(Cart cart, Coupon coupon) throws Exception {
        DBConnector<Coupon> couponConnector =
                new DBConnector<>(Coupon.class, Market.getConfigurations());
        couponConnector.insert(coupon);
        cartDBConnector().saveState(cart);
    }

    public void removeCoupon(Coupon coupon) throws Exception {
        DBConnector<Coupon> couponConnector =
                new DBConnector<>(Coupon.class, Market.getConfigurations());
        couponConnector.delete(coupon.getId());
    }

}
