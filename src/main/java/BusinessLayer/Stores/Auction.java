package BusinessLayer.Stores;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Auction {
    private int itemID;
    private int auctionID;
    private Store store;
    private double initialPrice;
    private double currentPrice;
    private TimerTask endOfAuctionTask;
    private int currentWinningUserID;
    private Map<Integer, Double> offers;
    private Calendar endOfSale;
    private Timer auctionTimer;

    public Auction(Store store, int AuctionID, int itemID, double initialPrice, int auctionPeriodInDays)
    {
        this.itemID = itemID;
        this.auctionID = auctionID;
        endOfSale = Calendar.getInstance();
        endOfSale.add(Calendar.DAY_OF_MONTH, auctionPeriodInDays);
        endOfSale.set(Calendar.SECOND, 0);
        endOfSale.set(Calendar.HOUR_OF_DAY, 0);
        endOfSale.set(Calendar.MINUTE, 0);
        this.store = store;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.currentWinningUserID = -1;
        this.offers = new HashMap<>();
        this.auctionTimer = new Timer();
        endOfAuctionTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (currentWinningUserID == -1)
                {
                    try {
                        store.finishAuctionUnsuccessfully(auctionID);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    store.finishAuctionSuccessfully(auctionID);
                }
            }
        };
        auctionTimer.schedule(endOfAuctionTask, endOfSale.getTime());
    }
    public int getItemID()
    {
        return itemID;
    }
    public Timer getAuctionTimer()
    {
        return auctionTimer;
    }
    public boolean offerToAuction(int userID, double offerPrice)
    {
        if (Calendar.getInstance().before(endOfSale))
        {
            if (offerPrice > currentPrice)
            {
                currentPrice = offerPrice;
                currentWinningUserID = userID;
                offers.put(userID, offerPrice);
                return true;
            }
        }
        return false;
    }

    public double getInitialPrice()
    {
        return initialPrice;
    }
    public int getDaysLeft()
    {
        Calendar now = Calendar.getInstance();
        if (now.after(endOfSale))
            return 0;
        return (int)TimeUnit.MILLISECONDS.toDays(endOfSale.getTimeInMillis()-now.getTimeInMillis());
    }
    public int getCurrentWinningUserID()
    {
        return currentWinningUserID;
    }
    public double getCurrentPrice()
    {
        return currentPrice;
    }

    public Integer getAuctionID() {
        return auctionID;
    }
}
