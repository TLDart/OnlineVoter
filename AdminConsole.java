import java.rmi.*;
import java.net.MalformedURLException;

public class AdminConsole {
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        int port = 3099;
        String svName = "SV";
        String ip = "localhost";
        RMIServerInterface rmiSv= (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s",ip,port,svName));
        rmiSv.test("Sending MSg");
    }
}
