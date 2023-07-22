package BusinessLayer.CartAndBasket;

import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.CartAndBasketDAOs.BasketDAO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DBConnector must have a separate query, and NOT use getById
 */
@Entity
@Table(name = "basket")
public class Basket {
//    fields
    @ManyToOne
    @JoinColumn(name = "storeId")
    private Store store;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "basketId")
    private List<Basket.ItemWrapper> items;

    private boolean itemsSaved; // true if the store saves the items inside the basket for the user
//    @OneToMany()
//    @LazyCollection(LazyCollectionOption.FALSE)
//    @JoinTable(name = "savedItems",
//            joinColumns = @JoinColumn(name = "basketId"))
//    private List<CartItemInfo> savedItems;

    private int userId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Transient
    private BasketDAO dao;

    //methods
    public Basket(Store _store, int _userId){
        store = _store;
        items = new ArrayList<>();
        itemsSaved = false;
//        savedItems = new ArrayList<>();
        userId = _userId;
        dao = new BasketDAO();
    }

    //for mocking
    public Basket(){
        dao = new BasketDAO();
        items = new ArrayList<>();
    }

    public void addItem(CatalogItem item, int quantity, List<Coupon> coupons) throws Exception {
        validateAddItem(item, quantity);
        ItemWrapper wrapper = searchInItemsById(item.getItemID());
        boolean wrapperPersisted = false;
        List<CartItemInfo> savedItemsInfos = getItemsInfo();

        if (wrapper != null){
            int prevAmount= wrapper.info.getAmount();
            wrapper.info.setAmount(quantity + prevAmount);
            wrapperPersisted = true;
        }
        else{
            wrapper = new ItemWrapper(item, quantity);
            items.add(wrapper);
        }

        addItemToDAO(wrapper, wrapperPersisted);

        releaseItems(savedItemsInfos);

        try{
            updateBasketWithCoupons(coupons);
        }
        catch(Exception e){
            // preventing an unwanted exception
        }
    }

    public void addItemToDAO(ItemWrapper wrapper, boolean wrapperPersisted) throws Exception {
        dao.addItem(this, wrapper, wrapperPersisted);
    }

    public void changeItemQuantity(int itemID, int quantity, List<Coupon> coupons) throws Exception {
        validateChangeItemQuantity(itemID, quantity);
        List<CartItemInfo> savedItemsInfos = getItemsInfo();

        ItemWrapper wrapper = searchInItemsById(itemID);
        wrapper.info.setAmount(quantity);

        changeItemQuantityInDAO(wrapper);

        releaseItems(savedItemsInfos);

        try{
            updateBasketWithCoupons(coupons);
        }
        catch(Exception e){
            // preventing an unwanted exception
        }
    }

    public void changeItemQuantityInDAO(ItemWrapper wrapper) throws Exception {
        dao.changeItemQuantity(wrapper.info);
    }

    public void removeItem(int itemID, List<Coupon> coupons) throws Exception {
        ItemWrapper wrapper = searchInItemsById(itemID);
        List<CartItemInfo> savedItemsInfos = getItemsInfo();

        if(wrapper == null){
            //LOG
            throw new Exception("ERROR: Basket::removeItemFromCart: no such item in basket!");
        }

        items.remove(wrapper);

        removeItemFromDAO(wrapper);

        releaseItems(savedItemsInfos);

        try{
            updateBasketWithCoupons(coupons);
        }
        catch(Exception e){
            // preventing an unwanted exception
        }
    }

    public void removeItemFromDAO(ItemWrapper wrapper) throws Exception {
        dao.removeItem(wrapper);
    }

    private void validateAddItem(CatalogItem item, int quantity) throws Exception {
        if(item == null){
            //LOG
            throw new Exception("ERROR: Basket:: the item ID you entered does not exist in the given store");
        }

        if(quantity < 1){
            //LOG
            throw new Exception("ERROR: Basket::addItemToCart: given quantity is not valid!");
        }

        if(!isItemInStoreCatalog(item)){
            throw new Exception("ERROR: Basket::addItemToCart: the item is not in the store!");
            //throw new Exception("ERROR: Basket::addItemToCart: the item is already in the basket!");
        }
    }

    public boolean isItemInStoreCatalog(CatalogItem item) {
        return store.isItemInCatalog(item.getItemID());
    }

    private void validateChangeItemQuantity(int itemID, int quantity) throws Exception {
        if(quantity < 1){
            //LOG
            throw new Exception("ERROR: Basket::changeItemQuantityInCart: given quantity is not valid!");
        }

        if(searchInItemsById(itemID) == null){
            //LOG
            throw new Exception("ERROR: Basket::changeItemQuantityInCart: the item is not in the basket!");
        }
    }

    public Store getStore(){
        return store;
    }

    public HashMap<CatalogItem, CartItemInfo> getItemsAsMap(){
        HashMap<CatalogItem, CartItemInfo> inBasket = new HashMap<>();

        for(ItemWrapper wrapper : items){
            inBasket.putIfAbsent(makeCopyOfCatalogItem(wrapper.item),
                    new CartItemInfo(wrapper.info));
        }

        return inBasket;
    }

    public List<ItemWrapper> getItems(){
        return items;
    }

    private CatalogItem makeCopyOfCatalogItem(CatalogItem item){ //@TODO TOMER
        return new CatalogItem(item.getItemID(), item.getItemName(), item.getPrice(), item.getCategory(), item.getStoreName(), getStore(), item.getWeight());
    }

    public void saveItems(List<Coupon> coupons, int userID, int age) throws Exception{
//        savedItems = getItemsInfo();

        try{
            store.saveItemsForUpcomingPurchase(getItemsInfo(), coupons, userID, age);
            itemsSaved = true;
            dao.saveItems(this);
        }
        catch(Exception e){
            //LOG

            /*
                NOTICE: the Store may throw an exception if Basket requests a certain
                item more than Store can provide.
            */

//            savedItems = null;
            itemsSaved = false;
            dao.saveItems(this);

            //NOTE: maybe need to delete every CartItemInfo in savedItems -> possible solution: empty savedItems, save, make null, save
            throw e;
        }



    }

    public List<CartItemInfo> getItemsInfo(){
        List<CartItemInfo> infos = new ArrayList<>();

        for(ItemWrapper wrapper : items){
            infos.add(new CartItemInfo(wrapper.info));
        }

        return infos;
    }

    /**
     * @return a HashMap of the bought items and their quantities
     * @throws Exception - the store can throw exceptions
     */
    public HashMap<CatalogItem, CartItemInfo> buyBasket(int userID) throws Exception{
        if(!itemsSaved){
            throw new Exception("The basket of store " + store.getStoreName() + " is not saved for buying");
        }

        try{
            store.buyBasket(getItemsInfo(), userID);
        }
        catch(Exception e){
            //LOG
            e.printStackTrace();
        }

        return prepareItemsForReceipt();
    }

    private HashMap<CatalogItem, CartItemInfo> prepareItemsForReceipt(){
        HashMap<CatalogItem, CartItemInfo> data = new HashMap<>();

        for(ItemWrapper item : items){
            data.putIfAbsent(item.item, item.info);
        }

        return data;
    }

    /**
     * if the basket contents had changed for some reason, the basket
     * asks the store to release the saved items
     */
    public void releaseItems(List<CartItemInfo> savedItemsInfos) throws Exception {
        if(itemsSaved){
            itemsSaved = false;
            store.reverseSavedItems(savedItemsInfos);

            dao.releaseItems(this);
//            savedItems.clear();

//            savedItems = null;
        }
    }

    public double calculateTotalPrice(){
        double price = 0;

        for(ItemWrapper wrapper : items){
            price += wrapper.info.getFinalPrice();
        }

        return price;
    }

    public boolean isItemInBasket(int itemID){
        return searchInItemsById(itemID) != null;
    }

    public void updateBasketWithCoupons(List<Coupon> coupons) throws Exception {
        List<CartItemInfo> updatedBasketItems = getItemsInfo();
        store.updateBasket(updatedBasketItems, coupons);
        //checkIfPurchaseIsValid(updatedBasketItems, age);
        updateBasketByCartItemInfoList(updatedBasketItems);
    }

    private void updateBasketByCartItemInfoList(List<CartItemInfo> updatedBasketItems) throws Exception {

        for(CartItemInfo info : updatedBasketItems){
            searchInItemsById(info.getItemID()).info.steal(info);
            dao.updateBasketByCartItemInfoList(searchInItemsById(info.getItemID()).info);
        }

    }

    private void checkIfPurchaseIsValid(List<CartItemInfo> updatedBasketItems, int age) throws Exception {
        if(!store.checkIfPurchaseIsValid(updatedBasketItems, age)){
            throw new Exception("ERROR: Basket::checkIfPurchaseIsValid: the purchase is not valid!");
        }
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setItems(List<ItemWrapper> items) {
        this.items = items;
    }

    public boolean isItemsSaved() {
        return itemsSaved;
    }

    public void setItemsSaved(boolean itemsSaved) {
        this.itemsSaved = itemsSaved;
    }

//    public List<CartItemInfo> getSavedItems() {
//        return savedItems;
//    }

//    public void setSavedItems(List<CartItemInfo> savedItems) {
//        this.savedItems = savedItems;
//    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private ItemWrapper searchInItemsById(int id){
        for(ItemWrapper wrapper : items){
            if(wrapper.item.getItemID() == id){
                return wrapper;
            }
        }

        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Basket{" +
                "store=" + store +
                ", items=" + items +
                ", itemsSaved=" + itemsSaved +
//                ", savedItems=" + savedItems +
                ", userId=" + userId +
                ", id=" + id +
                '}';
    }







    @Entity
    @Table(name = "itemwrapper")
    /**
     * <CatalogItem, quantity>
     * this class is a wrapper for Basket use only
     */
    public static class ItemWrapper{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

//        @ManyToOne
//        @JoinColumn(name = "itemId")
        @ManyToOne
        @JoinColumn(name = "itemId")
        public CatalogItem item;

        @OneToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "infoId")
        public CartItemInfo info;

        public ItemWrapper(CatalogItem _item, int quantity){
            item = _item;
            info = new CartItemInfo(item.getItemID(), quantity, item.getPrice(), _item.getCategory(), _item.getItemName(), _item.getWeight());
        }

        public ItemWrapper() {
        }

        public int getId() {
            return id;
        }

        public void setId(int _id) {
            this.id = _id;
        }

        public CatalogItem getItem() {
            return item;
        }

        public void setItem(CatalogItem item) {
            this.item = item;
        }

        public CartItemInfo getInfo() {
            return info;
        }

        public void setInfo(CartItemInfo info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return "ItemWrapper{" +
                    "id=" + id +
                    ", item=" + item +
                    ", info=" + info +
                    '}';
        }
    }

}


