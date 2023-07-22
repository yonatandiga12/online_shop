package BusinessLayer.Stores;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Entity
@IdClass(Appointmentid.class)
public class Appointment {//should be per store
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "ownerID")
    @Column(name = "accepted")
    private Map<Integer,Boolean> acceptMap=new HashMap<>();//<ownerId,acceptOrNo>
    @Id
    private int creatorId;
    @Id
    private int storeId;
    @Id
    private int newOwnerId;
    /**
     *
     * @param ownersIdList list of the owners of the store
     */
    public Appointment(List<Integer> ownersIdList,int creatorId,int storeId,int newOwnerId){
        this.newOwnerId=newOwnerId;
        this.storeId=storeId;
        this.creatorId=creatorId;
        for (Integer id:ownersIdList) {
            //the creator automatically accept, and the creator should be one of the owners
            acceptMap.put(id, id == creatorId);
        }
    }

    public Appointment() {

    }

    public Map<Integer, Boolean> getAcceptMap() {
        return acceptMap;
    }

    public void setAcceptMap(Map<Integer, Boolean> acceptMap) {
        this.acceptMap = acceptMap;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
    public void accept(int ownerId) throws Exception {
        if (acceptMap.containsKey(ownerId))
            acceptMap.put(ownerId,true);
        else throw new Exception("this id="+ownerId+" is not a owner of this store="+storeId);
    }

    public int getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(int newOwnerId) {
        this.newOwnerId = newOwnerId;
    }
}
