package ba.unsa.etf.rpr.model;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

//Model klasa koju povezujemo sa kontrolerom
public class PersonsModel {

    //Predstavlja obicnu kontejnersku klasu koja sadrzi observable listu da bi je mogli povezati sa listom i tabelom
    //Parametar u konstruktoru sluzi kako bi se mogle slusati promjene na atributima osobe
    // (npr. ukoliko se promijeni vrijednost u formi, ukoliko je selektovana neka osoba, da se mijenja i vrijednost u listi/tabeli)
    //NAPOMENA: za tabelu nije potrebno prosljedjivati ovaj parametar u konstruktor, vec se isto radi preko cellValueFactorija (objasnjeno u kontroleru)
    private ObservableList<Person> persons = FXCollections.observableArrayList((Person person) -> new Observable[]{person.firstNameProperty(), person.lastNameProperty(), person.emailProperty(), person.usernameProperty(), person.passwordProperty()});

    //Pomocna metoda koja nam pomaze da iz liste cvorova pronadjemo one koji predstavljaju tagove
    //Vraca se lista elemenata koja predstavlja tagove unutar roditeljskog elementa
    //Metoda getChildNodes koja se poziva da se dobije NodeList koji se prosljedjuju u ovu metodu nisu
    //samo tagovi unutar roditelja, vec mogu predstavljati atribute i sl.
    //Ukoliko smo stopostotno sigurni da neki element moze imati samo jedan tag kao dijete, kao sto smo pretpostavili
    //u nekim dijelovima koda, mozemo direktno pristupiti prvom elementu povratne liste

    //VAZNA NAPOMENA: Parsiranje dokumenta u ovom primjeru nije validirano, te svako odstupanje od forme xml fajla koju
    //generise XmlEncoder moze dovesti do nepredvidjenog ponasanja programa
    //Teoretski bi se parsiranje bilo kojeg xml dokumenta trebalo validirati u potpunosti i izbaciti gresku ukoliko
    //format nije adekvatan, medjutim u demonstrativne svrhe manipulisanja sa Document Object Modelom i cjelokupnog
    //parsiranja datoteke to nije bilo potrebno
    private ArrayList<Element> getElementsFromNodeList(NodeList parentNodeChildren) {
        ArrayList<Element> elements = new ArrayList<>();
        for (int i = 0; i < parentNodeChildren.getLength(); i++) {
            if(parentNodeChildren.item(i) instanceof Element)
                elements.add((Element) parentNodeChildren.item(i));
        }
        return elements;
    }

    //deserijalizacija konkretnih propertija vezanih za osobe
    private Person deserializeProperties(NodeList propertyNodes) {

        //inicijalna osoba
        Person person = new Person();

        //svaki property je obuhvacen ponovo void tagom generisanim od encodera
        //taj void tag predstavlja osnovni property, sto vidimo po atributu property koji ce se kasnije koristiti
        ArrayList<Element> properties = getElementsFromNodeList(propertyNodes);

        //prolazimo kroz sve propertije osobe
        for(Element property : properties) {

            //unutar void taga za svaki property je generisan string tag u kojem se nalazi vrijednost
            String propertyValue = getElementsFromNodeList(property.getChildNodes()).get(0).getTextContent();

            //provjeravamo atribut u void tagu i postavljamo adekvatnu vrijednost za properti osobe
            if (property.hasAttribute("property") && property.getAttribute("property").equals("firstName"))
                person.setFirstName(propertyValue);
            else if (property.hasAttribute("property") && property.getAttribute("property").equals("lastName"))
                person.setLastName(propertyValue);
            else if (property.hasAttribute("property") && property.getAttribute("property").equals("email"))
                person.setEmail(propertyValue);
            else if (property.hasAttribute("property") && property.getAttribute("property").equals("username"))
                person.setUsername(propertyValue);
            else if (property.hasAttribute("property") && property.getAttribute("property").equals("password"))
                person.setPassword(propertyValue);

        }

        //vracamo gotovu osobu u listu
        return person;
    }

    //pomocna metoda za deserijalizaciju osoba
    private void deserializePersons(Element root) {
        //preuzimanje glavnog object elementa generisanog arraylistom
        Element objectTag = getElementsFromNodeList(root.getChildNodes()).get(0);

        //preuzimanje svih osoba koje su u listi
        ArrayList<Element>  personsList = getElementsFromNodeList(objectTag.getChildNodes());

        //prolazak kroz listu osoba i dekodiranje pojedinacnih
        //svaka osoba u listi je omotana void tagom koji je generisan encoderom
        //s obzirom na to prvenstveno njemu pristupamo u listi
        for(Element voidTag : personsList){

            //pristupanje objektu unutar void taga sto predstavlja konkretnu osobu
            Element personElement = getElementsFromNodeList(voidTag.getChildNodes()).get(0);

            //dekodiranje osobe i dodavanje u listu
            persons.add(deserializeProperties(personElement.getChildNodes()));
        }


    }

    //pomocna metoda za dekodiranje fajla
    private void deserializeData(File file) throws IOException, SAXException, ParserConfigurationException {
        //lista se resetuje
        resetList();

        //pristupa se DOM - u (Document Object Model) preko document buildera (analogno bi se radilo za serijalizaciju)
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        //uzimanje root elementa parsiranog fajla
        Element root = builder.parse(file).getDocumentElement();

        //dekodiranje konkretnih podataka
        deserializePersons(root);
    }


    //Konstruktor bez parametara
    public PersonsModel() {
    }

    //Konstruktor sa parametrom
    public PersonsModel(List<Person> persons) {
        this.persons.addAll(persons);
    }

    //Getteri i setteri
    public ObservableList<Person> getPersons() {
        return persons;
    }

    public void setPersons(ObservableList<Person> persons) {
        this.persons = persons;
    }

    //metoda za reset liste
    public void resetList(){
        persons.clear();
    }


    //metoda za spasavanje u xml format
    //Koristi encoder sa jednostavnom sintaksom
    public void saveToXML(File file) throws FileNotFoundException {
        //otvaranje streama
        XMLEncoder xmlEncoder = new XMLEncoder(new FileOutputStream(file));

        //ne moze direktno observable list vec mora neka druga kolekcija
        xmlEncoder.writeObject(new ArrayList<>(persons));

        //zatvaranje streama
        xmlEncoder.close();
    }

    //metoda za citanje iz xml datoteke ukoliko je deserialization true cita se decoderom, ukoliko je false koristi se rucno implementirana deserijalizacija
    public void loadFromXML(File file, boolean deserialization) throws IOException, ParserConfigurationException, SAXException {
        if (deserialization) {
            //brisanje elemenata stare liste
            resetList();

            //Analogno encoderu otvaranje streama
            XMLDecoder xmlDecoder = new XMLDecoder(new FileInputStream(file));

            //parsiranje arrayliste
            persons.addAll((ArrayList<Person>) xmlDecoder.readObject());

            //zatvaranje streama
            xmlDecoder.close();
        } else deserializeData(file);
    }
}
