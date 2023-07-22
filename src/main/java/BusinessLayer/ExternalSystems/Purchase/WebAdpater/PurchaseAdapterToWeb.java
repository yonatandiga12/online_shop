package BusinessLayer.ExternalSystems.Purchase.WebAdpater;

import BusinessLayer.ExternalSystems.Purchase.PaymentTarget;

public class PurchaseAdapterToWeb implements PaymentTarget {


    private PaymentWebService service;
    public PurchaseAdapterToWeb(){
        service = new PaymentWebService();
    }

    @Override
    public boolean handshake() throws Exception {
        return service.handShake();
    }

    @Override
    public int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) throws Exception {
        return service.pay(cardNumber, month, year, buyerName, ccv, buyerId);
    }

    @Override
    public boolean cancelPay(int transactionId) throws Exception {
        return service.cancelPay(transactionId);
    }
}
