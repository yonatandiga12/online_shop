package PresentationLayer.views.storeManagement;

import PresentationLayer.views.MainLayout;
import ServiceLayer.Objects.AppointmentService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDialog extends VerticalLayout {
    Grid<AppointmentService> appointmentsGrid;
    ShoppingService shoppingService;
    UserService userService;
    private MainLayout mainLayout;

    public AppointmentDialog() {
        try {
            shoppingService = new ShoppingService();
            userService = new UserService();
        } catch (Exception e) {
            add("Problem initiating Store:(");
        }
        mainLayout = MainLayout.getMainLayout();

        createAppointmentDialog();
    }


    private void createAppointmentDialog() {

        appointmentsGrid = new Grid<>();
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Open Appointments");
        Div div = new Div();
        div.add(appointmentsGrid);
        dialog.add(div);
        dialog.setWidth("1000px");

        Result<List<AppointmentService>> result = shoppingService.getUserAppointments(mainLayout.getCurrUserID());
        if (!result.isError()) {
            List<AppointmentService> appointments;
            if (result.getValue() == null) {
                appointments = new ArrayList<>();
            } else
                appointments = result.getValue();
            appointmentsGrid.setItems(appointments);
            appointmentsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

            appointmentsGrid.addColumn(e -> shoppingService.getStoreName(e.getStoreId())).setHeader("Store").setSortable(true);
            appointmentsGrid.addColumn(e -> userService.getUsername(e.getNewOwnerId())).setHeader("Candidate").setSortable(true);
            appointmentsGrid.addColumn(e -> String.join(", ", e.getAccepted().stream().map(id -> userService.getUsername(id)).toList())).setHeader("Accepted").setSortable(true);
            appointmentsGrid.addColumn(e -> String.join(", ", e.getNotYetAnswer().stream().map(id -> userService.getUsername(id)).toList())).setHeader("Waiting For Response").setSortable(true);


            GridContextMenu<AppointmentService> menu = appointmentsGrid.addContextMenu();
            menu.setOpenOnClick(true);

            menu.addItem("Accept", event -> {
                if (event.getItem().isPresent())
                    acceptAppointment(event.getItem().get());
            });

            menu.addItem("Reject", event -> {
                if (event.getItem().isPresent())
                    rejectAppointment(event.getItem().get());
            });

            Button cancelButton = new Button("Exit", e -> dialog.close());
            dialog.getFooter().add(cancelButton);

            dialog.open();
            dialog.add(menu);
        }
    }

    private void rejectAppointment(AppointmentService appServ) {
        AppointmentService appointmentService = getSelectedAppointmentFromGrid(appointmentsGrid);
        if (appointmentService == null)
            printError("You didn't choose a Appointment");
        else {
            Result<Boolean> result = shoppingService.rejectAppointment(appointmentService.getStoreId(), appServ.getNewOwnerId());
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("Rejected Appointment");
                } else {
                    printError("Something went wrong");
                }
            }
        }
        refreshAppointmentsGrid();
    }

    private void acceptAppointment(AppointmentService appServ) {
        AppointmentService appointmentService = getSelectedAppointmentFromGrid(appointmentsGrid);
        if (appointmentService == null)
            printError("You didn't choose a Appointment");
        else {
            //this is leftover from bid
//            Result<Boolean> result = shoppingService.approve(
//                    appointmentService.getStoreId(), appointmentService.getCreatorId(), mainLayout.getCurrUserID());
            Result<Boolean> result = shoppingService.acceptAppointment(appointmentService.getStoreId(), mainLayout.getCurrUserID(), appServ.getNewOwnerId());
            if (result.isError()) {
                printError(result.getMessage());
            } else {
                if (result.getValue()) {
                    printSuccess("All managers accepted the Appointment");
                } else {
                    printSuccess("Accepted, waiting for other managers to respond");
                }
            }
        }
        refreshAppointmentsGrid();
    }

    private AppointmentService getSelectedAppointmentFromGrid(Grid<AppointmentService> grid) {
        List<AppointmentService> appointments = grid.getSelectedItems().stream().toList();
        if (appointments.size() > 1) {
            printError("Chosen More than one!");
            return null;
        } else if (appointments.size() == 0) {
            printError("You need to choose a Appointment!");
            return null;
        } else {
            return appointments.get(0);
        }
    }

    private void refreshAppointmentsGrid() {
        Result<List<AppointmentService>> result = shoppingService.getUserAppointments(mainLayout.getCurrUserID());
        if (!result.isError()) {
            List<AppointmentService> appointments;
            if (result.getValue() == null) {
                appointments = new ArrayList<>();
            } else
                appointments = result.getValue();
            appointmentsGrid.setItems(appointments);
        }
    }

    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
