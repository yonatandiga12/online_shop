package BusinessLayer.NotificationSystem.Observer;


public interface NotificationObserver {

    void notify(String notification);

    void listenToNotifications(int userId) throws Exception;

}
