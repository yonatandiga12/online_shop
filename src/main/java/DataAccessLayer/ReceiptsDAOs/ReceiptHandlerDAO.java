package DataAccessLayer.ReceiptsDAOs;

import BusinessLayer.Market;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptHandler;
import BusinessLayer.StorePermissions.StoreManager;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

public class ReceiptHandlerDAO {
    ConnectorConfigurations config;

    public ReceiptHandlerDAO() throws Exception {
        config = Market.getConfigurations();

    }

    private DBConnector<Receipt> receiptDBConnector() {
        return new DBConnector<>(Receipt.class, config);
    }

    private DBConnector<ReceiptHandler> receiptHandlerDBConnector() {
        return new DBConnector<>(ReceiptHandler.class, config);
    }

    public void addReceipt(ReceiptHandler handler, Receipt receipt) {
        receiptDBConnector().insert(receipt);
        receiptHandlerDBConnector().saveState(handler);
    }

}
