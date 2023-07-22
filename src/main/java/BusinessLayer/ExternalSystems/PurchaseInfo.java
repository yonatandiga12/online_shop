package BusinessLayer.ExternalSystems;

import java.time.LocalDate;
import java.time.Period;

public class PurchaseInfo {

    private String cardNumber;
    private int month;
    private int year;
    private String holderName;
    private int ccv;
    private int buyerId;
    private LocalDate birthday;

    public PurchaseInfo(String cardNumber, int month, int year, String holderName, int ccv, int buyerId, LocalDate birthday){
        this.ccv = ccv;
        this.holderName = holderName;
        this.buyerId = buyerId;
        this.cardNumber = cardNumber;
        this.month = month;
        this.year = year;
        this.birthday = birthday;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getHolderName() {
        return holderName;
    }

    public int getCcv() {
        return ccv;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public int getAge(){
        if(birthday == null)
            return -1;
        LocalDate now = LocalDate.now();
        Period period = Period.between(now, birthday);
        return period.getYears();
    }
}
