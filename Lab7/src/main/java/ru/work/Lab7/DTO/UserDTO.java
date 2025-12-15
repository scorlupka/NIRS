package ru.work.Lab7.DTO;

public class UserDTO {
    private String name;
    private String lastname;
    private int id;
    private String role;

    public UserDTO(String name, String lastname, int id, String role) {
        this.name = name;
        this.lastname = lastname;
        this.id = id;
        this.role = role;
    }
    public UserDTO(){};

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
