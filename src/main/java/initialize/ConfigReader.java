package initialize;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties;

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    private String filepath;
    public ConfigReader() {
        filepath = Objects.equals(System.getProperty("env"), "test") ? "testconfig.properties" : "config.properties";
        properties = new Properties();
    }
    private static class ConfigProperties{
        public static final String INITIALIZE_PATH="InitializePath";
        public static final String DB_name="DB_name";
        public static final String DB_url="DB_url";
        public static final String DB_username="DB_username";
        public static final String DB_password = "DB_password";
        public static final String DB_driver="DB_driver";
        public static final String ExternalSystems_URL="ExternalSystemsURL";
        public static final String adminUserName="adminUserName";
        public static final String adminPassword="adminPassword";


    }

    public String getInitializePath() {
        return getConfigDetail(ConfigProperties.INITIALIZE_PATH);
    }
    public String getDBName() {
        return getConfigDetail(ConfigProperties.DB_name);
    }
    private String getConfigDetail(String configProperty){
        String property="";

        try (FileInputStream fis = new FileInputStream(filepath)) {
            properties.load(fis);

            // Read the relative addresses of the files
            property = properties.getProperty(configProperty);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return property;
    }

    public String getDBUrl(){
        return getConfigDetail(ConfigProperties.DB_url);
    }

    public String getDBUsername(){
        return getConfigDetail(ConfigProperties.DB_username);
    }

    public String getDBPassword(){
        return getConfigDetail(ConfigProperties.DB_password);
    }

    public String getDBDriver(){
        return getConfigDetail(ConfigProperties.DB_driver);
    }

    public String getExternalSystemsURL(){
        return getConfigDetail(ConfigProperties.ExternalSystems_URL);
    }
    public String getAdminUserName(){return getConfigDetail(ConfigProperties.adminUserName);}
    public String getAdminPassword(){return getConfigDetail(ConfigProperties.adminPassword);}
}
