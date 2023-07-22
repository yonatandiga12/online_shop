package UnitTests;

import BusinessLayer.Market;
import BusinessLayer.Stores.StoreFacade;
import BusinessLayer.Users.UserFacade;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class AppointmentTest {
    static UserFacade userFacade;
    static StoreFacade storeFacade;
    static Market market;
    static String addressOk="addressOkAppointmentTest";
    static String goodPass1="ab123456AppointmentTest";
    static String userName2="avi2AppointmentTest";
    static String userName1="avi1AppointmentTest";
    static LocalDate bDayOk=LocalDate.of(2022, 7, 11);
    static int userId1;
    static int userId2;
    static int StoreId;
    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("env", "test");
        market = Market.getInstance();
        userFacade = market.getUserFacade();
        storeFacade=market.getStoreFacade();
        userId1=market.register(userName1,goodPass1,addressOk,bDayOk);
        userId2=market.register(userName2,goodPass1,addressOk,bDayOk);
        market.login(userName1,goodPass1);
        market.login(userName2,goodPass1);
        StoreId = market.addStore(userId1,"StoreName");
    }
    @Test
    public void appointShouldFailNotOwner() throws Exception {
        try {
            storeFacade.addAppointment(StoreId,userId2,userId1);
             fail("should not pass this test : userId2 is not the owner");
        } catch (Exception e) {
            assertEquals(e.getMessage(),"creator must be this store owner");
        }
    }

    @Test
    public void appointShouldFailStoreNotFound() throws Exception {
        try {
            storeFacade.addAppointment(-5,userId2,userId1);
            fail("should not pass this test : store id is negative");
        } catch (Exception e) {
            assertEquals(e.getMessage(),"id cant be negative");
        }
    }

    @Test
    public void appointShouldFailUserNotFound() throws Exception {
        try {
            storeFacade.addAppointment(StoreId,-5,userId1);
            fail("should not pass this test : userId is negative");
        } catch (Exception e) {
            assertEquals(e.getMessage(),"id cant be negative");
        }
    }

    @Test
    public void appointShouldFailUserNotFound2() throws Exception {
        try {
            storeFacade.addAppointment(StoreId,userId1,-5);
            fail("should not pass this test : userId is negative");
        } catch (Exception e) {
            assertEquals(e.getMessage(),"id cant be negative");
        }
    }


}
