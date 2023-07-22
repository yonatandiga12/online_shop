package DataAccessLayer.CartAndBasketDAOs;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Market;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

public class BasketDAO {

    ConnectorConfigurations config;

    public BasketDAO() {
        try {
            config = Market.getConfigurations();
        } catch (Exception e) {
        }
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

    public void addItem(Basket basket, Basket.ItemWrapper wrapper, boolean wrapperPersistent) throws Exception {
        if (wrapperPersistent) {
            cartItemInfoDBConnector().saveState(wrapper.info);
        }
        else {
            itemWrapperDBConnector().insert(wrapper);
        }

        basketDBConnector().saveState(basket);
    }

    public void changeItemQuantity(CartItemInfo info) {
        cartItemInfoDBConnector().saveState(info);
    }

    public void removeItem(Basket.ItemWrapper wrapper) {
        itemWrapperDBConnector().delete(wrapper.getId());
        cartItemInfoDBConnector().delete(wrapper.getInfo().getId());
    }

    public void saveItems(Basket basket) {
        basketDBConnector().saveState(basket);
    }

    public void releaseItems(Basket basket) {
        basketDBConnector().saveState(basket);
    }

    public void updateBasketByCartItemInfoList(CartItemInfo info) {
        cartItemInfoDBConnector().saveState(info);
    }

}
