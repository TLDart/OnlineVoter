import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private int type;
    private ArrayList<Vote> votedElections;

    Person(String name, String password, String dep, String address, int phoneNumber, int ccNr, int type, Calendar ccValidity){
       super();
       this.name = name;
       this.password = password;
       this.dep = dep;
       this.address = address;
       this.phoneNumber = phoneNumber;
       this.ccNr = ccNr;
       this.ccValidity = ccValidity;
       this.type = type;
       this.votedElections = new ArrayList<>();
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

    public int getType(){
        return this.type;
    }

    public void setType(int type){
        this.type = type;
    }

    public Calendar getCcValidity() {
        return this.ccValidity;
    }

    public void setCcValidity(Calendar ccValidity) {
        this.ccValidity = ccValidity;
    }

    public Boolean notVoted(Election e){
        for(Vote te : this.votedElections){
            if(e.getUid() == te.getElectionUid())
                return false;
        }
        return true;
    }
    public void addVotedElections(Vote v){
        v.setName("CONFIDENTIAL");
        this.votedElections.add(v);
    }

    public String toString(){
        String type_str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd");
        if (this.type == 0) type_str = "Student";
        else if (this.type == 1) type_str = "Teacher";
        else if (this.type == 2) type_str = "Staff";
        return String.format("Uid: %d | Name: %s | Department: %s | Address: %s | Phone number: %d | Type: %s | ccNr: %d | ccValidity: %s | password: %s", this.uid, this.name, this.dep, this.address, this.phoneNumber, type_str, this.ccNr, sdf.format(this.ccValidity.getTime()), this.password);
    }
}
