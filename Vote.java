import java.util.Calendar;
public class Vote {
    String votingTable;
    String listName;
    Calendar voteTime;

    Vote(String votingTable, String listName, Calendar voteTime){
        this.votingTable = votingTable; 
        this.listName = listName;
        this.voteTime = voteTime;
    }
    
}
