package BusinessLayer;

public interface ItemCollectionI<T> {

    T get(int ownerId, int id);

    void add(int id, T item);

    boolean delete(int ownerId, int id);

    boolean update(int ownerId, int id, T item);

    boolean exists(int ownId, int id);

}
