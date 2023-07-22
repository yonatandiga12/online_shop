package PresentationLayer.views;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Users.Guest;
import ServiceLayer.Objects.UserInfoService;
import ServiceLayer.Result;
import ServiceLayer.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.time.ZoneId;

public class PurchaseViewManager {
    private TextField cardNumber;
    private TextField cardholderId;
    private Select<Integer> month;
    private Select<Integer> year;
    private ExpirationDateField expiration;
    private PasswordField cvv;

    private TextField name;
    private TextField zip;
    private TextField address;
    private TextField city;
    private TextField state;
    private DatePicker dateOfBirth;
    private String CARD_REGEX="[0-9]{8,19}";
    private UserService us;
    private UserInfoService user;
    public PurchaseViewManager() {
        try {
            us = new UserService();
            int id = MainLayout.getMainLayout().getCurrUserID();
            if (id > Guest.MAX_GUEST_USER_ID) {
                Result<UserInfoService> res =  us.getUser(id);
                if (!res.isError())
                    user = res.getValue();
            }
        }
        catch (Exception e) {
            printError("Cannot Purchase Right Now");
        }
    }

    public void checkOutEvent(double price, PurchaseSuccess succeed_to_buy) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.getStyle().set("margin-right", "auto");

        Button buy = new Button("Buy", event -> {
            if (buyEvent(succeed_to_buy))
                dialog.close();
        });
        buy.setThemeName("success primary");

        FormLayout formLayout = new FormLayout();
        formLayout.add(createTitle());
        formLayout.add(createFormLayout());
        formLayout.add(createSupplyFormLayout());
        formLayout.add(new H3("Final Price: " + price));
        dialog.add(formLayout);
        dialog.getFooter().add(cancel, buy);
        dialog.open();
    }

    private boolean buyEvent(PurchaseSuccess purchaseSuccess) {
        boolean isValid= isAllFieldsValid();
        if (isValid){
            PurchaseInfo purchaseInfo = new PurchaseInfo(
                    cardNumber.getValue(), month.getValue(), year.getValue(),
                    name.getValue(),  Integer.parseInt(cvv.getValue()), Integer.parseInt(cardholderId.getValue()), dateOfBirth.getValue());
            SupplyInfo supplyInfo = new SupplyInfo(
                    name.getValue(), address.getValue(), city.getValue(), state.getValue(), zip.getValue());
            purchaseSuccess.purchase(purchaseInfo, supplyInfo);
            return true;
        }
        else
            printError("not all filed ok");
        return false;
    }
    private boolean isAllFieldsValid() {
        if (user != null) {
            return !expiration.isInvalid() & year.getValue() != null & month.getValue() != null
                    & !name.isInvalid() & name != null
                    & !cardholderId.isInvalid() & cardholderId.getValue() != null
                    & !cardNumber.isInvalid() & cardNumber.getValue() != null
                    & !cvv.isInvalid() & cvv.getValue() != null;
        }
        else {
            return !expiration.isInvalid() & year.getValue() != null & month.getValue() != null
                    & !name.isInvalid() & name != null
                    & !cardholderId.isInvalid() & cardholderId.getValue() != null
                    & !cardNumber.isInvalid() & cardNumber.getValue() != null
                    & !cvv.isInvalid() & cvv.getValue() != null
                    & !address.isInvalid() & address.getValue() != null
                    & !zip.isInvalid() & zip.getValue() != null
                    & !city.isInvalid() & city.getValue() != null
                    & !state.isInvalid() & state.getValue() != null
                    & !dateOfBirth.isInvalid() & dateOfBirth.getValue() != null;
        }
    }

    private Component createSupplyFormLayout() {
        zip = new TextField("ZIP");
        zip.setPattern("[0-9]{8}");
        zip.setPlaceholder("20912345");
        zip.setAllowedCharPattern("[\\d ]");
        zip.setRequired(true);
        zip.setErrorMessage("ZIP must contains 8 digits.");
        zip.setMaxLength(8);
        zip.setWidth("150px");

        address =new TextField("Address");
        address.setErrorMessage("This field is required");
        address.setWidthFull();
        address.setRequired(true);

        city =new TextField("City");
        city.setErrorMessage("This field is required");
        city.setRequired(true);

        state =new TextField("State");
        state.setErrorMessage("This field is required");
        state.setRequired(true);

        FormLayout formLayout = new FormLayout();
        HorizontalLayout addressZip =new HorizontalLayout(address,zip);
        HorizontalLayout cityState =new HorizontalLayout(city,state);
        formLayout.add(addressZip, cityState);
        if (user != null) {
            formLayout.add(addressZip, cityState);
        }
        return formLayout;
    }
    private boolean validateId(String id) {
        if (id.length() != 9 || !id.matches("[0-9]+")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < id.length(); i++) {
            int incNum = Character.getNumericValue(id.charAt(i)) * ((i % 2) + 1);
            sum += (incNum > 9) ? incNum - 9 : incNum;
        }

        return sum % 10 == 0;
    }

    private boolean isValid(){
        int currentYear = LocalDate.now().getYear()-2000;
        int currentMonth = LocalDate.now().getMonth().getValue();
        int selectedYear = year.getValue()!= null ?year.getValue():0;
        int selectedMonth = month.getValue()!= null ?month.getValue():0;

        return selectedYear > currentYear || (selectedYear == currentYear && selectedMonth >= currentMonth);
    }

    private Component createFormLayout() {
        name =new TextField("Name");
        name.setErrorMessage("This field is required"+name.isInvalid());
        name.setRequired(true);
        name.setWidthFull();

        cardNumber = new TextField("Credit card number");
        cardNumber.setPlaceholder("1234 5678 9123 4567");
        cardNumber.setPattern(CARD_REGEX);
        cardNumber.setAllowedCharPattern("[\\d ]");
        cardNumber.setRequired(true);
        cardNumber.setErrorMessage("This field is required");
        cardNumber.setMinLength(8);
        cardNumber.setMaxLength(19);


        cardholderId = new TextField("ID");
        cardholderId.setPattern("[0-9]{9}");
        cardholderId.setPlaceholder("209123456");
        cardholderId.setAllowedCharPattern("[\\d ]");
        cardholderId.setRequired(true);
        cardholderId.setErrorMessage("ID must contains 9 digits.");
        cardholderId.setMaxLength(9);
        cardholderId.setWidth("150px");
        cardholderId.addValidationStatusChangeListener(event -> {
            if (cardholderId.getValue().length()==9) {
                cardholderId.setErrorMessage("invalid ID");
                cardholderId.setInvalid(!validateId(cardholderId.getValue()));
            }
            else cardholderId.setErrorMessage("ID must contains 9 digits.");

        });

        month = new Select<>();
        month.setPlaceholder("Month");
        month.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

        year = new Select<>();
        year.setPlaceholder("Year");
        year.setItems(23, 24, 25, 26, 27, 28);

        expiration = new ExpirationDateField("Expiration date", month, year);

        cvv = new PasswordField("CVV");
        cvv.setRequired(true);
        cvv.setMinLength(3);
        cvv.setMaxLength(4);
        cvv.setPlaceholder("123");
        cvv.setPattern("[0-9]{3,4}");
        cvv.setAllowedCharPattern("[\\d ]");
        cvv.setErrorMessage("ID must contains 3 or 4 digits.");
        cvv.setRequiredIndicatorVisible(true);

        dateOfBirth = new DatePicker("Birthday");
        dateOfBirth.setWidth("150px");
        dateOfBirth.addValueChangeListener(event -> {
            dateOfBirth.setPlaceholder(event.getValue().toString());
        });
        dateOfBirth.setRequired(true);
        dateOfBirth.setErrorMessage("This field is required");
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        dateOfBirth.setMax(now);
        LocalDate start_date = LocalDate.of(2000, 1, 1);
        dateOfBirth.setInitialPosition(start_date);


        HorizontalLayout nameIdLayout=new HorizontalLayout(name,cardholderId);
        HorizontalLayout expDateCvv=new HorizontalLayout(expiration,cvv);
        HorizontalLayout cardNum=new HorizontalLayout(cardNumber);
        HorizontalLayout bday=new HorizontalLayout(dateOfBirth);
        FormLayout formLayout = new FormLayout();

        if (user != null) {
            formLayout.add(nameIdLayout, cardNum, expDateCvv);
            dateOfBirth.setValue(user.getBirthday());
        }
        else {
            formLayout.add(nameIdLayout, cardNum, expDateCvv, bday);
        }
        return formLayout;
    }

    private void printSuccess(String msg) {
        Notification notification = Notification.show(msg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

    private void printError(String errorMsg) {
        Notification notification = Notification.show(errorMsg, 2000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private Component createTitle() {
        return new H3("Credit Card and Billing Info");
    }


    private class ExpirationDateField extends CustomField<String> {
        public ExpirationDateField(String label, Select<Integer> month, Select<Integer> year) {
            setLabel(label);
            HorizontalLayout layout = new HorizontalLayout(month, year);
            layout.setFlexGrow(1.0, month, year);
            month.setWidth("100px");
            year.setWidth("100px");
            setErrorMessage("expire date must be bigger than the current date.");
            year.addValueChangeListener(event -> {
                expiration.setError(isValid());
            });
            month.addValueChangeListener(event -> {
                expiration.setError(isValid());
            });
            add(layout);
        }


        public void setError(boolean isValid) {
            if (isValid) {
                setInvalid(false);
                year.setInvalid(false);
                year.setErrorMessage("");
                month.setInvalid(false);
            } else {
                setInvalid(true);
                year.setInvalid(true);
                month.setInvalid(true);
            }
        }

        @Override
        protected String generateModelValue() {
            // Unused as month and year fields part are of the outer class
            return "";
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            // Unused as month and year fields part are of the outer class
        }

    }
}
