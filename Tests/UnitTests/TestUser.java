package UnitTests;
import BusinessLayer.Market;
import BusinessLayer.Users.RegisteredUser;
import BusinessLayer.Users.UserFacade;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestUser {
    static Market market;
    static String goodPass1="ab123456";
    static String badPass1="123";
    static String userName1="avi1";
    static String userName2="avi2";
    static String userName3="avi3";
    static UserFacade userFacade;
    static String addressOk="addressOk";
    static LocalDate bDayOk=LocalDate.of(2022, 7, 11);
    static int id1;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        userFacade=market.getUserFacade();

        userFacade = spy(UserFacade.class);
        doReturn(true).when(userFacade).checkPassword(anyString());
        doReturn(true).when(userFacade).checkAddress(anyString());
        doReturn(true).when(userFacade).checkBDay(any());
        doReturn(true).when(userFacade).checkUserName(anyString());
        doNothing().when(userFacade).saveUserInDB(any());
        //id1=userFacade.registerUser(userName1,goodPass1,addressOk,bDayOk);
    }




    @Test
    public void registerShouldFailExisted(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkPassword(anyString());
            doReturn(true).when(userFacade).checkAddress(anyString());
            doReturn(true).when(userFacade).checkBDay(any());

            int id=userFacade.registerUser(userName1,goodPass1,addressOk,bDayOk);//assume pass
//            NotificationHub.getInstance().removeFromService(id);
            userFacade.registerUser(userName1,goodPass1,addressOk,bDayOk);
            fail("should not pass this test : userName existed");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }
    @Test
    public void registerShouldFailPassword(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkUserName(anyString());
            doReturn(true).when(userFacade).checkAddress(anyString());
            doReturn(true).when(userFacade).checkBDay(any());

            userFacade.registerUser(userName3,badPass1,addressOk,bDayOk);
            fail("should not pass this test : password incorrect");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }
    @Test
    public void registerShouldFailPassNull(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkUserName(anyString());
            doReturn(true).when(userFacade).checkAddress(anyString());
            doReturn(true).when(userFacade).checkBDay(any());

            userFacade.registerUser(userName3,null,addressOk,bDayOk);
            fail("should not pass this test : password is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }
    @Test
    public void registerShouldFailUserNull(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkPassword(anyString());
            doReturn(true).when(userFacade).checkAddress(anyString());
            doReturn(true).when(userFacade).checkBDay(any());

            userFacade.registerUser(null,goodPass1,addressOk,bDayOk);
            fail("should not pass this test : user name is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }

    @Test
    public void registerShouldFailAddressNull(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkUserName(anyString());
            doReturn(true).when(userFacade).checkPassword(anyString());
            doReturn(true).when(userFacade).checkBDay(any());

            userFacade.registerUser(null,goodPass1,null,bDayOk);
            fail("should not pass this test : address is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }
    @Test
    public void registerShouldFailBDayNull(){
        try {
            userFacade = spy(UserFacade.class);
            doReturn(true).when(userFacade).checkUserName(anyString());
            doReturn(true).when(userFacade).checkPassword(anyString());
            doReturn(true).when(userFacade).checkAddress(anyString());

            userFacade.registerUser(null,goodPass1,addressOk,null);
            fail("should not pass this test : bDay is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }

    @Test
    public void z1RegisterShouldPass(){
        try {
            doNothing().when(userFacade).saveUserInDB(any());
            id1 = userFacade.registerUser(userName2,goodPass1,addressOk,bDayOk);//assume pass
            assertTrue("Registration succeeded",true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void z2LogInShouldPass(){
        try {
            try {
                userFacade.logout(id1);
            }
            catch (Exception ignored) {}
            userFacade.logIn(userName2, goodPass1);
            assertTrue("logIn succeeded",true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    @Test
    public void logInShouldFailNotRegistered(){
        try {
            userFacade.logIn(userName3,badPass1);
            fail("should not pass this test : Not Registered");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }
    @Test
    public void logInShouldFailWrongPass(){
        try {
            userFacade.logIn(userName1,badPass1);
            fail("should not pass this test : password incorrect");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }

    @Test
    public void logInShouldFailPassNull(){
        try {
            userFacade.logIn(userName1,null);
            fail("should not pass this test : password is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);

        }
    }

    @Test
    public void logInShouldFailUserNull(){
        try {
            userFacade.logIn(null,badPass1);
            fail("should not pass this test : userName is Null");
        } catch (Exception e) {
            assertTrue("Test passed\nException thrown:"+e.getMessage(),true);
        }
    }

}

