import java.rmi.*;
import java.net.MalformedURLException;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.text.DefaultEditorKit.CopyAction;

//import org.graalvm.compiler.nodes.java.ArrayLengthNode;

import jdk.jshell.spi.ExecutionControl.ExecutionControlException;

public class AdminConsole {
    RMIServerInterface rmiSv;
    InputStreamReader input;
    BufferedReader reader;

    public  boolean regPerson(){
        //ler da consola
        String name, password, dep, address;
        int phoneNumber, ccNr, type;
        Calendar ccValidity = Calendar.getInstance();
        String [] date_fields;

        try{
        //get name
            System.out.println("Insert your name:");
            name = this.reader.readLine();
            //password
            System.out.println("Insert a password:");
            password = this.reader.readLine();
            //dep
            System.out.println("Insert your department:");
            dep = this.reader.readLine();
            //address
            System.out.println("Insert your address:");
            address = this.reader.readLine();
            //phoneNumber
            System.out.println("Insert your phone number:");
            phoneNumber = Integer.parseInt(this.reader.readLine());
            //ccNr
            System.out.println("Insert your CC number:");
            ccNr = Integer.parseInt(this.reader.readLine());
            //type
            System.out.println("Insert your role (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2){
                System.out.println("Wrong type value inserted.");
                return false;
            }
            //ccValidity
            System.out.println("Insert your CC validity date(yyyy/mm/dd):");
            date_fields = this.reader.readLine().split("/");
            ccValidity.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) - 1, Integer.parseInt(date_fields[2]));
        }catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }

        //create person object
        //System.out.println("Create person objetct");
        Person p = new Person(name, password, dep, address, phoneNumber, ccNr, type, ccValidity);

        //enviar para o RMI server
        try{
            this.rmiSv.registerUser(p);
        }
        catch(Exception e){
            System.out.println("There was a problem registering the user");
        }

        return true;
    }

    public boolean regVotingList(){        
        long electionId;
        String name, response;
        int type;
        CopyOnWriteArrayList<Long> members_uid = new CopyOnWriteArrayList<Long>();


        try{
            //pedir o ID da eleicao
            System.out.println("Insert the election's Id:");
            electionId = Long.parseLong(this.reader.readLine());
            //pedir o nome da lista
            System.out.println("Insert the list's name:");
            name = this.reader.readLine();
            //pedir o tipo de lista
            System.out.println("Insert the list's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2){
                System.out.println("Wrong type value inserted.");
                return false;
            }
            //pedir uids dos membros da lista
            System.out.println("Insert the list's members separated by ',':");
            for (String uid : this.reader.readLine().split(",")){
                members_uid.add(Long.parseLong(uid));
            }

            response = this.rmiSv.createVotingList(electionId, name, type, members_uid);
            if(response.equals("")) return true;
            else{
                System.out.println(response);//dizer o que esta errado
                return false;
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }
    }

    public boolean regElection(){
        Calendar startTime = Calendar.getInstance(), endTime = Calendar.getInstance();
        String title, description, department, response;
        int type;
        String [] date_fields;

        try{
            //obter o nome
            System.out.println("Insert the election's title:");
            title = this.reader.readLine();
            //obter a descricao
            System.out.println("Insert the election's description:");
            description = this.reader.readLine();
            //obter o departamento
            System.out.println("Insert the election's department:");
            department = this.reader.readLine();
            //obter o tipo de eleicao
            System.out.println("Insert the election's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2){
                System.out.println("Wrong type value inserted.");
                return false;
            }
            //obter data de inicio
            System.out.println("Insert the election's starting date (yyyy/mm/dd):");
            date_fields = this.reader.readLine().split("/");
            startTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]), Integer.parseInt(date_fields[2]));
            //verify if the starting date is valid
            if (startTime.before(Calendar.getInstance())){
                System.out.println("The starting date is invalid.");
                return false;
            }
            //obter a data de fim da votacao
            System.out.println("Insert the election's ending date (yyyy/mm/dd):");
            date_fields = this.reader.readLine().split("/");
            endTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]), Integer.parseInt(date_fields[2]));
            //verify if the starting date is valid
            if (endTime.before(Calendar.getInstance())){
                System.out.println("The ending date is invalid.");
                return false;
            }

            response = this.rmiSv.createElection(startTime, endTime, description, title, department, type);
            if (response.equals("")) return true;
            else{
                System.out.println(response);
                return false;
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }
    }

    private void showUsers(){
        String department;
        int type;
        ArrayList<Person> users = new ArrayList<Person>();
        try{
            //obter o nome do departamento
            System.out.println("Insert the department name:");
            department = this.reader.readLine();
            //obter o tipo de user
            System.out.println("Insert person's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2){
                System.out.println("Wrong type value inserted.");
                return;
            }
            users = this.rmiSv.getListUsers(department, type);

            //print the users info
            for (Person p : users) System.out.println(p);
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void showElections(){
        String department;
        int type;
        ArrayList<Election> elections = new ArrayList<Election>();
        try{
            //obter o nome do departamento
            System.out.println("Insert the department name:");
            department = this.reader.readLine();
            //obter o tipo de user
            System.out.println("Insert person's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2){
                System.out.println("Wrong type value inserted.");
                return;
            }
            elections = this.rmiSv.getListElections(department, type);

            //print elections info
            for (Election e : elections) System.out.println(e);
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void menu(){
        boolean stop = false;
        int option;
        try{
            while(!stop){
                System.out.println("Choose an option:\n1 - Register user\n2 - Create election\n3 - Create a voting list\n4 - Show users from a department and a certain type\n5 - Show elections from a department and a certain type\n0 - exit");
                option = Integer.parseInt(this.reader.readLine());
                if (option == 1) this.regPerson();
                else if (option == 2) this.regElection();
                else if (option == 3) this.regVotingList();
                else if (option == 4) this.showUsers();
                else if (option == 5) this.showElections();
                else if (option == 0) stop = true;
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
    }

    AdminConsole(String ip, String svName, int port){
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        try{
            this.rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s",ip,port,svName));
            this.rmiSv.test("Sending MSg");
            //this.regPerson();
            this.menu();
        }
        catch(Exception e){
            System.out.println("there was an error.\n" + e.getMessage());
        }

    }
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        int port = 3099;
        String svName = "SV";
        String ip = "localhost";
        new AdminConsole(ip, svName, port);
    }

}
