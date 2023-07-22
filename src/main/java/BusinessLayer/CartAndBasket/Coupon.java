package BusinessLayer.CartAndBasket;

import javax.persistence.*;

@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "coupon")
    private String couponString;

    public Coupon(String _couponString){
        couponString = _couponString;
    }

    public Coupon(){

    }

    public void setCouponString(String _coupon){
        couponString = _coupon;
    }

    public String getCouponString(){
        return couponString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", couponString='" + couponString + '\'' +
                '}';
    }
}
