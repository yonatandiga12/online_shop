package BusinessLayer.ExternalSystems.Purchase;

public interface PaymentTarget {
    /**
     * The Interface the client knows about
     */


    /**
     * Version3
     * @return true if handshake has been successful
     */
    boolean handshake() throws Exception;


    /**
     * Version3
     * @return -1 if transaction failed, id in range [10000, 100000] if successful
     */
    int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) throws Exception;


    /**
     * Version3
     * @return true if successful
     */
    boolean cancelPay(int transactionId) throws Exception;

}
