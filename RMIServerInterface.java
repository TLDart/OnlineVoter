import java.rmi.*;
public interface RMIServerInterface extends Remote {
    public boolean registerUser(Person p) throws RemoteException;
    public boolean createElection(Election c) throws RemoteException;
    public int getPort() throws RemoteException;
    public void test(String msg) throws RemoteException;
    //boolean verify User(long id, String dep) throws RemoteException;
}
