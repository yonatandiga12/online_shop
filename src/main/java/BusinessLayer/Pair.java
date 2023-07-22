package BusinessLayer;

import java.util.List;

/**
 * this interface is supposed to replace the use of Map,
 * providing the same key-value services yet meant to be
 * a part of a list, in order to better fit the project
 * to work with hibernate.
 */
public interface Pair<Key, Value> {

    Key getKey();

    Value getValue();

    void setKey(Key k);

    void setValue(Value v);

    static <Key, Value> Pair<Key, Value> searchPair(List<? extends Pair<Key, Value>> list, Key toSearch){
        for(Pair<Key, Value> pair : list){
            if(pair.getKey() == toSearch){
                return pair;
            }
            if(pair.getKey().equals(toSearch)){
                return pair;
            }
        }

        return null;
    }

}
