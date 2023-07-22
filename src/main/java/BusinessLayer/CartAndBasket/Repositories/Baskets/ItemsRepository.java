package BusinessLayer.CartAndBasket.Repositories.Baskets;

import BusinessLayer.CartAndBasket.Basket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ItemsRepository {
    //TODO: add DAO
    private final ConcurrentHashMap<Integer, Basket.ItemWrapper> items;


    public ItemsRepository(){
        items = new ConcurrentHashMap<>();
    }

    public void putIfAbsent(int itemID, Basket.ItemWrapper wrapper){
        items.putIfAbsent(itemID, wrapper);
    }
    public void put(int itemID, Basket.ItemWrapper wrapper){
        items.put(itemID, wrapper);
    }

    public Basket.ItemWrapper get(int itemID){
        return items.get(itemID);
    }

    public boolean containsKey(int key){
        return items.containsKey(key);
    }

    public void remove(int itemID){
        items.remove(itemID);
    }

    public ConcurrentHashMap.KeySetView<Integer, Basket.ItemWrapper> keySet(){
        return items.keySet();
    }

    public Collection<Basket.ItemWrapper> values(){
        return items.values();
    }
    /**
     @return 0 if not contains key
     */
    public int getAmount(int itemID){
        if (items.containsKey(itemID))
            return items.get(itemID).info.getAmount();
        return 0;
    }


}
