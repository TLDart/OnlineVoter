import java.rmi.*;
import java.net.MalformedURLException;
import java.io.*;
import java.util.Calendar;

import jdk.jshell.spi.ExecutionControl.ExecutionControlException;

public class AdminConsole {
    RMIServerInterface rmiSv;

    public  boolean regPerson(){
        //ler da consola
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        String name, password, dep, address;
        int phoneNumber, ccNr;
        Calendar ccValidity = Calendar.getInstance();
        String [] date_fields;

        try{
        //get name
            System.out.println("Insert your name:");
            name = reader.readLine();
            //password
            System.out.println("Insert a password:");
            password = reader.readLine();
            //dep
            System.out.println("Insert your department:");
            dep = reader.readLine();
            //address
            System.out.println("Insert your address:");
            address = reader.readLine();
            //phoneNumber
            System.out.println("Insert your phone number:");
            phoneNumber = Integer.parseInt(reader.readLine());
            //ccNr
            System.out.println("Insert your CC number:");
            ccNr = Integer.parseInt(reader.readLine());
            //ccValidity
            System.out.println("Insert your CC validity date(yyyy/mm/dd):");
            date_fields = reader.readLine().split("/");
            ccValidity.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]), Integer.parseInt(date_fields[2]));
        }catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }

        //create person object
        //System.out.println("Create person objetct");
        Person p = new Person(name, password, dep, address, phoneNumber, ccNr, ccValidity);

        //enviar para o RMI server
        try{
            this.rmiSv.registerUser(p);
        }
        catch(Exception e){
            System.out.println("There was a problem registering the user");
        }
        //envolver em try catch porque pode dar erro do lado do servidor

        return true;
}

    AdminConsole(String ip, String svName, int port){
        try{
            this.rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s",ip,port,svName));
            this.rmiSv.test("Sending MSg");
            this.regPerson();
        }
        catch(Exception e){
            System.out.println("there was an error");
        }

    }
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        int port = 3099;
        String svName = "SV";
        String ip = "localhost";
        new AdminConsole(ip, svName, port);
    }

}
