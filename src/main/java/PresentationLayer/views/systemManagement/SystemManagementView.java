package PresentationLayer.views.systemManagement;

import PresentationLayer.Application;
import PresentationLayer.views.MainLayout;
import ServiceLayer.Objects.ReceiptItemService;
import ServiceLayer.Objects.ReceiptService;
import ServiceLayer.Objects.StoreService;
import ServiceLayer.Objects.UserInfoService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("About")
@Route(value = "admin", layout = MainLayout.class)
@PreserveOnRefresh
public class SystemManagementView extends VerticalLayout {
    ShoppingService shoppingService;
    UserService userService;
    Grid<UserInfoService> loggedOut;
    Grid<UserInfoService> loggedIn;
    Grid<StoreService> storeGrid;
    MainLayout mainLayout;

    public SystemManagementView() {
        setSpacing(false);
        mainLayout = MainLayout.getMainLayout();
        AccordionPanel managerLayout = new AccordionPanel("Store Info");
        AccordionPanel userLayout = new AccordionPanel("User Info");

        Accordion accordion = new Accordion();
        accordion.add(userLayout);
        accordion.add(managerLayout);

        try {
            shoppingService = new ShoppingService();
            userService = new UserService();
        } catch (Exception e) {
            add("Problem initiating Store:(");
        }

        createGrid(managerLayout);
        addUsersInfo(userLayout);
        accordion.setWidthFull();
        add(accordion);
        createExitButton();
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("text-align", "center");
    }

    private void createExitButton() {
        Button exitButton = new Button("Shut Down Market", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("System Shut Down");
            dialog.setText("Are you sure you want to shut down the Market?");
            dialog.setCancelable(true);
            dialog.addCancelListener(event -> printSuccess("Canceled"));
            dialog.addConfirmListener(a -> {
                Result<Boolean> r = userService.system_shutdown(MainLayout.getMainLayout().getCurrUserID());
                if (r.isError())
                    printError(r.getMessage());
                else if (r.getValue()){
                    ConfirmDialog dialog1 = new ConfirmDialog();
                    dialog1.setText("System Shut Down Successfully. You may leave this page");
                    dialog1.open();
                    dialog1.addConfirmListener(l->System.exit(0));
                }
            });
            dialog.setConfirmText("Shutdown");
            dialog.setConfirmButtonTheme("error primary");
            dialog.open();
        });
        exitButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        add(exitButton);
        setHorizontalComponentAlignment(Alignment.CENTER, exitButton);
    }

    private void addUsersInfo(AccordionPanel usersSpan) {
        Result<Map<Integer, UserInfoService>> loggedInUsersRes = userService.getLoggedInUsers();
        Result<Map<Integer, UserInfoService>> loggedOutUsersRes = userService.getLoggedOutUsers();

        if (loggedInUsersRes.isError() || loggedOutUsersRes.isError()) {
            usersSpan.addContent(new H2("Problem getting users :("));
        } else {
            loggedIn = new Grid();
            loggedIn.setItems(loggedInUsersRes.getValue().values());
            loggedIn.addColumn(UserInfoService::getUsername).setHeader("Username").setSortable(true);
            loggedIn.addColumn(UserInfoService::getStoreIManageString).setHeader("Manager of Stores");
            loggedIn.addColumn(UserInfoService::getStoreIOwnString).setHeader("Owner of Stores");

            loggedOut = new Grid();
            loggedOut.setItems(loggedOutUsersRes.getValue().values());
            loggedOut.addColumn(UserInfoService::getUsername).setHeader("Username").setSortable(true);
            loggedOut.addColumn(UserInfoService::getStoreIManageString).setHeader("Manager of Stores");
            loggedOut.addColumn(UserInfoService::getStoreIOwnString).setHeader("Owner of Stores");

            final Integer[] trigger = {0};
            SingleSelect<Grid<UserInfoService>, UserInfoService> selection1 = loggedIn.asSingleSelect();
            selection1.addValueChangeListener(e -> {
                if (trigger[0] != 1) {
                    loggedOut.deselectAll();
                    trigger[0] = 1;
                }
            });
            SingleSelect<Grid<UserInfoService>, UserInfoService> selection2 = loggedOut.asSingleSelect();
            selection2.addValueChangeListener(e -> {
                if (trigger[0] != 2) {
                    loggedIn.deselectAll();
                    trigger[0] = 2;
                }
            });

            HorizontalLayout usersGraphs = new HorizontalLayout(loggedIn, loggedOut);

            HorizontalLayout titles = new HorizontalLayout(new Label("Logged in Users"), new Label("Logged out Users"));
            titles.setPadding(true);
            titles.setJustifyContentMode(JustifyContentMode.AROUND);

            usersSpan.addContent(titles, usersGraphs);
            usersSpan.addContent(addDeleteUserButton(loggedIn, loggedOut));
        }
    }

    private void resetUserGrids() {
        Result<Map<Integer, UserInfoService>> loggedInUsersRes = userService.getLoggedInUsers();
        Result<Map<Integer, UserInfoService>> loggedOutUsersRes = userService.getLoggedOutUsers();

        if (loggedInUsersRes.isError() || loggedOutUsersRes.isError()) {
            printError("Problem Refreshing grid");
        } else {
            loggedIn.setItems(loggedInUsersRes.getValue().values());
            loggedOut.setItems(loggedOutUsersRes.getValue().values());
        }
    }

    private Button addDeleteUserButton(Grid loggedin, Grid loggedout) {
        Button closePermButton = new Button("Remove User");
        closePermButton.setEnabled(false);
        closePermButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        loggedin.addSelectionListener(selection -> {
            int size = selection.getAllSelectedItems().size();
            boolean isSingleSelection = size == 1;
            closePermButton.setEnabled(isSingleSelection);
        });

        loggedout.addSelectionListener(selection -> {
            int size = selection.getAllSelectedItems().size();
            boolean isSingleSelection = size == 1;
            closePermButton.setEnabled(isSingleSelection);
        });

        ConfirmDialog dialog = addConfirmationDialog("Remove User?", "Are you sure you want to remove this user from the system?"); //pop up screen for confirmation
        dialog.addConfirmListener(event -> deleteUser(loggedin, loggedout));  //This is what it does when pressed delete

        closePermButton.addClickListener(e -> dialog.open());

        return closePermButton;
    }

    private void deleteUser(Grid loggedin, Grid loggedout) {
        if (loggedin.getSelectedItems().size() == 0) {
            deleteUser(loggedout);
        } else deleteUser(loggedin);
    }

    private void deleteUser(Grid<UserInfoService> grid) {
        UserInfoService userToRemove = grid.getSelectedItems().stream().toList().get(0);
        Result<Boolean> result = userService.removeUser(mainLayout.getCurrUserID(), userToRemove.getId());
        if (result.isError()) {
            printError("Error Removing User:\n" + result.getMessage());
        } else {
            printSuccess("Removed User");
            resetUserGrids();
        }
    }

    private void addStoresInfo(Grid grid) {
        Result<Map<Integer, StoreService>> storesRes = shoppingService.getAllStoresInfo();
        if (storesRes.isError()) {
            printError("Problem getting stores :(");
        } else {
            grid.setItems(storesRes.getValue().values());
        }
    }


    private void createGrid(AccordionPanel dropdown) {

        storeGrid = new Grid<StoreService>();
        Editor<StoreService> editor = storeGrid.getEditor();
        addStoresInfo(storeGrid);

        storeGrid.addColumn(StoreService::getStoreName).setHeader("Name").setSortable(true);
        storeGrid.addColumn(entry -> getOwnersString(entry)).setHeader("Owners").setSortable(true);
        storeGrid.addColumn(entry -> getManagersString(entry)).setHeader("Managers").setSortable(true);
        storeGrid.addColumn(StoreService::getStoreStatus).setHeader("Status").setSortable(true);


        Binder<StoreService> binder = new Binder<>(StoreService.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        dropdown.addContent(storeGrid);
        HorizontalLayout footer = addButtons(storeGrid);
        footer.setPadding(true);
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        dropdown.addContent(footer);

    }

    private String getOwnersString(StoreService store) {
        return store.getOwners().keySet()
                .stream()
                .map(userService::getUsername)
                .collect(Collectors.joining(", "));
    }

    private String getManagersString(StoreService store) {
        return store.getManagers().keySet()
                .stream()
                .map(userService::getUsername)
                .collect(Collectors.joining(", "));
    }

    private HorizontalLayout addButtons(Grid grid) {

        Button closePermButton = new Button("Close Store Permanently");
        closePermButton.setEnabled(false);
        closePermButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
//        closePermButton.getStyle().set("margin-inline-start", "auto");

        Button storeReceiptsButton = new Button("Get Store Receipts");
        storeReceiptsButton.setEnabled(false);

        //https://vaadin.com/docs/latest/components/button#:~:text=Show%20code-,Global%20vs.%20Selection%2DSpecific%20Actions,-In%20lists%20of
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(selection -> {
            int size = selection.getAllSelectedItems().size();
            boolean isSingleSelection = size == 1;
            closePermButton.setEnabled(isSingleSelection);
            storeReceiptsButton.setEnabled(isSingleSelection);
            //Not sure if I need all of this. This was made to multiple selection!
        });


        ConfirmDialog dialog = addConfirmationDialog("Delete Store?", "Are you sure you want to permanently delete this store?"); //pop up screen for confirmation
        dialog.addConfirmListener(event -> closeStorePermanently(grid));  //This is what it does when pressed delete
        closePermButton.addClickListener(e -> dialog.open());

        storeReceiptsButton.addClickListener((ComponentEventListener<ClickEvent<Button>>)
                buttonClickEvent -> getStoreReceipts(grid));

        HorizontalLayout footer = new HorizontalLayout(closePermButton, storeReceiptsButton);
        footer.setPadding(false);
        footer.setAlignItems(Alignment.CENTER);
        return footer;
    }

    private ConfirmDialog addConfirmationDialog(String header, String message) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(message);

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> printError("Canceled"));

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        return dialog;
    }


    private void getStoreReceipts(Grid<StoreService> grid) {
        int chosenId = getIdOfSelectedRow(grid);

        if (chosenId != -1) {
            Grid<ReceiptService> receiptsGrid = new Grid<>();
            Dialog dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setHeaderTitle("Receipts");
            Div div = new Div();
            div.add(receiptsGrid);
            dialog.add(div);
            dialog.setWidth("1000px");

            Result<List<ReceiptService>> result = shoppingService.getSellingHistoryOfStoreForManager(chosenId, mainLayout.getCurrUserID());

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

                    menu.addItem("View Items", event -> viewReceiptItemsAction(receiptsGrid, result.getValue(),
                            receiptsGrid.getSelectedItems().stream().toList().get(0).getId()));

                    Button cancelButton = new Button("Exit", e -> dialog.close());
                    dialog.getFooter().add(cancelButton);


                    add(dialog);
                    dialog.open();
                    //dialog.add(itemsGrid);
                    dialog.add(menu);
                }
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

    private void closeStorePermanently(Grid grid) {
        int chosenId = getIdOfSelectedRow(grid);
        if (chosenId != -1) {

            Result<Boolean> result = shoppingService.closeStorePermanently(mainLayout.getCurrUserID(), chosenId);
            if (result.isError()) {
                printError("Error in close store permanently:\n" + result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Closed Store");

//                    StoreService curr = shoppingService.getStoreInfo(chosenId).getValue();
//                    stores.replace(chosenId, curr);
                    addStoresInfo(grid);
//                    grid.getDataProvider().refreshItem(curr);
                } else {
                    printError("Something went wrong");
                }
                System.out.println(result.getValue());
            }
        }
    }


    private int getIdOfSelectedRow(Grid grid) {
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


    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }


}
