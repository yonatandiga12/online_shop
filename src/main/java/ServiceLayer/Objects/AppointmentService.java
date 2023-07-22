package ServiceLayer.Objects;

import BusinessLayer.Stores.Appointment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentService {
    private int creatorId;
    private int storeId;
    private int newOwnerId;
    private Map<Integer,Boolean> acceptMap;//<ownerId,acceptOrNo>

    public AppointmentService(Appointment appointment){
        this.storeId= appointment.getStoreId();
        this.creatorId=appointment.getCreatorId();
        this.acceptMap=appointment.getAcceptMap();
        this.newOwnerId=appointment.getNewOwnerId();
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

    public Map<Integer, Boolean> getAcceptMap() {
        return acceptMap;
    }

    public void setAcceptMap(Map<Integer, Boolean> acceptMap) {
        this.acceptMap = acceptMap;
    }
    public List<Integer> getAccepted(){
        List<Integer> resultList = new ArrayList<>();
        acceptMap.forEach((key, value) -> {
            if (value) {
                resultList.add(key);
            }
        });
        return resultList;
    }
    public List<Integer> getNotYetAnswer(){
        List<Integer> resultList = new ArrayList<>();
        acceptMap.forEach((key, value) -> {
            if (!value) {
                resultList.add(key);
            }
        });
        return resultList;
    }

    public int getNewOwnerId() {
        return newOwnerId;
    }
}
