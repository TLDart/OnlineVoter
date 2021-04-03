package OnlineVoter;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.registry.Registry;

public class AdminConsole extends UnicastRemoteObject implements AdminConsoleInterface{
    private RMIServerInterface rmiSv;
    private InputStreamReader input;
    private BufferedReader reader;
    private String ip;
    private String svName;
    private int port;
    private int backup;
    private boolean showRealTimeData;

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
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
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
        CopyOnWriteArrayList<String> members = new CopyOnWriteArrayList<String>();


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
            System.out.println("Insert the list's members UID separated by ',':");
            try{
                for (String uid : this.reader.readLine().split(",")){
                    members.add(uid);
                }
            }catch(IOException e){
                System.out.println("WTF");
            }

            response = this.rmiSv.createVotingList(electionId, name, type, members);
            if(response.equals("")) return true;
            else{
                System.out.println(response);//dizer o que esta errado
                return false;
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
            return false;
        }
    }

    public boolean regElection(int option){//if option = 0 -> create election, option = 1 -> update election
        Calendar startTime = Calendar.getInstance(), endTime = Calendar.getInstance();
        String title, description, department, response, aux;
        int type = -1;
        String [] date_fields;
        long uid = -1;

        try{
            //pedir o uid da eleicao
            if (option == 1){
                System.out.println("Insert election's uid:");
                aux = this.reader.readLine();
                if(aux.equals("")){
                    System.out.println("A uid has to be given.");
                    return false;
                }
                else uid = Long.parseLong(aux);                
            }
            //obter o nome
            System.out.println("Insert the election's title:");
            title = this.reader.readLine();
            if(title.equals("") && option == 1) title = null;
            else if(title.equals("")){
                System.out.println("A title has to be given.");
                return false;
            }
            //obter a descricao
            System.out.println("Insert the election's description:");
            description = this.reader.readLine();
            if(description.equals("") && option == 1) description = null;
            else if(description.equals("")){
                System.out.println("A description has to be given.");
                return false;
            }
            //obter o departamento
            System.out.println("Insert the election's department:");
            department = this.reader.readLine();
            if(department.equals("") && option == 1) department = null;
            else if(department.equals("")){
                System.out.println("A department has to be given.");
                return false;
            }
            //obter o tipo de eleicao
            if(option == 0){
                System.out.println("Insert the election's type (0 - student, 1 - teacher, 2 - staff):");
                aux = this.reader.readLine();
                if(aux.equals("")){
                    System.out.println("A type has to be given.");
                    return false;
                }
                type = Integer.parseInt(aux);
                if (type != 0 && type != 1 && type != 2){
                    System.out.println("Wrong type value inserted.");
                    return false;
                }
            }
            //obter data de inicio
            System.out.println("Insert the election's starting date (yyyy/MM/dd/hh/mm):");
            aux = this.reader.readLine();
            if (aux.equals("") && option == 1) startTime = null;
            else if(aux.equals("")){
                System.out.println("A starting date has to be given.");
                return false;
            }
            else{
                date_fields = aux.split("/");
                startTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) - 1, Integer.parseInt(date_fields[2]), Integer.parseInt(date_fields[3]), Integer.parseInt(date_fields[4]));
                //verify if the starting date is valid
               /*  if (startTime.before(Calendar.getInstance())){
                    System.out.println("The starting date is invalid.");
                    return false;
                } */
            }
            //obter a data de fim da votacao
            System.out.println("Insert the election's ending date (yyyy/MM/dd/hh/mm):");
            aux = this.reader.readLine();
            if (aux.equals("") && option == 1) endTime = null;
            else if(aux.equals("")){
                System.out.println("A ending date has to be given.");
                return false;
            }
            else{
                date_fields = aux.split("/");
                endTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) -1, Integer.parseInt(date_fields[2]), Integer.parseInt(date_fields[3]), Integer.parseInt(date_fields[4]));
                //verify if the starting date is valid
                if (endTime.before(Calendar.getInstance())){
                    System.out.println("The ending date is invalid.");
                    return false;
                }
            }

            //verify if the endTime is after the startTime
            if(startTime != null && endTime != null){
                if(endTime.before(startTime)){
                    System.out.println("The ending date is before the starting date.");
                    return false;
                }
            }
            CopyOnWriteArrayList<String> validDeps = new CopyOnWriteArrayList<>();
            validDeps.add(department);
            if (option == 0) response = this.rmiSv.createElection(startTime, endTime, description, title, department, type, validDeps);
            else if (option == 1) response = this.rmiSv.updateElection(uid, startTime, endTime, description, title, department);
            else response = "Wrong option.";
            if (response.equals("")) return true;
            else{
                System.out.println(response);
                return false;
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            return false;
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
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
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
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
            System.out.println("---------------------------------------------");
            //print elections info
            for (Election e : elections){
              System.out.println(e);
              System.out.println("####################");
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
        }
    }

    private void realTimeData(){
        //mostrar real time data
        this.showRealTimeData = true;
        try{
            this.reader.readLine();
        }catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
        //se o user interagir com a consola desativa esta funcionalidade e volta ao menu
        this.showRealTimeData = false;
    }

    public void showFinishedEletcionData(){
        try{
            System.out.println("Insert election's uid.");
            String aux = this.reader.readLine();
            long electionId = Long.parseLong(aux);
            String response = this.rmiSv.finishedElectionData(electionId);
            if (response.equals("")){
                System.out.println("Invalid election's uid.");
            }
            else{
                System.out.println(response);
            }
        }
        catch(RemoteException rE){
            //server down?
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
        catch(NumberFormatException e){
            System.out.println("NumberFormatException: " + e.getMessage());
        }
    }

    private void menu(){
        boolean stop = false;
        int option;
        while(!stop){
            try{
                System.out.println("---------------------------------------------");
                System.out.println("Choose an option:\n1 - Register user\n2 - Create election\n3 - Create a voting list\n4 - Show users from a department and a certain type\n5 - Show elections from a department and a certain type\n6 - Update election\n7 - Show finished election's data.\n8 - Show real time data.\n0 - exit");
                option = Integer.parseInt(this.reader.readLine());
                if (option == 1) this.regPerson();
                else if (option == 2) this.regElection(0);
                else if (option == 3) this.regVotingList();
                else if (option == 4) this.showUsers();
                else if (option == 5) this.showElections();
                else if (option == 6) this.regElection(1);
                else if (option == 7) this.showFinishedEletcionData();
                else if (option == 8) this.realTimeData();
                else if (option == 0) stop = true;
            }
            catch(IOException e){
                System.out.println("IOException: " + e.getMessage());
            }catch(NumberFormatException e){
                System.out.println("Choose one of the numbers.");
            }
        }
        System.exit(0);
    }

    private void selectServer(){
        int temp;
        do{
            try{
                this.rmiSv.heartbeat();
            }
            catch(RemoteException e ){
                temp = port; 
                this.port = this.backup;
                this.backup = temp;
                try{
                    //this.rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s",ip,port,svName));
                    Registry reg = LocateRegistry.getRegistry(this.ip, this.port);
                    this.rmiSv = (RMIServerInterface) reg.lookup("SV");
                    this.rmiSv.heartbeat();
                }
                catch(Exception f){
                    System.out.println("there was an error.\n" + e.getMessage());
                    this.rmiSv = null;
                }
            }
        }while(this.rmiSv == null);
    }

    public void printOnConsole(String s) throws RemoteException{
        if(this.showRealTimeData){
            System.out.println(s);
        }
    }

    AdminConsole(String ip, String svName, int port, int backup) throws RemoteException{
        this.ip =  ip;
        this.svName = svName;
        this.port = port;
        this.backup = backup;
        this.showRealTimeData = false;

        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        try{
            // System.out.println("here1");
            // System.setSecurityManager(new SecurityManager());
            // System.setProperty("java.security.policy","file:./OnlineVoter/security.policy");
            
            
            //this.rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s",ip,port,svName));
            Registry reg = LocateRegistry.getRegistry(this.ip, this.port);
            this.rmiSv = (RMIServerInterface) reg.lookup("SV");
            
            //System.out.println("here2");
            this.rmiSv.test("Sending MSg");
            //this.regPerson();
            this.rmiSv.subscribe((AdminConsoleInterface) this);
            this.menu();
        }
        // catch(Exception e){
        //     System.out.println("there was an error.\n" + e.getMessage());
        // }
        catch(RemoteException e){
            System.out.println("RemoteException: " + e.getMessage());
        }
        // catch(MalformedURLException e){
        //     System.out.println("MalformedURLException: " + e.getMessage());
        // }
        catch(NotBoundException e){
            System.out.println("NotBoundException: " + e.getMessage());
        }

    }
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        int port = Integer.parseInt(args[0]);
        int backup = Integer.parseInt(args[1]);
        String svName = "SV";
        //String ip = "localhost";
        String ip = args[2];
        new AdminConsole(ip, svName, port, backup);
    }

}
