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

    public long getUid() {
        return this.uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Calendar getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTotalVoteCount() {
        return this.totalVoteCount;
    }

    public void setTotalVoteCount(long totalVoteCount) {
        this.totalVoteCount = totalVoteCount;
    }

    public CopyOnWriteArrayList<VotingList> getLists() {
        return this.lists;
    }

    public void setLists(CopyOnWriteArrayList<VotingList> lists) {
        this.lists = lists;
    }

    public String getDepartament() {
        return this.departament;
    }

    public void setDepartament(String departament) {
        this.departament = departament;
    }

}
