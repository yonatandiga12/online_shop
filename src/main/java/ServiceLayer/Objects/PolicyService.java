package ServiceLayer.Objects;

import BusinessLayer.Stores.Policies.DiscountPolicy;
import BusinessLayer.Stores.Policies.PurchasePolicy;

public class PolicyService {

    private int policyId;

    private String info;

    public int getPolicyId() {
        return policyId;
    }

    public String getInfo() {
        return info;
    }

    public PolicyService(PurchasePolicy purchasePolicy, int id){
        this.policyId = id;
        this.info = purchasePolicy.toString();
    }
    public PolicyService(DiscountPolicy discountPolicy, int id){
        this.policyId = id;
        this.info = discountPolicy.toString();
    }
}
