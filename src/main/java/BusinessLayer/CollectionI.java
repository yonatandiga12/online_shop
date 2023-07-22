package BusinessLayer;

public interface CollectionI<T> {

    T get(int id);

    void add(int id, T item);

    boolean delete(int id);

    boolean update(int id, T item);

    boolean exists(int id);

}
