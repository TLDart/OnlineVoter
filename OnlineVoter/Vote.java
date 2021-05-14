package OnlineVoter;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Defines the notion of Person to the System. This person has the following attributes:
 * <ol>
 *      <li> Voting Table </li>
 *      <li> List Name </li>
 *      <li> Departament </li>
 *      <li> voteTime </li>
 *      <li> electionUid </li>
 * </ol>
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
public class Vote implements Serializable{
    private String votingTable;
    private String listName;
    private Calendar voteTime;
    private long electionUid;

     /**
      * Instantiates the vote
      * @param electionUid UId of the Election of this vote
      * @param votingTable Table where this vote was made
      * @param listName Name of the list this vote as voted in
      * @param voteTime Time the vote was casted
      */
    public Vote(long electionUid, String votingTable, String listName, Calendar voteTime){
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

    public String getVotingTable(){
        return this.votingTable;
    }

    
}
