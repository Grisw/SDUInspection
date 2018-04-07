package pers.lxt.sduinspection.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    public enum Sex{
        M("男"), F("女");

        private String sex;
        Sex(String sex){
            this.sex = sex;
        }

        public String getSex(){
            return sex;
        }
    }

    private String phoneNumber;
    private Sex sex;
    private Date birthday;
    private String name;
    private String email;
    private String leader;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}
