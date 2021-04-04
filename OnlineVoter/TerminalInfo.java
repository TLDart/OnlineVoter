package OnlineVoter;
import java.io.Serializable;
import java.util.*;


/**
 * Auxilary class used in the voting table to manage terminal interaction
 * 
 * @see VotingTable
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes 
 */
public class TerminalInfo implements Serializable {
    private Person p;
    public ArrayList<Election> validElections;
    private int tNr;
    private Vote v;
    private Boolean state;
    private int option;

    public Person getP() {
        return this.p;
    }

    public int getOption(){
        return this.option;
    }
    
    public void setOption(int option){
        this.option = option;
    }

    public ArrayList<Election> getValidElections() {
        return this.validElections;
    }

    public void setVE(ArrayList<Election> el) {
        this.validElections = el;
    }

    public void setP(Person p) {
        this.p = p;
    }

    public int getTNr() {
        return this.tNr;
    }

    public void setTNr(int tNr) {
        this.tNr = tNr;
    }

    public Vote getV() {
        return this.v;
    }

    public void setV(Vote v) {
        this.v = v;
    }

    public Boolean getState() {
        return this.state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    // This mean if the thread is ready or not for scheduling

    TerminalInfo(int tNr){
        this.tNr = tNr;
        this.v = null;
        this.state = true;
        this.validElections = null;
        this.p = null;

    }

     /**
      * Instantiates the terminal Info Struct
      * @param tNr NUmber of the terminal
      * @param e Arraylist contain the Eligible Elections
      * @param p Person associated with the terminal
      */
    TerminalInfo(int tNr, ArrayList<Election> e, Person p){
        this.tNr = tNr;
        this.v = null;
        this.state = false;
        this.validElections = e;
        this.p = p;
    } 
}
