package OnlineVoter;
/**
 * Auxilary class used to manage data from each voting table
 * 
 * @see VotingTable
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes 
 */
public class VotingListInfo{ //Voting Table info??
    private String name;
    private long voteCount;

     /**
      * 
      * @param name Nameof the voting table
      */
    VotingListInfo(String name){
        this.name = name;
        this.voteCount = 0;
    }

    void addVoteCount(){
        this.voteCount++;
    }

    long getVoteCount(){
        return this.voteCount;
    }

    String getName(){
        return this.name;
    }
}