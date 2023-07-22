package BusinessLayer.Users;

import BusinessLayer.Market;
import BusinessLayer.StorePermissions.StoreEmployees;
import BusinessLayer.StorePermissions.StoreOwner;
import BusinessLayer.Stores.Store;
import BusinessLayer.Stores.StoreFacade;

import javax.annotation.processing.Generated;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class SystemManager {
    private final UserFacade userFacade;
    private final StoreFacade storeFacade;
    private RegisteredUser myUser;
    private Market market;

    public SystemManager(RegisteredUser user) throws Exception {
        this.myUser = user;
        this.market = Market.getInstance();
        this.userFacade = market.getUserFacade();
        this.storeFacade = market.getStoreFacade();
        this.market.addAdmin(myUser.getId(), this);
    }


    public SystemManager(){
        this.userFacade = null;
        this.storeFacade = null;
    }

    public void closeStorePermanently(Store store) throws Exception
    {
        //remove all owners -> will automatically remove all managers
        int founderID = store.getFounderID();
        RegisteredUser founder = userFacade.getRegisteredUser(founderID);
        myUser.closeStore(founder, store.getStoreID());
        storeFacade.closeStorePermanently(store.getStoreID());
    }

    public void removeUser(RegisteredUser userToRemove) throws Exception {
        if (userToRemove==null)
        {
            throw new Exception("UserToRemove is NULL!");
        }
        if (!userToRemove.getStoresIOwn().isEmpty() || !userToRemove.getStoresIManage().isEmpty()) {
            throw new Exception("Cannot remove User who owns or manages a store");
        }
        removeStoreAssociations(userToRemove);
        userFacade.removeUser(userToRemove);
    }

    private void removeStoreAssociations(RegisteredUser userToRemove) throws Exception
    {
        int storeId;
        int founderId;
        int parentUserId;
        Store store;
        RegisteredUser founder;
        RegisteredUser parentUser;
        List<Integer> storesIDs = new ArrayList<>(); //Amir
        List<RegisteredUser> parents = new ArrayList<>(); // Amir
        for (StoreOwner ownership : userToRemove.getStoresIOwn()) {
            //use store to find founder
            storeId = ownership.getStoreID();
            store = storeFacade.getStore(storeId);
            founderId = store.getFounderID();
            founder = userFacade.getRegisteredUser(founderId);
            parentUserId = founder.getStoreIOwn(storeId).findChild(userToRemove);
            parentUser = userFacade.getRegisteredUser(parentUserId);
            if (userToRemove.getId()==founderId) {
                parentUser.getStoreIOwn(storeId).closeStore();
                storeFacade.closeStorePermanently(storeId);
            }
            else {
                storesIDs.add(storeId); //Amir
                parents.add(parentUser); //Amir
                //parentUser.removeOwner(userToRemove, storeId); //Amir
            }
        }
        if (parents.size() == 0) //Amir
            return; //Amir
        for (int i = 0; i<parents.size(); i++) { //Amir
            parents.get(i).removeOwner(userToRemove, storesIDs.get(i)); //Amir
        } //Amir

        for (Integer storeID : userToRemove.getStoresIManage().stream().map(StoreEmployees::getStoreID).toList()) {
            myUser.removeManager(userToRemove, storeID);
        }
        //TODO also remove from notification system
    }
}
