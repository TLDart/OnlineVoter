package OnlineVoter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

/**
 * The Election class contains all the data related to a certain Election. This
 * is:
 * <ol>
 * <li>Start Time</li>
 * <li>End Time</li>
 * <li>Title</li>
 * <li>Description</li>
 * <li>Departament</li>
 * <li>Total Vote count</li>
 * <li>Lists to be voted in</li>
 * <li>Type of the Election</li>
 * <li>StartTime</li>
 * </ol>
 */
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
    private CopyOnWriteArrayList<VotingListInfo> tables;
    private long voteCountNull;
    private long voteCountBlank;

    /**
     * Instantiated the Election Class
     * 
     * @param startTime   Start time of the Election
     * @param endTime     End time of the Election
     * @param description Description of the Election
     * @param title       Title of the ELection
     * @param department  Departament of the Election
     * @param lists       Lists Present of the election
     * @param type        Type of the Election (Student , Teacher or Janitor)
     * @param validDeps   Valid Departaments to be voted in
     */
    Election(Calendar startTime, Calendar endTime, String description, String title, String department,
            CopyOnWriteArrayList<VotingList> lists, int type, CopyOnWriteArrayList<VotingListInfo> validDeps) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.title = title;
        this.totalVoteCount = 0;
        this.lists = lists;
        this.department = department;
        this.type = type;
        this.tables = validDeps;
        this.voteCountBlank = 0;
        this.voteCountNull = 0;
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CopyOnWriteArrayList<VotingListInfo> getVotingTables() {
        return this.tables;
    }

    public void setVotingTables(CopyOnWriteArrayList<VotingListInfo> tables) {
        this.tables = tables;
    }

    public void addNull() {
        this.voteCountNull++;
    }

    public void addBlank() {
        this.voteCountBlank++;
    }

    public long getVoteBlankCounter() {
        return this.voteCountBlank;
    }

    public long getVoteNullCounter() {
        return this.voteCountNull;
    }

    public synchronized void addVote(){
        this.totalVoteCount++;
    }

    /**
     * Adds a voting table to the departament
     * 
     * @param tableDepartment table to be added
     */
    public void addVotingTable(String tableDepartment) {
        for (VotingListInfo vt : this.tables) {
            // se ja existir nao faz nada
            if (vt.getName().equals(tableDepartment)) {
                return;
            }
        }
        // se nao existir, adicionar
        this.tables.add(new VotingListInfo(tableDepartment));
        System.out.println(this.tables.size());
    }

    /**
     * Removes a Voting lTable from a departament
     * 
     * @param tableDepartment table to be removed
     */
    public void remVotingTable(String tableDepartment) {
        for (VotingListInfo vt : this.tables) {
            // se existir, remove
            if (vt.getName().equals(tableDepartment)) {
                this.tables.remove(vt);
            }
        }
    }

    /** 
     * Builds a String with information about the Election
     * @return String with information about the Election
     */
    public String toString(){
        String type_str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        if (this.type == 0) type_str = "Student";
        else if (this.type == 1) type_str = "Teacher";
        else if (this.type == 2) type_str = "Staff";
        String res = String.format("Uid: %d | Title: %s | Start time : %s | End time: %s | Department: %s | Type : %s | Total votes: %d\n---------------------------------------------\nDescription: %s\n---------------------------------------------\nLists:\n", this.uid, this.title, sdf.format(this.startTime.getTime()), sdf.format(this.endTime.getTime()), this.department, type_str, this.totalVoteCount, this.description);
        //System.out.println("Voting Tables");
        //System.out.println("----------------");
        for (VotingList vl : this.lists){
            res = res + "\n" + vl.toString();
        }
        res = res + "\n----------------";
        res = res + "\nTables:\n";
        for(VotingListInfo table : this.tables){
            //System.out.println(table.getName());
            res  = res + "\n" + table.getName();

        }
        return res;
    }

}
