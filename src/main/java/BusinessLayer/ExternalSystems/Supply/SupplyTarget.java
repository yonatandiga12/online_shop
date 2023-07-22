package BusinessLayer.ExternalSystems.Supply;

public interface SupplyTarget {


    int supply(String buyerName, String address, String city, String country, String zip) throws Exception;

    boolean cancelSupply(int transactionId) throws Exception;

}
