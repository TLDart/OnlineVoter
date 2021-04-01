import java.util.Calendar;
public class Vote {
    private String votingTable;
    private String listName;
    private Calendar voteTime;
    private long electionUid;

    Vote(String votingTable, String listName, Calendar voteTime){
        this.votingTable = votingTable; 
        this.listName = listName;
        this.voteTime = voteTime;
    }

    long getElectionUid(){
        return this.electionUid;
    }

    
}
