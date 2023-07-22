package PresentationLayer.views;


import BusinessLayer.NotificationSystem.Observer.NotificationObserver;
import PresentationLayer.components.appnav.AppNav;
import PresentationLayer.components.appnav.AppNavItem;
import PresentationLayer.views.clients.ClientView;
import PresentationLayer.views.loginAndRegister.LoginAndRegisterView;
import PresentationLayer.views.loginAndRegister.UserPL;
import PresentationLayer.views.storeManagement.StoreManagementView;
import PresentationLayer.views.systemManagement.SystemManagementView;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.HttpSession;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.HashMap;
import java.util.Map;

import static org.vaadin.lineawesome.LineAwesomeIcon.*;

/**
 * The main view is a top-level placeholder for other views.
 */
@PageTitle("Main")
@Route(value = "main")
public class MainLayout extends AppLayout implements NotificationObserver, BeforeEnterObserver {

    private static Map<String, UserPL> currUsers = new HashMap<>();
    public UserService userService;
    public ShoppingService shoppingService;
    UI ui;
    private H2 viewTitle;
    private H2 user;
    private AppNavItem loginAndRegister;
    private AppNavItem systemAdmin;
    private AppNavItem bidview;
    private AppNavItem marketOwnerOrManager;

    private Button logoutBtn;
    private AppNavItem mailboxButton;

    public MainLayout() {
        ui = UI.getCurrent();
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        try {
            userService = new UserService();
        } catch (Exception e) {
            printError("Error initialize userService:\n" + e.getMessage());
            throw new RuntimeException("Error initialize userService:\n" + e.getMessage());
        }
    }

    public static MainLayout getMainLayout() {
        MainLayout mainLayout = (MainLayout) UI.getCurrent().getChildren().filter(component -> component.getClass() == MainLayout.class).findFirst().orElse(new MainLayout());
        return mainLayout;

    }

    public void setCurrUser(Integer value) {
        currUsers.get(getSessionID()).setCurrUserID(value);
        try {
            listenToNotifications(currUsers.get(getSessionID()).getCurrUserID());
        } catch (Exception e) {
            throw new RuntimeException("ERROR: MainLayout::MainLayout: " + e.getMessage() + "\n");
        }
    }

    public Integer getCurrUserID() {
        return currUsers.get(getSessionID()).getCurrUserID();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        user = new H2(getUserName());
        user.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        user.getStyle().set("margin-left", "auto");
        user.getStyle().set("padding", "15px");


        HorizontalLayout nav = new HorizontalLayout();
        mailboxButton = new AppNavItem("", Mailbox.class, ENVELOPE.create());
        nav.add(mailboxButton);
        nav.add(new AppNavItem("My Cart", Cart.class, SHOPPING_CART_SOLID.create()));
        nav.getStyle().set("margin-left", "auto");
        nav.getStyle().set("padding", "15px");

        addToNavbar(true, toggle, viewTitle, user, nav);
    }

    private String getUserName() {
        String username = userService.getUsername(getCurrUserID());
        return username != null ? "Welcome, " + username + "!" : "Guest";
    }

    private void addDrawerContent() {
        H1 appName = new H1("Shefa Isaschar");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();

        loginAndRegister = new AppNavItem("Login/Register", LoginAndRegisterView.class, LineAwesomeIcon.PERSON_BOOTH_SOLID.create());
        marketOwnerOrManager = new AppNavItem("Store Management", StoreManagementView.class, LineAwesomeIcon.TRUCK_LOADING_SOLID.create());
        systemAdmin = new AppNavItem("System Management", SystemManagementView.class, LineAwesomeIcon.WRENCH_SOLID.create());
        bidview = new AppNavItem("My Bids", BidView.class, MONEY_BILL_WAVE_SOLID.create());
        nav.addItem(new AppNavItem("Explore Market", ClientView.class, SHOPPING_CART_SOLID.create()));
        nav.addItem(loginAndRegister);
        nav.addItem(marketOwnerOrManager);
        nav.addItem(bidview);
        nav.addItem(systemAdmin);
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        logoutBtn = new Button("Logout", SIGN_OUT_ALT_SOLID.create());
        logoutBtn.addClickListener(e -> logoutAction());
        layout.add(logoutBtn);
        return layout;
    }

    private void logoutAction() {
        /**
         1.default is guest
         2.on login change to the according RegisterUser + load his data if needed
         3.on logout change the login of the RegisterUser to false, change the User to guest again.
         */
        Result<Boolean> result = userService.logout(getCurrUserID());
        if (result.isError()) {
            printError("Failed to logout: " + result.getMessage());
        } else {
            printSuccess("Succeed to logout currId=" + getCurrUserID());
            setCurrUser(getCurrUserID());
            setGuestView();
            currUsers.get(getSessionID()).setCurrIdToGuest();
            user.setText(getUserName());
            UI.getCurrent().navigate(LoginAndRegisterView.class);
        }
    }

    public void setGuestView() {
        logoutBtn.setVisible(false);
        systemAdmin.setVisible(false);
        marketOwnerOrManager.setVisible(false);
        loginAndRegister.setVisible(true);
        bidview.setVisible(false);
    }

    public void setUserView() {
        logoutBtn.setVisible(true);
        systemAdmin.setVisible(userService.isAdmin(getCurrUserID()));
        marketOwnerOrManager.setVisible(true);
        loginAndRegister.setVisible(false);
        bidview.setVisible(true);
        user.setText(getUserName());
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        if (viewTitle == null)
            viewTitle = new H2();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void notify(String notification) {
        ui.access(() -> {
            Notification systemNotification = Notification
                    .show(notification);
            systemNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        });
    }

    @Override
    public void listenToNotifications(int userId) {
        Result result = userService.listenToNotifications(userId, this);
        if (result.isError()) {
            printError(result.getMessage());
        }
    }

    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        HttpSession session = getSession();
        boolean isNewTab = (session.getAttribute("isNewTab") == null);

        if (isNewTab || user == null) {
            // Handle new tab event here
            try {
                int id = getCurrUserID();
                userService.logout(id);
            }
            catch (NullPointerException e) {
            }
            Result<Integer> res = userService.addGuest();
            UserPL userPL = new UserPL();
            if(res.getValue()!=null)
                userPL.setCurrUserID(res.getValue());
            currUsers.put(session.getId(), userPL);
            // Set the session attribute to indicate that the function has been called
            session.setAttribute("isNewTab", true);
            addHeaderContent();
            setGuestView();
            setCurrUser(userPL.getCurrUserID());
        }

    }

    private String getSessionID() {
        return getSession().getId();
    }

    private HttpSession getSession() {
        VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();
        return request.getHttpServletRequest().getSession();
    }
}
