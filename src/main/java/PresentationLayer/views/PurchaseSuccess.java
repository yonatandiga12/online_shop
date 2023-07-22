package PresentationLayer.views;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;

public interface PurchaseSuccess {
    public void purchase(PurchaseInfo p, SupplyInfo s);
}
