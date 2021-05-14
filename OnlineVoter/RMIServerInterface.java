package OnlineVoter;
import java.rmi.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Calendar;
public interface RMIServerInterface extends Remote {
    public boolean registerUser(Person p) throws RemoteException;
    public int getPort() throws RemoteException;
    public Election searchElectionById(long uid) throws RemoteException;
    public String createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<String> members) throws RemoteException;
    public String createElection(Calendar startTime, Calendar endTime, String description, String title, String department, int type, CopyOnWriteArrayList<VotingListInfo> validDeps) throws RemoteException;
    public String updateElection(long uid, Calendar startTime, Calendar endTime, String description, String title, String department) throws RemoteException;
    public void test(String msg) throws RemoteException;
    public ArrayList<Person> getListUsers(String department, int type) throws RemoteException;
    public ArrayList<Election> getListElections(String department, int type) throws RemoteException;
    public void setIsPrimary(boolean isPrimary) throws RemoteException;
    public String heartbeat() throws RemoteException;
    public TerminalInfo getPersonInfo(int cc, String name) throws RemoteException;
    public void processVote(TerminalInfo tInfo) throws RemoteException;
    public void subscribe(AdminConsoleInterface adminConsole) throws RemoteException;
    public String finishedElectionData(long electionId) throws RemoteException;
    public String updateTables(String tableDepartment, long electionId, int mode) throws RemoteException;
    public TerminalInfo getPersonInfoWeb(int cc, String curDepName) throws RemoteException;
}
