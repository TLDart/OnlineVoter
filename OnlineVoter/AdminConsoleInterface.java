package OnlineVoter;
import java.rmi.*;
public interface AdminConsoleInterface extends Remote {
    public void printOnConsole(String s) throws RemoteException;
}
