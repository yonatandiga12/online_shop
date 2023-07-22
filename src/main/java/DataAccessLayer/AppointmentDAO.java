package DataAccessLayer;

import BusinessLayer.Market;
import BusinessLayer.Stores.Appointment;
import BusinessLayer.Stores.Appointmentid;
import BusinessLayer.Stores.CatalogItem;
import BusinessLayer.Stores.Store;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;

import javax.persistence.EntityManager;
import java.util.*;

//DB mock
public class AppointmentDAO {
    ConnectorConfigurations config;
    public AppointmentDAO() {
        config = Market.getConfigurations();
    }
    public DBConnector<Appointment> getConnector() {
        return new DBConnector<>(Appointment.class, config);
    }


    public void addAppointment(Appointment appointment) {
        getConnector().insert(appointment);
    }

    public void removeAppointment(Appointment appointment) throws Exception {
//        Appointmentid key = new Appointmentid();
//        key.setCreatorId(appointment.getCreatorId()); // Set the values of the composite key
//        key.setStoreId(appointment.getStoreId());
//        key.setNewOwnerId(appointment.getNewOwnerId());
        DBConnector<Appointment> connector = getConnector();
//        Appointment toRemove = connector.getManager().find(Appointment.class, key);
        EntityManager m = connector.getManager();
        Appointment toRemove = m.merge(appointment);
        m.remove(toRemove);
    }

    public Set<Appointment> getAppointments() {
        List<Appointment> appointments = getConnector().getAll();
        return new HashSet<>(appointments);
    }

    public void save(Appointment appointment) {
        getConnector().saveState(appointment);
    }
}
