import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.io.*;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface{
    private CopyOnWriteArrayList<Person> pList;
    private CopyOnWriteArrayList<Election> eList;
    private int port; 
    private long lastElectionUid;
    private long lastPersonUid;
    String db = "data/db.csv";
    String db2 = "data/db2.csv";

    private Person getUserByUid(long uid){
        for (Person p : this.pList){
            if (p.getUid() == uid){
                return p;
            }
        }
        return null;
    }

    //verify if a voting list with a certain name already exists, if so it is returned
    private VotingList searchVotingList(Election election, String vlName){
        for (VotingList vl : election.getLists()){
            if (vlName.equals(vl.getName())) return vl;
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
        } catch (IOException | ClassNotFoundException e){
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

    public Election searchElectionById(long uid){
        for(Election e: eList){
            if(e.getUid() == uid){
                return e;
            }
        }
        return null;
    }

    public boolean registerUser(Person p) throws RemoteException{
        p.setUid(++lastPersonUid);
        pList.add(p);
        System.out.println(p.getName());
        saveObjectFile(db, pList);
        return true;
    }
    public boolean createElection(Election e) throws RemoteException{
        e.setUid(++lastElectionUid);
        return eList.add(e);
    }

    // public boolean createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<Long> members_uid){
    //     CopyOnWriteArrayList<Person> members = new CopyOnWriteArrayList<Person>();
    //     Person p;
    //     Election election;
    //     //encontrar as pessoas da lista
    //     for (long uid : members_uid){
    //         p = this.getUserByUid(uid);
    //         if (p != null) members.add(p);
    //     }
    //     //encontar a eleicao
    //     election = this.searchElectionById(electionId);
    //     election.getLists().add(new VotingList(name, type, members));

    //     return true;
    // }
    public String createVotingList(long electionId, String name, int type, CopyOnWriteArrayList<Long> members_uid){
        String response = "";//if empty it was sucessful, otherwise it says what was wrong with the request
        CopyOnWriteArrayList<Person> members = new CopyOnWriteArrayList<Person>();
        Person p;

        //get the election
        Election election = this.searchElectionById(electionId);
        if (election == null) response = response + "Election Id doesnt exist.\n";
        else{
            //verify if there's already a list with the same name for this election
            if(this.searchVotingList(election, name) != null) response = response + "A list with the given name already exists.\n";
        }

        //add members and check if the id is valid
        for (long uid : members_uid){
            p = this.getUserByUid(uid);
            if(p == null) response = response + String.format("Uid %f doesnt exist.\n", uid);
            else if(p.getType() != type) response = response + String.format("Uid %f doesnt have the same type as the list.");
            else members.add(p);
        }

        //check type
        if (election.getType() != type) response = response + String.format("Type is different from the election's type.");

        //no errors, add voting list to election
        if(response.equals("")){
            election.getLists().add(new VotingList(name, type, members));
            saveObjectFile(db2, eList);
        }


        return response;
    }

    public String createElection(Calendar startTime, Calendar endTime, String description, String title, String department, int type){
        String response = "";
        Election e;
        //verify if there is other election with the same name at the same time in the same departement for the same public
        for (Election election : this.eList){
            if (election.getTitle().equals(title) && election.getDepartment().equals(department) && election.getType() == type){
                if (election.getStartTime().before(endTime) || startTime.before(election.getEndTime())) return  "There is another election with the same name in the same department with overlapping time intervals.";
            }
        }

        //create the election
        e = new Election(startTime, endTime, description, title, department, new CopyOnWriteArrayList<VotingList>(), type);
        e.setUid(++lastElectionUid);
        if (response.equals("")){
            this.eList.add(e);
            saveObjectFile(db2, eList);
        }

        return response;
    }

    public String updateElection(long uid, Calendar startTime, Calendar endTime, String description, String title, String department){
        String response = "";
        Election election = this.searchElectionById(uid);
        if(election == null) return "Uid doens't exist.";

        if (startTime != null && endTime != null){
            election.setStartTime(startTime);
            election.setEndTime(endTime);
        }
        else if(startTime != null && endTime == null){
            if(election.getEndTime().before(startTime)) return "Starting time after the existing ending time.";
            else election.setStartTime(startTime);
        }
        else if(endTime != null){
            if(endTime.before(election.getStartTime())) return "Ending time before the existing starting time.";
            else election.setEndTime(endTime);
        }
        if(description != null) election.setDescription(description);
        if(department != null) election.setDepartment(department);
        if(title != null) election.setTitle(title);
        
        return response;
    }

    //get a list with the people of a certain department of a certain type
    public ArrayList<Person> getListUsers(String department, int type){
        ArrayList<Person> result = new ArrayList<Person>();
        for (Person p : this.pList){
            if (p.getDep().equals(department) && p.getType() == type) result.add(p);
        }
        return result;
    }

    public ArrayList<Election> getListElections(String department, int type){
        ArrayList<Election> result = new ArrayList<Election>();
        for (Election e : this.eList){
            if (e.getDepartment().equals(department) && e.getType() == type) result.add(e);
        }
        return result;
    }

    public void test(String msg) throws RemoteException{
        System.out.println(msg);
    }

    RMIServer(int port) throws RemoteException{
        super();
        this.port = port;
        Object t1 = this.loadObjectFile(db);
        Object t2 = this.loadObjectFile(db2);
        if(t1 != null){
            this.pList = (CopyOnWriteArrayList<Person>) t1;
            this.lastPersonUid = this.pList.size();
        }
        else{
            this.pList =  new CopyOnWriteArrayList<>();
        }
        if(t2 != null){
            this.eList = (CopyOnWriteArrayList<Election>) t2;
            this.lastElectionUid = this.eList.size();
        }
        else{
            this.eList =  new CopyOnWriteArrayList<>();
        }
        System.out.println(this.lastElectionUid);
        System.out.println(this.lastPersonUid);
    }

    public int getPort(){
        return this.port;
    }
    public static void main(String[] args) throws RemoteException {
        RMIServerInterface sv = new RMIServer(3099);
		LocateRegistry.createRegistry(sv.getPort()).rebind("SV", sv);
		System.out.println("Server ready...");
	}
}