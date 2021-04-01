import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

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

    public String toString(){
        return String.format("List name: %s | Number of votes: %d", this.name, this.voteCount);
    }
}
