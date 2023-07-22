package BusinessLayer.Receipts.Receipt;

import BusinessLayer.CollectionI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Collection of receipts, used by receipt handler
public class ReceiptCollection implements CollectionI<Receipt> {


    //OwnerId will be used for the db.
    private ArrayList<Receipt> receiptList;

    public ReceiptCollection(){
        receiptList = new ArrayList<>();
    }

    /**
     *
     * @param receiptId id of the receipt
     * @return the receipt
     */
    @Override
    public Receipt get(int receiptId) {
        for(Receipt receipt: receiptList){
            if(receipt.getId() == receiptId){
                return receipt;
            }
        }
        return null;
    }

    /**
     * @param ownerId id of user\store, where the receipt is saved:
     *               If its collection in store the id will be of the user
     *               If its collection in user the id will be of the store
     *                (will be used in DB)
     * @param obj  the receipt to add
     */
    @Override
    public void add(int ownerId, Receipt obj) {
        receiptList.add(obj);
    }

    /**
     *
     * @param id of the receipt id we want to delete
     * @return true if successful
     */
    @Override
    public boolean delete(int id) {
        for(Receipt receipt: receiptList){
            if(receipt.getId() == id){
                receiptList.remove(receipt);
                return true;
            }
        }
        return false;
    }

    /**
     * @param id of the receipt id we want to delete
     * @param obj the receipt to update
     * @return true if successful
     */
    @Override
    public boolean update(int id, Receipt obj) {
        for(Receipt receipt: receiptList){
            if(receipt.getId() == id){
                receiptList.remove(receipt);
                receiptList.add(obj);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exists(int id) {
        for(Receipt receipt: receiptList){
            if(receipt.getId() == id){
                return true;
            }
        }
        return false;
    }


    public ArrayList<Receipt> getByOwnerId(int ownerId) {
        ArrayList<Receipt> result = new ArrayList<>();
        for(Receipt receipt : receiptList){
            if(receipt.hasKeyId(ownerId))
                result.add(receipt);
        }
        return result;
    }

    public ArrayList<Receipt> getAll() {
        return receiptList;
    }
}
