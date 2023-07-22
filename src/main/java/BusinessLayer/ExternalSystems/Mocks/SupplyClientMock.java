package BusinessLayer.ExternalSystems.Mocks;

import BusinessLayer.ExternalSystems.Supply.SupplyClient;

public class SupplyClientMock extends SupplyClient {

    private boolean wantedAnswer;

    //_wantedAnswer is the answer we want to be returned in supply
    public SupplyClientMock(boolean _wantedAnswer){
        wantedAnswer = _wantedAnswer;
    }


    public int supply(String buyerName, String address, String city, String country, String zip) throws Exception {
        if(wantedAnswer)
            return 10005;
        return -1;
    }

    public boolean cancelSupply(int transactionId) throws Exception {
        return wantedAnswer;
    }
}
