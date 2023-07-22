package initialize;

import ServiceLayer.Objects.CatalogItemService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Loader {
    private MyData myData;
    private ShoppingService shoppingService;
    private UserService userService;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static String ADMIN_NAME = "admin";
    private final static String ADMIN_PASS = "adminpass";
    private List<String> registeredNames;
    private List<Store> createdStore;


    public Loader() throws Exception {
        shoppingService = new ShoppingService();
        userService = new UserService();
        registeredNames=new ArrayList<>();
        createdStore=new ArrayList<>();
    }
    public void load(String path) {
        try {
            // Read the JSON file as a String
            String jsonString = readFileAsString(path);

            // Create a Gson object
            Gson gson = new Gson();
            TypeToken<MyData> myDataTypeToken = new TypeToken<MyData> () {};

            // Parse the JSON string to MyData object
            myData = gson.fromJson(jsonString, myDataTypeToken.getType());
            //TODO build APP using API
            loadUsers(myData.getRegisteredUserList());
            loadAdmins(myData.adminsList);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUsers(List<RegisteredUser> userList){
        //users
        for (RegisteredUser user: userList) {
            if (!registeredNames.contains(user.getUsername())){
                Result<Integer> registerResult= userService.register(user.username,user.password, user.getAddress(),LocalDate.parse(user.getbDay(), formatter) );
                if (!registerResult.isError()){
                    registeredNames.add(user.getUsername());
                    Result<Integer> loginResult= userService.login(user.username,user.password);
                    if (!loginResult.isError()){
                        int founderId=registerResult.getValue();
                        buildStores(user.getStores(),founderId);

                        //logout
                        Result<Boolean> logoutResult= userService.logout(loginResult.getValue());
                        if (logoutResult.isError())
                            System.out.println("Fail to logout"+logoutResult.getMessage());
                    }
                    else {
                        System.out.println("Fail to login"+loginResult.getMessage());
                    }
                }
                else System.out.println("Fail to register"+registerResult.getMessage());
            }
            else {
                Result<Integer> loginResult= userService.login(user.username,user.password);
                if (!loginResult.isError()){
                    addOwners(user.getStores(),userService.getUserIdByName(user.getUsername()).getValue());//TODO dont assume
                    //logout
                    Result<Boolean> logoutResult= userService.logout(loginResult.getValue());
                    if (logoutResult.isError())
                        System.out.println("Fail to logout"+logoutResult.getMessage());
                }
                else {
                    System.out.println("Fail to login"+loginResult.getMessage());
                }

            }
        }
    }

    /**
     * assume all the store all ready created and now just add extra owners by owners that are not the founder
     * @param stores
     */
    private void addOwners(List<Store> stores, int founderId) {
        for (Store store:stores) {
            for (String ownerName:store.ownersList) {
                Result<Integer> newOwnerIdResult=userService.getUserIdByName(ownerName);
                if (!newOwnerIdResult.isError()) {
                    int newOwnerId=newOwnerIdResult.getValue();
                    Result<Boolean> addOwnerResult = userService.addOwner(
                            founderId,newOwnerId,
                            shoppingService.getStoreIdByName(store.getStoreName()).getValue());//TODO not assume all ok
                    if (addOwnerResult.isError()) System.out.println("Fail to addOwner"+addOwnerResult.getMessage());
                }else System.out.println("Fail to getUserIdByName"+newOwnerIdResult.getMessage());
            }
//            else System.out.println("Problem register or login"+registerResult.getMessage()+"\n"+loginResult.getMessage());
        }
    }

    private void loadAdmins(List<String> userNames) {
        Result<Integer> adminLoginResult=userService.login(ADMIN_NAME,ADMIN_PASS);
        if (!adminLoginResult.isError()) {
            int adminId=adminLoginResult.getValue();
            for (String userName:userNames) {
                Result<Integer> newAdminIdResult=userService.getUserIdByName(userName);
                if (!newAdminIdResult.isError()) {
                    int newAdminId=newAdminIdResult.getValue();
                    Result<Boolean> addAdminResult = userService.addAdmin(adminId,newAdminId);
                    if (addAdminResult.isError()) System.out.println("Fail to addAdminResult"+addAdminResult.getMessage());
                }else System.out.println("Fail to getUserIdByName"+newAdminIdResult.getMessage());
            }

            //logout from main Admin
            userService.logout(adminId);

        }else System.out.println("Fail to login to main admin"+adminLoginResult.getMessage());



    }

    private void buildStores(List<Store> stores, int founderId) {
        //stores
        for (Store store: stores) {
            Result<Integer> idResult =userService.getUserIdByName(store.founderName);
            if (!idResult.isError()){
                int id= idResult.getValue();
                //crate store
                Result<Integer> storeIdResult = shoppingService.createStore(id,store.getStoreName());//storeID
                if (!storeIdResult.isError()){
                    int storeId=storeIdResult.getValue();
                    //Load items & amount
                    loadItems(store, storeId);
                    loadOwners(founderId,store.ownersList,storeId);
                    loadManagers(founderId,store.managersList,storeId);

                }else System.out.println("Fail to create store"+storeIdResult.getMessage());
            }else System.out.println("Fail to getUserIdByName"+idResult.getMessage());

        }

    }

    private void loadManagers(int founderId, List<String> managersList, int storeId) {
        for (String managerName:managersList) {
            Result<Integer> newManagerIdResult=userService.getUserIdByName(managerName);
            if (!newManagerIdResult.isError()) {
                int newManagerId=newManagerIdResult.getValue();
                Result<Boolean> addManagerResult = userService.addManager(founderId,newManagerId,storeId);
                if (addManagerResult.isError()) System.out.println("Fail to addOwner"+addManagerResult.getMessage());
            }else System.out.println("Fail to getUserIdByName"+newManagerIdResult.getMessage());
        }
    }

    private void loadOwners(int founderId, List<String> ownersList, int storeID) {
        for (String ownerName:ownersList) {
            Result<Integer> newOwnerIdResult=userService.getUserIdByName(ownerName);
            if (!newOwnerIdResult.isError()) {
                int newOwnerId=newOwnerIdResult.getValue();
                Result<Boolean> addOwnerResult = userService.addOwner(founderId,newOwnerId,storeID);
                if (addOwnerResult.isError()) System.out.println("Fail to addOwner"+addOwnerResult.getMessage());
            }else System.out.println("Fail to getUserIdByName"+newOwnerIdResult.getMessage());
        }
    }

    private void loadItems(Store store, int storeID) {
        for (Item item: store.getItemList()) {
            Result<CatalogItemService> itemId = shoppingService.addItemToStore(storeID,item.getItemName(),
                    item.getItemPrice(), item.getItemCategory(), item.getWeight());
            if(!itemId.isError()) {
                Result<Boolean> addItemAmountResult=shoppingService.addItemAmount(storeID, itemId.getValue().getItemID(), item.getAmount());
                if (addItemAmountResult.isError()) System.out.println("Fail to addItemAmountResult"+addItemAmountResult.getMessage());
            }
            else System.out.println("Fail to addItemToStore"+itemId.getMessage());
        }
    }

    private static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private class MyData{
        private List<String> adminsList;
        private List<RegisteredUser> registeredUserList;
        public List<String> getAdminsList() {
            return adminsList;
        }

        public void setAdminsList(List<String> adminsList) {
            this.adminsList = adminsList;
        }

        public List<RegisteredUser> getRegisteredUserList() {
            return registeredUserList;
        }

        public void setRegisteredUserList(List<RegisteredUser> registeredUserList) {
            this.registeredUserList = registeredUserList;
        }
    }
    private class RegisteredUser {
        private String username;
        private String password;
        private String address;
        private String bDay;
        private List<Store> stores;
        // Getter and setter methods
        public List<Store> getStores() {
            return stores;
        }

        public void setStores(List<Store> stores) {
            this.stores = stores;
        }
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getbDay() {
            return bDay;
        }

        public void setbDay(String bDay) {
            this.bDay = bDay;
        }
    }

    private class Store {
        private String founderName;
        private String storeName;
        private List<String> ownersList;//names of users
        private List<String> managersList;//names of users
        private List<Item> itemList;
        // Getter and setter methods
        public List<String> getOwnersList() {
            return ownersList;
        }

        public void setOwnersList(List<String> ownersList) {
            this.ownersList = ownersList;
        }

        public List<String> getManagersList() {
            return managersList;
        }

        public void setManagersList(List<String> managersList) {
            this.managersList = managersList;
        }

        public List<Item> getItemList() {
            return itemList;
        }

        public void setItemList(List<Item> itemList) {
            this.itemList = itemList;
        }
        public String getFounderName() {
            return founderName;
        }

        public void setFounderName(String founderName) {
            this.founderName = founderName;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
    }

    private class Item {
        private String itemName;
        private double itemPrice;
        private String itemCategory;
        private double weight;
        private int amount;

        public Item(String itemName, double itemPrice, String itemCategory, double weight, int amount) {
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.itemCategory = itemCategory;
            this.weight = weight;
            this.amount = amount;
        }

        // Getter and setter methods
        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public double getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(double itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getItemCategory() {
            return itemCategory;
        }

        public void setItemCategory(String itemCategory) {
            this.itemCategory = itemCategory;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

    }
}
