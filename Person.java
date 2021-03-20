import java.io.Serializable;
import java.sql.Date;

import java.util.Calendar;

public class Person implements Serializable{
    private long uid;
    private String name;
    private String password;
    private String dep;
    private String address;
    private int phoneNumber;
    private int ccNr;
    private Calendar ccValidity;

  

    Person(String name, String password, String dep, String address, int phoneNumber, int ccNr, Calendar ccValidity){
       super();
       this.name = name;
       this.password = password;
       this.dep = dep;
       this.address = address;
       this.phoneNumber = phoneNumber;
       this.ccNr = ccNr;
       this.ccValidity = ccValidity;
   }
   Person(String s){
        super();
        System.out.println("receiving data");
   } 
    public long getUid() {
        return this.uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDep() {
        return this.dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCcNr() {
        return this.ccNr;
    }

    public void setCcNr(int ccNr) {
        this.ccNr = ccNr;
    }

    public Calendar getCcValidity() {
        return this.ccValidity;
    }

    public void setCcValidity(Calendar ccValidity) {
        this.ccValidity = ccValidity;
    }
}
