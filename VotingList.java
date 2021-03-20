import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

public class VotingList implements Serializable {
    CopyOnWriteArrayList<Person> listMembers;
    String name;
    long voteCount;

    VotingList(String name, long voteCount, CopyOnWriteArrayList<Person> listMembers){
        super();
        this.name = name;
        this.voteCount = voteCount;
        this.listMembers = listMembers;
    }
}
