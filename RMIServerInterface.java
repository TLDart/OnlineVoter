import java.rmi.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Calendar;
public interface RMIServerInterface extends Remote {
    public boolean registerUser(Person p) throws RemoteException;
    public boolean createElection(Election c) throws RemoteException;
    public int getPort() throws RemoteException;
    public Election searchElectionById(long uid) throws RemoteException;
    //public boolean createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<Long> members_uid);
    public String createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<String> members) throws RemoteException;
    public String createElection(Calendar startTime, Calendar endTime, String description, String title, String department, int type, CopyOnWriteArrayList<String> validDeps) throws RemoteException;
    public String updateElection(long uid, Calendar startTime, Calendar endTime, String description, String title, String department) throws RemoteException;
    public void test(String msg) throws RemoteException;
    public ArrayList<Person> getListUsers(String department, int type) throws RemoteException;
    public ArrayList<Election> getListElections(String department, int type) throws RemoteException;
    public void setIsPrimary(boolean isPrimary) throws RemoteException;
    public String heartbeat() throws RemoteException;
    public TerminalInfo getPersonInfo(int cc, String name) throws RemoteException;
    //public Info getEligibleElection(int cc);
    //boolean verify User(long id, String dep) throws RemoteException;
}
