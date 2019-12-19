package ba.unsa.etf.rpr.model;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

//Model klasa koju povezujemo sa kontrolerom
public class PersonsModel {

    //Predstavlja obicnu kontejnersku klasu koja sadrzi observable listu da bi je mogli povezati sa listom i tabelom
    //Parametar u konstruktoru sluzi kako bi se mogle slusati promjene na atributima osobe
    // (npr. ukoliko se promijeni vrijednost u formi, ukoliko je selektovana neka osoba, da se mijenja i vrijednost u listi/tabeli)
    //NAPOMENA: za tabelu nije potrebno prosljedjivati ovaj parametar u konstruktor, vec se isto radi preko cellValueFactorija (objasnjeno u kontroleru)
    private ObservableList<Person> persons = FXCollections.observableArrayList((Person person) -> new Observable[]{person.firstNameProperty(), person.lastNameProperty(), person.emailProperty(), person.usernameProperty(), person.passwordProperty()});

    //Konstruktor bez parametara
    public PersonsModel(){}

    //Konstruktor sa parametrom
    public PersonsModel(List<Person> persons){
        this.persons.addAll(persons);
    }

    //Getteri i setteri
    public ObservableList<Person> getPersons() {
        return persons;
    }

    public void setPersons(ObservableList<Person> persons) {
        this.persons = persons;
    }


    //TODO: Metode za export i import xml datoteka
    public void saveToXML(){

    }

    public void loadFromXML(){

    }
}
