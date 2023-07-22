package BusinessLayer.ExternalSystems.Purchase.WebAdpater;
import BusinessLayer.ExternalSystems.URLRequest;
import java.util.HashMap;
import java.util.Map;

public class PaymentWebService extends URLRequest{

    private boolean handShaked = false;


    public boolean handShake() throws Exception {
        Map<String, String> postContent = new HashMap<>();
        postContent.put("action_type", "handshake");  // Example action_type: handshake

        StringBuilder query = setQuery(postContent);
        String response = getUrlResponse(query);
        if(response.equals("-1")) {
            return false;
        }
        else{
            handShaked = response.equals("OK");
            return handShaked;
        }
    }


    public int pay(String cardNumber, int month, int year, String buyerName, int ccv, int buyerId) throws Exception {
        if(!handShaked)
            return -1;
        Map<String, String> postContent = new HashMap<>();
        postContent.put("action_type", "pay");
        postContent.put("card_number", cardNumber);
        postContent.put("month", String.valueOf(month));
        postContent.put("year", String.valueOf(year));
        postContent.put("holder", String.valueOf(buyerName));
        postContent.put("ccv", String.valueOf(ccv));
        postContent.put("id", String.valueOf(buyerId));

        StringBuilder query = setQuery(postContent);
        String response = getUrlResponse(query);

        if(response.equals("")|| response.equals("unexpected-output"))
            return -1;
        return Integer.parseInt(response);
    }

    public boolean cancelPay(int transactionId) throws Exception {
        if(!handShaked)
            return false;

        Map<String, String> postContent = new HashMap<>();
        postContent.put("action_type", "cancel_pay");
        postContent.put("transaction_id",String.valueOf(transactionId));

        StringBuilder query = setQuery(postContent);
        String response = getUrlResponse(query);

        if(response.equals(""))
            return false;

        return !response.equals("-1");
    }


}
