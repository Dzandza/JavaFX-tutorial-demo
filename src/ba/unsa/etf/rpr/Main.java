package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.controllers.HomeController;
import ba.unsa.etf.rpr.model.PersonsModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Injection model klase u kontroler
        HomeController homeController = new HomeController(new PersonsModel());

        //Ucitavanje fxmla i postavljanje adekvatnog kontrolera
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        loader.setController(homeController);
        loader.load();

        //Otvaranje prozora i postavljanje scene
        primaryStage.setTitle("Osobe");
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(300);
        primaryStage.setScene(new Scene(loader.getRoot(), 800, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
