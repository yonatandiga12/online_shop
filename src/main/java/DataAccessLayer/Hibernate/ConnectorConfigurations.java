package DataAccessLayer.Hibernate;

public class ConnectorConfigurations {

    private String unitName;
    private String url;
    private String username;
    private String password;
    private String driver;

    public ConnectorConfigurations(String _unitName, String _url, String _username, String _password, String _driver){
        unitName = _unitName;
        url = _url;
        username = _username;
        password = _password;
        driver = _driver;
    }

    public String getUnitName(){
        return unitName;
    }

    public String getUrl(){
        return url;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getDriver(){
        return driver;
    }

}
