package PresentationLayer.views.storeManagement;

import BusinessLayer.StorePermissions.StoreActionPermissions;
import BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites;
import BusinessLayer.Stores.Conditions.NumericCompositions.NumericComposites;
import PresentationLayer.views.MainLayout;
import PresentationLayer.views.StoreMailbox;
import ServiceLayer.Objects.*;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import org.apache.commons.lang3.text.WordUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@PageTitle("About")
@Route(value = "stores", layout = MainLayout.class)
@PreserveOnRefresh
public class StoreManagementView extends VerticalLayout {


    final int ITEMS = 0;
    final int CATEGORY = 1;
    final int STORE = 2;
    ShoppingService shoppingService;
    UserService userService;
    Select<String> storeSelectorAdd;
    Select<String> storeSelectorRemoveManager;
    Select<String> storeSelectorRemoveOwner;
    Grid<UserInfoService> userGrid;
    Grid<UserInfoService> ownersIDefinedGrid;
    Grid<UserInfoService> managersIDefinedGrid;
    Grid<Map.Entry<Integer, Integer>> managerInfoGrid;
    Grid<StoreService> storesIOwnGrid;
    Grid<StoreService> storesManagedGrid;
    Grid<BidService> bidsGrid;


    //uncomment
    boolean isStoreOwner;
    boolean isStoreManager;
    int PURCHASE_POLICY = 1;
    int DISCOUNT_POLICY = 2;
    private Map<Integer, UserInfoService> users;
    private Map<Integer, StoreService> storesIOwn;
    private Map<Integer, StoreService> storesIManage;
    private MainLayout mainLayout;
    private Tab userTab;
    private Paragraph storesParagraphInUserTab;

    public StoreManagementView() {
        setSpacing(false);
        mainLayout = MainLayout.getMainLayout();
        H2 header = new H2("Store Owner/Manager View");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        add(header);

        try {
            shoppingService = new ShoppingService();
            userService = new UserService();
        } catch (Exception e) {
            add("Problem initiating Store:(");
        }
        setSpacing(false);

        Result<Map<Integer, UserInfoService>> usersRes = userService.getAllRegisteredUsers();
        Result<Map<Integer, StoreService>> storesIOwnRes = shoppingService.getStoresIOwn(mainLayout.getCurrUserID());
        Result<Map<Integer, StoreService>> storesIManageRes = shoppingService.getStoresIManage(mainLayout.getCurrUserID());


        if (usersRes.isError() || storesIOwnRes.isError()) {
            printError("Problem occurred:\n" + (usersRes.isError() ? usersRes.getMessage() : storesIOwnRes.getMessage()));
        } else {
            isStoreOwner(mainLayout.getCurrUserID(), usersRes);
            isStoreManager(mainLayout.getCurrUserID(), usersRes);

            TabSheet mainTabSheet = new TabSheet();
            users = usersRes.getValue();
            storesIOwn = storesIOwnRes.getValue();
            storesIManage = storesIManageRes.getValue();
            refreshStoreList();
            updateStoresGrid();

            Div storesDiv = new Div();
            Div usersDiv = new Div();
            Accordion accordion = new Accordion();
            createUserGrid(accordion);
            createOwnersGrid(accordion);
            createManagersGrid(accordion);
            createManagerInfoGrid(accordion);
            usersDiv.add(accordion);
            mainTabSheet.setSizeFull();
            createStoresGrid(storesDiv);
            userTab = new Tab("Users");
            userTab.setEnabled(isStoreOwner());

            mainTabSheet.add("Stores", storesDiv);
            mainTabSheet.add(userTab, usersDiv);
            add(mainTabSheet);
        }
    }

    private void createManagerInfoGrid(Accordion accordion) {
        //Name, Store, List of Permissions
        AccordionPanel managerInfo = new AccordionPanel("View Manager Permissions");

        managerInfoGrid = new Grid();
        refreshManagerInfoGrid();
        managerInfoGrid.addColumn(entry -> storesIOwn.get(entry.getKey()).getStoreName()).setHeader("Store").setSortable(true);
        managerInfoGrid.addColumn(entry -> userService.getUsername(entry.getValue())).setHeader("Manager").setSortable(true);

        for (String permission : shoppingService.possibleManagerPermissions()) {
            managerInfoGrid.addComponentColumn(entry -> shoppingService.managerHasPermission(entry.getValue(), entry.getKey(),
                            StoreActionPermissions.valueOf(permission.replace(' ', '_'))) ?
                            LineAwesomeIcon.CHECK_CIRCLE_SOLID.create() : new Icon(VaadinIcon.BAN))
                    .setHeader(WordUtils.capitalizeFully(permission)).setSortable(true);
        }
        managerInfo.addContent(managerInfoGrid);
        accordion.add(managerInfo);

    }

    private void refreshManagerInfoGrid() {
        storesIOwn = shoppingService.getStoresIOwn(mainLayout.getCurrUserID()).getValue();
        Map<Integer, Integer> storeToManagerMap = new HashMap<>();
        for (StoreService store : storesIOwn.values()) {
            for (Integer managerID : store.getManagers().keySet()) {
                storeToManagerMap.put(store.getStoreId(), managerID);
            }
        }
        managerInfoGrid.setItems(storeToManagerMap.entrySet());


        GridContextMenu<Map.Entry<Integer, Integer>> menu = managerInfoGrid.addContextMenu();
        menu.setOpenOnClick(true);
        managerInfoGrid.addSelectionListener(e ->
        {
            if (e.getAllSelectedItems().size() == 0) {
                menu.close();
            }
        });
        menu.addItem("Add Permissions", event -> {editPermissionsDialog(event.getItem().get(), true);});
        menu.addItem("Remove Permissions", event -> {editPermissionsDialog(event.getItem().get(), false);});
    }

    private void editPermissionsDialog(Map.Entry<Integer, Integer> item, boolean addPermission) { //if addPermission == false -> remove!
        int store = item.getKey();
        int manager = item.getValue();

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(addPermission ? "Select Permissions to Add:" : "Select Permissions to Remove:");

        List<String> permissions = shoppingService.getManagerInfo(manager, store);

        if (addPermission) {
            List<String> allPermissions = new ArrayList<>(shoppingService.possibleManagerPermissions());
            allPermissions.removeAll(permissions);
            permissions = allPermissions;
        }
        MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
        listBox.setItems(permissions);
        dialog.add(listBox);

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> printError("Canceled"));

        dialog.setConfirmText(addPermission ? "Add" : "Remove");
        dialog.setConfirmButtonTheme("success primary");
        dialog.addConfirmListener(event -> {
            Result r;
            if (addPermission) {
                r = shoppingService.addManagerPermission(mainLayout.getCurrUserID(), store, manager, listBox.getSelectedItems());
            }
            else {
                r = shoppingService.removeManagerPermission(mainLayout.getCurrUserID(), store, manager, listBox.getSelectedItems());
            }
            if (r.isError()) {
                printError(r.getMessage());
            }
            else {
                printSuccess(addPermission ? "Successfully added permissions" : "Successfully removed permissions");
            }
            refreshManagerInfoGrid();
        });

        add(dialog);
        dialog.open();
    }

    private boolean isStoreOwner(int storeId) {
        Result<Map<Integer, UserInfoService>> usersRes = userService.getAllRegisteredUsers();
        for (UserInfoService userInfoService : usersRes.getValue().values()) {
            if (userInfoService.getId() == mainLayout.getCurrUserID()) {
                ArrayList<Integer> storesIds = userInfoService.getStoresIOwn();
                return storesIds.contains(storeId);
            }
        }
        return false;
    }

    private boolean isStoreOwner() {
        Result<Map<Integer, UserInfoService>> usersRes = userService.getAllRegisteredUsers();
        for (UserInfoService userInfoService : usersRes.getValue().values()) {
            if (userInfoService.getId() == mainLayout.getCurrUserID()) {
                isStoreOwner = userInfoService.getStoresIOwn().size() != 0;
                return isStoreOwner;
            }
        }
        isStoreOwner = false;
        return isStoreOwner;
    }

    private void updateStoresGrid() {
        Result<Map<Integer, UserInfoService>> usersRes = userService.getAllRegisteredUsers();
        Result<Map<Integer, StoreService>> storesIOwnRes = shoppingService.getStoresIOwn(mainLayout.getCurrUserID());
        if (usersRes.isError() || storesIOwnRes.isError()) {
            printError("Problem occurred:\n" + (usersRes.isError() ? usersRes.getMessage() : storesIOwnRes.getMessage()));
        } else {
            if (isStoreOwner) {
                storesIOwn = storesIOwnRes.getValue();
            }
            if (isStoreManager) {
                updateStoresIManage(usersRes.getValue());
            }
        }
    }

    private void updateStoresIManage(Map<Integer, UserInfoService> allUsers) {
        List<Integer> storesIManageLocal = new ArrayList<>();
        for (UserInfoService userInfoService : allUsers.values()) {
            if (userInfoService.getId() == mainLayout.getCurrUserID()) {
                storesIManageLocal = userInfoService.getStoresIManage();
            }
        }
        if (storesIManageLocal != null) {
            Result<Map<Integer, StoreService>> result = shoppingService.getAllStoresInfo();
            if (!result.isError() && result.getValue() != null) {
                for (Map.Entry<Integer, StoreService> entry : result.getValue().entrySet()) {
                    int storeId = entry.getValue().getStoreId();
                    if (storesIManageLocal.contains(storeId)) {
                        storesIManage.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    private void isStoreManager(int ownerId, Result<Map<Integer, UserInfoService>> usersRes) {
        for (UserInfoService userInfoService : usersRes.getValue().values()) {
            if (userInfoService.getId() == ownerId) {
                isStoreManager = userInfoService.getStoresIManage().size() != 0;
                return;
            }
        }
        isStoreManager = false;
    }

    private void isStoreOwner(int ownerId, Result<Map<Integer, UserInfoService>> usersRes) {
        for (UserInfoService userInfoService : usersRes.getValue().values()) {
            if (userInfoService.getId() == ownerId) {
                isStoreOwner = userInfoService.getStoresIOwn().size() != 0;
                return;
            }
        }
        isStoreOwner = false;
//        Result<List<UserInfoService>> result = userService.getAllOwnersIDefined(ownerId);
//        if(result.isError())
//            return false;
//        return result.getValue().size() != 0;
    }

    private void createStoresGrid(Div storesDiv) {
        Accordion accordion = new Accordion();
        AccordionPanel owning = new AccordionPanel("Stores I Own");
        AccordionPanel managing = new AccordionPanel("Stores I Manage");

        Paragraph storeParagraph = new Paragraph("Stores I Own");
        Paragraph helper = new Paragraph("Select the Store you want to edit");
        storeParagraph.getStyle().set("font-size", "40px");
        helper.getStyle().set("font-size", "20px");
        owning.addContent(storeParagraph, helper);

        storesIOwnGrid = new Grid<>();
        storesIOwnGrid.setItems(storesIOwn.values());
        storesIOwnGrid.setAllRowsVisible(true);
        storesIOwnGrid.setWidth("1500px");

        storesIOwnGrid.addColumn(StoreService::getStoreId).setHeader("ID").setSortable(true);
        storesIOwnGrid.addColumn(StoreService::getStoreName).setHeader("Name").setSortable(true);
        storesIOwnGrid.addColumn(StoreService::getStoreStatus).setHeader("Status").setSortable(true);
        addMenuItems(storesIOwnGrid, false);

        owning.addContent(storesIOwnGrid);

        Paragraph storeParagraph2 = new Paragraph("Stores I Manage");
        Paragraph helper2 = new Paragraph("Select the Store you want to edit");
        storeParagraph2.getStyle().set("font-size", "40px");
        helper2.getStyle().set("font-size", "20px");
        managing.addContent(storeParagraph2, helper2);

        storesManagedGrid = new Grid<>();
        storesManagedGrid.setItems(storesIManage.values());

        storesManagedGrid.addColumn(StoreService::getStoreId).setHeader("ID").setSortable(true);
        storesManagedGrid.addColumn(StoreService::getStoreName).setHeader("Name").setSortable(true);
        storesManagedGrid.addColumn(StoreService::getStoreStatus).setHeader("Status").setSortable(true);
        addMenuItems(storesManagedGrid, true);

        managing.addContent(storesManagedGrid);

        accordion.add(owning);
        accordion.add(managing);

        Button createStore = new Button("Create Store", e -> createStoreDialog());
        owning.addContent(createStore);

        Button getBids = new Button("Get Pending Bids", e -> createBidDialog());
        Button getAppointments = new Button("Get Pending Appointments", e -> {
            new AppointmentDialog();
            refreshStoreList();
            updateStoresGrid();
        });

        storesDiv.add(getBids,getAppointments, accordion);
    }



    private void addMenuItems(Grid<StoreService> storesGrid, boolean managerMode) {
        GridContextMenu<StoreService> menu = storesGrid.addContextMenu();
        menu.setOpenOnClick(true);

        storesGrid.addSelectionListener(e ->
        {
            if (e.getAllSelectedItems().size() == 0) {
                menu.close();
            }
        });

        storesGrid.getDataProvider().fetch(new Query<>()).forEach(bean -> {
            menu.removeAll();
            addMenuItems(bean, menu, storesGrid, managerMode);
        });
    }

    private void addMenuItems(StoreService store, GridContextMenu<StoreService> menu, Grid<StoreService> storesGrid, boolean managerMode) {
        menu.addItem("View Items Of Store", event -> {
            viewItemsDialog(storesGrid, managerMode);
        }).setVisible(hasPermission(store, StoreActionPermissions.INVENTORY));
        menu.addItem("View Discounts Of Store", e -> {
            viewDiscountsDialog(storesGrid);
        }).setVisible(hasPermission(store, StoreActionPermissions.DISCOUNT_POLICY));
        menu.addItem("Close Store", event -> {
            closeStoreDialog();
        }).setVisible(!managerMode);  //only store founder
        menu.addItem("Open Store", event -> {
            openStoreDialog();
        }).setVisible(!managerMode);   //only store founder
        menu.addItem("Get Store History", event -> {
            getHistoryDialog(storesGrid);
        }).setVisible(hasPermission(store, StoreActionPermissions.HISTORY));  //Requirement 4.13
        menu.addItem("Get Staff Info", event -> {
            getStaffInfoDialog();
        }).setVisible(!managerMode);  //Requirement 4.11
        menu.addItem("View Store Purchase policies", event -> {
            viewPoliciesDialog(PURCHASE_POLICY, storesGrid);
        }).setVisible(hasPermission(store, StoreActionPermissions.PURCHASE_POLICY));
        menu.addItem("View Store Discount policies", event -> {
            viewPoliciesDialog(DISCOUNT_POLICY, storesGrid);
        }).setVisible(hasPermission(store, StoreActionPermissions.DISCOUNT_POLICY));
        menu.addItem("Store Mailbox", event -> startMailbox()).setVisible(!managerMode);
    }

    private boolean isFounder(StoreService storeService) {
        int userId = mainLayout.getCurrUserID();
        boolean res = storeService.getFounderID() == userId;
        return res;
    }

    private boolean hasPermission(StoreService store, StoreActionPermissions storeActionPermissions) {
        if(isStoreOwner(store.getStoreId()))
            return true;
        int userID = mainLayout.getCurrUserID();
        return shoppingService.managerHasPermission(userID, store.getStoreId(), storeActionPermissions);
    }

    //mark
    private void startMailbox() {
        int storeId = getStoreIdOfSelectedRow(storesIOwnGrid);
        String storeName = shoppingService.getStoreName(storeId);

        StoreMailbox mailbox = new StoreMailbox(storeId, storeName, shoppingService, userService);

        mailbox.makeMailboxDialog();
    }

    private void createStoreDialog() {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Add Item");

        TextField name = new TextField("Store Name");
        VerticalLayout dialogLayout = new VerticalLayout(name);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add Store", e -> {
            dialog.close();
            addStoreAction(name.getValue(), mainLayout.getCurrUserID());
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);


        add(dialog);
        dialog.open();
    }

    private void addStoreAction(String name, int userId) {
        if (name != null) {
            Result<Integer> result = shoppingService.createStore(userId, name);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != -1) {
                    printSuccess("Store added Successfully");
                    updateStoresGrid();
                    refreshStoreFromBusiness(result.getValue());
                    userTab.setEnabled(isStoreOwner());

                    int newStoreId = result.getValue();
                    storesParagraphInUserTab.add(", " + newStoreId + ": " + shoppingService.getStoreName(newStoreId));
                    refreshUserGrids();
                } else {
                    printError("Something went wrong:\n" + result.getMessage());
                }
            }
        }
    }

    private void createUserGrid(Accordion usersDiv) {
        AccordionPanel addOwnerManager = new AccordionPanel("Add Owner or Manager");
        Paragraph userParagraph = new Paragraph("Users available");
        userParagraph.getStyle().set("font-size", "40px");

        storesParagraphInUserTab = new Paragraph("Stores I Own- ");
        storesParagraphInUserTab.add(users.get(mainLayout.getCurrUserID()).getStoreIOwnString());
        storesParagraphInUserTab.getStyle().set("font-size", "30px");

        Paragraph helperParagraph = new Paragraph("Select a User you want to appoint and enter the Store ID in the field below");
        helperParagraph.getStyle().set("font-size", "20px");
        addOwnerManager.addContent(userParagraph, storesParagraphInUserTab, helperParagraph);

        userGrid = new Grid<>();
        Editor<UserInfoService> editor = userGrid.getEditor();
        userGrid.setItems(users.values());
        userGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        userGrid.addColumn(UserInfoService::getUsername).setHeader("Name").setSortable(true);
        Binder<UserInfoService> binder = new Binder<>(UserInfoService.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        HorizontalLayout footer = addButtons();
        addOwnerManager.addContent(userGrid, footer);

        usersDiv.add(addOwnerManager);
    }

    private void createManagersGrid(Accordion usersDiv) {
        AccordionPanel removeManager = new AccordionPanel("Remove Manager");
        Paragraph headerParagraph = new Paragraph("Managers I appointed");
        headerParagraph.getStyle().set("font-size", "40px");
        Paragraph helperParagraph = new Paragraph("Select a User you want to appoint and enter the Store ID in the field below");
        helperParagraph.getStyle().set("font-size", "15px");
        removeManager.addContent(headerParagraph, helperParagraph);

        Result<List<UserInfoService>> managersIDefinedRes = userService.getAllManagersIDefined(mainLayout.getCurrUserID());
        if (managersIDefinedRes.isError()) {
            printError(managersIDefinedRes.getMessage());
        } else {
            managersIDefinedGrid = new Grid<>();
            managersIDefinedGrid.setItems(managersIDefinedRes.getValue());
            managersIDefinedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            managersIDefinedGrid.addColumn(UserInfoService::getUsername).setHeader("Name").setSortable(true);
            managersIDefinedGrid.addColumn(UserInfoService::getStoreIManageString).setHeader("Manager appointed by me to Stores");

            Button removeManagerbutton = new Button("Remove Manager");
            removeManagerbutton.addClickListener(e -> removeManagerAction(Integer.parseInt(storeSelectorRemoveManager.getValue().split(":")[0])));
            removeManager.addContent(managersIDefinedGrid, storeSelectorRemoveManager, removeManagerbutton);
        }
        usersDiv.add(removeManager);
    }

    private void createOwnersGrid(Accordion usersDiv) {
        AccordionPanel removeOwner = new AccordionPanel("Remove Owner");
        Paragraph headerParagraph = new Paragraph("Owners I appointed");
        headerParagraph.getStyle().set("font-size", "40px");
        Paragraph helperParagraph = new Paragraph("Select a User you want to appoint and enter the Store ID in the field below");
        helperParagraph.getStyle().set("font-size", "15px");
        removeOwner.addContent(headerParagraph, helperParagraph);

        Result<List<UserInfoService>> usersIDefinedRes = userService.getAllOwnersIDefined(mainLayout.getCurrUserID());
        if (usersIDefinedRes.isError()) {
            printError(usersIDefinedRes.getMessage());
        } else {
            ownersIDefinedGrid = new Grid<>();
            ownersIDefinedGrid.setItems(usersIDefinedRes.getValue());
            ownersIDefinedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            ownersIDefinedGrid.addColumn(UserInfoService::getUsername).setHeader("Name").setSortable(true);
            ownersIDefinedGrid.addColumn(UserInfoService::getStoreIOwnString).setHeader("Owner appointed by me to Stores");

            Button removeOwnerbutton = new Button("Remove Owner");
            removeOwnerbutton.addClickListener(e -> removeOwnerAction(Integer.parseInt(storeSelectorRemoveOwner.getValue().split(":")[0])));
            removeOwner.addContent(ownersIDefinedGrid, storeSelectorRemoveOwner, removeOwnerbutton);
        }
        usersDiv.add(removeOwner);

    }

    private void refreshStoreList() {
        if (storeSelectorAdd == null) {
            storeSelectorAdd = new Select<>();
            storeSelectorRemoveManager = new Select<>();
            storeSelectorRemoveOwner = new Select<>();

            storeSelectorAdd.setLabel("Store");
            storeSelectorAdd.setHelperText("Select a User  from grid and select the Store from dropdown");

            storeSelectorRemoveManager.setLabel("Store");
            storeSelectorRemoveManager.setHelperText("Select a User  from grid and select the Store from dropdown");

            storeSelectorRemoveOwner.setLabel("Store");
            storeSelectorRemoveOwner.setHelperText("Select a User  from grid and select the Store from dropdown");
        }
        if (storesIOwn.isEmpty())
            return;
        List<String> stores = storesIOwn.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().getStoreName()).collect(Collectors.toList());
        storeSelectorAdd.setItems(stores);
        storeSelectorAdd.setValue(stores.get(0));

        storeSelectorRemoveManager.setItems(stores);
        storeSelectorRemoveManager.setValue(stores.get(0));

        storeSelectorRemoveOwner.setItems(stores);
        storeSelectorRemoveOwner.setValue(stores.get(0));

    }

    private HorizontalLayout addButtons() {
        /*Button addOwnerButton = new Button("Add Owner");
        addOwnerButton.addClickListener(e -> addOwnerAction());*/
        Button addOwnerButton = new Button("Request to Add Owner");
        addOwnerButton.addClickListener(e -> createAppointAction());

        Button addManagerButton = new Button("Add Manager");
        addManagerButton.addClickListener(e -> addManagerAction());

        setPadding(false);
        setAlignItems(Alignment.AUTO);

        HorizontalLayout horizontalLayout1 = new HorizontalLayout(storeSelectorAdd, addOwnerButton, addManagerButton);

        horizontalLayout1.setAlignItems(FlexComponent.Alignment.BASELINE);
        return horizontalLayout1;
    }

    private void createAppointAction() {
        int chosenUserId = getIdOfSelectedRow(userGrid);
        int storeId = Integer.parseInt(storeSelectorAdd.getValue().split(":")[0]);

        if (chosenUserId != -1) {
            Result<Boolean> result =shoppingService.addAppointment(storeId,mainLayout.getCurrUserID(),chosenUserId);
            //userService.addOwner(mainLayout.getCurrUserID(), chosenUserId, storeId);

            if (result.isError()) {
                printError("Error in addAppointment:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Appointment added Successfully");
                    refreshUserGrids();
                    refreshStoreList();
                    updateStoresGrid();
                    refreshManagerInfoGrid();
                } else {
                    printError("Something went wrong");
                }
            }
        }

    }

    private void addOwnerAction() {
        int chosenUserId = getIdOfSelectedRow(userGrid);
        int storeId = Integer.parseInt(storeSelectorAdd.getValue().split(":")[0]);

        if (chosenUserId != -1) {
            Result<Boolean> result = userService.addOwner(mainLayout.getCurrUserID(), chosenUserId, storeId);

            if (result.isError()) {
                printError("Error in add Owner:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Owner added Successfully");
                    refreshUserGrids();
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }

    private void removeOwnerAction(int storeId) {
        int chosenUserId = getIdOfSelectedRow(ownersIDefinedGrid);

        if (chosenUserId != -1) {
            Result<Boolean> result = userService.removeOwner(mainLayout.getCurrUserID(), chosenUserId, storeId);

            if (result.isError()) {
                printError("Error in Remove Owner:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Owner removed Successfully");
                    refreshUserGrids();
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }

    private void addManagerAction() {
        int chosenUserId = getIdOfSelectedRow(userGrid);
        int storeId = Integer.parseInt(storeSelectorAdd.getValue().split(":")[0]);

        if (chosenUserId != -1) {
            Result<Boolean> result = userService.addManager(mainLayout.getCurrUserID(), chosenUserId, storeId);

            if (result.isError()) {
                printError("Error in Adding Manager:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Manager added Successfully");
                    refreshUserGrids();
                    //UserInfoService curr = users.get(chosenUserId);
                    //curr.addStoresIManage(storeId);
                    //userGrid.getDataProvider().refreshItem(curr);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }

    private void removeManagerAction(int storeId) {
        int chosenUserId = getIdOfSelectedRow(managersIDefinedGrid);

        if (chosenUserId != -1) {
            Result<Boolean> result = userService.removeManager(mainLayout.getCurrUserID(), chosenUserId, storeId);

            if (result.isError()) {
                printError("Error in Remove Manager:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Manager removed Successfully");
                    refreshUserGrids();
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }

    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 4000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private int getIdOfSelectedRow(Grid<UserInfoService> grid) {
        List<UserInfoService> users = grid.getSelectedItems().stream().toList();
        if (users.size() > 1) {
            printError("Chosen More than one!");
            return -1;
        } else if (users.size() == 0) {
            printError("You need to choose a User!");
            return -1;
        } else {
            return users.get(0).getId();
        }
    }

    private int getStoreIdOfSelectedRow(Grid<StoreService> grid) {
        List<StoreService> stores = grid.getSelectedItems().stream().toList();
        if (stores.size() > 1) {
            printError("Chosen More than one!");
            return -1;
        } else if (stores.size() == 0) {
            printError("You need to choose a User!");
            return -1;
        } else {
            return stores.get(0).getStoreId();
        }
    }

    private int getIdOfSelectedReceiptRow(Grid<ReceiptService> receiptsGrid) {
        List<ReceiptService> receipts = receiptsGrid.getSelectedItems().stream().toList();
        if (receipts.size() > 1) {
            printError("Chosen More than one!");
            return -1;
        } else if (receipts.size() == 0) {
            printError("You need to choose a Receipt!");
            return -1;
        } else {
            return receipts.get(0).getId();
        }
    }

    private int getItemIdOfSelectedRow(Grid<CatalogItemService> itemsGrid) {
        List<CatalogItemService> items = itemsGrid.getSelectedItems().stream().toList();
        if (items.size() > 1) {
            printError("Chosen More than one!");
            return -1;
        } else if (items.size() == 0) {
            printError("You need to choose an Item!");
            return -1;
        } else {
            return items.get(0).getItemID();
        }
    }

    private BidService getSelectedBidFromGrid(Grid<BidService> grid) {
        List<BidService> bids = grid.getSelectedItems().stream().toList();
        if (bids.size() > 1) {
            printError("Chosen More than one!");
            return null;
        } else if (bids.size() == 0) {
            printError("You need to choose a Bid!");
            return null;
        } else {
            return bids.get(0);
        }
    }

    private List<Integer> getMultiIdsOfSelectedDiscounts(Grid<DiscountService> discountGrid) {
        List<DiscountService> discounts = discountGrid.getSelectedItems().stream().toList();
        if (discounts.size() == 0) {
            printError("You need to choose a Discount!");
            return null;
        } else if (discounts.size() == 1) {
            printError("You need to choose at least 2 Discount!");
            return null;
        } else {
            List<Integer> ids = new ArrayList<>();
            for (DiscountService discountService : discounts) {
                ids.add(discountService.getId());
            }
            return ids;
        }
    }

    private List<Integer> getMultiIdsOfSelectedItemsInItemsDiscount(Grid<CatalogItemService> catalogItemGrid) {
        List<CatalogItemService> itemsIds = catalogItemGrid.getSelectedItems().stream().toList();
        if (itemsIds.size() == 0) {
            printError("You need to choose at least 1 item!");
            return null;
        } else {
            List<Integer> ids = new ArrayList<>();
            for (CatalogItemService catalogItemService : itemsIds) {
                ids.add(catalogItemService.getItemID());
            }
            return ids;
        }
    }

    private List<Integer> getMultiIdsOfSelectedRules(Grid<RuleService> rulesGrid) {
        List<RuleService> rules = rulesGrid.getSelectedItems().stream().toList();
        if (rules.size() == 0) {
            printError("You need to choose at least 1 Rule!");
            return null;
        } else {
            List<Integer> ids = new ArrayList<>();
            for (RuleService ruleService : rules) {
                ids.add(ruleService.getId());
            }
            return ids;
        }
    }

    private void addItemDialog(Grid<CatalogItemService> itemsGrid, int storeId) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Add Item");

        TextField itemNameField = new TextField("Item Name");
        NumberField itemPriceField = new NumberField("Item Price");
        itemPriceField.setMin(0);
        TextField itemCategoryField = new TextField("Item Category");
        NumberField itemWeightField = new NumberField("Item Weight");
        itemWeightField.setMin(0);

        VerticalLayout dialogLayout = new VerticalLayout(itemNameField, itemPriceField, itemCategoryField, itemWeightField);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            addItemToStoreAction(storeId, itemNameField.getValue(),
                    itemPriceField.getValue(), itemCategoryField.getValue(), itemsGrid, itemWeightField.getValue());
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);


        add(dialog);
        dialog.open();
    }

    private void addAmountToItemDialog(Grid<CatalogItemService> itemsGrid, int storeId) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Add Item amount");

        IntegerField itemAmountField = new IntegerField("Item Amount");
        itemAmountField.setMin(0);

        //VerticalLayout dialogLayout = new VerticalLayout(itemIdField, itemAmountField);
        VerticalLayout dialogLayout = new VerticalLayout(itemAmountField);

        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            addItemAmountAction(storeId, getItemIdOfSelectedRow(itemsGrid),
                    itemAmountField.getValue(), itemsGrid);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);


        add(dialog);
        dialog.open();

    }

    private void removeItemDialog(Grid<CatalogItemService> itemsGrid, int storeId) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Remove Item");
        // itemIdField = new IntegerField("Item ID");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Remove", e -> {
            dialog.close();
            removeItemAction(storeId, getItemIdOfSelectedRow(itemsGrid), itemsGrid);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);


        add(dialog);
        dialog.open();

    }

    private void changeNameDialog(Grid<CatalogItemService> itemsGrid, int storeId) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Change Item Name");

        TextField itemNewNameField = new TextField("New Name");

        VerticalLayout dialogLayout = new VerticalLayout(itemNewNameField);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Update", e -> {
            dialog.close();
            changeItemNameAction(storeId, getItemIdOfSelectedRow(itemsGrid), itemNewNameField.getValue(), itemsGrid);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);


        add(dialog);
        dialog.open();
    }

    private void openStoreDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Open Store?");
        dialog.setText("Are you sure you want to reopen this store?");

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> printError("Canceled"));

        dialog.setConfirmText("Open");
        dialog.setConfirmButtonTheme("success primary");
        dialog.addConfirmListener(event -> reOpenStoreAction(getStoreIdOfSelectedRow(storesIOwnGrid), mainLayout.getCurrUserID()));

        add(dialog);
        dialog.open();

    }

    private void closeStoreDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Close Store?");
        dialog.setText("Are you sure you want to close this store?");

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> printError("Canceled"));

        dialog.setConfirmText("Close");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> closeStoreAction(getStoreIdOfSelectedRow(storesIOwnGrid), mainLayout.getCurrUserID()));

        add(dialog);
        dialog.open();
    }

    private void viewItemsDialog(Grid<StoreService> storesGrid, boolean managerMode) {

        Grid<CatalogItemService> itemsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Store Items");
        Div div = new Div();
        div.add(itemsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        int storeId = getStoreIdOfSelectedRow(storesGrid);
        if (managerMode)
            itemsGrid.setItems(storesIManage.get(storeId).getItems());
        else
            itemsGrid.setItems(storesIOwn.get(storeId).getItems());
        itemsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        itemsGrid.addColumn(CatalogItemService::getItemID).setHeader("ID").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getItemName).setHeader("Name").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getCategory).setHeader("Category").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getAmount).setHeader("Amount").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getPrice).setHeader("Price").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getWeight).setHeader("Weight").setSortable(true);

        GridContextMenu<CatalogItemService> menu = itemsGrid.addContextMenu();
        menu.setOpenOnClick(true);


        menu.addItem("Add Amount to Item", event -> addAmountToItemDialog(itemsGrid, storeId));
        menu.addItem("Remove Item", event -> removeItemDialog(itemsGrid, storeId));
        menu.addItem("Change Item Name", event -> changeNameDialog(itemsGrid, storeId));


        Button addItem = new Button("Add Item", e -> {
            addItemDialog(itemsGrid, storeId);
        });
        Button cancelButton = new Button("Exit", e -> dialog.close());
        dialog.getFooter().add(addItem, cancelButton);


        add(dialog);
        dialog.open();
        //dialog.add(itemsGrid);
        dialog.add(menu);

    }

    private void getHistoryDialog(Grid<StoreService> storesGrid) {

        Grid<ReceiptService> receiptsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Receipts");
        Div div = new Div();
        div.add(receiptsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        int storeId = getStoreIdOfSelectedRow(storesGrid);

        Result<List<ReceiptService>> result = shoppingService.getSellingHistoryOfStoreForManager(storeId, mainLayout.getCurrUserID());


        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue() == null) {
                printError("Something went wrong");
            } else {
                receiptsGrid.setItems(result.getValue());
                receiptsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

                receiptsGrid.addColumn(ReceiptService::getId).setHeader("Receipt ID").setSortable(true);
                receiptsGrid.addColumn(ReceiptService::getOwnerId).setHeader("User ID").setSortable(true);
                receiptsGrid.addColumn(ReceiptService::getDate).setHeader("Date").setSortable(true);

                GridContextMenu<ReceiptService> menu = receiptsGrid.addContextMenu();
                menu.setOpenOnClick(true);

                menu.addItem("View Items", event -> viewReceiptItemsAction(receiptsGrid, result.getValue(), getIdOfSelectedReceiptRow(receiptsGrid)));

                Button cancelButton = new Button("Exit", e -> dialog.close());
                dialog.getFooter().add(cancelButton);


                add(dialog);
                dialog.open();
                //dialog.add(itemsGrid);
                dialog.add(menu);
            }
        }
    }

    private void viewDiscountsDialog(Grid<StoreService> storesGrid) {
        Grid<DiscountService> discountsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Discounts");
        Div div = new Div();
        div.add(discountsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        int storeId = getStoreIdOfSelectedRow(storesGrid);

        Result<List<DiscountService>> result = shoppingService.getStoreDiscounts(storeId);
        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue() == null) {
                printError("Something went wrong");
            } else {

                discountsGrid.setItems(result.getValue());
                discountsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                discountsGrid.addColumn(DiscountService::getType).setHeader("Discount Type").setSortable(true);
                discountsGrid.addColumn(DiscountService::getDiscountString).setHeader("Discount").setSortable(true).setWidth("9em");
                discountsGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

                Button ADDButton = new Button("ADD", event -> newTypeDiscountAction(discountsGrid, NumericComposites.ADD, storeId));
                Button MAXButton = new Button("MAX", e -> newTypeDiscountAction(discountsGrid, NumericComposites.MAX, storeId));
                Button MINButton = new Button("MIN", e -> newTypeDiscountAction(discountsGrid, NumericComposites.MIN, storeId));
                Button createButton = new Button("Create New Discount", e -> createNewDiscountDialog(storeId, discountsGrid));
                Button cancelButton = new Button("Exit", e -> dialog.close());
                Button deleteButton = new Button("Delete", e -> deleteDiscounts(storeId, discountsGrid));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                createButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

                dialog.getFooter().add(deleteButton, ADDButton, MAXButton, MINButton, createButton, cancelButton);
                add(dialog);
                dialog.open();
            }
        }
    }

    private void deleteDiscounts(int storeId, Grid<DiscountService> discountsGrid) {
        Set<DiscountService> discounts = discountsGrid.getSelectedItems();
        for (DiscountService discountService : discounts) {
            shoppingService.removeDiscount(storeId, discountService.getId());
        }
        discountsGrid.setItems(shoppingService.getStoreDiscounts(storeId).getValue());
    }

    private void deletePolicies(int storeId, Grid<PolicyService> grid, int policy) {
        Set<PolicyService> policies = grid.getSelectedItems();
        if (policy == PURCHASE_POLICY) {
            for (PolicyService p : policies) {
                shoppingService.removePolicy(storeId, p.getPolicyId());
            }
            grid.setItems(shoppingService.getStorePurchasePolicies(storeId).getValue());
        } else if (policy == DISCOUNT_POLICY) {
            for (PolicyService p : policies) {
                shoppingService.removeDiscountPolicy(storeId, p.getPolicyId());
            }
            grid.setItems(shoppingService.getStoreDiscountPolicies(storeId).getValue());
        }
    }

    private void viewPoliciesDialog(int policy, Grid<StoreService> storesGrid) {
        Grid<PolicyService> policiesGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        if (policy == PURCHASE_POLICY)
            dialog.setHeaderTitle("Purchase Policies");
        else
            dialog.setHeaderTitle("Discount Policies");
        Div div = new Div();
        div.add(policiesGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        int storeId = getStoreIdOfSelectedRow(storesGrid);
        Result<List<PolicyService>> result;
        if (policy == PURCHASE_POLICY)
            result = shoppingService.getStorePurchasePolicies(storeId);
        else
            result = shoppingService.getStoreDiscountPolicies(storeId);
        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue() == null) {
                printError("Something went wrong");
            } else {

                policiesGrid.setItems(result.getValue());
                policiesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                policiesGrid.addColumn(PolicyService::getInfo).setHeader("Policy").setSortable(true).setWidth("9em");
                policiesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

                Button createButton = new Button("Create New Policy", e -> {
                    createNewPolicyDialog(policiesGrid, storeId, policy);
                });
                createButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

                Button cancelButton = new Button("Exit", e -> dialog.close());
                Button deleteButton = new Button("Delete", e -> deletePolicies(storeId, policiesGrid, policy));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                dialog.getFooter().add(createButton, deleteButton, cancelButton);
                add(dialog);
                dialog.open();
            }
        }
    }

    private void createNewPolicyDialog(Grid<PolicyService> policiesGrid, int storeId, int policyMode) {
        Grid<RuleService> rulesGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Rules");
        Div div = new Div();
        div.add(rulesGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        rulesGrid.setItems(new ArrayList<>());
        rulesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        rulesGrid.addColumn(RuleService::getInfo).setHeader("Rule");
        rulesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        MenuBar menuBar = new MenuBar();
        SubMenu newRuleSubMenu = menuBar.addItem("New Rule").getSubMenu();
        menuBar.addItem("And", e -> compositeRuleAction(rulesGrid, storeId, LogicalComposites.AND, policyMode));
        menuBar.addItem("Or", e -> compositeRuleAction(rulesGrid, storeId, LogicalComposites.OR, policyMode));
        menuBar.addItem("Conditional", e -> conditioningDialog(rulesGrid, storeId, LogicalComposites.CONDITIONING, policyMode));

        newRuleSubMenu.addItem("Basket Weight Limit", e -> ruleBasketWeightOrPriceLimitDialog(rulesGrid, storeId, "Weight", true, policyMode));
        if (policyMode == PURCHASE_POLICY)
            newRuleSubMenu.addItem("Age Limit", e -> ruleAgeDialog(rulesGrid, storeId, policyMode));
        newRuleSubMenu.addItem("Forbidden Category", e -> ruleForbiddenCategoryDialog(rulesGrid, storeId, policyMode));
        newRuleSubMenu.addItem("Forbidden Dates", e -> ruleForbiddenAndOrDatesDialog(rulesGrid, storeId, "Forbidden Dates", true, policyMode));
        newRuleSubMenu.addItem("Forbidden Hours", e -> rulesForbiddenHoursDialog(rulesGrid, storeId, policyMode));
        newRuleSubMenu.addItem("Must Dates", e -> ruleForbiddenAndOrDatesDialog(rulesGrid, storeId, "Must Dates", false, policyMode));
        newRuleSubMenu.addItem("Item and Weights", e -> ruleItemsAmountsOrWeightsLimits(rulesGrid, storeId, "Weight", policyMode));
        newRuleSubMenu.addItem("Basket Price Limit", e -> ruleBasketWeightOrPriceLimitDialog(rulesGrid, storeId, "Price", false, policyMode));
        newRuleSubMenu.addItem("Item and Amounts", e -> ruleItemsAmountsOrWeightsLimits(rulesGrid, storeId, "Amount", policyMode));


        menuBar.addItem("Finish", e -> {
            dialog.close();
            refreshPoliciesFromBusiness(policiesGrid, storeId);
        });

        dialog.getFooter().add(menuBar);
        add(dialog);
        dialog.open();
    }

    private void conditioningDialog(Grid<RuleService> rulesGrid, int storeId, LogicalComposites logicalComposites, int policyMode) {
        List<Integer> ids = getMultiIdsOfSelectedRules(rulesGrid);
        if (ids == null || ids.size() != 2) {
            printError("You need to choose exactly 2 Rules!");
        } else {
            List<RuleService> allRules = rulesGrid.getSelectedItems().stream().toList();
            List<RuleService> rulesChosen = new ArrayList<>();
            for (RuleService ruleService : allRules) {
                if (ids.contains(ruleService.getId())) {
                    rulesChosen.add(ruleService);
                }
            }
            Grid<RuleService> twoRulesGrid = new Grid<>();
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setHeaderTitle("Rules");
            Paragraph helper = new Paragraph("Choose the one you want to be the first condition, the second will be if the first is not happening");
            Div div = new Div();

            div.add(helper, twoRulesGrid);
            dialog.add(div);
            dialog.setWidth("1000px");

            twoRulesGrid.setItems(rulesChosen);
            twoRulesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            twoRulesGrid.addColumn(RuleService::getInfo).setHeader("Rule");
            twoRulesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

            Button addButton = new Button("Add", e -> {
                List<Integer> ruleChosen = getMultiIdsOfSelectedRules(twoRulesGrid);
                if (ruleChosen == null || ruleChosen.size() != 1) {
                    printError("You need to choose exactly 1 Rule!");
                } else {
                    int firstId = ruleChosen.get(0);
                    int secondId = -1;
                    for (RuleService ruleService : rulesChosen) {
                        if (ruleService.getId() != firstId)
                            secondId = ruleService.getId();
                    }
                    if (secondId != -1) {
                        dialog.close();
                        Result<RuleService> result;
                        if (policyMode == PURCHASE_POLICY)
                            result = shoppingService.wrapPurchasePolicies(storeId, Arrays.asList(firstId, secondId), logicalComposites);
                        else
                            result = shoppingService.wrapDiscountPolicies(storeId, Arrays.asList(firstId, secondId), logicalComposites);
                        handleRuleServiceResult(result, ids, rulesGrid);

                    }
                }
            });

            dialog.getFooter().add(addButton);
            add(dialog);
            dialog.open();
        }
    }

    private void ruleItemsAmountsOrWeightsLimits(Grid<RuleService> rulesGrid, int storeId, String value, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Items and " + value + "s");
        dialog.setWidth("800px");
        //showItems
        Div itemsDiv = new Div();
        Grid<CatalogItemService> itemsGrid = new Grid<>();
        itemsDiv.add(new Paragraph("Items of Store"));
        itemsDiv.add(itemsGrid);
        setItemsGridForDiscounts(itemsGrid, storeId);
        itemsGrid.setSelectionMode(Grid.SelectionMode.NONE);

        Paragraph paragraph = new Paragraph("Map of Items: ");
        TextField textField = new TextField("ID, " + value);

        Button addButton;
        Button createButton;
        if (value.equals("Amount")) {  // amount window
            Map<Integer, Integer> idsMap = new HashMap<>();
            addButton = getIntIntMapFromUser(idsMap, textField, paragraph);
            dialog.add(itemsDiv, paragraph, textField, addButton);
            createButton = new Button("Create", e -> {
                if (storeId != -1 && idsMap.size() > 0) {
                    dialog.close();
                    Result<RuleService> result;
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyMustItemsAmountsRule(storeId, idsMap);
                    else
                        result = shoppingService.addDiscountPolicyMustItemsAmountsRule(storeId, idsMap);
                    handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
                }
            });
        } else {   // weight window
            Map<Integer, Double> idsMap = new HashMap<>();
            addButton = getIntDoubleMapFromUser(idsMap, textField, paragraph);
            dialog.add(itemsDiv, paragraph, textField, addButton);
            createButton = new Button("Create", e -> {
                if (storeId != -1 && idsMap.size() > 0) {
                    dialog.close();
                    Result<RuleService> result;
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyItemsWeightLimitRule(storeId, idsMap);
                    else
                        result = shoppingService.addDiscountPolicyItemsWeightLimitRule(storeId, idsMap);
                    handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
                }
            });
        }

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, createButton);

        add(dialog);
        dialog.open();
    }

    private void rulesForbiddenHoursDialog(Grid<RuleService> rulesGrid, int storeId, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Forbidden Hours");

        IntegerField startHour = new IntegerField("Start");
        startHour.setValue(10);
        startHour.setStepButtonsVisible(true);
        startHour.setMin(0);
        startHour.setMax(23);

        IntegerField endHour = new IntegerField("End");
        endHour.setValue(12);
        endHour.setStepButtonsVisible(true);
        endHour.setMin(0);
        endHour.setMax(23);

        VerticalLayout dialogLayout = new VerticalLayout(startHour, endHour);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            Integer startHourInt = startHour.getValue();
            Integer endHourInt = endHour.getValue();
            if (startHourInt != null && endHourInt != null && storeId != -1) {
                Result<RuleService> result;
                if (policyMode == PURCHASE_POLICY)
                    result = shoppingService.addPurchasePolicyForbiddenHoursRule(storeId, startHourInt, endHourInt);
                else
                    result = shoppingService.addDiscountPolicyForbiddenHoursRule(storeId, startHourInt, endHourInt);
                handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        add(dialog);
        dialog.open();
    }

    private void ruleForbiddenAndOrDatesDialog(Grid<RuleService> rulesGrid, int storeId, String headline, boolean forbidden, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle(headline);

        DatePicker dateField = setDateButton();
        Set<Calendar> dates = new HashSet<>();
        Paragraph paragraph = new Paragraph("List of Dates Chosen: ");
        Button addButton = new Button("Add Date", e -> {
            if (dateField.getValue() != null) {
                Calendar calendar = convertToCalender(dateField.getValue());
                if (calendar != null) {
                    dates.add(calendar);
                    paragraph.add(getDateString(calendar) + "; ");
                }
            }
        });

        dialog.add(dateField, addButton, paragraph);

        Button saveButton = new Button("Add Rule", e -> {
            dialog.close();
            if (dates.size() != 0 && storeId != -1) {
                Result<RuleService> result;
                if (forbidden) {
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyForbiddenDatesRule(storeId, new ArrayList<>(dates));
                    else
                        result = shoppingService.addDiscountPolicyForbiddenDatesRule(storeId, new ArrayList<>(dates));
                } else {
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyMustDatesRule(storeId, new ArrayList<>(dates));
                    else
                        result = shoppingService.addDiscountPolicyMustDatesRule(storeId, new ArrayList<>(dates));
                }
                handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        add(dialog);
        dialog.open();
    }

    private String getDateString(Calendar date) {
        return date.get(Calendar.DATE) + "." + (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.YEAR);
    }

    private void ruleForbiddenCategoryDialog(Grid<RuleService> rulesGrid, int storeId, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Forbidden Category");

        TextField field = new TextField("Forbidden Category");

        VerticalLayout dialogLayout = new VerticalLayout(field);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            String category = field.getValue();
            if (category != null && storeId != -1 && !category.equals("")) {
                Result<RuleService> result;
                if (policyMode == PURCHASE_POLICY)
                    result = shoppingService.addPurchasePolicyForbiddenCategoryRule(storeId, category);
                else
                    result = shoppingService.addDiscountPolicyForbiddenCategoryRule(storeId, category);
                handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        add(dialog);
        dialog.open();
    }

    private void ruleAgeDialog(Grid<RuleService> rulesGrid, int storeId, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Basket Weight Limit");

        IntegerField field = new IntegerField("Age limit");
        field.setMin(0);

        VerticalLayout dialogLayout = new VerticalLayout(field);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            Integer age = field.getValue();
            if (age != null && storeId != -1 && age > 0) {
                Result<RuleService> result;
                if (policyMode == PURCHASE_POLICY)
                    result = shoppingService.addPurchasePolicyBuyerAgeRule(storeId, age);
                else
                    result = shoppingService.addDiscountPolicyBuyerAgeRule(storeId, age);
                handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        add(dialog);
        dialog.open();
    }

    private void ruleBasketWeightOrPriceLimitDialog(Grid<RuleService> rulesGrid, int storeId, String weightOrPrice, boolean weightBool, int policyMode) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Basket " + weightOrPrice + " Limit");

        NumberField field = new NumberField(weightOrPrice + " limit");
        field.setMin(0);

        VerticalLayout dialogLayout = new VerticalLayout(field);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button saveButton = new Button("Add", e -> {
            dialog.close();
            Double num = field.getValue();
            if (num != null && storeId != -1 && num >= 0) {
                Result<RuleService> result;
                if (weightBool) {
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyBasketWeightLimitRule(storeId, num);
                    else
                        result = shoppingService.addDiscountPolicyBasketWeightLimitRule(storeId, num);
                } else {
                    if (policyMode == PURCHASE_POLICY)
                        result = shoppingService.addPurchasePolicyBasketTotalPriceRule(storeId, num);
                    else
                        result = shoppingService.addDiscountPolicyBasketTotalPriceRule(storeId, num);
                }
                handleRuleServiceResult(result, new ArrayList<>(), rulesGrid);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        add(dialog);
        dialog.open();
    }

    private void compositeRuleAction(Grid<RuleService> rulesGrid, int storeId, LogicalComposites logicalComposites, int policy) {
        List<Integer> ids = getMultiIdsOfSelectedRules(rulesGrid);
        if (ids == null || ids.size() < 2) {
            printError("You didn't choose enough Rules");
        } else if (storeId != -1) {
            Result<RuleService> result;
            if (policy == PURCHASE_POLICY)
                result = shoppingService.wrapPurchasePolicies(storeId, ids, logicalComposites);
            else
                result = shoppingService.wrapDiscountPolicies(storeId, ids, logicalComposites);
            handleRuleServiceResult(result, ids, rulesGrid);
        }
    }

    public void handleRuleServiceResult(Result<RuleService> result, List<Integer> ids, Grid<RuleService> rulesGrid) {
        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue() != null) {
                printSuccess("Rule added Successfully");
                changeRulesListInScreen(result.getValue(), ids, rulesGrid);
            } else {
                printError("Something went wrong");
            }
        }
    }

    private void createNewDiscountDialog(int storeId, Grid<DiscountService> discountsGrid) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Discounts");
        dialog.setWidth("1000px");

        TabSheet tabSheet = new TabSheet();

        //calendar percent buttons and fields!!
        DatePicker datePicker = setDateButton();
        NumberField percentField = new NumberField("Percentage Of Discount");
        percentField.setMin(0);
        percentField.setValue((double) 0);


        Div divItemsDiscounts = new Div();
        //showItems
        Div itemsDiv = new Div();
        Grid<CatalogItemService> itemsGrid = new Grid<>();
        itemsDiv.add(new Paragraph("Select all the items you want for a discount"));
        itemsDiv.add(itemsGrid);
        itemsDiv.setHeight("200");

        setItemsGridForDiscounts(itemsGrid, storeId);

        divItemsDiscounts.add(itemsDiv);

        Div divCategoryDiscount = new Div();
        TextField categoryField = new TextField("Category");
        categoryField.setHelperText("Enter Category here");
        divCategoryDiscount.add(categoryField);

        Div divStoreDiscount = new Div();

        tabSheet.add("Items Discounts", divItemsDiscounts);
        tabSheet.add("Category Discount", divCategoryDiscount);
        tabSheet.add("Store Discount", divStoreDiscount);

        Button visible = new Button("Create Visible", e -> {
            List<Integer> itemsIds = new ArrayList<>();
            if (tabSheet.getSelectedIndex() == 0) {
                itemsIds = getMultiIdsOfSelectedItemsInItemsDiscount(itemsGrid);
            }
            visibleDiscountTypeAction(convertToCalender(datePicker.getValue()), tabSheet.getSelectedIndex(), categoryField, storeId, percentField.getValue(), discountsGrid, itemsIds);
            dialog.close();
            //refreshDiscountsFromBusiness(storeId, discountsGrid); doing this inside the function!
        });
        Button conditional = new Button("Create Conditional", e -> {
            List<Integer> itemsIds = new ArrayList<>();
            if (tabSheet.getSelectedIndex() == 0) {
                itemsIds = getMultiIdsOfSelectedItemsInItemsDiscount(itemsGrid);
            }
            conditionalDiscountTypeAction(convertToCalender(datePicker.getValue()), tabSheet.getSelectedIndex(), categoryField, storeId, percentField.getValue(), discountsGrid, itemsIds);
            dialog.close();
            //refreshDiscountsFromBusiness(storeId, discountsGrid);
        });
        Button hidden = new Button("Create Hidden", e -> {
            List<Integer> itemsIds = new ArrayList<>();
            if (tabSheet.getSelectedIndex() == 0) {
                itemsIds = getMultiIdsOfSelectedItemsInItemsDiscount(itemsGrid);
            }
            getCouponDialog(convertToCalender(datePicker.getValue()), tabSheet.getSelectedIndex(), categoryField, storeId, percentField.getValue(), discountsGrid, itemsIds);
            dialog.close();
            //refreshDiscountsFromBusiness(storeId, discountsGrid);
        });
        Button cancelButton = new Button("Exit", e -> dialog.close());

        dialog.add(datePicker, percentField);
        dialog.add(tabSheet);
        dialog.getFooter().add(visible, conditional, hidden, cancelButton);

        add(dialog);
        dialog.open();

    }

    private void setItemsGridForDiscounts(Grid<CatalogItemService> itemsGrid, int storeId) {
        itemsGrid.setItems(storesIOwn.get(storeId).getItems());
        itemsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        itemsGrid.addColumn(CatalogItemService::getItemID).setHeader("ID").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getItemName).setHeader("Name").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getCategory).setHeader("Category").setSortable(true);
        //itemsGrid.addColumn(CatalogItemService:: getAmount).setHeader("Amount").setSortable(true);
        itemsGrid.addColumn(CatalogItemService::getPrice).setHeader("price").setSortable(true);
    }

    private void getCouponDialog(Calendar calendar, int selectedIndex, TextField categoryField, int storeId, Double percent, Grid<DiscountService> discountsGrid, List<Integer> itemsIds) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Discounts");
        dialog.setWidth("1000px");

        TextField couponField = new TextField("Enter Coupon here");
        couponField.setMinLength(0);

        Button addButton = new Button("Add Coupon", e -> {
            if (couponField.getValue().length() == 0) {
                printError("Enter a coupon please");
            } else {
                hiddenDiscountTypeAction(couponField.getValue(), calendar, selectedIndex, categoryField, storeId, percent, discountsGrid, itemsIds);
                dialog.close();
            }
        });
        Button cancelButton = new Button("Exit", e -> dialog.close());

        dialog.add(couponField);
        dialog.getFooter().add(addButton, cancelButton);
        add(dialog);
        dialog.open();
    }

    private Calendar convertToCalender(LocalDate localDate) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth(), 0, 0, 0);
            return calendar;
        } catch (Exception e) {
            return null;
        }
    }

    private DatePicker setDateButton() {
        DatePicker.DatePickerI18n multiFormatI18n = new DatePicker.DatePickerI18n();
        multiFormatI18n.setDateFormats("dd-MM-yyyy", "dd/MM/yyyy", "dd.MM.yyyy");
        Locale locale = new Locale("en", "US");
        DatePicker datePicker = new DatePicker("Select a date:");
        datePicker.setLocale(locale);
        datePicker.setI18n(multiFormatI18n);
        datePicker.setValue(LocalDate.now().plusDays(7));
        return datePicker;
    }

    private void hiddenDiscountTypeAction(String coupon, Calendar calendar, int selectedIndex, TextField categoryField, int storeId, Double percent, Grid<DiscountService> discountsGrid, List<Integer> itemsIds) {
        if (calendar == null)
            printError("Enter Date please!");
        else if (percent == null)
            printError("Enter percent please!");
        else if (storeId == -1)
            printError("Something went wrong");
        else {
            Result<Integer> result = null;
            switch (selectedIndex) {
                case ITEMS -> {
                    if (itemsIds.size() == 0)
                        printError("Enter Ids please!");
                    else
                        result = shoppingService.addHiddenItemsDiscount(storeId, itemsIds, percent, coupon, calendar);
                }
                case CATEGORY -> {
                    if (categoryField.getValue() != null) {
                        String category = categoryField.getValue();
                        result = shoppingService.addHiddenCategoryDiscount(storeId, category, percent, coupon, calendar);
                    }
                }
                case STORE -> result = shoppingService.addHiddenStoreDiscount(storeId, percent, coupon, calendar);
            }
            if (result == null)
                printError("Something went wrong");
            else if (result.isError()) {
                printError(result.getMessage());
            } else if (result.getValue() == -1) {
                printError("Error in adding discount");
            } else {
                printSuccess("Added discount successfully");
                refreshDiscountsFromBusiness(storeId, discountsGrid);
            }
        }
    }

    private void conditionalDiscountTypeAction(Calendar calendar, int selectedIndex, TextField categoryField, int storeId, Double percent, Grid<DiscountService> discountsGrid, List<Integer> itemsIds) {
        if (calendar == null)
            printError("Enter Date please!");
        else if (percent == null)
            printError("Enter percent please!");
        else if (storeId == -1)
            printError("Something went wrong");
        else {
            Result<Integer> result = null;
            switch (selectedIndex) {
                case ITEMS -> {
                    if (itemsIds.size() == 0)
                        printError("Enter Ids please!");
                    else
                        result = shoppingService.addConditionalItemsDiscount(storeId, percent, calendar, itemsIds);
                }
                case CATEGORY -> {
                    if (categoryField.getValue() != null) {
                        String category = categoryField.getValue();
                        result = shoppingService.addConditionalCategoryDiscount(storeId, percent, calendar, category);
                    }
                }
                case STORE -> result = shoppingService.addConditionalStoreDiscount(storeId, percent, calendar);
            }
            if (result == null)
                printError("Something went wrong");
            else if (result.isError()) {
                printError(result.getMessage());
            } else if (result.getValue() == -1) {
                printError("Error in adding discount");
            } else {
                printSuccess("Added discount successfully");
                createNewRulesDialog(discountsGrid, result.getValue(), storeId);
            }
        }
    }


    private void visibleDiscountTypeAction(Calendar calendar, int selectedIndex, TextField categoryField, int storeId, Double percent, Grid<DiscountService> discountsGrid, List<Integer> itemsIds) {
        if (calendar == null)
            printError("Enter Date please!");
        else if (percent == null)
            printError("Enter percent please!");
        else if (storeId == -1)
            printError("Something went wrong");
        else {
            Result<Integer> result = null;
            switch (selectedIndex) {
                case ITEMS -> {
                    if (itemsIds.size() == 0)
                        printError("Enter Ids please!");
                    else
                        result = shoppingService.addVisibleItemsDiscount(storeId, itemsIds, percent, calendar);
                }
                case CATEGORY -> {
                    if (categoryField.getValue() != null) {
                        String category = categoryField.getValue();
                        result = shoppingService.addVisibleCategoryDiscount(storeId, category, percent, calendar);
                    }
                }
                case STORE -> result = shoppingService.addVisibleStoreDiscount(storeId, percent, calendar);
            }
            if (result == null)
                printError("Something went wrong");
            else if (result.isError()) {
                printError(result.getMessage());
            } else if (result.getValue() == -1) {
                printError("Error in adding discount");
            } else {
                printSuccess("Added discount successfully");
                refreshDiscountsFromBusiness(storeId, discountsGrid);
            }
        }
    }


    private void viewReceiptItemsAction(Grid<ReceiptService> receiptsGrid, List<ReceiptService> receipts, int receiptId) {

        Grid<ReceiptItemService> itemsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Receipt Items");
        Div div = new Div();
        div.add(itemsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        ReceiptService curr = null;
        for (ReceiptService receiptService : receipts) {
            if (receiptService.getId() == receiptId)
                curr = receiptService;
        }
        if (curr != null) {
            itemsGrid.setItems(curr.getItemsInList());
            itemsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            itemsGrid.addColumn(ReceiptItemService::getOwnerId).setHeader("User ID").setSortable(true);
            itemsGrid.addColumn(ReceiptItemService::getId).setHeader("Item ID").setSortable(true);
            itemsGrid.addColumn(ReceiptItemService::getName).setHeader("Name").setSortable(true);
            itemsGrid.addColumn(ReceiptItemService::getAmount).setHeader("Amount").setSortable(true);
            itemsGrid.addColumn(ReceiptItemService::getPriceBeforeDiscount).setHeader("Price Before Discount").setSortable(true);
            itemsGrid.addColumn(ReceiptItemService::getFinalPrice).setHeader("Final Price").setSortable(true);

            Button cancelButton = new Button("Exit", e -> dialog.close());
            dialog.getFooter().add(cancelButton);

            add(dialog);
            dialog.open();
            dialog.add(itemsGrid);
        }


    }


    private void changeItemNameAction(int storeId, int itemId, String newName, Grid<CatalogItemService> itemsGrid) {
        if (storeId != -1) {
            Result<String> result = shoppingService.updateItemName(storeId, itemId, newName);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getMessage().contains("Changed item name from")) {
                    printSuccess("Item Name Updated Successfully");
                    refreshItemFromBusiness(storeId, itemId, itemsGrid);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void removeItemAction(int storeId, int itemId, Grid<CatalogItemService> itemsGrid) {
        if (storeId != -1) {
            Result<CatalogItemService> result = shoppingService.removeItemFromStore(storeId, itemId);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != null) {
                    printSuccess("Item removed Successfully");
                    refreshItemFromBusiness(storeId, itemId, itemsGrid);

                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void addItemAmountAction(int storeId, int itemId, int amount, Grid<CatalogItemService> itemsGrid) {
        if (storeId != -1) {
            Result<Boolean> result = shoppingService.addItemAmount(storeId, itemId, amount);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Amount of Item added Successfully");
                    refreshItemFromBusiness(storeId, itemId, itemsGrid);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void addItemToStoreAction(int storeId, String itemName, Double price, String category, Grid<CatalogItemService> itemsGrid, Double weight) {
        if (storeId != -1 && price != null) {
            Result<CatalogItemService> result = shoppingService.addItemToStore(storeId, itemName, price, category, weight);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != null) {
                    printSuccess("Item added Successfully");
                    refreshItemFromBusiness(storeId, result.getValue().getItemID(), itemsGrid);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void reOpenStoreAction(int storeId, int userId) {
        if (storeId != -1) {
            Result<Boolean> result = shoppingService.reopenStore(userId, storeId);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Store reOpened Successfully");

                    refreshStoreFromBusiness(storeId);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }

    private void closeStoreAction(int storeId, int userId) {
        if (storeId != -1) {
            Result<Boolean> result = shoppingService.closeStore(userId, storeId);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Store closed Successfully");

                    refreshStoreFromBusiness(storeId);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void newTypeDiscountAction(Grid<DiscountService> discountGrid, NumericComposites numericComposites, int storeId) {
        if (storeId != -1) {
            List<Integer> discounts = getMultiIdsOfSelectedDiscounts(discountGrid);
            if (discounts != null) {
                Result<Integer> result = shoppingService.wrapDiscounts(storeId, discounts, numericComposites);
                if (result.isError()) {
                    printError(result.getMessage());
                } else {
                    if (result.getValue() != -1) {
                        printSuccess("New Discount created!");
                        refreshDiscountsFromBusiness(storeId, discountGrid);
                    } else {
                        printError("Something went wrong");
                    }
                }
            }
        }

    }


    private void createNewRulesDialog(Grid<DiscountService> discountsGrid, int discountId, int storeId) {
        Grid<RuleService> rulesGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Rules");
        Div div = new Div();
        div.add(rulesGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        rulesGrid.setItems(new ArrayList<>());
        rulesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        rulesGrid.addColumn(RuleService::getInfo).setHeader("Rule");
        rulesGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        Button priceRuleButton = new Button("new Price Rule", e -> newPriceRuleDialog(discountId, storeId, rulesGrid));
        Button quantityRuleButton = new Button("new Quantity Rule", e -> newQuantityRuleDialog(discountId, storeId, rulesGrid));
        Button andButton = new Button("And", e -> ruleCompositeAction(discountId, rulesGrid, storeId, LogicalComposites.AND));  //in the function get the ids selected
        Button orButton = new Button("Or", e -> ruleCompositeAction(discountId, rulesGrid, storeId, LogicalComposites.OR));     //in the function get the ids selected
        Button finishButton = new Button("Finish", e -> {
            boolean errorOccurred = finishCompositeAction(discountId, storeId);
            if (!errorOccurred) {
                dialog.close();
                refreshDiscountsFromBusiness(storeId, discountsGrid);
            }
        });

        dialog.getFooter().add(priceRuleButton, quantityRuleButton, andButton, orButton, finishButton);
        add(dialog);
        dialog.open();
    }

    private boolean finishCompositeAction(int discountId, int storeId) {
        boolean errorOccurred = false;
        if (storeId != -1) {
            Result<Boolean> result = shoppingService.finishConditionalDiscountBuilding(storeId, discountId);
            if (result.isError()) {
                printError(result.getMessage());
                errorOccurred = true;
            } else {
                if (result.getValue()) {
                    printSuccess("Rule Finished!");
                } else {
                    printError("Something went wrong");
                    errorOccurred = true;
                }
            }
        }
        return errorOccurred;
    }

    private void ruleCompositeAction(int discountId, Grid<RuleService> rulesGrid, int storeId, LogicalComposites logicalComposite) {
        List<Integer> ids = getMultiIdsOfSelectedRules(rulesGrid);
        if (ids == null || ids.size() < 2) {
            printError("You didn't choose enough Rules");
        } else if (storeId != -1) {
            Result<RuleService> result = shoppingService.addDiscountComposite(storeId, discountId, logicalComposite, ids);
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != null) {
                    printSuccess("Rule added Successfully");
                    changeRulesListInScreen(result.getValue(), ids, rulesGrid);
                } else {
                    printError("Something went wrong");
                }
            }
        }
    }


    private void newPriceRuleDialog(int discountId, int storeId, Grid<RuleService> rulesGrid) {

        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("New Price Rule");

        NumberField minPriceField = new NumberField("Minimum Price");
        minPriceField.setMin(0);

        VerticalLayout dialogLayout = new VerticalLayout(minPriceField);

        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        dialog.add(dialogLayout);

        Button createButton = new Button("Create", e -> {

            RuleService newRule = addPriceRuleAction(minPriceField.getValue(), discountId, storeId);
            changeRulesListInScreen(newRule, new ArrayList<>(), rulesGrid);
            if (newRule != null)
                dialog.close();
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, createButton);

        add(dialog);
        dialog.open();

    }

    private RuleService addPriceRuleAction(Double price, int discountId, int storeId) {
        RuleService resultRule = null;
        if (storeId != -1 && price != null) {
            Result<RuleService> result = shoppingService.addDiscountBasketTotalPriceRule(storeId, discountId, price);

            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != null) {
                    printSuccess("Price Rule added Successfully");
                    resultRule = result.getValue();
                } else {
                    printError("Something went wrong");
                }
            }
        }
        return resultRule;
    }

    private void newQuantityRuleDialog(int discountId, int storeId, Grid<RuleService> rulesGrid) {

        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("New Quantity Rule");
        dialog.setWidth("800px");
        //showItems
        Div itemsDiv = new Div();
        Grid<CatalogItemService> itemsGrid = new Grid<>();
        itemsDiv.add(new Paragraph("Items of Store"));
        itemsDiv.add(itemsGrid);
        setItemsGridForDiscounts(itemsGrid, storeId);
        itemsGrid.setSelectionMode(Grid.SelectionMode.NONE);

        Map<Integer, Integer> idsToAmounts = new HashMap<>();
        Paragraph paragraph = new Paragraph("Map of Items: ");
        TextField textField = new TextField("ID, Amount");
        Button addButton = getIntIntMapFromUser(idsToAmounts, textField, paragraph);

        dialog.add(itemsDiv, paragraph, textField, addButton);

        Button createButton = new Button("Create", e -> {

            RuleService newRule = addQuantityRuleAction(idsToAmounts, storeId, discountId);
            changeRulesListInScreen(newRule, new ArrayList<>(), rulesGrid);
            if (newRule != null)
                dialog.close();
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, createButton);

        add(dialog);
        dialog.open();
    }

    private RuleService addQuantityRuleAction(Map<Integer, Integer> idsToAmounts, int storeId, int discountId) {
        RuleService resultRule = null;
        if (idsToAmounts.size() == 0) {
            printError("You didn't added nothing to the list");
        } else if (storeId != -1) {
            Result<RuleService> result = shoppingService.addDiscountQuantityRule(storeId, discountId, idsToAmounts);
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue() != null) {
                    printSuccess("Quantity Rule added Successfully");
                    resultRule = result.getValue();
                } else {
                    printError("Something went wrong");
                }
            }
        }
        return resultRule;
    }

    private Button getIntIntMapFromUser(Map<Integer, Integer> idsToAmounts, TextField textField, Paragraph paragraph) {
        textField.setHelperText("Enter an ID a ',' and amount");
        return new Button("Add Id,Amount", e -> {

            if (textField.getValue() == null) {
                printError("Enter an ID and Amount please");
            } else {
                String res = textField.getValue().replace(" ", "");
                int secondIndex = res.indexOf(',', res.indexOf(',') + 1);
                if (!res.contains(",")) {
                    printError("You didn't enter a ','");
                } else if (secondIndex != -1) {
                    printError("You entered too many values");
                } else {
                    String[] resSplit = res.split(",");
                    try {
                        idsToAmounts.put(Integer.parseInt(resSplit[0]), Integer.parseInt(resSplit[1]));
                        paragraph.add(resSplit[0] + ":" + resSplit[1] + "   ");
                    } catch (Exception e1) {
                        printError("You didn't enter number!");
                    }
                }
            }
        });
    }

    private Button getIntDoubleMapFromUser(Map<Integer, Double> idsToWeights, TextField textField, Paragraph paragraph) {
        textField.setHelperText("Enter an ID a ',' and weight");
        return new Button("Add Id,Weight", e -> {

            if (textField.getValue() == null) {
                printError("Enter an ID and weight please");
            } else {
                String res = textField.getValue().replace(" ", "");
                int secondIndex = res.indexOf(',', res.indexOf(',') + 1);
                if (!res.contains(",")) {
                    printError("You didn't enter a ','");
                } else if (secondIndex != -1) {
                    printError("You entered too many values");
                } else {
                    String[] resSplit = res.split(",");
                    try {
                        idsToWeights.put(Integer.parseInt(resSplit[0]), Double.parseDouble(resSplit[1]));
                        paragraph.add(resSplit[0] + ":" + resSplit[1] + "   ");
                    } catch (Exception e1) {
                        printError("You didn't enter number!");
                    }
                }
            }
        });
    }

    private void changeRulesListInScreen(RuleService newRule, List<Integer> toRemoveIds, Grid<RuleService> rulesGrid) {
        if (newRule != null) {
            ListDataProvider dataProvider = (ListDataProvider) rulesGrid.getDataProvider();
            List<RuleService> rules = new ArrayList<>(dataProvider.getItems());
            rules.add(newRule);
            rules.removeIf(ruleService -> toRemoveIds.contains(ruleService.getId()));
            rulesGrid.setItems(rules);
            rulesGrid.getDataProvider().refreshAll();
        }
    }


    private void refreshStoreFromBusiness(int storeId) {
        StoreService curr = shoppingService.getStoreInfo(storeId).getValue();

        if (shoppingService.checkIfStoreOwner(mainLayout.getCurrUserID(), storeId).getValue()) {
            //refresh owner grid
            if (storesIOwn.containsKey(storeId))
                storesIOwn.replace(storeId, curr);
            else
                storesIOwn.put(storeId, curr);
            storesIOwnGrid.setItems(storesIOwn.values());
            storesIOwnGrid.getDataProvider().refreshAll();
        }

        if (shoppingService.checkIfStoreManager(mainLayout.getCurrUserID(), storeId).getValue()) {
            //refresh manager grid
            if (storesIManage.containsKey(storeId))
                storesIManage.replace(storeId, curr);
            else
                storesIManage.put(storeId, curr);
            storesManagedGrid.setItems(storesIManage.values());
            storesManagedGrid.getDataProvider().refreshAll();
        }

    }

    private void refreshItemFromBusiness(int storeId, int itemId, Grid<CatalogItemService> itemsGrid) {
        StoreService currStore = shoppingService.getStoreInfo(storeId).getValue();

        storesIOwn.replace(storeId, currStore);
        storesIManage.replace(storeId, currStore);

        itemsGrid.setItems(currStore.getItems());
        itemsGrid.getDataProvider().refreshAll();
        storesIOwnGrid.getDataProvider().refreshAll();
        storesManagedGrid.getDataProvider().refreshAll();
    }


    private void refreshDiscountsFromBusiness(int storeId, Grid<DiscountService> discountsGrid) {
        Result<List<DiscountService>> result = shoppingService.getStoreDiscounts(storeId);
        discountsGrid.setItems(result.getValue());
        discountsGrid.getDataProvider().refreshAll();
    }

    private void refreshUserGrids() {
        refreshStoreList();
        Result<List<UserInfoService>> result1 = userService.getAllOwnersIDefined(mainLayout.getCurrUserID());
        Result<Map<Integer, UserInfoService>> result2 = userService.getAllRegisteredUsers();
        Result<List<UserInfoService>> result3 = userService.getAllManagersIDefined(mainLayout.getCurrUserID());
        if (result1.isError() || result1.getValue() == null) {
            printError(result1.getMessage());
        } else if (result2.isError() || result2.getValue() == null) {
            printError(result2.getMessage());
        } else if (result3.isError() || result3.getValue() == null) {
            printError(result3.getMessage());
        } else {

            users = result2.getValue();
            userGrid.setItems(users.values());
            userGrid.getDataProvider().refreshAll();

            ownersIDefinedGrid.setItems(result1.getValue());
            ownersIDefinedGrid.getDataProvider().refreshAll();

            managersIDefinedGrid.setItems(result3.getValue());
            managersIDefinedGrid.getDataProvider().refreshAll();
        }
    }

    private void refreshPoliciesFromBusiness(Grid<PolicyService> policiesGrid, int storeId) {
        Result<List<PolicyService>> result = shoppingService.getStorePurchasePolicies(storeId);
        policiesGrid.setItems(result.getValue());
        policiesGrid.getDataProvider().refreshAll();
    }

    private void getStaffInfoDialog() {
        //TODO add refresh
        Grid<Map.Entry<Integer, Integer>> staffInfo = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Staff");
        Div div = new Div();
        div.add(staffInfo);
        dialog.add(div);
        dialog.setWidth("1000px");

        int storeId = getStoreIdOfSelectedRow(storesIOwnGrid);

        Result<StoreService> result = shoppingService.getStoreInfo(storeId);


        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue() == null) {
                printError("Something went wrong");
            } else {
                Map<Integer, Integer> data = result.getValue().getOwners();
                data.putAll(result.getValue().getManagers());
                staffInfo.setItems(data.entrySet());
                staffInfo.setSelectionMode(Grid.SelectionMode.SINGLE);

                staffInfo.addColumn(Map.Entry::getKey).setHeader("UserID").setSortable(true).setVisible(false);
                staffInfo.addColumn(e -> userService.getUsername(e.getKey())).setHeader("User Name").setSortable(true);
                staffInfo.addColumn(e -> userService.getUsername(e.getValue())).setHeader("Boss Name").setSortable(true);
                staffInfo.addColumn(e -> result.getValue().getFounderID() == e.getKey() ? "Founder" :
                        result.getValue().getManagers().containsKey(e.getKey()) ? "Manager" : "Owner").setHeader("Position").setSortable(true);

                GridContextMenu<Map.Entry<Integer, Integer>> menu = staffInfo.addContextMenu();
                menu.setOpenOnClick(true);

                menu.addItem("View Permissions", event -> getPermissions(staffInfo, result.getValue()));

                add(dialog);
                dialog.open();

            }
        }
    }

    private void getPermissions(Grid<Map.Entry<Integer, Integer>> staffInfo, StoreService store) {
        int userID = staffInfo.getSelectedItems().stream().toList().get(0).getKey();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Permissions for user: " + userService.getUsername(userID));
        Div div = new Div();

        List<String> permissions = shoppingService.getManagerInfo(userID, store.getStoreId());
        for (String permission : permissions)
            div.add(new Label(permission));
        dialog.add(div);
        dialog.setWidth("1000px");

        Button cancelButton = new Button("Exit", e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        add(dialog);
        dialog.open();
    }

    private void refreshBidsGrid() {
        Result<List<BidService>> result = shoppingService.getUserBidsToReply(mainLayout.getCurrUserID());
        if (!result.isError()) {
            List<BidService> bids;
            if (result.getValue() == null) {
                bids = new ArrayList<>();
            } else
                bids = result.getValue();
            bidsGrid.setItems(bids);
        }
    }

    private void createBidDialog() {

        bidsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Open Bids");
        Div div = new Div();
        div.add(bidsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        Result<List<BidService>> result = shoppingService.getUserBidsToReply(mainLayout.getCurrUserID());
        if (!result.isError()) {
            List<BidService> bids;
            if (result.getValue() == null) {
                bids = new ArrayList<>();
            } else
                bids = result.getValue();
            bidsGrid.setItems(bids);
            bidsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

            bidsGrid.addColumn(e -> shoppingService.getStoreName(e.getStoreId())).setHeader("Store").setSortable(true);
            bidsGrid.addColumn(e -> userService.getUsername(e.getUserId())).setHeader("User").setSortable(true);
            bidsGrid.addColumn(BidService::getItemName).setHeader("Item").setSortable(true);
            bidsGrid.addColumn(BidService::getOriginalPrice).setHeader("Original Price").setSortable(true);
            bidsGrid.addColumn(BidService::getNewPrice).setHeader("Offered Price").setSortable(true);
            bidsGrid.addColumn(e -> {
                        double max = e.getCounterOffer();
                        return max == -1 ? "No Counters Yet" : max;
                    })
                    .setHeader("Max Counter Offer").setSortable(true);

            GridContextMenu<BidService> menu = bidsGrid.addContextMenu();
            menu.setOpenOnClick(true);

            menu.addItem("Accept", event -> acceptBid(bidsGrid));
            menu.addItem("Reject", event -> rejectBid(bidsGrid));
            menu.addItem("Counter Offer", event -> counterOfferBid(bidsGrid));


            Button cancelButton = new Button("Exit", e -> dialog.close());
            dialog.getFooter().add(cancelButton);

            add(dialog);
            dialog.open();
            dialog.add(menu);
        }
    }

    private void counterOfferBid(Grid<BidService> bidsGrid) {
        BidService bidService = getSelectedBidFromGrid(bidsGrid);
        if (bidService == null)
            printError("You didn't choose a Bid");
        else {
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setHeaderTitle("Create Counter Offer");
            dialog.setWidth("1000px");
            NumberField numberField = new NumberField();
            numberField.setStepButtonsVisible(true);
            numberField.setMin(Math.max(bidService.getNewPrice(), bidService.getCounterOffer()));
            numberField.setValue(bidService.getNewPrice() + 1);
            numberField.setMax(bidService.getOriginalPrice() - 1);
            Button saveButton = new Button("Counter Offer", e -> {
                Result<Boolean> result = shoppingService.counterOffer(bidService.getStoreId(), bidService.getId(), MainLayout.getMainLayout().getCurrUserID(), numberField.getValue());
                handleCounterRes(result);
                dialog.close();
                refreshBidsGrid();
            });
            Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                    e -> {
                        printError("Cancelled");
                        dialog.close();
                    });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                    ButtonVariant.LUMO_ERROR);
            HorizontalLayout actions = new HorizontalLayout(numberField, saveButton,
                    cancelButton);
            actions.setPadding(false);
            dialog.add(actions);
            dialog.open();
        }
    }

    private void handleCounterRes(Result<Boolean> result) {
        if (result.isError()) {
            printError(result.getMessage());
        } else {
            if (result.getValue()) {
                printSuccess("All Managers answered, Counter Offer had been sent to Buyer");
            } else {
                printSuccess("Counter offer received, Waiting for other managers to respond");
            }
        }
    }

    private void rejectBid(Grid<BidService> bidsGrid) {
        BidService bidService = getSelectedBidFromGrid(bidsGrid);
        if (bidService == null)
            printError("You didn't choose a Bid");
        else {
            Result<Boolean> result = shoppingService.reject(bidService.getStoreId(), bidService.getId(), mainLayout.getCurrUserID());
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Rejected Bid");
                } else {
                    printError("Something went wrong");
                }
            }
        }
        refreshBidsGrid();
    }

    private void acceptBid(Grid<BidService> bidsGrid) {
        BidService bidService = getSelectedBidFromGrid(bidsGrid);
        if (bidService == null)
            printError("You didn't choose a Bid");
        else {
            Result<Boolean> result = shoppingService.approve(bidService.getStoreId(), bidService.getId(), mainLayout.getCurrUserID());
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("All managers accepted the Bid");
                } else {
                    printSuccess("Accepted, waiting for other managers to respond");
                }
            }
        }
        refreshBidsGrid();
    }
}
