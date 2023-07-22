package PresentationLayer.views;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import ServiceLayer.Objects.BidService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Bids")
@Route(value = "bids", layout = MainLayout.class)
@PreserveOnRefresh
public class BidView extends VerticalLayout {

    ShoppingService shoppingService;
    Grid<BidService> grid;
    PurchaseViewManager purchaseViewManager;
    public BidView() {
        try {
            shoppingService = new ShoppingService();
        } catch (Exception e) {
            add("Problem initiating Shefa Isaschar :(");
        }
        purchaseViewManager = new PurchaseViewManager();
        setSpacing(false);
        Result<List<BidService>> bids = shoppingService.getUserBids(MainLayout.getMainLayout().getCurrUserID());
        if (bids.isError()) {
            add("Problem getting bids :(");
        } else {
            grid = createGrid(bids.getValue());
            addMenuItems(grid);
            add(grid);
        }
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    private void gridRefresh() {
        grid.setItems(shoppingService.getUserBids(MainLayout.getMainLayout().getCurrUserID()).getValue());
    }
    private Grid<BidService> createGrid(List<BidService> bids) {
        Grid<BidService> grid = new Grid<>();
        grid.setItems(bids);
        grid.addColumn(e -> shoppingService.getStoreName(e.getStoreId())).setHeader("Store").setSortable(true).setResizable(true);
        grid.addColumn(BidService::getItemName).setHeader("Item").setSortable(true).setResizable(true);
        grid.addColumn(BidService::getOriginalPrice).setHeader("Original Price").setSortable(true).setResizable(true);
        grid.addColumn(BidService::getNewPrice).setHeader("Offered Price").setSortable(true).setResizable(true);
        grid.addColumn(BidService::getStatus).setHeader("Status").setSortable(true).setResizable(true);
        grid.addColumn(e->e.getStatus().equals(BidService.COUNTERED_STRING) ? e.getCounterOffer() : "No Counter-Offers")
                .setHeader("Counter-Offer").setSortable(true).setResizable(true);
        return grid;
    }

    private void addMenuItems(Grid<BidService> grid) {
        GridContextMenu<BidService> menu = grid.addContextMenu();
        grid.addSelectionListener(e ->
        {
            if (e.getAllSelectedItems().size() == 0) {
                menu.close();
            }
        });
        menu.setOpenOnClick(true);
        menu.addItem("Pay for Bid", event -> payForBid(grid));
        menu.addItem("Cancel Bid", event -> cancelBid(grid));
    }

    private void cancelBid(Grid<BidService> grid) {
        BidService bid = grid.getSelectedItems().stream().toList().get(0);
        shoppingService.cancelBid(bid.getStoreId(), bid.getId());
        gridRefresh();
    }

    private void payForBid(Grid<BidService> grid) {
        BidService bid = grid.getSelectedItems().stream().toList().get(0);
        if (!(bid.getStatus().equals(BidService.APPROVED_STRING) || bid.getStatus().equals(BidService.COUNTERED_STRING))) {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Bid is not approved yet, Cannot purchase");
            dialog.open();
        }
        else {
            purchaseViewManager.checkOutEvent(Math.max(bid.getNewPrice(), bid.getCounterOffer()),
                    (PurchaseInfo p, SupplyInfo s) -> {
                        Result<Boolean> r = shoppingService.payForBid(bid.getStoreId(), bid.getId(), p, s);
                        if (r.isError()) {
                            printError(r.getMessage());
                        }
                        else if (r.getValue()) {
                            printSuccess("Bid Successfully Completed and paid for, Delivery has been scheduled");
                        }
                        else {
                            printSuccess(r.getMessage());
                        }
                        gridRefresh();
                    });
        }
    }

//    private HorizontalLayout addButtons() {
//        Button startNewChat = new Button("New Chat", event -> newChatDialog());
//        startNewChat.setEnabled(true);
//        startNewChat.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        startNewChat.getStyle().set("margin-inline-start", "auto");
//
//        //https://vaadin.com/docs/latest/components/button#:~:text=Show%20code-,Global%20vs.%20Selection%2DSpecific%20Actions,-In%20lists%20of
//
//
//        HorizontalLayout footer = new HorizontalLayout(startNewChat);
//        footer.getStyle().set("flex-wrap", "wrap");
//        setPadding(false);
//        setAlignItems(Alignment.AUTO);
//        return footer;
//    }


    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

}
