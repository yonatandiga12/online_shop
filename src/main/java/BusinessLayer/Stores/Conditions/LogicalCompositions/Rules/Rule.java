package BusinessLayer.Stores.Conditions.LogicalCompositions.Rules;

import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComponent;
import BusinessLayer.Stores.Store;

public abstract class Rule extends LogicalComponent
{
    public Rule(int id, Store store)
    {
        super(id, store);
    }
}
