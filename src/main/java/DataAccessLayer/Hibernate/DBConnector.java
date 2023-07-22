package DataAccessLayer.Hibernate;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBConnector<T>{

    private final Class<T> clazz;
    private EntityManager manager;
    private ConnectorConfigurations configurations;
    private Map<String, String> persistenceMap = new HashMap<String, String>();

    private EntityManagerFactory factory;
    public DBConnector(Class<T> c, ConnectorConfigurations conf){
        clazz = c;
        manager = null;
        configurations = conf;

        persistenceMap.put("javax.persistence.jdbc.url", configurations.getUrl());
        persistenceMap.put("javax.persistence.jdbc.user", configurations.getUsername());
        persistenceMap.put("javax.persistence.jdbc.password", configurations.getPassword());
        persistenceMap.put("javax.persistence.jdbc.driver", configurations.getDriver());

        factory = null;

    }

    /**
     * start should be called before each non-basic query.
     */
    public void start(){
        factory = Persistence.createEntityManagerFactory(configurations.getUnitName(), persistenceMap);
        manager = factory.createEntityManager();
    }

    public EntityManager getManager(){
        start();
        return manager;
    }


    /** Basic Query **/
    public void insert(T object){
        start();

        manager.getTransaction().begin();

        manager.persist(object);

        manager.getTransaction().commit();

        manager.close();
        factory.close();
    }

    /** Basic Query **/
    public T getById(int _id){
        start();

        String stringQuery = "SELECT x " +
                             "FROM " + clazz.getName() + " x " +
                             "WHERE x.id = :_id";

        TypedQuery<T> query = manager.createQuery(stringQuery, clazz);
        query.setParameter("_id", _id);

        T object = singleValueQuery(query);

        manager.close();
        factory.close();
        return object;
    }

    /** Basic Query **/
    public List<T> getAll(){
        start();

        String queryString = "SELECT x " +
                             "FROM " + clazz.getName() + " x ";

        TypedQuery<T> query = manager.createQuery(queryString, clazz);

        List<T> all = manyValuesQuery(query);

        manager.close();
        factory.close();
        return all;
    }

    /** Basic Query
     * @param attributeName the name of the attribute in the Class
     */
    public void update(int _id, int newValue, String attributeName){
        start();

        String queryString = "UPDATE " + clazz.getName() + " x " +
                             "SET x." + attributeName + " = :newValue " +
                             "WHERE x.id = :_id";

        Query query = manager.createQuery(queryString);
        query.setParameter("newValue", newValue);
        query.setParameter("_id", _id);

        noValueQuery(query);

        manager.close();
        factory.close();
    }

    /** Basic Query
     * @param attributeName the name of the attribute in the Class
     */
    public void update(int _id, String newValue, String attributeName){
        start();

        String queryString = "UPDATE " + clazz.getName() + " x " +
                             "SET x." + attributeName + " = :newValue " +
                             "WHERE x.id = :_id";

        Query query = manager.createQuery(queryString);
        query.setParameter("newValue", newValue);
        query.setParameter("_id", _id);

        noValueQuery(query);

        manager.close();
        factory.close();
    }

    /** Basic Query **/
    public void delete(int _id){
        start();

        String queryString = "DELETE FROM " + clazz.getName() + " x " +
                             "WHERE x.id = :_id";

        Query query = manager.createQuery(queryString);
        query.setParameter("_id", _id);

        noValueQuery(query);

        manager.close();
        factory.close();
    }

    /** Basic Query **/
    public void emptyTable(){
        start();

        String queryString = "DELETE FROM " + clazz.getName() + " x";

        Query query = manager.createQuery(queryString);

        noValueQuery(query);

        manager.close();
        factory.close();
    }

    //send here an object after an update
    public void saveState(T updatedObject){
        start();

        EntityTransaction et = null;

        try {
            // Get transaction and start
            et = manager.getTransaction();
            et.begin();

            // Save the customer object
            manager.merge(updatedObject);
            et.commit();
        }
        catch (Exception ex) {
            // If there is an exception rollback changes
            if (et != null) {
                et.rollback();
            }
            ex.printStackTrace();
        }
        finally {
            // Close EntityManager
            manager.close();
            factory.close();
        }
    }

    // General Queries
    /**
     * @param query a query for a single value
     * @return the requested value
     */
    public T singleValueQuery(TypedQuery<T> query){
        T object = null;

        try{
            object = query.getSingleResult();
        }
        catch(NoResultException e){
            System.out.println("\nERROR!");
            System.out.println(e.getMessage());
            e.getCause();
            e.printStackTrace();
        }
        finally{
            manager.close();
            factory.close();
        }

        return object;
    }

    /**
     * @param query a query for a list of values
     * @return the list of values in the db
     */
    public List<T> manyValuesQuery(TypedQuery<T> query){
        List<T> list = null;

        try{
            list = query.getResultList();
        }
        catch(NoResultException e){
            System.out.println("\nERROR!");
            System.out.println(e.getMessage());
        }
        finally{
            manager.close();
            factory.close();
        }

        return list;
    }

    /**
     * @param query a query that does not expect return value
     */
    public void noValueQuery(Query query){
        EntityTransaction et = null;

        try{
            et = manager.getTransaction();
            et.begin();
            query.executeUpdate();
            et.commit();
        }
        catch(NoResultException e){
            System.out.println("\nERROR!");
            System.out.println(e.getMessage());
        }
        finally{
            manager.close();
            factory.close();
        }
    }

    /**
     * @param query a query that does not expect return value
     */
    public void noValueQuery(String queryString){
        start();
        Query query = manager.createQuery(queryString);
        noValueQuery(query);
    }

}
