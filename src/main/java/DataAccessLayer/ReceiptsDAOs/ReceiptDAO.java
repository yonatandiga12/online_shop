package DataAccessLayer.ReceiptsDAOs;

import BusinessLayer.Market;
import BusinessLayer.Receipts.Pairs.ItemsPair;
import BusinessLayer.Receipts.Receipt.Receipt;
import BusinessLayer.Receipts.ReceiptItem.ReceiptItem;
import BusinessLayer.Users.Guest;
import BusinessLayer.Users.RegisteredUser;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

public class ReceiptDAO {
    ConnectorConfigurations config;

    public ReceiptDAO() throws Exception {
        config = Market.getConfigurations();
    }

    private DBConnector<Receipt> receiptDBConnector() {
        return new DBConnector<>(Receipt.class, config);
    }

    private DBConnector<ItemsPair> itemsPairDBConnector() {
        try {
            return new DBConnector<>(ItemsPair.class, config);
        }
        catch (Exception e) {
            return null;
        }
    }

    private DBConnector<ReceiptItem> receiptItemDBConnector() {
        try {
            return new DBConnector<>(ReceiptItem.class, config);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void addItems(Receipt receipt, ItemsPair pair, ReceiptItem item, boolean newPair) {
        if(newPair){
            itemsPairDBConnector().insert(pair);
        }

        receiptItemDBConnector().insert(item);

        itemsPairDBConnector().saveState(pair);
        receiptDBConnector().saveState(receipt);
    }

    public void addItemPair(Receipt receipt, ItemsPair pair){
        itemsPairDBConnector().insert(pair);
        receiptDBConnector().saveState(receipt);
    }

    public void addItemToPAir(ItemsPair pair, ReceiptItem item){
        receiptItemDBConnector().insert(item);
        itemsPairDBConnector().saveState(pair);
    }

    public void deleteItem(ReceiptItem item) {
        receiptItemDBConnector().delete(item.getId());
    }

    public void update(Receipt receipt, ItemsPair pair, ReceiptItem toDelete, ReceiptItem toInsert) throws Exception {
        deleteItem(toDelete);
        addItems(receipt, pair, toInsert, false);
    }

}
