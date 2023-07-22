package BusinessLayer.ExternalSystems.Purchase.ProxyAdapter;

import BusinessLayer.ExternalSystems.Purchase.PaymentTarget;

public class PaymentRequestAdapter implements PaymentTarget {

    /**
     * This is the adapter which links between the unknown service to the interface we are working with (PaymentTarget)
     */

    private final PaymentProxyService service;
    public PaymentRequestAdapter(){
        this.service = new PaymentProxyService();
    }


    @Override
    public boolean handshake() {
        return service.handshake();
    }

    @Override
    public int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) {
        return service.pay(cardNumber, month, year, buyerName, ccv, buyerId);
    }

    @Override
    public boolean cancelPay(int transactionId) {
        return service.cancelPay(transactionId);
    }
}
