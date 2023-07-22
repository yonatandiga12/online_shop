package UnitTests.StoreTests;

import BusinessLayer.Stores.*;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BidsUnitTests {
    static StoreFacade storeFacade;
    static int storeOwner1Id = 178;
    static int storeOwner2Id = 179;
    static int storeOwner3Id = 180;
    static int noRoleId = 181;
    static Store store1;
    static Bid bid1;

    @BeforeClass
    public static void setUp() throws Exception {
        //
        System.setProperty("env", "test");
        storeFacade = new StoreFacade();
        store1 = spy(Store.class);
        doNothing().when(store1).saveItemAmount(anyInt(), anyInt());
        doNothing().when(store1).sendMsgToList(anyList(), anyString());
        doNothing().when(store1).sendMsg(anyInt(), anyString());
        doNothing().when(store1).addSavedItemAmount(anyInt(), anyInt());
        doNothing().when(store1).addItemAmount(anyInt(), anyInt());

        CatalogItem catalogItem = new CatalogItem(1212, "name", 10, "asd", "asd", null, 12);
        doReturn(catalogItem).when(store1).getItem(anyInt());
        doReturn(null).when(store1).getItem(-1);
        doReturn(Arrays.asList(storeOwner1Id, storeOwner2Id, storeOwner3Id)).when(store1).addContactsToBid();
    }

    @Test
    public void aAddBidNoStore() {
        try {
            storeFacade.addBid(999, 1, noRoleId, 5);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("No store with ID: 999", e.getMessage());
        }
    }

    @Test
    public void bAddBidNoItem() {
        try {
            store1.addBid(-1, storeOwner1Id, 100);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("Item ID: -1 does not exist", e.getMessage());
        }
    }

    @Test
    public void cAddBidSuccessfully() {
        try {
            int bidsBefore = store1.getBids().size();
            bid1 = store1.addBid(1054, storeOwner1Id, 30);
            assertEquals(bidsBefore + 1, store1.getBids().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void dGetBidsToReply() {
        try {
            List<Bid> x = store1.getUserBidsToReply(storeOwner1Id);
            assertEquals(1, x.size());
            assertTrue(x.get(0).getRepliers().containsKey(storeOwner1Id));
            assertTrue(x.get(0).getRepliers().containsKey(storeOwner2Id));
            assertTrue(x.get(0).getRepliers().containsKey(storeOwner3Id));

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void eApproveNoBid() {
        try {
            store1.approve(999, storeOwner1Id);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("Bid ID: 999 does not exist", e.getMessage());
        }
    }

    @Test
    public void fApproveNoPermission() {
        try {
            store1.approve(bid1.getBidID(), noRoleId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + noRoleId + " is not allowed to reply to bid in this store", e.getMessage());
        }
    }

    @Test
    public void gApproveSuccess() {
        try {
            BidReplies reply = store1.approve(bid1.getBidID(), storeOwner1Id);
            assertNotEquals(reply, BidReplies.APPROVED);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void hApproveUserAlreadyReplied() {
        try {
            store1.approve(bid1.getBidID(), storeOwner1Id);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + storeOwner1Id + " has already replied to this bid", e.getMessage());
        }
    }
    @Test
    public void iRejectUserAlreadyReplied() {
        try {
            store1.reject(bid1.getBidID(), storeOwner1Id);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + storeOwner1Id + " has already replied to this bid", e.getMessage());
        }
    }

    @Test
    public void jCounterOfferNoBid() {
        try {
            store1.counterOffer(999, storeOwner3Id, 8);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("Bid ID: 999 does not exist", e.getMessage());
        }
    }
    @Test
    public void kCounterOfferNoPermission() {
        try {
            store1.counterOffer(bid1.getBidID(), noRoleId, 8);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + noRoleId + " is not allowed to reply to bid in this store", e.getMessage());
        }
    }
    @Test
    public void lCounterOfferNonPositive() {
        try {
            store1.counterOffer(bid1.getBidID(), storeOwner3Id, -8);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The counter offer must be positive", e.getMessage());
        }
    }
    @Test
    public void mCounterOfferLowerThanBuyerOffer() {
        try {
            store1.counterOffer(bid1.getBidID(), storeOwner3Id, 2);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The counter offer must be higher than the offered price of the buyer", e.getMessage());
        }
    }
    @Test
    public void nCounterOfferSuccess() {
        try {
            boolean result = store1.counterOffer(bid1.getBidID(), storeOwner3Id, 50);
            assertFalse(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void oCounterOfferAlreadyReplied() {
        try {
            store1.counterOffer(bid1.getBidID(), storeOwner3Id, 55);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + storeOwner3Id + " has already replied to this bid", e.getMessage());
        }
    }

    @Test
    public void pRejectNoBid() {
        try {
            store1.reject(999, storeOwner2Id);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("Bid ID: 999 does not exist", e.getMessage());
        }
    }
    @Test
    public void qRejectNoPermission() {
        try {
            store1.reject(bid1.getBidID(), noRoleId);
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("The user " + noRoleId+ " is not allowed to reply to bid in this store", e.getMessage());
        }
    }
    @Test
    public void rRejectSuccess() {
        try {
            boolean result = store1.reject(bid1.getBidID(), storeOwner2Id);
            assertTrue(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}