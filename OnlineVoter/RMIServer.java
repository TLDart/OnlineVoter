package OnlineVoter;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.io.*;
import java.rmi.*;
import java.util.concurrent.*;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    private CopyOnWriteArrayList<Person> pList;
    private CopyOnWriteArrayList<Election> eList;
    private int port;
    private int backUp;
    private long lastElectionUid;
    private long lastPersonUid;
    String db = "data/db.csv";
    String db2 = "data/db2.csv";
    private boolean isPrimary = false;
    private ArrayList<AdminConsoleInterface> adminConsoles;

    private Person getUserByUid(long uid) {
        for (Person p : this.pList) {
            if (p.getUid() == uid) {
                return p;
            }
        }
        return null;
    }

    // verify if a voting list with a certain name already exists, if so it is
    // returned
    private VotingList searchVotingList(Election election, String vlName) {
        for (VotingList vl : election.getLists()) {
            if (vlName.equals(vl.getName()))
                return vl;
        }
        return null;
    }

    private Object loadObjectFile(String path) {
        Object obj;
        try {
            FileInputStream fileObj = new FileInputStream(path);
            ObjectInputStream inputStreamObj = new ObjectInputStream(fileObj);
            obj = inputStreamObj.readObject();
            inputStreamObj.close();
            fileObj.close();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }

    public static boolean saveObjectFile(String path, Object object) {
        try {
            FileOutputStream objFile = new FileOutputStream(path);
            ObjectOutputStream outputStreamObj = new ObjectOutputStream(objFile);
            outputStreamObj.writeObject(object);
            outputStreamObj.close();
            objFile.close();
            System.out.println("secure");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Election searchElectionById(long uid) {
        for (Election e : eList) {
            if (e.getUid() == uid) {
                return e;
            }
        }
        return null;
    }

    public boolean registerUser(Person p) throws RemoteException {
        p.setUid(++lastPersonUid);
        pList.add(p);
        System.out.println(p.getName());
        saveObjectFile(db, pList);
        return true;
    }

    public boolean createElection(Election e) throws RemoteException {
        e.setUid(++lastElectionUid);
        return eList.add(e);
    }

    // public boolean createVotingList(long electionId, String name, int type,
    // CopyOnWriteArrayList<Long> members_uid){
    // CopyOnWriteArrayList<Person> members = new CopyOnWriteArrayList<Person>();
    // Person p;
    // Election election;
    // //encontrar as pessoas da lista
    // for (long uid : members_uid){
    // p = this.getUserByUid(uid);
    // if (p != null) members.add(p);
    // }
    // //encontar a eleicao
    // election = this.searchElectionById(electionId);
    // election.getLists().add(new VotingList(name, type, members));

    // return true;
    // }
    public String createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<String> members) {
        String response = "";// if empty it was sucessful, otherwise it says what was wrong with the request
        Person p;

        // get the election
        Election election = this.searchElectionById(electionId);
        if (election == null)
            response = response + "Election Id doesnt exist.\n"; // TODO check here if something should be returned
        else {
            // verify if there's already a list with the same name for this election
            if (this.searchVotingList(election, name) != null)
                response = response + "A list with the given name already exists.\n";
        }

        // add members and check if the id is valid
        /*
         * for (long uid : members_uid){ p = this.getUserByUid(uid); if(p == null)
         * response = response + String.format("Uid %lu doesnt exist.%n", uid); else
         * if(p.getType() != type) response = response +
         * String.format("Uid %lu doesnt have the same type as the list.", uid); else
         * members.add(p); }
         */

        // check type
        if (election != null && election.getType() != type)
            response = response + "Type is different from the election's type.";

        // no errors, add voting list to election
        if (response.equals("")) {
            election.getLists().add(new VotingList(name, type, members));
            saveObjectFile(db2, eList);
        }

        return response;
    }

    public String createElection(Calendar startTime, Calendar endTime, String description, String title,
            String department, int type, CopyOnWriteArrayList<String> validDeps) {
        String response = "";
        Election e;
        // verify if there is other election with the same name at the same time in the
        // same departement for the same public
        for (Election election : this.eList) {
            if (election.getTitle().equals(title) && election.getDepartment().equals(department)
                    && election.getType() == type) {
                if (election.getStartTime().before(endTime) || startTime.before(election.getEndTime()))
                    return "There is another election with the same name in the same department with overlapping time intervals.";
            }
        }

        // create the election
        e = new Election(startTime, endTime, description, title, department, new CopyOnWriteArrayList<VotingList>(),
                type, validDeps);
        e.setUid(++lastElectionUid);
        if (response.equals("")) {
            this.eList.add(e);
            saveObjectFile(db2, eList);
        }

        return response;
    }

    public String updateElection(long uid, Calendar startTime, Calendar endTime, String description, String title,
            String department) {
        String response = "";
        Election election = this.searchElectionById(uid);
        if (election == null)
            return "Uid doens't exist.";
        if (election.getStartTime().before(Calendar.getInstance()))
            return "You cannot update this Election";

        if (startTime != null && endTime != null) {
            election.setStartTime(startTime);
            election.setEndTime(endTime);
        } else if (startTime != null && endTime == null) {
            if (election.getEndTime().before(startTime))
                return "Starting time after the existing ending time.";
            else
                election.setStartTime(startTime);
        } else if (endTime != null) {
            if (endTime.before(election.getStartTime()))
                return "Ending time before the existing starting time.";
            else
                election.setEndTime(endTime);
        }
        if (description != null)
            election.setDescription(description);
        if (department != null)
            election.setDepartment(department);
        if (title != null)
            election.setTitle(title);

        return response;
    }

    // get a list with the people of a certain department of a certain type
    public ArrayList<Person> getListUsers(String department, int type) {
        ArrayList<Person> result = new ArrayList<Person>();
        for (Person p : this.pList) {
            if (p.getDep().equals(department) && p.getType() == type)
                result.add(p);
        }
        return result;
    }

    public ArrayList<Election> getListElections(String department, int type) {
        ArrayList<Election> result = new ArrayList<Election>();
        for (Election e : this.eList) {
            if (e.getDepartment().equals(department) && e.getType() == type)
                result.add(e);
        }
        return result;
    }

    public void test(String msg) throws RemoteException {
        System.out.println(msg);
    }

    private void loader() {
        Object t1 = this.loadObjectFile(db);
        Object t2 = this.loadObjectFile(db2);
        if (t1 != null) {
            this.pList = (CopyOnWriteArrayList<Person>) t1;
            this.lastPersonUid = this.pList.size();
        } else {
            this.pList = new CopyOnWriteArrayList<>();
        }
        if (t2 != null) {
            this.eList = (CopyOnWriteArrayList<Election>) t2;
            this.lastElectionUid = this.eList.size();
        } else {
            this.eList = new CopyOnWriteArrayList<>();
        }
        System.out.println(this.lastElectionUid);
        System.out.println(this.lastPersonUid);
    }

    public Person getPersonByCC(int cc) {
        for (Person p : pList) {
            System.out.println(String.format("User %s", p.getName()));
            System.out.println(p.getCcNr());
            System.out.println(cc);
            if (p.getCcNr() == cc) {
                return p;
            }
        }
        return null;
    }

    public TerminalInfo getPersonInfo(int cc, String curDepName) {
        ArrayList<Election> result = new ArrayList<Election>();
        Person p = getPersonByCC(cc);
        if (p == null)
            return new TerminalInfo(-1, result, p);
        else {
            for (Election e : eList) {
                System.out.println(e.getTitle());
                System.out.println(String.format("%b %b %b", p.getDep().equals(e.getDepartment()),
                        e.getStartTime().before(Calendar.getInstance()), e.getEndTime().after(Calendar.getInstance())));
                System.out
                        .println(String.format("%s %s", e.getStartTime().getTime(), Calendar.getInstance().getTime()));
                if (p.getDep().equals(e.getDepartment()) && e.getStartTime().before(Calendar.getInstance())
                        && e.getEndTime().after(Calendar.getInstance())) { //
                    System.out.println(inVotingTables(e, curDepName));
                    System.out.println(p.notVoted(e));
                    if (inVotingTables(e, curDepName) && p.notVoted(e)) // If the current Voting table is valid and the
                                                                        // user did not vote before
                        result.add(e);
                }
            }
        }
        TerminalInfo tInfo = new TerminalInfo(-1, result, p);
        return tInfo;
    }

    public void processVote(TerminalInfo tInfo) throws RemoteException {
        System.out.println("started Processing");
        Election temp = null;
        if (tInfo.getV() == null || tInfo.getP() == null)
            System.out.println("SOmething went wrong");
        else {
            for (Election e : this.eList) {
                if (e.getUid() == tInfo.getV().getElectionUid()) {
                    temp = e;
                    if (tInfo.getV().getListName() == "null") {
                        e.addNull();
                    } else if (tInfo.getV().getListName() == "blank") {
                        e.addBlank();
                    }
                    else {
                        for (VotingList v : e.getLists()) {
                            System.out.println(v.getName()); 
                            System.out.println(tInfo.getV().getListName());
                            if(v.getName().equals(tInfo.getV().getListName())){
                                v.addCounter();
                                System.out.println(v);
                                break;
                            }
                        }
                    }
                break;
                }
            }
            for (Person p : this.pList) {
                if (p.getCcNr() == tInfo.getP().getCcNr()) {
                    p.addVotedElections(tInfo.getV());
                }
            }
            System.out.println("REached end");
            tInfo.setState(true);
            saveObjectFile(db2, eList);
            saveObjectFile(db, pList);
        }

    }

    public void subscribe(AdminConsoleInterface adminConsole) throws RemoteException{
        this.adminConsoles.add(adminConsole);
        // for(int i = 0; i < 10; i++){
        //     this.infoForAdminConsoles("teste.");
        // }
    }

    //usar quando um voto e recebido -> numero de votos que vieram para aquela eleicao daquela mesa
    //verificar de onde veio o voto, depois ir somar os votos que foram feitos nessa mesa e adicionar +1 do que acabou de chegar
    //construir a string e passa-la a este metodo para que seja print nas consolas de administracao
    private void infoForAdminConsoles(String s) throws RemoteException{
        for (AdminConsoleInterface ac : this.adminConsoles){
            try{
                ac.printOnConsole(s);
            }
            catch(RemoteException e){
                //ocorreu um erro a mandar a string para a console, pode estar offline
            }
        }
    }

    private Election getElectionById(long electionId){
        for (Election election : this.eList){
            if (election.getUid() == electionId){
                return election;
            }
        }
        return null;
    }

    public String finishedElectionData(long electionId) throws RemoteException{
        //check if the id corresponds to an election
        Election election = this.getElectionById(electionId);
        if (election == null){
            return "";
        }
        //check if the id corresponds to a finished election
        if (election.getEndTime().before(Calendar.getInstance())){
            //calculate results
            String response = "";
            long totalVoteCount = election.getTotalVoteCount();
            if (totalVoteCount == 0) totalVoteCount = 1; //para nao dar erro de divisao por zero quando ninguem votou e a eleicao acabou
            long voteNullCounter = election.getVoteNullCounter();
            long voteBlankCounter = election.getVoteBlankCounter();
            long vlVoteCount;
            for (VotingList vl : election.getLists()){
                vlVoteCount = vl.getVoteCount();
                response = response + String.format("%s: %d   %d%%\n", vl.getName(), vlVoteCount, vlVoteCount/totalVoteCount);
            }
            response = response + String.format("Blank: %d   %d%%\nNull: %d   %d%%", voteBlankCounter, voteBlankCounter/totalVoteCount, voteNullCounter, voteNullCounter/totalVoteCount);
            return response;
        }
        return "";
    }

    RMIServer(int port, int backup) throws RemoteException {
        super();
        this.port = port;
        this.backUp = backup;
        this.adminConsoles = new ArrayList<AdminConsoleInterface>();
        loader();
    }

    private boolean inVotingTables(Election e, String curDepName) {
        for (String s : e.getVotingTables()) {
            if (s.equals(curDepName)) {
                return true;
            }
        }
        return false;
    }

    public int getPort() {
        return this.port;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
        loader();
    }

    public String heartbeat() throws RemoteException {
        return "ACK";
    }

    public static void main(String[] args) throws RemoteException {
        int port = Integer.parseInt(args[0]);
        int backup = Integer.parseInt(args[1]);
        int counter = 0;
        RMIServerInterface svBack = null;
        while (counter < 5) {
            try {
                svBack = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", "localhost", backup, "SV"));
                System.out.println(String.format("//%s:%d/%s", "localhost", backup, "SV"));
                counter = 10;
            } catch (Exception e) {
                counter++;
                System.out.println(e.getMessage());
            }
            try {
                TimeUnit.SECONDS.sleep(1); // TODO check this try catch
            } catch (InterruptedException e) {
                return;
            }
        }
        counter = (counter == 10) ? 0 : 5;
        while (counter < 5) {
            try {
                if (svBack != null)
                    svBack.heartbeat();
                counter = 0;
                System.out.println("HeartBeat Successful");
            } catch (RemoteException e) {// This means that the other server is down, and therefore we check if we
                                         // assume primary
                System.out.println("Remote server failed");
                counter++;
            }
            try {
                TimeUnit.SECONDS.sleep(1); // TODO check this try catch
            } catch (InterruptedException e) {
                return;
            }
        }
        RMIServerInterface sv = new RMIServer(port, backup);
        // System.setSecurityManager(new SecurityManager());
        // System.setProperty("java.security.policy","./OnlineVoter/security.policy");
        LocateRegistry.createRegistry(sv.getPort()).rebind("SV", sv);
        System.out.println("Server ready...");
        sv.setIsPrimary(true);
        System.out.println("Changed to primary");
    }
}