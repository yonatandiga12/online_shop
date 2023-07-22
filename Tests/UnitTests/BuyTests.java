package UnitTests;

import BusinessLayer.CartAndBasket.Cart;
import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.ExternalSystems.Purchase.PurchaseClient;
import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.Supply.SupplyClient;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Stores.CatalogItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isA;

public class BuyTests {

    static Cart cart;
    private static  PurchaseClient purchaseClient;
    private static  SupplyClient supplyClient;
    private static final int SUCCESS_BUY = 1;
    private static final int FAIL_BUY = 2;
    private final String SUCCESS_SUPPLY = "Success";
    private final String FAIL_SUPPLY = "Fail";
    private final SupplyInfo supplyInfo = new SupplyInfo("s", SUCCESS_SUPPLY, "s", "s", "s");
    private final PurchaseInfo purchaseSuccess = new PurchaseInfo("asd", SUCCESS_BUY, 1, "1",1, 1, LocalDate.now());
    private final PurchaseInfo purchaseFail = new PurchaseInfo("asd", FAIL_BUY, 1, "1",1, 1, LocalDate.now());


    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        cart = new Cart(1);

        cart = spy(Cart.class);
        doNothing().when(cart).saveItemsInBaskets(isA(PurchaseInfo.class));
        doNothing().when(cart).releaseItemFromBaskets();
        doNothing().when(cart).emptyCart();
        doNothing().when(cart).createReceipt(isA(HashMap.class));
        when(cart.calculateTotalPrice()).thenReturn(1.6);

        createSupplyAndPurchase();
    }

    private static void createSupplyAndPurchase(){
        try {
            purchaseClient = spy(PurchaseClient.class);
            when(purchaseClient.handShake()).thenReturn(true);
            when(purchaseClient.cancelPay(anyInt())).thenReturn(true);
            when(purchaseClient.pay(anyString(), eq(SUCCESS_BUY), anyInt(), anyString(), anyInt(), anyInt())).thenReturn(10000);
            when(purchaseClient.pay(anyString(), eq(FAIL_BUY), anyInt(), anyString(), anyInt(), anyInt())).thenReturn(-1);
            doNothing().when(purchaseClient).chooseService();


            supplyClient = spy(SupplyClient.class);
            when(supplyClient.cancelSupply(anyInt())).thenReturn(true);
            doNothing().when(supplyClient).chooseService();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Before
    public void Before_SupplyReturnTrue(){
        try {
            when(supplyClient.supply(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(10000);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void buyTest_Valid(){
        try{
            Map<Integer, Map<CatalogItem, CartItemInfo>> res =
                    cart.buyCart(purchaseClient, supplyClient, purchaseSuccess, supplyInfo);

            assertNotNull(res);
        } catch (Exception e){
            fail(e.getMessage());
        }
    }



    @Test
    public void buyTest_FailBuy(){
        try{

            Map<Integer, Map<CatalogItem, CartItemInfo>> res =
                    cart.buyCart(purchaseClient, supplyClient, purchaseFail, supplyInfo);

            fail("Should have thrown an exception");
        } catch (Exception e){
            assertTrue(true);
        }
    }


    @Test
    public void buyTest_FailHandShake(){
        try{
            when(purchaseClient.handShake()).thenReturn(false);
            cart.buyCart(purchaseClient, supplyClient, purchaseFail, supplyInfo);

            fail("Should have thrown an exception");
        } catch (Exception e){
            assertTrue(true);
            try {
                when(purchaseClient.handShake()).thenReturn(true); //now handshake will return true always
            } catch (Exception ex) {
                fail();
            }
        }
    }


    @Test
    public void buyTest_FailSupply(){
        try{
            when(supplyClient.supply(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(-1);

            cart.buyCart(purchaseClient, supplyClient, purchaseFail, supplyInfo);

            fail("Should have thrown an exception");
        } catch (Exception e){
            assertTrue(true);
        }
    }

    @Test
    public void buyTest_FailInSaveItems(){
        try {
            doThrow(Exception.class).when(cart).saveItemsInBaskets(isA(PurchaseInfo.class));

            cart.buyCart(purchaseClient, supplyClient, purchaseFail, supplyInfo);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertTrue(true);
            try {
                doNothing().when(cart).saveItemsInBaskets(isA(PurchaseInfo.class));
            } catch (Exception ex) {
                fail();
            }

        }
    }



}
