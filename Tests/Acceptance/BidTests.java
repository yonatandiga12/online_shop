package Acceptance;

import ServiceLayer.Objects.BidService;
import ServiceLayer.Objects.ChatService;
import ServiceLayer.Objects.MessageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class BidTests extends ProjectTest{


    public static boolean doneSetUp = false;


    protected static int user2LoggedInId = -1;
    protected static int user3LoggedInId = -1;   // registered, not logged in
    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
    protected static int store2Id = -1;             //store is open
    protected static int store21IdOneOwner = -1;
    protected static int item1Id = -1;
    protected static int item11Id = -1;             //item11 in store2 but not in basket
    protected static int item21Id = -1;
    protected static int item211Id = -1;


    /**
     * User2: Member, logged in, Store Owner and Manager of store2
     */
    protected void setUpUser2(){
        if(user2LoggedInId != -1){
            return;
        }
        user2LoggedInId = setUser("User2BidTests","User2!", MEMBER, LOGGED);
        user6OwnerOfStore2 = setUser("User6BidTests", "User6!", MEMBER, LOGGED);
        store2Id = createStore(user2LoggedInId, "Store2"); //store is open

        //Make user6 and user5 manager Owner
        defineStoreOwner(store2Id, user2LoggedInId, user6OwnerOfStore2);

        store21IdOneOwner = createStore(user2LoggedInId, "Store21"); //store is open

        //add items
        item1Id = addItemToStoreForTests(store2Id, "item1", 10, "Books", 10);
        item11Id = addItemToStoreForTests(store2Id, "item11", 10, "Books", 10);
        item21Id = addItemToStoreForTests(store21IdOneOwner, "item21", 10, "Books", 10);
        item211Id = addItemToStoreForTests(store21IdOneOwner, "item211", 10, "Books", 10);
    }

    /**
     * User3: Member, logged in, Has a cart with items
     */
    protected void setUpUser3() {
        if(user3LoggedInId != -1)
            return;
        user3LoggedInId = setUser("User3BidTests","User3!", MEMBER, LOGGED);
    }




    @Before
    public void setUp() {
        super.setUp();
        if(!doneSetUp) {
            setUpUser2();
            setUpUser3();
            doneSetUp = true;
        }
    }


    @After
    public void tearDown() {
        //delete stores and delete users from DB
    }


    /**
     * Add Bid #48
     */
    @Test
    public void addBid_Valid(){
        int newItemId = addItemToStoreForTests(store2Id, "addBid_Valid", 10, "Cat", 10);
        boolean added = this.getBridge().addBid(store2Id, newItemId, user3LoggedInId, 5);

        assertTrue(added);
        List<BidService> bids = this.getBridge().getUserBidsToReply(user2LoggedInId); //need to get all the storeBids

        boolean myBid = false;
        for(BidService bidService: bids){
            if(bidService.getUserId() == user3LoggedInId){
                myBid = true;
                break;
            }
        }
        assertTrue(myBid);
    }

    @Test
    public void addBid_itemNotExists(){
        boolean added = this.getBridge().addBid(store2Id, -1, user3LoggedInId, 5);

        assertFalse(added);
    }

    @Test
    public void addBid_UserNotValid(){
        int newItem = addItemToStoreForTests(store2Id, "item11", 10, "Cat", 13);
        boolean added = this.getBridge().addBid(store2Id, newItem, -1, 5);

        assertFalse(added);
    }

    /**
     * Approve Bid, Reject Bid, Counteroffer Bid One Owner #49
     */
    @Test
    public void approveBidOneOwner_Valid(){
        int newPrice = 1;
        boolean added = this.getBridge().addBid(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        assertTrue(added);

        int bidId = getBidId(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        boolean user2Accepts = this.getBridge().approve(store21IdOneOwner, bidId, user2LoggedInId);
        assertTrue(user2Accepts);

        boolean foundMsg = doesMsgExists("your bid for the item: item21, was approved by the store");

        assertTrue(foundMsg);
    }

    @Test
    public void rejectBidOneOwner_Valid(){
        int newPrice = 2;
        boolean added = this.getBridge().addBid(store21IdOneOwner, item21Id, user3LoggedInId, 2);
        assertTrue(added);

        int bidId = getBidId(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        boolean user2Rejects = this.getBridge().reject(store21IdOneOwner, bidId, user2LoggedInId);
        assertTrue(user2Rejects);

        boolean foundMsg = doesMsgExists("your bid for the item: item21, was rejected by the store");

        assertTrue(foundMsg);
    }


    @Test
    public void counterOffer_Valid(){
        int newPrice = 3;
        boolean added = this.getBridge().addBid(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        assertTrue(added);

        int bidId = getBidId(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        int counterOffer= newPrice + 1;
        boolean user2Counter = this.getBridge().counterOffer(store21IdOneOwner, bidId, user2LoggedInId, counterOffer);
        assertTrue(user2Counter);

        boolean foundMsg = doesMsgExists("Hi, your bid for the item: item21, was countered by the store");
        assertTrue(foundMsg);


        //check if user got the counterOffer
        BidService counteredBid = getBidForUser(store21IdOneOwner, item21Id, user3LoggedInId, newPrice);
        assertNotNull(counteredBid);

        assertEquals(counteredBid.getCounterOffer(), counterOffer);
        assertEquals(counteredBid.getStatus(), "Countered!");

    }

    /**
     * Approve Bid, Reject Bid, Counteroffer Bid Multiple Owners #50
     */
    @Test
    public void approveBidMultipleOwners_Valid(){
        int newPrice = 4;
        boolean added = this.getBridge().addBid(store2Id, item1Id, user3LoggedInId, newPrice);
        assertTrue(added);

        int bidId = getBidId(store2Id, item1Id, user3LoggedInId, newPrice);
        boolean user2Accepts = this.getBridge().approve(store2Id, bidId, user2LoggedInId);
        assertFalse(user2Accepts);

        boolean foundMsg = doesMsgExists("your bid for the item: item21, was approved by the store");
        assertFalse(foundMsg);

        //Now the second manager approves and the msg will be sent

        boolean user6Accepts = this.getBridge().approve(store2Id, bidId, user6OwnerOfStore2);
        assertTrue(user6Accepts);

        boolean foundMsg6 = doesMsgExists("your bid for the item: item1, was approved by the store");
        assertTrue(foundMsg6);
    }


    /**
     * , Reject Bid Multiple Owners #51
     */
    @Test
    public void rejectBidMultipleOwner_Valid(){
        int newPrice = 2;
        boolean added = this.getBridge().addBid(store2Id, item1Id, user3LoggedInId, 2);
        assertTrue(added);

        int bidId = getBidId(store2Id, item1Id, user3LoggedInId, newPrice);
        boolean user2Rejects = this.getBridge().reject(store2Id, bidId, user2LoggedInId);
        assertTrue(user2Rejects);

        //The msg should be sent to the user when the first manager rejects
        boolean foundMsg = doesMsgExists("your bid for the item: item1, was rejected by the store");
        assertTrue(foundMsg);
    }


    /**
     * Counteroffer Bid Multiple Owners #52
     */
    @Test
    public void counterOfferThanAcceptMultipleUsers_Valid(){
        int newPrice = 3;
        boolean added = this.getBridge().addBid(store2Id, item11Id, user3LoggedInId, newPrice);
        assertTrue(added);

        int bidId = getBidId(store2Id, item11Id, user3LoggedInId, newPrice);
        int counterOffer= newPrice + 1;
        boolean user2Counter = this.getBridge().counterOffer(store2Id, bidId, user2LoggedInId, counterOffer);
        assertFalse(user2Counter);

        boolean foundMsg = doesMsgExists("Hi, your bid for the item: item11, was countered by the store");
        assertFalse(foundMsg);

        this.getBridge().approve(store2Id, bidId, user6OwnerOfStore2);

        boolean foundMsg2 = doesMsgExists("Hi, your bid for the item: item1, was countered by the store");
        assertTrue(foundMsg2);


        //check if user got the counterOffer
        BidService counteredBid = getBidForUser(store2Id, item11Id, user3LoggedInId, newPrice);
        assertNotNull(counteredBid);

        assertEquals(counteredBid.getCounterOffer(), counterOffer);
        assertEquals(counteredBid.getStatus(), "Countered!");

    }


    @Test
    public void acceptThanCounterOfferMultipleUsers_Valid(){
        int newPrice = 3;
        boolean added = this.getBridge().addBid(store2Id, item1Id, user3LoggedInId, newPrice);
        assertTrue(added);

        int bidId = getBidId(store2Id, item1Id, user3LoggedInId, newPrice);
        int counterOffer= newPrice + 1;

        this.getBridge().approve(store2Id, bidId, user6OwnerOfStore2);

        boolean foundMsg2 = doesMsgExists("Hi, your bid for the item: item1, was countered by the store");
        assertFalse(foundMsg2);

        boolean user2Counter = this.getBridge().counterOffer(store2Id, bidId, user2LoggedInId, counterOffer);
        assertTrue(user2Counter);

        boolean foundMsg = doesMsgExists("Hi, your bid for the item: item1, was countered by the store");
        assertTrue(foundMsg);

        //check if user got the counterOffer
        BidService counteredBid = getBidForUser(store2Id, item1Id, user3LoggedInId, newPrice);
        assertNotNull(counteredBid);

        assertEquals(counteredBid.getCounterOffer(), counterOffer);
        assertEquals(counteredBid.getStatus(), "Countered!");

    }




    private boolean doesMsgExists(String msg) {
        //check if user3 got msg that the bid was approved
        HashMap<Integer, ChatService> notifications = getChats(user3LoggedInId);

        for(ChatService chatService: notifications.values()){
            for(MessageService messageService: chatService.getMessages()){
                if(messageService.getContent().contains(msg)){
                    return true;
                }
            }
        }
        return false;
    }


    private int getBidId(int storeId, int itemId, int userId, int newPrice) {
        List<BidService> bids = this.getBridge().getUserBidsToReply(user2LoggedInId);
        for(BidService bid: bids){
            if(bid.getStoreId() == storeId & bid.getUserId() == userId && bid.getItemId() == itemId && bid.getNewPrice() == newPrice)
                return bid.getId();
        }
        return -1;
    }


    private BidService getBidForUser(int storeId, int itemId, int userId, int newPrice) {
        List<BidService> bids = this.getBridge().getUserBids(userId);
        for(BidService bid: bids){
            if(bid.getStoreId() == storeId & bid.getUserId() == userId && bid.getItemId() == itemId && bid.getNewPrice() == newPrice)
                return bid;
        }
        return null;
    }


}
