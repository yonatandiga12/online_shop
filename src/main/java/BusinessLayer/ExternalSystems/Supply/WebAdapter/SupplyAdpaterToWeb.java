package BusinessLayer.ExternalSystems.Supply.WebAdapter;

import BusinessLayer.ExternalSystems.Purchase.WebAdpater.PaymentWebService;
import BusinessLayer.ExternalSystems.Supply.SupplyTarget;

public class SupplyAdpaterToWeb implements SupplyTarget {

    private SupplyWebService service;
    public SupplyAdpaterToWeb(){
        service = new SupplyWebService();
    }

    @Override
    public int supply(String buyerName, String address, String city, String country, String zip) throws Exception {
        return service.supply(buyerName, address, city, country, zip);
    }

    @Override
    public boolean cancelSupply(int transactionId) throws Exception {
        return service.cancelSupply(transactionId);
    }

}
