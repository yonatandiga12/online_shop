package BusinessLayer.ExternalSystems.Purchase;

import BusinessLayer.ExternalSystems.Purchase.ProxyAdapter.PaymentRequestAdapter;
import BusinessLayer.ExternalSystems.Purchase.WebAdpater.PurchaseAdapterToWeb;

public class PurchaseClient {
    /**
     * This class will hold all the adapters. Later we will choose which external system to use
     * explanation: <a href="https://www.geeksforgeeks.org/adapter-pattern/">...</a>
     */

    //currently we have 1 external system, so i=only 1 adapter
    private PaymentRequestAdapter proxyAdapter;
    private PurchaseAdapterToWeb adapterToWebVersion3;


    public PurchaseClient(){
        proxyAdapter = new PaymentRequestAdapter();
        adapterToWebVersion3 = new PurchaseAdapterToWeb();
    }

    public void chooseService(){}


    /**
     * Version 3
     */

    public boolean handShake() throws Exception {
        return adapterToWebVersion3.handshake();
    }

    public int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) throws Exception {
        return adapterToWebVersion3.pay(cardNumber, month, year, buyerName, ccv, buyerId);
    }


    public boolean cancelPay(int transactionId) throws Exception {
        return adapterToWebVersion3.cancelPay(transactionId);
    }
}
