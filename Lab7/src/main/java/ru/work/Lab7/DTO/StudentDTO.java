package ru.work.Lab7.DTO;

public class StudentDTO {
    private String name;
    private String lastname;
    private int money;


    public StudentDTO(){};

    public StudentDTO(String name, String lastname, int money) {
        this.name = name;
        this.lastname = lastname;
        this.money = money;
    }

    public String getName() {
        return name;
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
}
