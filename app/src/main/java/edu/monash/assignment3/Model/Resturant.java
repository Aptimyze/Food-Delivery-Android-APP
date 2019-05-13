package edu.monash.assignment3.Model;

public class Resturant {

    private String Address;
    private String Name;
    private String Image;
    private String Type;


    public Resturant() {
    }

    public Resturant(String address, String name, String image, String type) {
        Address = address;
        Name = name;
        Image = image;
        Type = type;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
