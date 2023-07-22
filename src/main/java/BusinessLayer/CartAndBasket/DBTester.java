package BusinessLayer.CartAndBasket;

import BusinessLayer.Market;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import java.util.ArrayList;
import java.util.List;

public class DBTester {

    public static void main(String[] args){
        String url = "jdbc:mysql://localhost:3306/shefaissashar";
        String driver = "com.mysql.cj.jdbc.Driver";
        ConnectorConfigurations conf = new ConnectorConfigurations("Name", url, "root", "S41r@kT1e", driver);

//        infoTest(conf);

//        wrapperTest(conf);

//        basketTest(conf);

//        cartTest(conf);



        emptyTables(conf);
    }


    public static void infoTest(ConnectorConfigurations conf){
        DBConnector<CartItemInfo> infoConnector = new DBConnector<>(CartItemInfo.class, conf);

        CartItemInfo info1 = new CartItemInfo(1, 1, 1.0, "c1", "item1", 1.0);

        infoConnector.insert(info1);

        CartItemInfo info2 = new CartItemInfo(2, 2, 2.0, "c2", "item2", 2.0);
        CartItemInfo info3 = new CartItemInfo(3, 3, 3.0, "c3", "item3", 3.0);
        CartItemInfo info4 = new CartItemInfo(4, 4, 4.0, "c4", "item4", 4.0);

        infoConnector.insert(info2);
        infoConnector.insert(info3);
        infoConnector.insert(info4);

        info1.setAmount(17);
        infoConnector.saveState(info1);

        System.out.println(infoConnector.getAll());
    }

    public static void wrapperTest(ConnectorConfigurations conf) throws Exception {
        DBConnector<Basket.ItemWrapper> wrapperConnector = new DBConnector<>(Basket.ItemWrapper.class, conf);
        Store store1 = new Store(11, 75, "store1");
        Store store2 = new Store(22, 89, "store2");

        CatalogItem item1 = new CatalogItem(1, "item1", 1.0,
                                           "c1", "store1", store1, 1.0);
        Basket.ItemWrapper wrapper1 = new Basket.ItemWrapper(item1, 1);

        CatalogItem item2 = new CatalogItem(2, "item2", 2.0,
                "c2", "store2", store2, 2.0);
        Basket.ItemWrapper wrapper2 = new Basket.ItemWrapper(item2, 2);

        wrapperConnector.insert(wrapper1);
        wrapperConnector.insert(wrapper2);


        Basket.ItemWrapper w1 = wrapperConnector.getById(wrapper1.getId());
        Basket.ItemWrapper w2 = wrapperConnector.getById(wrapper2.getId());
        System.out.println(w1);
        System.out.println(w2);

    }

    public static void basketTest(ConnectorConfigurations conf) throws Exception {
        try{
            Market.getInstance().setConfigurations(conf);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

        Store store = new Store(1, 2, "store1");
        Basket basket = new Basket(store, 222);

        DBConnector<Basket> basketConnector = new DBConnector<>(Basket.class, conf);

        basketConnector.insert(basket);

        try{
            store.addCatalogItem(17, "item1", 53.22,
                    "category1", 82);
            store.addItemAmount(17, 33);
            store.addCatalogItem(18, "item2", 42.12,
                    "category2", 44);
            store.addItemAmount(18, 86);

            basket.addItem(store.getItem(17), 22, new ArrayList<>());
            basket.addItem(store.getItem(17), 3, new ArrayList<>());
            basket.addItem(store.getItem(18), 3, new ArrayList<>());
            basket.changeItemQuantity(17, 44, new ArrayList<>());
            basket.removeItem(17, new ArrayList<>());
            basket.saveItems(new ArrayList<>(), 222, 81);
            basket.addItem(store.getItem(17), 22, new ArrayList<>());

        }
        catch(Exception e){
            System.err.println(e.getCause());
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            System.exit(1);
        }

        System.out.println(basketConnector.getAll());
    }

    public static void cartTest(ConnectorConfigurations conf){
        try{
            Market.getInstance().setConfigurations(conf);

            Store store1 = new Store(1, 11, "store1");
            Store store2 = new Store(2, 22, "store2");

            store1.addCatalogItem(17, "item1", 53.22,
                    "category1", 82);
            store1.addItemAmount(17, 33);

            store2.addCatalogItem(25, "item2", 43.69,
                    "category2", 23);
            store2.addItemAmount(25, 100);


            DBConnector<Cart> cartConnector = new DBConnector<>(Cart.class, conf);

            Cart cart = new Cart(36);
            cartConnector.insert(cart);

            cart.addItem(store1, store1.getItem(17), 1);
            cart.addItem(store2, store2.getItem(25), 17);
            cart.removeBasket(store1.getStoreID());
            cart.addCoupon("coupon1");
            cart.addCoupon("coupon2");
            cart.removeCoupon("coupon1");


            System.out.println(cartConnector.getById(cart.getUserID()));
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void emptyTables(ConnectorConfigurations conf){
        List<DBConnector<?>> connectors = new ArrayList<>();

        connectors.add(new DBConnector<>(CartItemInfo.class, conf));
        connectors.add(new DBConnector<>(Basket.ItemWrapper.class, conf));
        connectors.add(new DBConnector<>(Basket.class, conf));
        connectors.add(new DBConnector<>(Cart.class, conf));
        connectors.add(new DBConnector<>(Coupon.class, conf));

        for(DBConnector<?> connector : connectors){
            connector.emptyTable();
        }
    }

}
