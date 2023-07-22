package ServiceLayer;

import BusinessLayer.Log;
import BusinessLayer.Market;
import BusinessLayer.NotificationSystem.Chat;
import BusinessLayer.NotificationSystem.Message;
import BusinessLayer.NotificationSystem.Observer.NotificationObserver;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.User;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import ServiceLayer.Objects.ChatService;
import ServiceLayer.Objects.MessageService;
import ServiceLayer.Objects.UserInfoService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.logging.Logger;

public class UserService {
    private static final Logger log = Log.log;
    private final Market market;
    private UserInfoService loggedInUser;
    public UserService() throws Exception {
        market = Market.getInstance();
    }

    public Result<Integer> addGuest() {
        try {
            User newGuest = market.addGuest();
            log.info("Created new guest with ID (Session): " + newGuest.getId());
            return new Result<Integer>(false, newGuest.getId());
        }
        catch (Exception e) {
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> system_shutdown(int userID) {
        try {
            boolean res = market.system_shutdown(userID);
            return new Result<>(false, res);
        }
        catch (Exception e) {
            return new Result<Boolean>(true, e.getMessage());
        }
    }

    public Result<Integer> login(String userName, String pass) {
        try {
            int id=market.login(userName,pass);
            log.info("logIn succeeded");
            return new Result<>(false, id);//login == true
        } catch (Exception e) {
            log.info("logIn failed");
            return new Result<>(true, e.getMessage());//login==false
        }
    }

    public Result<Integer> register(String userName, String pass, String address, LocalDate bDay) {
        try {
            int id=market.register(userName, pass, address, bDay);
            log.info("register succeeded");
            return new Result<>(false, id);//login == true,isErr==false
        } catch (Exception e) {
            log.info("register failed");
            return new Result<>(true, e.getMessage());//login==false
        }
    }

    public Result<Boolean> logout(int userID) {
        try {
            market.logout(userID);
            log.info("Logout succeeded");
            return new Result<>(false, true);//login == true,isErr==false
        } catch (Exception e) {
            log.info("Logout failed");
            return new Result<>(true, e.getMessage());//login==false
        }
    }

    public Result<Boolean> addOwner(int userID, int userToAddID, int storeID) {
        try {
            market.addOwner(userID, userToAddID, storeID);
            log.info("Added user to list of store owners");
            return new Result<>(false, true);
        } catch (Exception e) {
            log.info("failed to add user to store owners");
            return new Result<>(true, e.getMessage());
        }
    }


    public Result<Boolean> addManager(int userID, int userToAdd, int storeID) {
        try {
            market.addManager(userID, userToAdd, storeID);
            log.info("Added user to list of store managers");
            return new Result<>(false, true);
        } catch (Exception e) {
            log.info("failed to add user to store managers");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> removeOwner(int userID, int userToRemove, int storeID) {
        try {
            market.removeOwner(userID, userToRemove, storeID);
            log.info("removed owner and subsequent owners/managers");
            return new Result<>(false, true);
        } catch (Exception e) {
            log.info("failed to remove owner or subsequent owners/managers");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> removeManager(int userID, int userToRemove, int storeID) {
        try {
            market.removeManager(userID, userToRemove, storeID);
            log.info("removed manager");
            return new Result<>(false, true);
        } catch (Exception e) {
            log.info("failed to remove store manager");
            return new Result<>(true, e.getMessage());
        }
    }

    /*
    here instead of StoreService bc only system admin can do this?
     */
    public Result<Boolean> closeStorePermanently(int userID, int storeID) throws Exception
    {
        try {
            market.closeStorePermanently(userID, storeID);
            log.info("closed store");
            return new Result<>(false, true);
        } catch (Exception e) {
            log.info("failed to close store");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> sendMessage(int userID, int receiverID, String content) throws Exception
    {
        boolean answer = market.sendMessage(userID, receiverID, content);

        if(answer){
            return new Result<Boolean>(false, "Success");
        }
        else{
            return new Result<Boolean>(true, "Failure");
        }
    }

//    public Result<Boolean> markMessageAsRead(int userID, MessageService messageService){
//        try{
//            Message message = new Message(messageService);
//
//            market.markMessageAsRead(userID, message);
//
//            return new Result<Boolean>(false, "Success");
//        }
//        catch(Exception e){
//            return new Result<Boolean>(true, e.getMessage());
//        }
//    }
//
//    public Result<Boolean> markMessageAsNotRead(int userID, MessageService messageService){
//        try{
//            Message message = new Message(messageService);
//
//            market.markMessageAsNotRead(userID, message);
//
//            return new Result<Boolean>(false, "Success");
//        }
//        catch(Exception e){
//            return new Result<Boolean>(true, e.getMessage());
//        }
//    }
//
//    public Result<List<MessageService>> watchNotReadMessages(int userID) throws Exception
//    {
//        List<MessageService> messageServices;
//        List<Message> messages = market.watchNotReadMessages(userID);
//
//        if(messages == null){
//            return new Result<>(true, "Failure");
//        }
//
//        messageServices = messageListToMessageServiceList(messages);
//
//        return new Result<>(false, messageServices);
//
//    }
//
//    private List<MessageService> messageListToMessageServiceList(List<Message> messages){
//        List<MessageService> toReturn = new ArrayList<>();
//
//        for(Message message : messages){
//            toReturn.add(new MessageService(message));
//        }
//
//        return toReturn;
//    }
//
//    public Result<List<MessageService>> watchReadMessages(int userID) throws Exception
//    {
//        List<MessageService> messageServices;
//        List<Message> messages = market.watchReadMessages(userID);
//
//        if(messages == null){
//            return new Result<>(true, "Failure");
//        }
//
//        messageServices = messageListToMessageServiceList(messages);
//
//        return new Result<>(false, messageServices);
//
//    }
//
//    public Result<List<MessageService>> watchSentMessages(int userID) throws Exception
//    {
//        List<MessageService> messageServices;
//        List<Message> messages = market.watchSentMessages(userID);
//
//        if(messages == null){
//            return new Result<>(true, "Failure");
//        }
//
//        messageServices = messageListToMessageServiceList(messages);
//
//        return new Result<>(false, messageServices);
//
//    }

    public Result<HashMap<Integer, ChatService>> getChats(int userId){
        try{
            ConcurrentHashMap<Integer, Chat> _chats = market.getChats(userId);
            HashMap<Integer, ChatService> chats = new HashMap<>();

            for(Integer id : _chats.keySet()){
                chats.put(id, new ChatService(_chats.get(id), market.getNameById(userId), market.getNameById(id)));
            }

            return new Result<>(false, chats);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Map<Integer, UserInfoService>> getAllRegisteredUsers() {
        try{
            Map<Integer, RegisteredUser> users = market.getAllRegisteredUsers();
            Map<Integer, UserInfoService> usersService = new HashMap<>();
            for(Map.Entry<Integer, RegisteredUser> user : users.entrySet()){
                usersService.put(user.getKey(), new UserInfoService(user.getValue()));
            }
            log.info("Users information received successfully");
            return new Result<>(false, usersService);
        }
        catch (Exception e){
            log.info("Users information not received");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Map<Integer, UserInfoService>> getLoggedInUsers() {
        try{
            Map<Integer, RegisteredUser> users = market.getLoggedInUsers();
            Map<Integer, UserInfoService> usersService = new HashMap<>();
            for(Map.Entry<Integer, RegisteredUser> user : users.entrySet()){
                usersService.put(user.getKey(), new UserInfoService(user.getValue()));
            }
            log.info("Users information received successfully");
            return new Result<>(false, usersService);
        }
        catch (Exception e){
            log.info("Users information not received");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Map<Integer, UserInfoService>> getLoggedOutUsers() {
        try{
            Map<Integer, RegisteredUser> users = market.getLoggedOutUsers();
            Map<Integer, UserInfoService> usersService = new HashMap<>();
            for(Map.Entry<Integer, RegisteredUser> user : users.entrySet()){
                usersService.put(user.getKey(), new UserInfoService(user.getValue()));
            }
            log.info("Users information received successfully");
            return new Result<>(false, usersService);
        }
        catch (Exception e){
            log.info("Users information not received");
            return new Result<>(true, e.getMessage());
        }
    }

    public boolean isAdmin(int currUserID) {
        return market.isAdmin(currUserID);
    }

    public Result<Boolean> addAdmin(int currUserID, int newUserID) {
        try {
            market.addAdmin(currUserID, newUserID);
            return new Result<Boolean>(false, "User was given admin permissions");
        }
        catch (Exception e) {
            return new Result<Boolean>(true, e.getMessage());
        }
    }
    public boolean isOwnerOrManager(int currUserID) {
        return market.getUserFacade().isOwnerOrManager(currUserID);
    }


    public Result<List<UserInfoService>> getAllOwnersIDefined(int ownerId) {
        try{
            Map<RegisteredUser, Set<Integer>> users = market.getAllOwnersIDefined(ownerId);
            List<UserInfoService> usersService = new ArrayList<>();
            for(Map.Entry<RegisteredUser, Set<Integer>> entry: users.entrySet()){
                usersService.add(new UserInfoService(entry.getKey(), entry.getValue(), "owner"));
            }
            log.info("Users information received successfully");
            return new Result<>(false, usersService);
        }
        catch (Exception e){
            log.info("Users information not received");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<List<UserInfoService>> getAllManagersIDefined(int ownerId) {
        try{
            Map<RegisteredUser, Set<Integer>> users = market.getAllManagersIDefined(ownerId);
            List<UserInfoService> usersService = new ArrayList<>();
            for(Map.Entry<RegisteredUser, Set<Integer>> entry: users.entrySet()){
                usersService.add(new UserInfoService(entry.getKey(), entry.getValue(), "manager"));
            }
            log.info("Users information received successfully");
            return new Result<>(false, usersService);
        }
        catch (Exception e){
            log.info("Users information not received");
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> removeUser(int userID, int userToRemove) {
        try {
            market.removeUser(userID, userToRemove);
            return new Result(false, true);
        }
        catch (Exception e) {
            return new Result(true, e.getMessage());
        }
    }

    public Result<Boolean> addCouponToCart(int userId, String coupon){
        try{
            market.addCouponToCart(userId, coupon);
            return new Result<>(false, true);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> removeCouponFromCart(int userId, String coupon){
        try{
            market.removeCouponFromCart(userId, coupon);
            return new Result<>(false, true);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<List<String>> getCoupons(int userId){
        try{
            List<String> res = market.getCoupons(userId);
            return new Result<>(false, res);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }

    public Result<Boolean> listenToNotifications(int userId, NotificationObserver listener){
        try{
            market.getUserFacade().listenToNotifications(userId, listener);
            return new Result<>(false, "good");
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }

    public String getUsername(Integer key) {
        try {
            return market.getUserFacade().getRegisteredUser(key).getUsername();
        }
        catch (Exception e) {
            return null;
        }
    }

    public Result<Integer> getUserIdByName(String name){
        try{
            int id = market.getUserIdByName(name);
            return new Result<>(false, id);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }
    }
    public Result<String> getAddress(int userID){
        try{
            String address = market.getUserAddress(userID);
            return new Result<>(false, address);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }

    }
    public Result<LocalDate> getBDay(int userID){
        try{
            LocalDate bDay = market.getUserBDay(userID);
            return new Result<>(false, bDay);
        }
        catch(Exception e){
            return new Result<>(true, e.getMessage());
        }

    }

    public Result<UserInfoService> getUser(int id) {
        try {
            RegisteredUser user = market.getUserFacade().getRegisteredUser(id);
            return new Result<>(false, new UserInfoService(user));
        }
        catch (Exception e) {
            return new Result<>(true, e.getMessage());
        }
    }
}
