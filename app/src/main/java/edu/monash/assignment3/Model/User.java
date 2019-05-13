package edu.monash.assignment3.Model;

public class User {

    private String Name;
    private String Password;
    private String phone;
    private String Belong;

    public User() {
    }
//
//    public User(String name, String password, String belong) {
//        Name = name;
//        Password = password;
//        Belong = belong;
//    }


    public User(String name, String password, String phone, String belong) {
        Name = name;
        Password = password;
        this.phone = phone;
        Belong = belong;
    }

    public String getBelong() {
        return Belong;
    }

    public void setBelong(String belong) {
        Belong = belong;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
