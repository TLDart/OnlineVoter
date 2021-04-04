package OnlineVoter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
/**
 * Defines the notion of Person to the System. This person has the following attributes:
 * <ol>
 *      <li> Name </li>
 *      <li> Password </li>
 *      <li> Departament </li>
 *      <li> Address </li>
 *      <li> PhoneNumber </li>
 *      <li> CC number </li>
 *      <li> Type </li>
 *      <li> CC Validity </li>
 * </ol>
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
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

    /**
     * Instanciates the Person class
     * @param name Name of the person
     * @param password
     * @param dep Departament of the user
     * @param address Address of the User
     * @param phoneNumber Phone Number of the user
     * @param ccNr CC of the user 
     * @param type Type of the user
     * @param ccValidity Validity of the CC
     */
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

    
    /** 
     * Checks if the user has voted in the election e, by comparing all the voted he has made to the current Election Uid
     * @param e Election Object in search
     * @return Boolean True if not voted , false if voted
     */
    public Boolean notVoted(Election e){
        for(Vote te : this.votedElections){
            if(e.getUid() == te.getElectionUid())
                return false;
        }
        return true;
    }
    
    /** 
     * Adds a vote to the current Person voting list (to prevent double voting)
     * The list to which the user has voted is censured for privacy reasons
     * @param v vote to be added
     */
    public void addVotedElections(Vote v){
        v.setName("CONFIDENTIAL");
        this.votedElections.add(v);
    }

    
    /** 
     * Prints the user information in string format
     * @return String with user information
     */
    public String toString(){
        String type_str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd");
        if (this.type == 0) type_str = "Student";
        else if (this.type == 1) type_str = "Teacher";
        else if (this.type == 2) type_str = "Staff";
        return String.format("Uid: %d | Name: %s | Department: %s | Address: %s | Phone number: %d | Type: %s | ccNr: %d | ccValidity: %s | password: %s", this.uid, this.name, this.dep, this.address, this.phoneNumber, type_str, this.ccNr, sdf.format(this.ccValidity.getTime()), this.password);
    }
}
