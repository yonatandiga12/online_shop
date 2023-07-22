package Acceptance;

import ServiceLayer.Objects.CartService;
import org.junit.*;

import static org.junit.Assert.*;

public class GuestVisitorTests extends ProjectTest{


    public static boolean init = false;

    @Before
    public void setUp() {
        super.setUp();
        if(!init) {
            setUpUser2();
            setUpUser3();
            init = true;
        }
    }


    @After
    public void tearDown() {
        //delete stores and delete users from DB
        //delete info from registerUserValid()
    }


    /**
     * Load System #7
     */

    @Test
    public void loadSystemValid(){
        assertTrue(setUser("aa", "AAAAAAAA", MEMBER, LOGGED) > 0);
    }



    /**
     * Exit System #8
     */

    @Test
    public void exitSystemGuest_Valid(){
        this.exitSystem(user1GuestId);
        CartService cart = this.getCart(user1GuestId);
        assertNull(cart);
    }



    /**
     * Register User #9
     * same as #24
     */
    @Test
    public void registerUserValid(){
        int id = this.registerUser("unusedUserName", "unusedPassword!23");
        assertTrue(id > 0);

    }

    @Test
    public void registerUsedUser(){
        int id = this.registerUser("User2GuestVisitorTests", "User2!");
        assertTrue(id < 0);
    }

    @Test
    public void registerFaultyUser(){
        int id1 = this.registerUser("aa", "!");
        assertTrue(id1 < 0);

        int id2 = this.registerUser("aaaaa", "Y");
        assertTrue(id2 < 0);

        int id3 = this.registerUser("aaaaa", "a");
        assertTrue(id3 < 0);

    }



    /**
     * Login User #10
     */
    @Test
    public void loginUserValid(){
        boolean loggedIn = this.loginUser("User3GuestVisitorTests", "User3!");
        assertTrue(loggedIn);

        //boolean check = checkIfLoggedIn(user3NotLoggedInId);
        //assertTrue(check);  /** Check Here*/
    }

    @Test
    public void loginUserWrongPassword(){
        boolean loggedIn = this.loginUser("User3", "Y!");
        assertFalse(loggedIn);
    }

    @Test
    public void loginUserNotRegistered(){
        boolean loggedIn = this.loginUser("User1", "Yona123!");
        assertFalse(loggedIn);

        loggedIn = this.loginUser("User0", "Yona123!");
        assertFalse(loggedIn);
    }





    protected static int user1GuestId = -1;         //guest - active
    protected static int user2LoggedInId = -1;
    protected static int user3NotLoggedInId = -1;   // registered, not logged in
    protected static int user5ManagerOfStore2ToBeRemoved = -1; //Owner/Manager of store2, to be removed positioned  by user2
    protected static int user6OwnerOfStore2 = -1;            //Owner/Manager of store2, positioned by user2
    protected static int store2Id = -1;             //store is open
    protected static int store2ClosedId = -1;


    protected void setUpUser1(){
        user1GuestId = setUser("User1","User1!", GUEST, NOT_LOGGED);
    }


    /**
     * User2: Member, logged in, Store Owner and Manager of store2
     */
    protected void setUpUser2(){
        if(user2LoggedInId != -1){
            return;
        }
        user2LoggedInId = setUser("User2GuestVisitorTests","User2!", MEMBER, LOGGED);
        store2Id = createStore(user2LoggedInId, "Store2"); //store is open

    }

    /**
     * User3: Member, Not logged in, Has a cart with items
     */
    protected void setUpUser3() {
        if(user3NotLoggedInId != -1)
            return;
        user3NotLoggedInId = setUser("User3GuestVisitorTests","User3!", MEMBER, NOT_LOGGED);
    }



}
