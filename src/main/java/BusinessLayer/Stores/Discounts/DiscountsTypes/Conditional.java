package BusinessLayer.Stores.Discounts.DiscountsTypes;

import BusinessLayer.CartAndBasket.CartItemInfo;
import BusinessLayer.CartAndBasket.Coupon;
import BusinessLayer.Stores.Conditions.LogicalCompositions.*;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.BasketTotalPriceRule;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.MustItemsAmountsRule;
import BusinessLayer.Stores.Conditions.LogicalCompositions.Rules.Rule;
import BusinessLayer.Stores.Discounts.DiscountScopes.DiscountScope;
import BusinessLayer.Stores.Store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


//@Entity
public class Conditional extends DiscountType {
    //    @Transient
    private LogicalComponent root;
    private boolean finished;
    //    @Transient
    private List<LogicalComponent> inProgressList;
    private int logicalComponentsIDsCounter;
    //    @ManyToOne
//    @JoinColumn(name = "storeId")
    private Store store;

    public Conditional(int discountID, double percent, Calendar endOfSale, DiscountScope discountScope, Store store) {
        super(discountID, percent, endOfSale, discountScope);
        this.root = null;
        finished = false;
        inProgressList = new ArrayList<>();
        logicalComponentsIDsCounter = 1;
        this.store = store;
    }

    protected boolean checkConditions(List<CartItemInfo> basketItems, List<Coupon> coupons) {
        return finished && root.checkConditions(basketItems, -1);
    }

    @Override
    public String toString() {
        return super.toString() + ", the condition is: " + (root == null ? "" : root.toString());
    }

    private String getConditionString(LogicalComponent logicalComponent) {
        return logicalComponent.getID() + ": " + logicalComponent;
    }

    public String addBasketTotalPriceRule(double minimumPrice) throws Exception {
        return addRule(new BasketTotalPriceRule(minimumPrice, logicalComponentsIDsCounter++, store));
    }

    public String addQuantityRule(Map<Integer, Integer> itemsAmounts) {
        return addRule(new MustItemsAmountsRule(itemsAmounts, logicalComponentsIDsCounter++, store));
    }

    public String addComposite(LogicalComposites logicalComposite, List<Integer> logicalComponentsIDs) throws Exception {
        List<LogicalComponent> logicalComponents = new ArrayList<>();
        for (Integer logicalComponentID : logicalComponentsIDs) {
            LogicalComponent logicalComponent = getLogicalComponentByIDFromInProgressList(logicalComponentID);
            if (logicalComponent == null) {
                throw new Exception("Can't find logical component with id: " + logicalComponentID + " in discount of id: " + getDiscountID());
            }
            logicalComponents.add(logicalComponent);
        }
        switch (logicalComposite) {
            case AND: {
                LogicalComposite and = new And(logicalComponents, logicalComponentsIDsCounter++, store);
                removeLogicalComponentsFromInProgressList(logicalComponentsIDs);
                inProgressList.add(and);
                return getConditionString(and);
            }
            case OR: {
                LogicalComposite or = new Or(logicalComponents, logicalComponentsIDsCounter++, store);
                removeLogicalComponentsFromInProgressList(logicalComponentsIDs);
                inProgressList.add(or);
                return getConditionString(or);
            }
            case CONDITIONING: {
                return "We are currently not support conditioning in conditional discounts";
            }
        }
        return "Unrecognized logical composite type";
    }

    public String finish() throws Exception {
        if (inProgressList.size() == 0) {
            throw new Exception("There is no logical component to give to the conditional discount, please create one and try again");
        } else if (inProgressList.size() > 1) {
            throw new Exception("There are at least 2 logical components left, please remove one of them or combine them and try again");
        } else {
            root = inProgressList.get(0);
            inProgressList.remove(0);
            finished = true;
            return toString();
        }
    }


    private String addRule(Rule newRule) {
        inProgressList.add(newRule);
        return getConditionString(newRule);
    }

    private void removeLogicalComponentsFromInProgressList(List<Integer> logicalComponentsIDs) {
        inProgressList.removeIf(x -> logicalComponentsIDs.contains(x.getID()));
    }

    private LogicalComponent getLogicalComponentByIDFromInProgressList(Integer logicalComponentID) {
        for (LogicalComponent logicalComponent : inProgressList) {
            if (logicalComponent.getID() == logicalComponentID.intValue()) {
                return logicalComponent;
            }
        }
        return null;
    }

    @Override
    public void removeItem(int itemID) {
        super.removeItem(itemID);
        root.removeItem(itemID);
    }

    public LogicalComponent getRoot() {
        return root;
    }

    public void setRoot(LogicalComponent root) {
        this.root = root;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<LogicalComponent> getInProgressList() {
        return inProgressList;
    }

    public void setInProgressList(List<LogicalComponent> inProgressList) {
        this.inProgressList = inProgressList;
    }

    public int getLogicalComponentsIDsCounter() {
        return logicalComponentsIDsCounter;
    }

    public void setLogicalComponentsIDsCounter(int logicalComponentsIDsCounter) {
        this.logicalComponentsIDsCounter = logicalComponentsIDsCounter;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public void setStore(Store store) {
        this.store = store;
    }
}
