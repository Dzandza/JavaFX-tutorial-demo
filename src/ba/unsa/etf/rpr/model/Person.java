package ba.unsa.etf.rpr.model;

import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;


//Klasa osoba koja prati javafx beans konvenciju
public class Person implements Serializable {

    //atributi u obliku propertija
    private SimpleStringProperty firstName = new SimpleStringProperty("");
    private SimpleStringProperty lastName = new SimpleStringProperty("");
    private SimpleStringProperty email = new SimpleStringProperty("");
    private SimpleStringProperty username = new SimpleStringProperty("");
    private SimpleStringProperty password = new SimpleStringProperty("");

    //Konstruktor bez parametara (zbog beansa)
    public Person() {}

    //Konstruktor sa svim parametrima (prima regularne stringove jer se treba ponasati kao regularna klasa)
    public Person(String firstName, String lastName, String email, String username, String password) {
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.email = new SimpleStringProperty(email);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    //Getteri i setteri za propertije kako bi mogli manipulisati sa njima u kontroleru
    public SimpleStringProperty firstNameProperty() {
        return firstName;
    }

    public SimpleStringProperty lastNameProperty() {
        return lastName;
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    //Getteri i setteri (zbog obicnog java beansa)
    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }


    //Override metoda equals, toString i hashCode radi lakse manipulacije
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
        if (email != null ? !email.equals(person.email) : person.email != null) return false;
        if (username != null ? !username.equals(person.username) : person.username != null) return false;
        return password != null ? password.equals(person.password) : person.password == null;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return firstName.get() + " " + lastName.get();
    }

}
