package BusinessLayer.ExternalSystems.Supply.ProxyAdapter;

import BusinessLayer.ExternalSystems.Supply.SupplyTarget;

public class SupplyRequestAdapter implements SupplyTarget {


    private final SupplyProxyService service;

    public SupplyRequestAdapter(){
        service = new SupplyProxyService();
    }

    public boolean supply(int userId, String address) {
        return service.supply(userId, address);
    }

    @Override
    public int supply(String buyerName, String address, String city, String country, String zip) {
        return 0;
    }

    @Override
    public boolean cancelSupply(int transactionId) {
        return false;
    }
}
