package BusinessLayer.CartAndBasket;

import BusinessLayer.CartAndBasket.Repositories.Carts.BasketsRepository;
import BusinessLayer.ExternalSystems.ESPurchaseManager;
import BusinessLayer.ExternalSystems.Purchase.PurchaseClient;
import BusinessLayer.ExternalSystems.Supply.SupplyClient;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Log;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.CartAndBasketDAOs.CartDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import BusinessLayer.ExternalSystems.PurchaseInfo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Entity
public class Cart {

    //fields
    @Id
    private int userID;

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "userId")
    private List<Basket> baskets;

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "userId")
    private List<Coupon> coupons;

    @Transient
    private CartDAO dao;


    //methods
    /**
     * a constructor for registered user
     * @param _userID the id of the user
     */
    public Cart(int _userID) throws Exception {
        userID = _userID;
        baskets = new ArrayList<>();
        coupons = new ArrayList<>();
        dao = new CartDAO();
        Log.log.info("A new cart was created for user " + userID);
    }

    //for tests with Mocks
    public Cart() throws Exception {
        userID = 1;
        baskets = new ArrayList<>();
        coupons = new ArrayList<>();
        dao = new CartDAO();
    }

    public void addItem(Store store, CatalogItem item, int quantity) throws Exception {
        Basket b = baskets_getBasketByStoreId(store.getStoreID());
        if(b == null){
            b = new Basket(store, userID);
            baskets.add(b);

            dao.addItem(this, b);
        }

        b.addItem(item, quantity, coupons);

        Log.log.info("The item " + item.getItemID() + " of store " +
                store.getStoreID() + " was added (" + quantity + " units)");
    }

    public void removeItem(int storeID, int itemID) throws Exception {
        Basket basket = baskets_getBasketByStoreId(storeID);

        if(basket == null){
            Log.log.warning("Cart::removeItemFromCart: the store " + storeID + " was not found!");
            throw new Exception("Cart::removeItemFromCart: the store " + storeID + " was not found!");
        }

        basket.removeItem(itemID, coupons);
        Log.log.info("The item " + itemID + " of store " + storeID + " was removed from the cart");
    }

    public Basket removeBasket(int storeID) throws Exception {
        Basket basket = baskets_getBasketByStoreId(storeID);

        if(basket == null){
            Log.log.warning("Cart::removeBasket: the store " + storeID + " was not found!");
            throw new Exception("Cart::removeBasket: the store " + storeID + " was not found!");
        }
        else {
            baskets.remove(basket);

            dao.removeBasket(basket);

            Log.log.info("The Basket from user"+userID+" of store " + storeID + " was removed from the cart");
        }

        return basket;
    }

    public void changeItemQuantity(int storeID, int itemID, int quantity) throws Exception {
        Basket basket = baskets_getBasketByStoreId(storeID);

        if(basket == null){
            Log.log.warning("Cart::changeItemQuantityInCart: the store " + storeID + " was not found!");
            throw new Exception("Cart::changeItemQuantityInCart: the store " + storeID + " was not found!");
        }

        basket.changeItemQuantity(itemID, quantity, coupons);
        Log.log.info("The quantity of item " + itemID + "was changed");
    }

    /**
     * this method is used to show the costumer all the stores he added,
     * he can choose one of them and see what is inside with getItemsInBasket
     * @return List<String>
     */
    public List<String> getStoresOfBaskets(){
        List<String> names = new ArrayList<>();

        for(Basket basket : baskets){
            names.add(basket.getStore().getStoreName());
        }

        return names;
    }

    public HashMap<CatalogItem, CartItemInfo> getItemsInBasket(String storeName) throws Exception {
        int storeID = findStoreWithName(storeName);

        if(storeID == -1){
            Log.log.warning("Cart::getItemsInBasket: the store " + storeName + " was not found!");
            throw new Exception("Cart::getItemsInBasket: the store " + storeName + " was not found!");
        }

        return baskets_getBasketByStoreId(storeID).getItemsAsMap();
    }

    private int findStoreWithName(String name){
        for(Basket basket : baskets){
            if(basket.getStore().getStoreName().equals(name)){
                return basket.getStore().getStoreID();
            }
        }

        return -1; //not found
    }

    /**
     * HashMap <k,v> = <StoreID, <CatalogItem, quantity>>
     * @return a HashMap to give the ReceiptHandler in order to make a receipt
     * @throws Exception if the store throw exception for some reason
     */
    public Map<Integer, Map<CatalogItem, CartItemInfo>> buyCart(PurchaseClient purchase, SupplyClient supply, PurchaseInfo purchaseInfo, SupplyInfo supplyInfo) throws Exception {
        HashMap<Integer, Map<CatalogItem, CartItemInfo>> receiptData = new HashMap<>();

        ESPurchaseManager purchaseManager = new ESPurchaseManager(purchase, supply, purchaseInfo, supplyInfo);
        if(!purchaseManager.handShake()){
            throw new Exception("Problem with connection to external System");
        }
        saveItemsInBaskets(purchaseInfo);

        //TODO: should change in future versions
        double finalPrice = calculateTotalPrice();

        int purchaseTransId = purchaseManager.pay();

        purchaseManager.chooseSupplyService();
        int supplyTransId = purchaseManager.supply();

        if( purchaseTransId == -1 || supplyTransId == -1 ){
            releaseItemFromBaskets();
            purchaseManager.cancelSupply(supplyTransId);
            purchaseManager.cancelPay(purchaseTransId);
            throw new Exception("Problem with Supply or Purchase");
        }
        else{
            Log.log.info("Cart " + userID + " payment completed");
            Log.log.info("Cart " + userID + " delivery is scheduled");
        }

        createReceipt(receiptData);

        emptyCart();

        return receiptData;
    }

    public void emptyCart() {
        empty();
        Log.log.info("Cart " + userID + " is empty");
    }

    public void createReceipt(HashMap<Integer, Map<CatalogItem, CartItemInfo>> receiptData) throws Exception {
        for(Basket basket : baskets){
            receiptData.putIfAbsent(basket.getStore().getStoreID(), basket.buyBasket(userID));
        }
        Log.log.info("All items of " + userID + "are bought");
    }

    public void releaseItemFromBaskets() throws Exception {
        for(Basket basket : baskets){
            basket.releaseItems(basket.getItemsInfo());
        }
    }

    public void saveItemsInBaskets(PurchaseInfo purchaseInfo) throws Exception {
        for(Basket basket : baskets){

            basket.saveItems(coupons, userID, purchaseInfo.getAge());
        }
        Log.log.info("Items of cart " + userID + " are saved");
    }

    /**
     * empties the cart
     */
    public void empty(){
        try{
            dao.empty(baskets, coupons);
            baskets.clear();
            coupons.clear();
        }
        catch(Exception e){
            System.out.println("Cart::empty: " + e.getMessage());
        }
    }

    public double calculateTotalPrice(){
        double price = 0;

        for(Basket basket : baskets){
            price += basket.calculateTotalPrice();
        }

        return price;
    }

    public boolean isItemInCart(int itemID, int storeID){
        return baskets_getBasketByStoreId(storeID).isItemInBasket(itemID);
    }

    public ConcurrentHashMap<Integer, Basket> getBasketsAsHashMap() {
        ConcurrentHashMap<Integer, Basket> basketMap = new ConcurrentHashMap<>();

        for(Basket basket : baskets){
            basketMap.put(basket.getStore().getStoreID(), basket);
        }

        return basketMap;
    }

    public void addCoupon(String coupon) throws Exception {
        if(coupon.isBlank()){
            throw new Exception("Cart::addCoupon: given coupon is invalid!");
        }

        Coupon _coupon = new Coupon(coupon);
        coupons.add(_coupon);
        dao.addCoupon(this, _coupon);

        updateBasketsWithCoupons();
    }

    public void removeCoupon(String _coupon) throws Exception {
        if(_coupon.isBlank()){
            throw new Exception("Cart::removeCoupon: given coupon is invalid!");
        }

        Coupon coupon = getCouponWithString(_coupon);

        if(coupon == null){
            throw new Exception("Cart::removeCoupon: given coupon is not in cart!");
        }

        coupons.remove(coupon);

        dao.removeCoupon(coupon);

        updateBasketsWithCoupons();
    }

    private void updateBasketsWithCoupons() throws Exception {
        for(Basket basket : baskets){
            basket.updateBasketWithCoupons(coupons);
        }
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Basket baskets_getBasketByStoreId(int storeId){
        for(Basket b : baskets){
            if(b.getStore().getStoreID() == storeId){
                return b;
            }
        }

        return null;
    }

    public List<Basket> getBaskets() {
        return baskets;
    }

    public void setBaskets(List<Basket> baskets) {
        this.baskets = baskets;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    public List<String> getCouponStrings(){
        List<String> list = new ArrayList<>();

        for(Coupon coupon : coupons){
            list.add(coupon.getCouponString());
        }

        return list;
    }

    private Coupon getCouponWithString(String _coupon){
        for(Coupon coupon : coupons){
            if(coupon.getCouponString().equals(_coupon)){
                return coupon;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "userID=" + userID +
                ", baskets=" + baskets +
                ", coupons=" + coupons +
                ", dao=" + dao +
                '}';
    }
}
