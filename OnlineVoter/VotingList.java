package OnlineVoter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

/**
 * Defines the notion of VotingList to the System. This person has the following attributes:
 * <ol>
 *      <li> Name </li>
 *      <li> Members </li>
 *      <li> voteCount </li>
 *      <li> Type </li>
 * </ol>
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
public class VotingList implements Serializable {
    private CopyOnWriteArrayList<String> listMembers;
    private String name;
    private long voteCount;
    private int type;//0 -> student, 1 -> teacher, 2 -> staff


    VotingList(String name, int type, CopyOnWriteArrayList<String> listMembers){
        super();
        this.name = name;
        this.voteCount = 0;
        this.type = type;
        this.listMembers = listMembers;
    }


    public String getName(){
        return this.name;
    }

    public void addCounter(){
        this.voteCount++;
    }

    public String toString(){
        return String.format("List name: %s | Number of votes: %d", this.name, this.voteCount);
    }

    public long getVoteCount(){
        return this.voteCount;
    }
}
