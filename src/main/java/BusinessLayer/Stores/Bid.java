package BusinessLayer.Stores;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static BusinessLayer.Stores.BidReplies.*;

public class Bid {
    private int bidID;
    private int storeID;
    private int itemID;
    private double offeredPrice;
    private double originalPrice;
    private int userID;
    private boolean bidRejected;
    private double highestCounterOffer;
    private Map<Integer, BidReplies> repliers;
    private String itemName;
    public Bid(int bidID, int itemID, String itemName, int userID, double offeredPrice, double originalPrice, int storeID) {
        this.bidID = bidID;
        this.storeID = storeID;
        this.itemID = itemID;
        this.itemName = itemName;
        this.offeredPrice = offeredPrice;
        this.originalPrice = originalPrice;
        this.userID = userID;
        this.highestCounterOffer = -1;
        this.bidRejected = false;
        this.repliers = new HashMap<>();
    }

    public int getStoreID() {
        return storeID;
    }

    public boolean isUserNeedToReply(int userID) {
        return (repliers.containsKey(userID) && repliers.get(userID) == null);
    }

    public int getBidID() {
        return bidID;
    }

    public int getItemID() {
        return itemID;
    }

    public int getUserID() {
        return userID;
    }

    public double getHighestCounterOffer() {
        return highestCounterOffer;
    }

    public boolean approve(int replierUserID) throws Exception {
        if (!repliers.keySet().contains(replierUserID)) {
            throw new Exception("The user " + replierUserID + " is not allowed to reply to bid in this store");
        }
        if (repliers.get(replierUserID) != null) {
            throw new Exception("The user " + replierUserID + " has already replied to this bid");
        }
        repliers.put(replierUserID, APPROVED);
        if (allReplied()) {
            return true;
        }
        return false;
    }

    public boolean replyToCounterOffer(boolean approved) throws Exception {
        if (getHighestCounterOffer() == -1 || repliers.values().stream().filter(r-> r!=null &&
                r.equals(COUNTERED)).toList().isEmpty()) {
            throw new Exception("There is no Counter Offer at the moment");
        }
        if (bidRejected) {
            throw new Exception("The bid has already been rejected by store");
        }

        return approved;
    }

    public boolean reject(int replierUserID) throws Exception {
        if (!repliers.keySet().contains(replierUserID)) {
            //return false;
            throw new Exception("The user " + replierUserID + " is not allowed to reply to bid in this store");
        }
        if (repliers.get(replierUserID) != null) {
            throw new Exception("The user " + replierUserID + " has already replied to this bid");
        }
        repliers.put(replierUserID, REJECTED);
        if (bidRejected)
            return false;
        bidRejected = true;
        return true;
    }
    public boolean counterOffer(int replierUserID, double counterOffer) throws Exception
    {
        if (!repliers.keySet().contains(replierUserID))
        {
            throw new Exception("The user " + replierUserID + " is not allowed to reply to bid in this store");
        }
        if (repliers.get(replierUserID) != null) {
            throw new Exception("The user " + replierUserID + " has already replied to this bid");
        }
        if (counterOffer <= 0)
        {
            throw new Exception("The counter offer must be positive");
        }
        if (counterOffer <= offeredPrice)
        {
            throw new Exception("The counter offer must be higher than the offered price of the buyer");
        }
        repliers.put(replierUserID, COUNTERED);
        if (counterOffer > highestCounterOffer) {
            highestCounterOffer = counterOffer;
        }
        if (allReplied())
        {
            return true;
        }
        return false;
    }

    private boolean allReplied() {
        for (Map.Entry<Integer, BidReplies> entry : repliers.entrySet()) {
            if (entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public Map<Integer, BidReplies> getRepliers() {
        return repliers;
    }

    public void setRepliers(List<Integer> repliersIDs) {
        for (Integer replierID : repliersIDs) {
            repliers.put(replierID, null);
        }
    }

    public double getOfferedPrice() {
        return offeredPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public String getItemName() {
        return itemName;
    }
}