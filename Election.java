import java.io.Serializable;
import java.text.SimpleDateFormat;
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
    private String department;
    private int type;
    private CopyOnWriteArrayList<String> tables;

   

    Election(Calendar startTime, Calendar endTime, String description, String title, String department, CopyOnWriteArrayList<VotingList> lists, int type) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.title = title; 
        this.totalVoteCount = 0;
        this.lists = lists;
        this.department = department;
        this.type = type;
        this.tables = new CopyOnWriteArrayList<String>();
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

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String departament) {
        this.department = departament;
    }

    public int getType(){
        return this.type;
    }

    public void setType(int type){
        this.type = type;
    }

    public CopyOnWriteArrayList<String> getVotingTables(){
        return this.tables;
    }

    public void setVotingTables(CopyOnWriteArrayList<String> tables ){
        this.tables = tables;
    }

    public String toString(){
        String type_str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        if (this.type == 0) type_str = "Student";
        else if (this.type == 1) type_str = "Teacher";
        else if (this.type == 2) type_str = "Staff";
        String res = String.format("Uid: %d | Title: %s | Start time : %s | End time: %s | Department: %s | Type : %s | Total votes: %d\n---------------------------------------------\nDescription: %s\n---------------------------------------------\nLists:\n", this.uid, this.title, sdf.format(this.startTime.getTime()), sdf.format(this.endTime.getTime()), this.department, type_str, this.totalVoteCount, this.description);
        for (VotingList vl : this.lists){
            res = res + "\n" + vl.toString();
        }
        return res;
    }

}
