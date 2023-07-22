package BusinessLayer.ExternalSystems.Supply.WebAdapter;

import BusinessLayer.ExternalSystems.URLRequest;

import java.util.HashMap;
import java.util.Map;

public class SupplyWebService extends URLRequest {



    public int supply(String buyerName, String address, String city, String country, String zip) throws Exception {
        Map<String, String> postContent = new HashMap<>();
        postContent.put("action_type", "supply");
        postContent.put("name", buyerName);
        postContent.put("address", address);
        postContent.put("city", city);
        postContent.put("country", country);
        postContent.put("zip", zip);

        StringBuilder query = setQuery(postContent);
        String response = getUrlResponse(query);

        if(response.equals(""))
            return -1;
        return Integer.parseInt(response);
    }

    public boolean cancelSupply(int transactionId) throws Exception {

        Map<String, String> postContent = new HashMap<>();
        postContent.put("action_type", "cancel_supply");
        postContent.put("transaction_id",String.valueOf(transactionId));

        StringBuilder query = setQuery(postContent);
        String response = getUrlResponse(query);

        if(response.equals(""))
            return false;

        return !response.equals("-1");
    }
}
