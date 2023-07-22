package ServiceLayer.Objects;

import BusinessLayer.Stores.Bid;
import BusinessLayer.Stores.BidReplies;

import java.util.Map;

public class BidService {

    public static final String APPROVED_STRING = "Approved :)";
    public static final String COUNTERED_STRING = "Countered!";
    private static final String REJECTED_STRING = "Rejected :(";
    private int bidId;
    private int storeId;
    private int userId;
    private double originalPrice;
    private double newPrice;
    private int itemId;
    private String itemName;
    private String status;
    private double counterOffer;

    public BidService(Bid bid){
        this.itemId = bid.getItemID();
        this.newPrice = bid.getOfferedPrice();
        this.originalPrice = bid.getOriginalPrice();
        this.storeId = bid.getStoreID();
        this.bidId = bid.getBidID();
        this.itemName = bid.getItemName();
        this.status = createStatus(bid.getRepliers());
        this.counterOffer = bid.getHighestCounterOffer();
        this.userId = bid.getUserID();
    }

    private String createStatus(Map<Integer, BidReplies> repliers) {
        int numApproved = repliers.values().stream().filter(r->r!=null && r.equals(BidReplies.APPROVED)).toList().size();
        int numRejected = repliers.values().stream().filter(r->r!=null && r.equals(BidReplies.REJECTED)).toList().size();
        int numCountered = repliers.values().stream().filter(r->r!=null && r.equals(BidReplies.COUNTERED)).toList().size();
        double size = repliers.size();
        if (numApproved==size) {
            return APPROVED_STRING;
        }
        if (numRejected==0 && numCountered==0 && numApproved>0) {
            return numApproved + "/" + (int) size + " Approved";
        }
        if (numRejected>0) {
            return REJECTED_STRING;
        }
        if (numCountered>0 && numApproved+numCountered==size) {
            return COUNTERED_STRING;
        }
        return "Processing, Waiting for " + (int)(size-numApproved-numCountered) + " Responses";

    }

    public int getId(){
        return bidId;
    }
    public int getStoreId() {
        return storeId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getStatus() {
        return status;
    }
    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public int getUserId() {
        return userId;
    }

    public double getCounterOffer() {
        return counterOffer;
    }
}
