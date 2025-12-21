package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "clients")
public class User {
    @Id
    @Column(name = "pasportnumber", nullable = false, unique = true)
    private String passportNumber;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "namelastname", nullable = false)
    private String nameLastname;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "pasportseria", nullable = false)
    private String passportSeria;

    public User() {
    }

    public User(String passportNumber, String username, String password, String role) {
        this.passportNumber = passportNumber;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNameLastname() {
        return nameLastname;
    }

    public void setNameLastname(String nameLastname) {
        this.nameLastname = nameLastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassportSeria() {
        return passportSeria;
    }

    public void setPassportSeria(String passportSeria) {
        this.passportSeria = passportSeria;
    }
}

