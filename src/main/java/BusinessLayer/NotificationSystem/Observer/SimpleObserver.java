package BusinessLayer.NotificationSystem.Observer;

import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Observer.NotificationObserver;

public class SimpleObserver implements NotificationObserver {
    private String givenNotification;

    public SimpleObserver(){
        givenNotification = null;
    }

    @Override
    public void notify(String notification) {
        System.out.println("The notification is: " + notification);
        givenNotification = notification;
    }

    @Override
    public void listenToNotifications(int userId) throws Exception {
        Market.getInstance().getUserFacade().listenToNotifications(userId, this);
    }

    public String getGivenNotification(){
        return givenNotification;
    }
}


/*
    1) make all windows implement NotificationObserver

    2) listenToNotifications should be:

    public void listenToNotifications(userId){
        userService.listenToNotifications(userId, this);
    }


    3) notify function should be:

    public void notify(String notification){
        Notification notification = Notification
            .show(notification);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }
 */
