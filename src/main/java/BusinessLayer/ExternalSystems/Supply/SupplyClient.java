package BusinessLayer.ExternalSystems.Supply;

import BusinessLayer.ExternalSystems.Supply.ProxyAdapter.SupplyRequestAdapter;
import BusinessLayer.ExternalSystems.Supply.WebAdapter.SupplyAdpaterToWeb;

public class SupplyClient {



    private SupplyRequestAdapter proxyAdapter;
    private SupplyAdpaterToWeb webAdapter;

    public SupplyClient(){
        proxyAdapter = new SupplyRequestAdapter();
        webAdapter = new SupplyAdpaterToWeb();
    }


    public void chooseService(){}


    /**
     * Version 3
     */
    public int supply(String buyerName, String address, String city, String country, String zip) throws Exception {
        return webAdapter.supply(buyerName, address, city, country, zip);
    }

    public boolean cancelSupply(int transactionId) throws Exception {
        return webAdapter.cancelSupply(transactionId);
    }

}
