package OnlineVoter;
import java.io.Serializable;
import java.util.Calendar;
public class Vote implements Serializable{
    private String votingTable;
    private String listName;
    private Calendar voteTime;
    private long electionUid;

    Vote(long electionUid, String votingTable, String listName, Calendar voteTime){
        this.votingTable = votingTable; 
        this.listName = listName;
        this.voteTime = voteTime;
        this.electionUid = electionUid;
    }

    public void setName(String s){
        this.listName = s;
    }
    public long getElectionUid(){
        return this.electionUid;
    }
    public String getListName(){
        return this.listName;
    }

    
}
