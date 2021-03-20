import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private Election searchElectionById(long uid){
        for(Election e: eList){
            if(e.getUid() == uid){
                return e;
            }
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