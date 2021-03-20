import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
public class Election implements Serializable {
    private long uid;
    private Calendar startTime; 
    private Calendar endTime; 
    private String description;
    private String title;
    private long totalVoteCount;
    private CopyOnWriteArrayList<VotingList> lists;
    private String departament;

    Election(long uid, Calendar startTime, Calendar endTime, String description, String title, String departament, CopyOnWriteArrayList<VotingList> lists) {
        super();
        this.uid = uid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.title = title; 
        this.totalVoteCount = 0;
        this.lists = lists;
        this.departament = departament;
    }

}
