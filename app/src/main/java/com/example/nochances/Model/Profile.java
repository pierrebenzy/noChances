package com.example.nochances.Model;

public class Profile {
    private String name;
    private int gender;
    private String email;
    private String password;
    private int dartClass;
    private String major;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public String getMajor() {
        return major;
    }

    public int getDartClass() {
        return dartClass;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public int getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



    public void setMajor(String major) {
        this.major = major;
    }

    public void setDartClass(int dartClass) {
        this.dartClass = dartClass;
    }

    @Override
    public String toString() {
        return name+" "+email+" "+password+" "+dartClass+" "+major+" "+phone+" "+gender;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public void setName(String name) {
        this.name = name;

    }

}
