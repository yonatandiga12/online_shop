package BusinessLayer.Stores;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Lottery {
    private int itemID;
    private double price;
    private TimerTask endOfLotteryTask;
    private int lotteryID;
    private Store store;
    private double priceLeft;
    private int winnerID;
    private Calendar endOfSale;
    private boolean lotteryFinished;
    private Map<Integer, Double> winningOdds;
    private Timer lotteryTimer;

    public Lottery(Store store, int lotteryID, int itemID, double price, int lotteryPeriodInDays)
    {
        this.itemID = itemID;
        this.lotteryID = lotteryID;
        this.store = store;
        this.price = price;
        this.winnerID = -1;
        this.priceLeft = price;
        this.lotteryFinished = false;
        this.endOfSale = Calendar.getInstance();
        this.endOfSale.add(Calendar.DAY_OF_MONTH, lotteryPeriodInDays);
        this.endOfSale.set(Calendar.SECOND, 0);
        this.endOfSale.set(Calendar.HOUR_OF_DAY, 0);
        this.endOfSale.set(Calendar.MINUTE, 0);
        this.winningOdds = new HashMap<>();
        this.lotteryTimer = new Timer();
        endOfLotteryTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try {
                    store.finishLotteryUnsuccessfully(lotteryID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        lotteryTimer.schedule(endOfLotteryTask, endOfSale.getTime());
    }
    public int getItemID()
    {
        return itemID;
    }
    public Timer getLotteryTimer()
    {
        return lotteryTimer;
    }
    public int getWinnerID() {
        return winnerID;
    }
    public List<Integer> getParticipants()
    {
        return winningOdds.keySet().stream().toList();
    }
    public boolean participateInLottery(int userID, double offerPrice)
    {
        if (Calendar.getInstance().before(endOfSale))
        {
            if (offerPrice <= priceLeft && offerPrice>0)
            {
                priceLeft -= offerPrice;
                winningOdds.put(userID, offerPrice);
                if (priceLeft == 0)
                {
                    lotteryFinished = true;
                    winnerID = generateWinner();
                    System.out.println("The item is sold to user ID: " + winnerID + " at a price of " + winningOdds.get(winnerID)); //The task
                }
                return true;
            }
        }
        return false;
    }

    public int getDaysLeft()
    {
        Calendar now = Calendar.getInstance();
        if (now.after(endOfSale))
            return 0;
        return (int) TimeUnit.MILLISECONDS.toDays(endOfSale.getTimeInMillis()-now.getTimeInMillis());
    }

    private int generateWinner()
    {
        Random random = new Random();
        double randomNumber = random.nextDouble();
        for (Map.Entry<Integer, Double> entry : winningOdds.entrySet())
        {
            randomNumber -= (entry.getValue()/price);
            if (randomNumber <= 0)
            {
                return entry.getKey();
            }
        }
        return -1;
    }
    public boolean isLotteryFinished()
    {
        return lotteryFinished;
    }

    public Integer getLotteryID() {
        return lotteryID;
    }
}
