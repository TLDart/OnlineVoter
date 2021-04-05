package OnlineVoter;

import java.io.Serializable;

/**
 * Auxilary class used to manage data from each voting table
 * 
 * @see VotingTable
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes 
 */
public class VotingListInfo implements Serializable{ //Voting Table info??
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

    public synchronized void addVoteCount(){
        this.voteCount++;
    }

    public long getVoteCount(){
        return this.voteCount;
    }

    public String getName(){
        return this.name;
    }
}