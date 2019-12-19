package ba.unsa.etf.rpr.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;



public class ErrorController {
    private String errorMessage;
    @FXML
    private Text errorText;


    public ErrorController(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @FXML
    public void initialize(){
        errorText.setText(errorMessage);
    }

    public void closeWindow(ActionEvent actionEvent){
        ((Stage) errorText.getScene().getWindow()).close();
    }
}
