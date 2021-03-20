import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface{
    private CopyOnWriteArrayList<Person> pList = new CopyOnWriteArrayList<Person>();
    private CopyOnWriteArrayList<Election> eList = new CopyOnWriteArrayList<Election>();
    private int port;

    private Election searchElectionById(long id){
        return null;
    }
     
    private boolean loadData(String path){
        return true;
    }
    public boolean registerUser(Person p) throws RemoteException{
        return true;
    }
    public boolean createElection(Election c) throws RemoteException{
        return true;
    }

    public void test(String msg) throws RemoteException{
        System.out.println(msg);
    }

    RMIServer(int port) throws RemoteException{
        super();
        this.port = port;
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