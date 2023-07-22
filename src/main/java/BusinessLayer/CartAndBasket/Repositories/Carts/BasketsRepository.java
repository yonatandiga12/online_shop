package BusinessLayer.CartAndBasket.Repositories.Carts;

import BusinessLayer.CartAndBasket.Basket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class BasketsRepository {
    private final ConcurrentHashMap<Integer, Basket> baskets; // <storeID, Basket>

    public BasketsRepository(){
        baskets = new ConcurrentHashMap<>();
    }

    public void putIfAbsent(int storeID, Basket basket){
        baskets.putIfAbsent(storeID, basket);
    }

    public Basket get(int storeID){
        return baskets.get(storeID);
    }

    public boolean containsKey(int key){
        return baskets.containsKey(key);
    }

    public Collection<Basket> values(){
        return baskets.values();
    }

    public void clear(){
        baskets.clear();
    }

    /**
     * @param storeID
     * key – the key that needs to be removed
     * @Returns
     * the previous value associated with key, or null if there was no mapping for key
     * @Throws
     * NullPointerException – if the specified key is null
     *
     */
    public Basket remove(int storeID){
        return baskets.remove(storeID);
    }

    public ConcurrentHashMap<Integer, Basket> getBaskets(){
        return baskets;
    }



}
