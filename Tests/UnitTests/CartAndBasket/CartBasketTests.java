package UnitTests.CartAndBasket;

import BusinessLayer.CartAndBasket.Basket;
import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.Stores.CatalogItem;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Cart and basket are tested together since Basket is an extension of Cart
 * and functioning as a Cart for a certain Store
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CartBasketTests {

    static CatalogItem item1;
    static CatalogItem item2;
    static CatalogItem item3;
    static CatalogItem item4;

    static Basket basket;
    static Cart cartMock;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");

        basket = spy(Basket.class);
        cartMock = spy(Cart.class);
        doReturn(true).when(basket).isItemInStoreCatalog(any());
        doNothing().when(basket).updateBasketWithCoupons(any());
        doNothing().when(basket).releaseItems(any());
        doNothing().when(basket).addItemToDAO(any(), anyBoolean());
        doNothing().when(basket).removeItemFromDAO(any());
        doNothing().when(basket).changeItemQuantityInDAO(any());

        doReturn(basket).when(cartMock).baskets_getBasketByStoreId(anyInt());
        doReturn(null).when(cartMock).baskets_getBasketByStoreId(-1);


        //int itemID, String itemName, double price, String category, String storeName, Store store, double weight
        item1 = new CatalogItem(15010, "name", 10, "asd", "asd", null, 2);
        item2 = new CatalogItem(15011, "name", 10, "asd", "asd", null, 2);
        item3 = new CatalogItem(15012, "name", 10, "asd", "asd", null, 2);
        item4 = new CatalogItem(15013, "name", 10, "asd", "asd", null, 2);

    }

    @Test
    public void aAddItem_positiveResults(){
        try{
            //GOOD
            basket.addItem(item1, 10, null);
            assertTrue("The item is not inside the cart!", basket.isItemInBasket(item1.getItemID()));

            basket.addItem(item2, 10, null);
            assertTrue("The item is not inside the cart!", basket.isItemInBasket(item2.getItemID()));

            basket.addItem(item3, 10, null);
            assertTrue("The item is not inside the cart!", basket.isItemInBasket(item3.getItemID()));

        }
        catch(Exception e){
            fail(e.getMessage());
        }
    }


    @Test
    public void bAddItem_nonPositiveAmount(){
        try{
            basket.addItem(item3, -10, null);
            fail("The cart added a non-positive amount of the item.");
        }
        catch(Exception e){
            assertEquals("ERROR: Basket::addItemToCart: given quantity is not valid!", e.getMessage());
        }
    }

    @Test
    public void cAddItem_alreadyInCart(){
        try{
            basket.addItem(item1, 10, null);
        }
        catch(Exception e){
            fail("should be able to increase the amount in the cart. got error:\n" + e.getMessage());
        }

    }


    @Test
    public void cRemoveItem_storeNotExist(){
        try{

            cartMock.addItem(null,  item4, 10);
            fail("The store and item does not exist!");
        }
        catch(Exception e){
            assertTrue(e.getMessage().contains("BusinessLayer.Stores.Store.getStoreID()"));
        }
    }

    @Test
    public void dRemoveItem_positiveResults(){
        try{
            basket.removeItem(item1.getItemID(), null);
            boolean res = basket.isItemInBasket(item1.getItemID());
            assertFalse("The item is still inside the cart!", res);

            basket.removeItem(item2.getItemID(), null);
            assertFalse("The item is still inside the cart!", basket.isItemInBasket(item2.getItemID()));

            basket.removeItem(item3.getItemID(), null);
            assertFalse("The item is still inside the cart!", basket.isItemInBasket(item3.getItemID()));
        }
        catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    public void eRemoveItem_storeAndItemNotExist(){
        try{

            cartMock.removeItem(10, -1);
            fail("The store and item does not exist!");
        }
        catch(Exception e){
            assertEquals("ERROR: Basket::removeItemFromCart: no such item in basket!", e.getMessage());
        }
    }

    @Test
    public void fRemoveItem_itemNotExist(){
        try{
            cartMock.removeItem(10, 3251351);
            fail("An item that does not exists was removed!");
        }
        catch(Exception e){
            assertEquals("ERROR: Basket::removeItemFromCart: no such item in basket!", e.getMessage());
        }
    }

    @Test
    public void gChangeItemQuantity_positiveResults(){

        HashMap<CatalogItem, CartItemInfo> map;
        try{
            basket.addItem(item1, 10, null);
            basket.addItem(item2, 10, null);
            basket.addItem(item3, 10, null);

            basket.changeItemQuantity(item1.getItemID(), 20, null);
            map = basket.getItemsAsMap();
            assertEquals("The item quantity was not change!", getCiiFromMap(map, item1).getAmount(), 20);


            basket.changeItemQuantity(item1.getItemID(), 30, null);
            map = basket.getItemsAsMap();
            assertEquals("The item quantity was not change!", getCiiFromMap(map, item1).getAmount(), 30);

        }
        catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    public void hChangeItemQuantity_storeAndItemNotExist(){
        try{

            cartMock.changeItemQuantity(-1, item1.getItemID(), 10);

            fail("The store and item does not exist!");
        }
        catch(Exception e){
            assertEquals("Cart::changeItemQuantityInCart: the store " + -1 + " was not found!", e.getMessage());
        }
    }

    @Test
    public void IChangeItemQuantity_itemNotExist(){
        try{
            cartMock.changeItemQuantity(10, -1, 10);

            fail("An item that does not exists was changed!");
        }
        catch(Exception e){
            assertEquals("ERROR: Basket::changeItemQuantityInCart: the item is not in the basket!", e.getMessage());
        }
    }

    @Test
    public void JChangeItemQuantity_nonPositiveQuantity(){
        try{
            cartMock.changeItemQuantity(10, item2.getItemID(), -10);
            fail("An item quantity was changed to non-positive!");
        }
        catch(Exception e){
            assertEquals("ERROR: Basket::changeItemQuantityInCart: given quantity is not valid!", e.getMessage());
        }

    }

    private CartItemInfo getCiiFromMap(HashMap<CatalogItem, CartItemInfo> map, CatalogItem item){
        for(CatalogItem t : map.keySet()){
            if(item.equals(t)){
                return map.get(t);
            }
        }

        return null;
    }


}
