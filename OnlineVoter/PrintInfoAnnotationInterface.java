package OnlineVoter;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintInfoAnnotationInterface extends Remote{
    public void sendRealTimeData(String str, int type) throws RemoteException;
}
