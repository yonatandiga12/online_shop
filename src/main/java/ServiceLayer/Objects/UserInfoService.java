package ServiceLayer.Objects;

import BusinessLayer.StorePermissions.StoreManager;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Users.RegisteredUser;
import ServiceLayer.ShoppingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class UserInfoService {

    private final LocalDate birthday;
    private final String address;
    private String username;
    private int id;
    private ArrayList<Integer> storesIOwn;
    private ArrayList<Integer> storesIManage;
    private ShoppingService shoppingService;

    public UserInfoService(RegisteredUser user) throws Exception {
        shoppingService = new ShoppingService();
        this.username = user.getUsername();
        this.id = user.getId();
        this.birthday = user.getbDay();
        this.address = user.getAddress();
        this.storesIOwn = new ArrayList<>(user.getStoresIOwn().stream().map(StoreOwner::getStoreID).toList());
        this.storesIManage = new ArrayList<>(user.getStoresIManage().stream().map(StoreManager::getStoreID).toList());
    }


    //Sets the Store for Owner Screen. Only the Stores he appointed me are here
    //if action is owner than Only storeI Own will be defined.
    public UserInfoService(RegisteredUser user, Set<Integer> value, String action) throws Exception {
        this.shoppingService = new ShoppingService();
        this.username = user.getUsername();
        this.id = user.getId();
        this.birthday = user.getbDay();
        this.address = user.getAddress();
        if(action.equals("owner")){
            this.storesIOwn = new ArrayList<>(value);
            this.storesIManage = new ArrayList<>();
        }
        else{
            this.storesIManage = new ArrayList<>(value);
            this.storesIOwn = new ArrayList<>();
        }

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Integer> getStoresIOwn() {
        return storesIOwn;
    }

    public ArrayList<Integer> getStoresIManage() {
        return storesIManage;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }
    public void addStoresIOwn(int storeId) {
        this.storesIOwn.add(storeId);
    }

    public void addStoresIManage(int storeId) {
        this.storesIManage.add(storeId);
    }

    public void removeStoresIOwn(Integer storeId) {
        this.storesIOwn.remove(storeId);
    }

    public void removeStoresIManage(Integer storeId) {
        this.storesIManage.remove(storeId);
    }

    public boolean ownStore(int storeId) {
        return this.storesIOwn.contains(storeId);
    }

    public boolean manageStore(int storeId) {
        return this.storesIManage.contains(storeId);
    }

    public String getStoreIOwnString(){
        StringBuilder res = new StringBuilder();
        for(Integer id : storesIOwn){
            res.append(id).append(": ").append(shoppingService.getStoreName(id)).append(" ,");
        }
        if(res.length() > 0){
            res.deleteCharAt(res.length() - 1);   //deletes ,
        }
        return res.toString();
    }

    public String getStoreIManageString(){
        StringBuilder res = new StringBuilder();
        for(Integer id : storesIManage){
            res.append(id).append(": ").append(shoppingService.getStoreName(id)).append(" ,");
        }
        if(res.length() > 0){
            res.deleteCharAt(res.length() - 1);   //deletes ,
        }
        return res.toString();
    }
}