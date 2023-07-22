package PresentationLayer;

import BusinessLayer.ExternalSystems.PurchaseInfo;
import BusinessLayer.ExternalSystems.SupplyInfo;
import BusinessLayer.Stores.Store;
import DataAccessLayer.Hibernate.ConnectorConfigurations;
import DataAccessLayer.Hibernate.DBConnector;
import initialize.ConfigReader;
import initialize.Loader;
import ServiceLayer.Objects.RuleService;
import ServiceLayer.Result;
import ServiceLayer.ShoppingService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static BusinessLayer.Stores.Conditions.LogicalCompositions.LogicalComposites.CONDITIONING;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "Market")
@Push
public class Application implements AppShellConfigurator {


    public static void main(String[] args) throws Exception {
        ConfigReader configReader = new ConfigReader();
        DBConnector<Store> connector = new DBConnector<>(Store.class, readDBConfigurations(configReader));

        List<Store> stores = connector.getAll();
        if (stores.size() > 0) {
            System.out.println("The system already has stores, not loading from loader.");
        } else {
            String relativePath = configReader.getInitializePath();

            Loader loader = new Loader();
            loader.load(relativePath);
        }

        addAlcoholAgeRestriction();

        SpringApplication.run(Application.class, args);

    }

    private static void addAlcoholAgeRestriction() throws Exception{
        ShoppingService shoppingService = new ShoppingService();
        Result<RuleService> ruleService1 = shoppingService.addPurchasePolicyForbiddenCategoryRule(0, "Alcohol");
        Result<RuleService> ruleService2 = shoppingService.addPurchasePolicyBuyerAgeRule(0, 18);
        List<Integer> rulesIDs = new ArrayList<>();
        rulesIDs.add(ruleService1.getValue().getId());
        rulesIDs.add(ruleService2.getValue().getId());
        shoppingService.wrapPurchasePolicies(0, rulesIDs, CONDITIONING);
    }


    public static PurchaseInfo getPurchaseInfo(){
        return new PurchaseInfo("123", 1, 2222, "asd", 1222, 1, LocalDate.of(2000, 1, 1));
    }

    public static SupplyInfo getSupplyInfo(){
        return new SupplyInfo("Name", "address", "city", "counyrt", "asd");
    }

    public static ConnectorConfigurations readDBConfigurations(ConfigReader configReader) throws Exception {
        String name = configReader.getDBName();
        String url = configReader.getDBUrl();
        String username = configReader.getDBUsername();
        String password = configReader.getDBPassword();
        String driver = configReader.getDBDriver();

        return new ConnectorConfigurations(name, url, username, password, driver);
    }

}
