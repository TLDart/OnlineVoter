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

/** Defines the caracteristics of the rmiserver
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
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

    
    /** 
     * @param uid
     * @return Person person with corresponding uid or null
     */
    private Person getUserByUid(long uid) {
        for (Person p : this.pList) {
            if (p.getUid() == uid) {
                return p;
            }
        }
        return null;
    }

    
    /** 
     * Verifies if a certain Voting list already exists in an Election
     * @param election Election object
     * @param vlName name of the voting list
     * @return VotingList respective voting list or null
     */
    private VotingList searchVotingList(Election election, String vlName) {
        for (VotingList vl : election.getLists()) {
            if (vlName.equals(vl.getName()))
                return vl;
        }
        return null;
    }

    
    /** 
     * Load objects from file
     * @param path path of the object file
     * @return Object Loaded object
     */
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

    
    /** 
     * @param path path to saved object
     * @param object Object to be saved
     * @return boolean true if saved sucessfully, false if not
     */
    public static boolean saveObjectFile(String path, Object object) {
        try {
            FileOutputStream objFile = new FileOutputStream(path);
            ObjectOutputStream outputStreamObj = new ObjectOutputStream(objFile);
            outputStreamObj.writeObject(object);
            outputStreamObj.close();
            objFile.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    
    /** 
     * @param uid Election uid
     * @return Election if found, null if not
     */
    public Election searchElectionById(long uid) {
        for (Election e : eList) {
            if (e.getUid() == uid) {
                return e;
            }
        }
        return null;
    }

    
    /** 
     * @param p Person Object to be saved
     * @return boolean if user registered sucessfully
     * @throws RemoteException if connection cannot be established
     */
    public boolean registerUser(Person p) throws RemoteException {
        p.setUid(++lastPersonUid);
        pList.add(p);
        System.out.println(p.getName());
        saveObjectFile(db, pList);
        return true;
    }

    
   /*  /** 
     * @param e Election Object
     * @return boolean true if creation was successful
     * @throws RemoteException
    public boolean createElection(Election e) throws RemoteException {
        e.setUid(++lastElectionUid);
        return eList.add(e);
    } */

    
    
    
    
    /** 
     * @param electionId uid of the election
     * @param name Name of the Election
     * @param type Type of the election
     * @param members Member the Table
     * @return String Response according to the status of the creation of the election
     */
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

    
    /** 
     * @param startTime Starttime of  the election
     * @param endTime End time of the election
     * @param description Description of the Election
     * @param title Title of the Election
     * @param department Departament of the Election
     * @param type Type of the Election
     * @param validDeps Valid Departaments of the election
     * @return String Response in of any errors
     */
    public String createElection(Calendar startTime, Calendar endTime, String description, String title,
            String department, int type, CopyOnWriteArrayList<VotingListInfo> validDeps) {
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
        e.getLists().add(new VotingList("null", type, new CopyOnWriteArrayList<String>()));
        e.getLists().add(new VotingList("blank", type, new CopyOnWriteArrayList<String>()));
        if (response.equals("")) {
            this.eList.add(e);
            saveObjectFile(db2, eList);
        }

        return response;
    }

    
    /** 
     * @param uid Uid of the election to be changed
     * @param startTime Starttime of  the election
     * @param endTime End time of the election
     * @param description Description of the Election
     * @param title Title of the Election
     * @param department Departament of the Election
     * @return String with status of the election
     */
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

    
    /** 
     * @param department Departament Name to be searched
     * @param type type of person to be searched
     * @return ArrayList<Person> with query of the conditions above
     */
    // get a list with the people of a certain department of a certain type
    public ArrayList<Person> getListUsers(String department, int type) {
        ArrayList<Person> result = new ArrayList<Person>();
        for (Person p : this.pList) {
            if (p.getDep().equals(department) && p.getType() == type)
                result.add(p);
        }
        return result;
    }

    
    /** 
     * @param department Departament Name to be searched
     * @param type type of person to be searchede
     * @return ArrayList<Election> with query of the conditions above
     */
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

    /**
     * Calls load Object File to load files into the correct lists
     */
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
        //System.out.println(this.lastElectionUid);
        //System.out.println(this.lastPersonUid);
    }

    
    /** 
     * @param cc CC of the Person
     * @return Person Person if found , null if not
     */
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


    
    /** 
     * @param cc CC of the Person
     * @param curDepName Name of the Departament currently in
     * @return TerminalInfo Structure with person and eligible Election to vote in
     */
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
                    System.out.println(curDepName);
                    System.out.println(p.notVoted(e));
                    if (inVotingTables(e, curDepName) && p.notVoted(e) && p.getType() == e.getType()) // If the current Voting table is valid and the
                                                                        // user did not vote before
                        result.add(e);
                }
            }
        }
        TerminalInfo tInfo = new TerminalInfo(-1, result, p);
        return tInfo;
    }

    
    /** 
     * @param tInfo Temrinal information (passed by the multicast server)
     * @throws RemoteException if connection is not available
     */
    public void processVote(TerminalInfo tInfo) throws RemoteException {
        System.out.println("started Processing");
        Election temp = null;
        if (tInfo.getV() == null || tInfo.getP() == null)
            System.out.println("SOmething went wrong");
        else {
            for (Election e : this.eList) {
                if (e.getUid() == tInfo.getV().getElectionUid()) {
                    temp = e;
                    for (VotingList v : e.getLists()) {
                        System.out.println(v.getName());
                        System.out.println(tInfo.getV().getListName());
                        if (v.getName().equals(tInfo.getV().getListName())) {
                            v.addCounter();
                            System.out.println(v);
                            break;
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
        // obter a eleicao onde o voto ocorreu
        Election election = this.getElectionById(tInfo.getV().getElectionUid());
        String response = String.format("%d - %s\n", election.getUid(), election.getTitle());
        for (VotingListInfo vt : election.getVotingTables()) {
            response = response + String.format("%s: %d\n", vt.getName(), vt.getVoteCount());
        }
        // mandar a info da eleicao a que este voto pertence para as consolas de
        // administracao (votos por mesa)
        this.infoForAdminConsoles(response);
    }
    /**
     * This function is used to create a connection with the admin console
     * @param adminConsole Admin console object representing the 
     * @throws RemoteException if connection fails
     */
    public void subscribe(AdminConsoleInterface adminConsole) throws RemoteException {
        if(!this.adminConsoles.contains(adminConsole)){
            this.adminConsoles.add(adminConsole);
        }
        // for(int i = 0; i < 10; i++){
        // this.infoForAdminConsoles("teste.");
        // }
    }

    // usar quando um voto e recebido -> numero de votos que vieram para aquela
    // eleicao daquela mesa
    // verificar de onde veio o voto, depois ir somar os votos que foram feitos
    // nessa mesa e adicionar +1 do que acabou de chegar
    // construir a string e passa-la a este metodo para que seja print nas consolas
    // de administracao
    /**
     * 
     * @param s
     * @throws RemoteException
     */
    private void infoForAdminConsoles(String s) throws RemoteException {
        for (AdminConsoleInterface ac : this.adminConsoles) {
            try {
                ac.printOnConsole(s);
            } catch (RemoteException e) {
                // ocorreu um erro a mandar a string para a console, pode estar offline
            }
        }
    }

    /**
     * Searches the election list for an election with a specific id
     * 
     * @param electionId the id of the election
     * @return THe instance of the election with the correspoding id, returns null
     *         otherwise
     */
    private Election getElectionById(long electionId) {
        for (Election election : this.eList) {
            if (election.getUid() == electionId) {
                return election;
            }
        }
        return null;
    }

    /**
     * Gathers the election data and output a response in String format
     * 
     * @param electionId The id of the current election
     * @return String with election information
     * @throws RemoteException if there is a failure sending connecting with the
     *                         server
     */
    public String finishedElectionData(long electionId) throws RemoteException {
        // check if the id corresponds to an election
        Election election = this.getElectionById(electionId);
        if (election == null) {
            return "";
        }
        // check if the id corresponds to a finished election
        if (election.getEndTime().before(Calendar.getInstance())) {
            // calculate results
            String response = "";
            long totalVoteCount = election.getTotalVoteCount();
            if (totalVoteCount == 0)
                totalVoteCount = 1; // para nao dar erro de divisao por zero quando ninguem votou e a eleicao acabou
            long voteNullCounter = election.getVoteNullCounter();
            long voteBlankCounter = election.getVoteBlankCounter();
            long vlVoteCount;
            for (VotingList vl : election.getLists()) {
                vlVoteCount = vl.getVoteCount();
                response = response
                        + String.format("%s: %d   %d%%\n", vl.getName(), vlVoteCount, vlVoteCount / totalVoteCount);
            }
            response = response + String.format("Blank: %d   %d%%\nNull: %d   %d%%", voteBlankCounter,
                    voteBlankCounter / totalVoteCount, voteNullCounter, voteNullCounter / totalVoteCount);
            return response;
        }
        return "";
    }

    
    /** 
     * Updates the list eligible tables for an election
     * @param tableDepartment Table to be added/ removed
     * @param electionId Id of the election to which the change is going to occur
     * @param mode 0 if addition, 1 if removal
     * @return String debug response sent to the terminal
     * @throws RemoteException
     */
    public String updateTables(String tableDepartment, long electionId, int mode) throws RemoteException {
        // verificar se a eleicao existe
        Election election = this.getElectionById(electionId);
        if (election == null)
            return "The election Id given doesn't exist.";
        //if (election.getStartTime().before(Calendar.getInstance()))
          //  return "Election already started, can't update tables.";

        // verificar o mode em que estamos -> 0 (adicionar mesa), 1 (remover mesa)
        if (mode == 0) {
            election.addVotingTable(tableDepartment);
        } else if (mode == 1) {
            election.remVotingTable(tableDepartment);
        }

        return "";
    }

    /**
     * Class constructor for the RMIServer
     * 
     * @param port   Port of the Current instance of the server
     * @param backup Port of the backup of the server
     * @throws RemoteException
     */
    RMIServer(int port, int backup) throws RemoteException {
        super();
        this.port = port;
        this.backUp = backup;
        this.adminConsoles = new ArrayList<AdminConsoleInterface>();
        loader();
    }

    /**
     * Verifies if a certain departament is eligible for the election
     * 
     * @param e          The current Election
     * @param curDepName The current departament
     * @return true if adepartament is valid, false if not
     */
    private boolean inVotingTables(Election e, String curDepName) {
    System.out.println(e.getVotingTables().size());
        for (VotingListInfo s : e.getVotingTables()) {
            System.out.println(s.getName());
            if (s.getName().equals(curDepName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the port of the server
     */
    public int getPort() {
        return this.port;
    }

    
    /** 
     * @param isPrimary
     */
    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
        loader();
    }

    /**
     * 
     */
    public String heartbeat() throws RemoteException {
        return "ACK";
    }

    /**
     * Parses the command line arguments and start the RMI server
     * 
     * @param args Command Line arguments that specify the of the main and backup
     *             server
     * @throws RemoteException When There is a failure in the connection
     */
    public static void main(String[] args) throws RemoteException {
        String svIP = args[0];
        int svPort = Integer.parseInt(args[1]);
        String backupIP = args[2];
        int backupPort = Integer.parseInt(args[3]);
        String svName = args[4];
        int counter = 0;
        RMIServerInterface svBack = null;
        while (counter < 5) {
               //System.out.println(String.format("//%s:%d/%s", "localhost", backup, "SV"));
               System.out.println(String.format("//%s:%d/%s", backupIP, backupPort, svName));
            try {
                svBack = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", backupIP, backupPort, svName));
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

        RMIServerInterface sv = new RMIServer(svPort, backupPort);
        // System.setSecurityManager(new SecurityManager());
        // System.setProperty("java.security.policy","./OnlineVoter/security.policy");
        // System.setProperty("java.rmi.server.hostname", "10.211.55.4");
        // System.setProperty("java.rmi.activation.port", String.format("%d", port));
        LocateRegistry.createRegistry(sv.getPort()).rebind(svName, sv);
        System.out.println(svName);
        System.out.println(svPort);
        System.out.println("Server ready...");
        sv.setIsPrimary(true);
        System.out.println("Changed to primary");
    }
}