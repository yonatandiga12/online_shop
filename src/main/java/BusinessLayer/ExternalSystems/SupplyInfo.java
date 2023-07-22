package BusinessLayer.ExternalSystems;

public class SupplyInfo {


    private String name;
    private String address;
    private String city;
    private String country;
    private String zip;

    public SupplyInfo(String buyerName,String address, String city, String country, String zip){
        this.address = address;
        this.name = buyerName;
        this.city = city;
        this.country = country;
        this.zip = zip;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }
}
