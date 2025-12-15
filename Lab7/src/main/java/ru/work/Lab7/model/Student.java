package ru.work.Lab7.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String lastname;
    private int money;

    public String getName() {
        return name;
    }

    public Student(){}

    public Student( String name, String lastname, int money) {
        this.name = name;
        this.lastname = lastname;
        this.money = money;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "ID = " + id + ", NAME = " + name + ", LASTNAME = " + lastname + ", MONEY = " + money;
    }
}