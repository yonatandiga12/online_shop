package PresentationLayer.views;

import BusinessLayer.NotificationSystem.Observer.NotificationObserver;
import PresentationLayer.views.MainLayout;
import ServiceLayer.Objects.ChatService;
import ServiceLayer.Objects.MessageService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoreMailbox extends VerticalLayout implements NotificationObserver {
    ShoppingService shoppingService;
    UserService userService;
    private Grid<ChatService> grid;
    private HashMap<Integer, ChatService> chats;
    private int otherSideId;
    private String otherSideName;
    private String myStoreName;
    private int myId;
    private Dialog chatDialog;
    private Dialog mainDialog;

    public StoreMailbox(int storeId, String storeName, ShoppingService _shoppingService, UserService _userService) {
        myId = storeId;
        myStoreName = storeName;
        shoppingService = _shoppingService;
        userService = _userService;

        Result<HashMap<Integer, ChatService>> chatsResult = shoppingService.getChats(myId);
        if (chatsResult.isError()) {
            add(chatsResult.getMessage());
        }
        else {
            chats = chatsResult.getValue();
            createGrid();
        }
    }

    private Grid<ChatService> createGrid() {
        grid = new Grid<>();
        Editor<ChatService> editor = grid.getEditor();
        grid.setItems(chats.values());

        grid.addColumn(ChatService::getOtherName).setHeader("Name").setSortable(true);
        Binder<ChatService> binder = new Binder<>(ChatService.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        Grid.Column<ChatService> enterChatColumn = grid.addComponentColumn(chat -> {
            Button enterChatButton = new Button("Enter Chat", event -> enterChatPressed(chat));
            return enterChatButton;
        }).setFlexGrow(1).setFrozenToEnd(true);

        return grid;
    }

    private HorizontalLayout addButtons() {
        Button startNewChat = new Button("New Chat", event -> newChatDialog());
        startNewChat.setEnabled(true);
        startNewChat.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startNewChat.getStyle().set("margin-inline-start", "auto");

        //https://vaadin.com/docs/latest/components/button#:~:text=Show%20code-,Global%20vs.%20Selection%2DSpecific%20Actions,-In%20lists%20of

        HorizontalLayout footer = new HorizontalLayout(startNewChat);
        footer.getStyle().set("flex-wrap", "wrap");
        setPadding(false);
        setAlignItems(Alignment.AUTO);
        return footer;
    }

    public void makeMailboxDialog(){
        mainDialog = new Dialog();
        grid = createGrid();

        mainDialog.add(grid);
        mainDialog.add(addButtons());



        mainDialog.setWidth("1000px");
        mainDialog.setDraggable(true);
        mainDialog.setResizable(true);
        mainDialog.open();
    }

    private void enterChatPressed(ChatService chat){
        refreshChats();

        otherSideName = chat.getOtherName();
        otherSideId = chat.getOtherSideId();

        chatDialog = new Dialog();
        chatDialog.setDraggable(true);
        chatDialog.setResizable(true);
        chatDialog.setHeaderTitle("Chat with " + otherSideName);
        chatDialog.setWidth("1000px");
        mainDialog.add(chatDialog);

        //make message list
        MessageList list = new MessageList();

        //make message input
        MessageInput input = new MessageInput();
        input.addSubmitListener(submitEvent -> {
            MessageListItem newMessage = new MessageListItem(
                    submitEvent.getValue(), Instant.now(), myStoreName);
            newMessage.setUserColorIndex(0);
            List<MessageListItem> items = new ArrayList<>(list.getItems());
            items.add(newMessage);
            list.setItems(items);

            try {
                userService.sendMessage(myId, otherSideId, newMessage.getText());
            } catch (Exception e) {
                Notification systemNotification = Notification
                        .show(e.getMessage());
                systemNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            refreshChats();
        });

        //load chat
        loadChatToMessageList(list);

        VerticalLayout chatLayout = new VerticalLayout(list, input);
        chatLayout.setHeight("500px");
        chatLayout.setWidth("400px");
        chatLayout.expand(list);
        chatLayout.expand(input);
        mainDialog.add(chatLayout);

        //dialog.add(list);
        chatDialog.add(chatLayout);
        chatDialog.add(input);

        //open the dialog
        chatDialog.open();
    }

    private void newChatDialog(){
        //make dialog
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Search For A New Chat");
        dialog.setWidth("1000px");
        mainDialog.add(dialog);

        //make searchbar
        TextField searchbar = new TextField();
        searchbar.setPlaceholder("Search");
        searchbar.setTooltipText("Enter the name of a user or a store");
        searchbar.setClearButtonVisible(true);
        //dialog.add(searchbar);

        //make start chat button
        Button startChatButton = new Button("Start Chat", event -> startChat(dialog));
        startChatButton.setEnabled(false);
        //dialog.add(startChatButton);
        HorizontalLayout startHorizontalLayout = new HorizontalLayout();
        startHorizontalLayout.setPadding(true);
        startHorizontalLayout.setAlignItems(Alignment.CENTER);
        startHorizontalLayout.add(startChatButton);
        dialog.add(startHorizontalLayout);

        //make search button
        Button searchButton = new Button("Search", event -> searchButtonPressed(searchbar.getValue(), startChatButton));
        //dialog.add(searchButton);




        //make search horizontal layout
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(true);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        horizontalLayout.add(searchbar);
        horizontalLayout.add(searchButton);
        dialog.add(horizontalLayout);

        //make vertical layout
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(true);
        verticalLayout.setAlignItems(Alignment.START);
        verticalLayout.add(horizontalLayout);
        verticalLayout.add(startHorizontalLayout);
        dialog.add(verticalLayout);

        //open the dialog
        dialog.open();
    }

    private void searchButtonPressed(String toSearch, Button startChatButton){
        Result<Integer> userIdRes = userService.getUserIdByName(toSearch);
        Result<Integer> storeIdRes;
        boolean found = false;

        if(userIdRes.isError()){
            storeIdRes = shoppingService.getStoreIdByName(toSearch);

            if(!storeIdRes.isError()){
                found = true;
                otherSideId = storeIdRes.getValue();
                otherSideName = toSearch;
            }

        }
        else{
            found = true;
            otherSideId = userIdRes.getValue();
            otherSideName = toSearch;
        }



        if(found){
            startChatButton.setEnabled(true);

            //notify the user that he can start the chat.
            Notification systemNotification = Notification
                    .show(toSearch + " was found, press \"Start Chat\" in order to start the conversation");
            systemNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else{
            Notification systemNotification = Notification
                    .show(toSearch + " was not found, please try again");
            systemNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void startChat(Dialog searchNewChatDialog){
        //close previous dialog
        searchNewChatDialog.close();

        //make dialog
        chatDialog = new Dialog();
        chatDialog.setDraggable(true);
        chatDialog.setResizable(true);
        chatDialog.setHeaderTitle("Chat with " + otherSideName);
        chatDialog.setWidth("1000px");
        mainDialog.add(chatDialog);

        //make message list
        MessageList list = new MessageList();

        //make message input
        MessageInput input = new MessageInput();
        input.addSubmitListener(submitEvent -> {
            MessageListItem newMessage = new MessageListItem(
                    submitEvent.getValue(), Instant.now(), myStoreName);
            newMessage.setUserColorIndex(0);
            List<MessageListItem> items = new ArrayList<>(list.getItems());
            items.add(newMessage);
            list.setItems(items);

            try {
                userService.sendMessage(myId, otherSideId, newMessage.getText());
            } catch (Exception e) {
                Notification systemNotification = Notification
                        .show(e.getMessage());
                systemNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            refreshChats();

        });

        //load chat
        loadChatToMessageList(list);

        VerticalLayout chatLayout = new VerticalLayout(list, input);
        chatLayout.setHeight("500px");
        chatLayout.setWidth("400px");
        chatLayout.expand(list);
        chatLayout.expand(input);
        mainDialog.add(chatLayout);

        //dialog.add(list);
        chatDialog.add(chatLayout);
        chatDialog.add(input);

        //open the dialog
        chatDialog.open();
    }

    private void loadChatToMessageList(MessageList list){
        if(!chats.containsKey(otherSideId)){
            return;
        }

        ChatService chat = chats.get(otherSideId);
        List<MessageListItem> items = new ArrayList<>(list.getItems());

        for(MessageService message : chat.getMessages()){
            MessageListItem message1 = new MessageListItem(
                    message.getContent(),
                    message.getSendingTime().toInstant(ZoneOffset.UTC),
                    fitUsernameToId(message.getSenderID()));
            message1.setUserColorIndex(fitColorToId(message.getSenderID()));

            items.add(message1);
        }

        list.setItems(items);
    }

    private String fitUsernameToId(int id){
        if(id == otherSideId){
            return otherSideName;
        }
        else{
            return myStoreName;
        }
    }

    private int fitColorToId(int id){
        if(id == otherSideId){
            return 1;
        }
        else{
            return 0;
        }
    }

    private void refreshChats(){
        Result<HashMap<Integer, ChatService>> chatsResult = userService.getChats(myId);
        chats = chatsResult.getValue();
        grid.setItems(chats.values());

        grid.getDataProvider().refreshAll();

        loadChatToMessageList(new MessageList());

    }

    @Override
    public void notify(String notification) {
        refreshChats();

    }

    @Override
    public void listenToNotifications(int userId) throws Exception {
        userService.listenToNotifications(userId, this);
    }
}
