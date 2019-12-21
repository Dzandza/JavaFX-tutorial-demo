package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.model.Person;
import ba.unsa.etf.rpr.model.PersonsModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class HomeController {
    //privatni atributi za logicke potrebe kontrolera
    private PersonsModel personsModel;  //model koji smo primili u konstruktoru
    private final String BLANK_FIELD_ERROR_MESSAGE = "Greška: Neko od polja je prazno!"; //konstantni atribut za poruku koja se prikazuje na ekranu u slucaju greske
    private final String NO_SELECTION_ERROR_MESSAGE = "Greška: Ništa nije selektovano!"; //konstantni atribut za poruku koja se prikazuje na ekranu u slucaju greske

    //privatni atributi koji povezuju fxml sa kontrolerom
    @FXML
    private TextField firstNameTextField; //atribut povezan sa tekstualnim input poljem za unos imena
    @FXML
    private TextField lastNameTextField;  //atribut povezan sa tekstualnim input poljem za unos prezimena
    @FXML
    private TextField emailTextField;    //atribut povezan sa tekstualnim input poljem za unos email
    @FXML
    private TextField usernameTextField;  //atribut povezan sa tekstualnim input poljem za unos usernamea
    @FXML
    private PasswordField passwordPasswordField;  //atribut povezan sa password (u sustini takodjer tekstualno) input poljem za unos passworda
    @FXML
    private ListView<Person> personListView;  //Lista u kojoj ce se prikazivati unesene osobe iz liste koja se nalazi u modelu
    @FXML
    private TableView<Person> personTableView;  //Tabela u kojoj ce se prikazivati unesene osobe iz liste koja se nalazi u modelu
    @FXML
    private CheckBox toggleViewCheckBox;    //Checkbox da prikazujemo tabelu, odnosno listu, zavisno je li cekirano ili ne
    @FXML
    private TableColumn<String, Person> firstNameColumn;    //atribut povezan sa kolonom tabele koja je odgovorna za ime
    @FXML
    private TableColumn<String, Person> lastNameColumn;     //atribut povezan sa kolonom tabele koja je odgovorna za prezime
    @FXML
    private TableColumn<String, Person> emailColumn;    //atribut povezan sa kolonom tabele koja je odgovorna za email
    @FXML
    private TableColumn<String, Person> usernameColumn;     //atribut povezan sa kolonom tabele koja je odgovorna za username
    @FXML
    private Text errorText;     //atribut povezan sa textom koji prikazuje tekst greske ukoliko se ista desi pri unosu u formu (povezuje se sa konstantnim stringom kad je greska)
    @FXML
    private Button addButton;   //atribut koji se vezuje za dodavanje osobe u listu/tabelu (nije nuzno potreban atribut, ali se koristi za dodatne efekte na gui - u)


    //Pomocna metoda koja resetuje sva tekstualna polja forme
    private void setEmptyTextFields() {
        firstNameTextField.setText("");
        lastNameTextField.setText("");
        emailTextField.setText("");
        usernameTextField.setText("");
        passwordPasswordField.setText("");
    }

    //Metoda za validaciju forme (validacija je da polje ne smije biti prazno ukoliko se dodaje ili edituje nova osoba)
    //Koristi se metoda trim da ukloni sve whitespace - e (" "), nove redove ("\n") ili tabove ("\t"), te se onda provjerava da li je polje prazno
    private boolean isFormValid() {
        if (firstNameTextField.getText().trim().isEmpty())
            return false;
        if (lastNameTextField.getText().trim().isEmpty())
            return false;
        if (emailTextField.getText().trim().isEmpty())
            return false;
        if (usernameTextField.getText().trim().isEmpty())
            return false;
        return !passwordPasswordField.getText().trim().isEmpty();
    }

    //Pomocna metoda koja povezuje propertije selektovane osobe iz liste sa tekstualnim poljima na formi
    //Poveznica je bidirectional sto znaci da se svaka promjena na formi odrazava na selektovanu osobu iz liste sto ce se odraziti na krajnju tabelu/listu na gui - u
    private void bindPropertiesWithTextFields(Person selectedPerson) {
        firstNameTextField.textProperty().bindBidirectional(selectedPerson.firstNameProperty());
        lastNameTextField.textProperty().bindBidirectional(selectedPerson.lastNameProperty());
        emailTextField.textProperty().bindBidirectional(selectedPerson.emailProperty());
        usernameTextField.textProperty().bindBidirectional(selectedPerson.usernameProperty());
        passwordPasswordField.textProperty().bindBidirectional(selectedPerson.passwordProperty());
    }

    //Pomocna metoda koja prekida poveznicu izmedju propertija selektovane osobe iz liste i tekstualnih poljia na formi
    private void unbindPropertiesFromTextFields(Person selectedPerson) {
        firstNameTextField.textProperty().unbindBidirectional(selectedPerson.firstNameProperty());
        lastNameTextField.textProperty().unbindBidirectional(selectedPerson.lastNameProperty());
        emailTextField.textProperty().unbindBidirectional(selectedPerson.emailProperty());
        usernameTextField.textProperty().unbindBidirectional(selectedPerson.usernameProperty());
        passwordPasswordField.textProperty().unbindBidirectional(selectedPerson.passwordProperty());
    }

    //Pomocna metoda koja disable - uje listu i tabelu
    private void disableViews(boolean value) {
        personListView.setDisable(value);
        personTableView.setDisable(value);
    }


    //Pomocna metoda koja dodaje listenere proslijedjenom tekstualnom polju (radit ce i za passwordField jer je izvedena klasa TextFielda)
    private void addListenerToTextField(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {

            //Provjerava se da li je nova vrijednost unesena u polje prazna
            //Ukoliko jeste i ukoliko barem jedan od pogleda na podatke (odnosno lista ili tabela) sadrze u sebi selektovanu osobu
            //Lista i tabela se disable - uju kako ne bi doslo do selektovanja neke druge vrijednosti iz istih, te bi se time neispravna (prazna vrijednost polja)
            //spasila u tabelu/listu
            if (newValue.trim().isEmpty()
                    && (personListView.getSelectionModel().getSelectedItem() != null
                    || personTableView.getSelectionModel().getSelectedItem() != null)
            ) disableViews(true);

                //Ukoliko je ovo polje validno potrebno je provjeriti da li je i dalje nesto selektovano, te da li je forma validna
                //Ukoliko jeste tada ce se disable pogleda na podatke ugasiti, te ce se moci ponovo selektovati i manpulisati sa istim
            else if (personListView.getSelectionModel().getSelectedItem() != null || personTableView.getSelectionModel().getSelectedItem() != null)
                disableViews(!isFormValid());

                //Ukoliko nije nista od prethodnog zadovoljeno podaci se mogu selektovati slodno jer je sve validno
            else disableViews(false);

            //Reset error poruke na prvi input u polja
            errorText.setText("");
        }));
    }


    //Poziv prethodne metode za sva polja forme
    private void addTextFieldListeners() {
        addListenerToTextField(firstNameTextField);
        addListenerToTextField(lastNameTextField);
        addListenerToTextField(emailTextField);
        addListenerToTextField(usernameTextField);
        addListenerToTextField(passwordPasswordField);
    }


    //Listener za checkbox koji nam mijenja pogled na podatke
    //Ukoliko je deselektovan imamo pogled u listi gdje vidimo samo ime i prezime
    //Ukoliko je selektovan imamo pogled u tabeli sa kolonama ime, prezime, email i username
    private void addCheckBoxListener() {
        toggleViewCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {

            //Provjeravamo da li je nas pogled u trenutku prelaska s jednog u drugi disabled
            //Ukoliko jeste moramo na kraju ponovo postaviti da bude, s obzirom da manipulacije sa tabelom i selekcijom adekvatnog reda poremete vrijednost atributa
            boolean disabled = personListView.isDisabled() || personTableView.isDisabled();

            //Uz pomoc atributa visible postavljamo koji pogled na podatke imamo
            //Kako je inicijalno listView, tako ce prvobitna oldValue biti false pa ce sklanjati listView, dok ce newValue biti true jer ce biti cekiran Checkbox
            //Analogno vrijedi u suprotnom smjeru
            personListView.setVisible(oldValue);
            personTableView.setVisible(newValue);

            //Ako je Checkbox cekiran newValue ce biti true, te ukoliko je selektovano nesto u listi potrebno je da tu selekciju prebacimo u tabelu
            if (newValue && personListView.getSelectionModel().getSelectedItem() != null) {

                //Sklanjamo binding sa svih tekstualnih polja jer ne mozemo postaviti nove poveznice sa tabelom ukoliko vec postoji binding sa listom
                unbindPropertiesFromTextFields(personListView.getSelectionModel().getSelectedItem());

                //Selektujemo adekvatan element u tabeli
                personTableView.getSelectionModel().select(personListView.getSelectionModel().getSelectedIndex());

                //Sklanjamo selekciju sa liste
                personListView.getSelectionModel().clearSelection();

                //Povezujemo element iz tabele koji je selektovan sa poljima forme
                bindPropertiesWithTextFields(personTableView.getSelectionModel().getSelectedItem());
            }

            //Ako je Checkbox nije cekiran newValue ce biti false a oldValue true, te ukoliko je selektovano nesto u tabeli potrebno je da tu selekciju prebacimo u listu
            else if (oldValue && personTableView.getSelectionModel().getSelectedItem() != null) {

                //Sklanjamo binding sa svih tekstualnih polja jer ne mozemo postaviti nove poveznice sa listom ukoliko vec postoji binding sa tabelom
                unbindPropertiesFromTextFields(personTableView.getSelectionModel().getSelectedItem());

                //Selektujemo adekvatan element u listi
                personListView.getSelectionModel().select(personTableView.getSelectionModel().getSelectedIndex());

                //Sklanjamo selekciju sa tabele
                personTableView.getSelectionModel().clearSelection();

                //Povezujemo element iz liste koji je selektovan sa poljima forme
                bindPropertiesWithTextFields(personListView.getSelectionModel().getSelectedItem());
            }

            //postavljanje da li je pogled na podatke iskljucen
            disableViews(disabled);
        });

    }

    //Pomocna metoda za inicijalizaciju liste
    private void initializeListView() {

        //Postavljanje elemenata liste iz modela
        personListView.setItems(personsModel.getPersons());

        //Placeholder ukoliko nema elemenata u listi
        personListView.setPlaceholder(new Label("Nema podataka u listi"));

        //Listener za povezivanje selektovanog elementa sa formom
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //ukoliko je nesto bilo selektovano prije, treba se ukloniti poveznica sa formom
            if (oldValue != null) unbindPropertiesFromTextFields(oldValue);

            //ukoliko postoji nova selekcija, s obzirom da se slusa na bilo kakvu promjenu, potrebno je
            //da se iskljuci dugme za dodavanje s obzirom da prelazimo u edit mode da ne mozemo da dodajemo nove elemente ukoliko manipulisemo nad postojecim
            //da se doda poveznica izmedju novih vrijednosti i polja forme kako bi se moglo manipulisati sa istim
            if (newValue != null) {
                addButton.setDisable(true);
                bindPropertiesWithTextFields(newValue);
            }

            //Ukoliko je samo uklonjena selekcija sa svih elemenata liste, potrebno je elemente forme postaviti na defaultne vrijednosti
            else setEmptyTextFields();

        });
    }

    //Pomocna metoda za inicijalizaciju tabele
    private void initializeTableView() {

        //Postavljanje elemenata tabele iz modela
        personTableView.setItems(personsModel.getPersons());

        //Placeholder ukoliko nema elemenata u tabele
        personTableView.setPlaceholder(new Label("Nema podataka u tabeli"));

        //postavljanje cellValueFactorija
        //u sustini za atribute elemenata liste osoba koje zelimo prikazati u tabeli, na ovaj nacin povezujemo sa gui - em
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        //Listener za povezivanje selektovanog elementa sa formom
        personTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            //ukoliko je nesto bilo selektovano prije, treba se ukloniti poveznica sa formom
            if (oldValue != null) unbindPropertiesFromTextFields(oldValue);

            //ukoliko postoji nova selekcija, s obzirom da se slusa na bilo kakvu promjenu, potrebno je
            //da se iskljuci dugme za dodavanje s obzirom da prelazimo u edit mode da ne mozemo da dodajemo nove elemente ukoliko manipulisemo nad postojecim
            //da se doda poveznica izmedju novih vrijednosti i polja forme kako bi se moglo manipulisati sa istim
            if (newValue != null) {
                addButton.setDisable(true);
                bindPropertiesWithTextFields(newValue);
            }

            //Ukoliko je samo uklonjena selekcija sa svih elemenata liste, potrebno je elemente forme postaviti na defaultne vrijednosti
            else setEmptyTextFields();
        });
    }

    //Pomocna metoda za prikazivanje prozora koji signalizira gresku
    private void showErrorStage(String message) throws IOException {

        //Ucitavanje prozora sa porukom kao u mainu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/error.fxml"));
        loader.setController(new ErrorController(message));
        loader.load();

        //Kreiranje novog prozora i postavljanje vrijednosti
        Stage errorStage = new Stage();
        errorStage.setTitle("Greška");
        errorStage.setResizable(false);
        errorStage.setScene(new Scene(loader.getRoot(), 400, 100));

        //Ovim zabranjujemo da se rade bilo kakve operacije dok je otvoren prozor za gresku
        errorStage.initModality(Modality.APPLICATION_MODAL);

        //Otvaranje prozora
        errorStage.show();
    }

    //Konstruktor koji prima model
    public HomeController(PersonsModel personsModel) {
        this.personsModel = personsModel;
    }

    //Metoda initialize koja poziva prethodno navedene metode i adekvatno inicijalizira elemente gui - a
    @FXML
    public void initialize() {
        addTextFieldListeners();
        addCheckBoxListener();
        initializeTableView();
        initializeListView();
    }


    //Metoda za dodavanje osobe u listu
    public void addPerson(ActionEvent actionEvent) {

        //Provjera da li je forma validna i ukoliko jeste dodaje se osoba i resetuje se forma
        if (isFormValid()) {
            Person newPerson = new Person(firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText(), usernameTextField.getText(), passwordPasswordField.getText());
            personsModel.getPersons().add(newPerson);
            setEmptyTextFields();
        }

        //ukoliko nije forma validna postavlja se tekst greske
        else errorText.setText(BLANK_FIELD_ERROR_MESSAGE);
    }

    //Metoda za deselektovanje trenutno selektovane osobe
    public void deselectPerson() {

        //Provjera da li je ista selektovano, ukoliko nije javlja se greska
        if (personListView.getSelectionModel().getSelectedItem() != null || personTableView.getSelectionModel().getSelectedItem() != null) {

            //Provjera da li je forma validna, odnosno da li postoje prazna polja, te ukoliko postoje javlja se greska
            //U suprotnom se sklanja disable sa buttona za dodavanje i deselektuje se osoba (odnosno finalizira se izmjena), te se forma resetuje
            if (isFormValid()) {

                addButton.setDisable(false);
                personListView.getSelectionModel().clearSelection();
                personTableView.getSelectionModel().clearSelection();
                setEmptyTextFields();

            } else errorText.setText(BLANK_FIELD_ERROR_MESSAGE);

        } else errorText.setText(NO_SELECTION_ERROR_MESSAGE);
    }

    //pravljenje novog radnog prostora
    public void newFile(ActionEvent actionEvent) {
        personTableView.getSelectionModel().clearSelection();
        personListView.getSelectionModel().clearSelection();
        personsModel.resetList();
        setEmptyTextFields();
    }


    //metoda za otvaranje fajlova
    public void openFile(ActionEvent actionEvent) throws IOException, ParserConfigurationException {
        //filechooser se koristi za otvaranje novog prozora za odabir datoteke
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otvori datoteku");

        //filtriranje ekstenzija, da mozemo samo odabrati adekvatnu vrstu fajlova
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));

        //otvaranje dijaloga
        File file = fileChooser.showOpenDialog(addButton.getScene().getWindow());

        //provjera da li je selektovan fajl
        if (file != null) {
            try {
                //poziv metode modela
                personsModel.loadFromXML(file, true); // drugi parametar metode se moze proizvoljno mijenjati, efekat ce biti isti
            } catch (FileNotFoundException e) {
                showErrorStage("Fajl ne postoji"); //ukoliko se desi greska pri citanju
            } catch (SAXException e) {
                showErrorStage("Fajl nije validan xml dokument");  //ukoliko se desi greska pri parsiranju
            }
        }
    }

    //metoda za spasavanje podataka u xml datoteku
    public void saveFile(ActionEvent actionEvent) throws IOException {

        //ukoliko je u toku editovanje mora se zavrsiti radi validnosti podataka da bi se spasio fajl
        if (personListView.getSelectionModel().getSelectedItem() == null && personTableView.getSelectionModel().getSelectedItem() == null) {

           //analogno se koristi filechooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Spasi u datoteku datoteku");

            //prijedlog naziva fajla
            fileChooser.setInitialFileName("file-" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".xml");

            //dozvoljene ekstenzije
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));

            //otvaranje dijaloga za biranje lokacije i imena fajla
            File file = fileChooser.showSaveDialog(addButton.getScene().getWindow());

            //spasavanje fajla
            if (file != null) personsModel.saveToXML(file);
        } else showErrorStage("Završite editovanje kako biste mogli spasiti fajl"); //prikaz ukoliko se desi greska
    }

    //gasenje aplikacije
    public void exitApp(ActionEvent actionEvent) {
        ((Stage) addButton.getScene().getWindow()).close();
    }
}
