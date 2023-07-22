package BusinessLayer.ExternalSystems.Purchase.ProxyAdapter;

public class PaymentProxyService {
    /**
     * Dumb implementation until we will connect to external service
     */

    public PaymentProxyService(){

    }


    public boolean pay(int userId, double creditNumber) {
        //for testing
        return creditNumber > 0 ;
    }

    public boolean handshake() {
        return true;
    }

    public int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) {
        return 10000;
    }

    public boolean cancelPay(int transactionId) {
        return true;
    }


}
