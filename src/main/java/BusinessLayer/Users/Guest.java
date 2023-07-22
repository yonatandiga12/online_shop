package BusinessLayer.Users;

import BusinessLayer.Market;
import com.mysql.cj.Session;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Guests")
public class Guest extends User{
    public static final int MAX_GUEST_USER_ID = 999999;
    public static int GUEST_USER_ID = 999999;
    public Guest () throws Exception {
        super(GUEST_USER_ID);
    }
}
