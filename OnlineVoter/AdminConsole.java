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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;

/**
 * Manager interface for the RMIServer
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
public class AdminConsole extends UnicastRemoteObject implements AdminConsoleInterface {
    private RMIServerInterface rmiSv;
    private InputStreamReader input;
    private BufferedReader reader;
    private String svIP;
    private String backupIP;
    private String svName;
    private int svPort;
    private int backupPort;
    private int timeoutTime;
    private boolean showRealTimeData;

    /**
     * Bridges a connection with RMI serves, works as Wrapper for the
     * selectServerTool Specifying a Timeout Time
     * 
     * @param timeoutTime Time until the Connection Fails and execution is ended
     */
    private void connectRMI(long timeoutTime, AdminConsole adm) {
        try {
            FutureTask<RMIServerInterface> task = new FutureTask<RMIServerInterface>(() -> {
                return selectServer();
            });
            Thread thread = new Thread(task);
            thread.start();
            this.rmiSv = task.get(timeoutTime, TimeUnit.SECONDS);
            this.rmiSv.subscribe((AdminConsoleInterface) this);
        } catch (Exception e) {
            System.out.println("There are no servers available, exiting");
            System.exit(1);
        }
    }

    /**
     * Server selector tool
     * <p>
     * Logic : Try to connect to the main Server, if that fails, try to connect to
     * the second one if that fails, wait 1 second
     * 
     * @return Returns an instance of a Remote object
     */
    RMIServerInterface selectServer() {
        RMIServerInterface rmiSv;
        while (true) {
            System.out.println("Loading");
            System.out.println(String.format("//%s:%d/%s", this.svIP, this.svPort, this.svName));
            System.out.println(String.format("//%s:%d/%s", this.backupIP, this.backupPort, this.svName));
            try {
                rmiSv = (RMIServerInterface) Naming
                        .lookup(String.format("//%s:%d/%s", this.svIP, this.svPort, this.svName));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                // System.out.println("Server 1 is not currently Available , switch to backup
                // server" + e.getMessage());
            }
            try {
                rmiSv = (RMIServerInterface) Naming
                        .lookup(String.format("//%s:%d/%s", this.backupIP, this.backupPort, this.svName));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                // System.out.println("Server 2 is currently also not available, waiting for
                // connection" + e.getMessage());
                rmiSv = null;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                ;
            }
        }
    }

    /**
     * Register a user into the database
     * 
     * @return True if registered false if not
     */
    public boolean regPerson() {
        // ler da consola
        String name, password, dep, address;
        int phoneNumber, ccNr, type;
        Calendar ccValidity = Calendar.getInstance();
        String[] date_fields;

        try {
            // get name
            System.out.println("Insert your name:");
            name = this.reader.readLine();
            // password
            System.out.println("Insert a password:");
            password = this.reader.readLine();
            // dep
            System.out.println("Insert your department:");
            dep = this.reader.readLine();
            // address
            System.out.println("Insert your address:");
            address = this.reader.readLine();
            // phoneNumber
            System.out.println("Insert your phone number:");
            phoneNumber = Integer.parseInt(this.reader.readLine());
            // ccNr
            System.out.println("Insert your CC number:");
            ccNr = Integer.parseInt(this.reader.readLine());
            // type
            System.out.println("Insert your role (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2) {
                System.out.println("Wrong type value inserted.");
                return false;
            }
            // ccValidity
            System.out.println("Insert your CC validity date(yyyy/mm/dd):");
            date_fields = this.reader.readLine().split("/");
            ccValidity.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) - 1,
                    Integer.parseInt(date_fields[2]));
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
            return false;
        }

        //verificar se o nome da lista contem caracteres invalidos
        if (password.contains(";") || password.contains("|")){
            System.out.println("List's name is invalid, it cannot contain ';' or '|'");
            return false;
        }

        // create person object
        // System.out.println("Create person objetct");
        Person p = new Person(name, password, dep, address, phoneNumber, ccNr, type, ccValidity);

        // enviar para o RMI server
        try {
            this.rmiSv.registerUser(p);
        } catch (Exception e) {
            System.out.println("There was a problem registering the user");
            connectRMI(this.timeoutTime, this);
        }

        return true;
    }

    /**
     * Registers a voting list
     * 
     * @return true if registered successfully, false if ont
     */
    public boolean regVotingList() {
        long electionId;
        String name, response;
        int type;
        CopyOnWriteArrayList<String> members = new CopyOnWriteArrayList<String>();

        try {
            // pedir o ID da eleicao
            System.out.println("Insert the election's Id:");
            electionId = Long.parseLong(this.reader.readLine());
            // pedir o nome da lista
            System.out.println("Insert the list's name:");
            name = this.reader.readLine();
            // pedir o tipo de lista
            System.out.println("Insert the list's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2) {
                System.out.println("Wrong type value inserted.");
                return false;
            }
            // pedir uids dos membros da lista
            System.out.println("Insert the list's members UID separated by ',':");
            try {
                for (String uid : this.reader.readLine().split(",")) {
                    members.add(uid);
                }
            } catch (IOException e) {
                System.out.println("Parsing error of the list members.");
            }

            //verificar se o nome da lista contem caracteres invalidos
            if (name.contains(";") || name.contains("|")){
                System.out.println("List's name is invalid, it cannot contain ';' or '|'");
                return false;
            }

            response = this.rmiSv.createVotingList(electionId, name, type, members);
            if (response.equals(""))
                return true;
            else {
                System.out.println(response);// dizer o que esta errado
                return false;
            }
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @param option 1 if update, 0 if new election
     * @return boolean true if created/updated, false if not
     */
    public boolean regElection(int option) {// if option = 0 -> create election, option = 1 -> update election
        Calendar startTime = Calendar.getInstance(), endTime = Calendar.getInstance();
        String title, description, department, response, aux;
        int type = -1;
        String[] date_fields;
        long uid = -1;

        try {
            // pedir o uid da eleicao
            if (option == 1) {
                System.out.println("Insert election's uid:");
                aux = this.reader.readLine();
                if (aux.equals("")) {
                    System.out.println("A uid has to be given.");
                    return false;
                } else
                    uid = Long.parseLong(aux);
            }
            // obter o nome
            System.out.println("Insert the election's title:");
            title = this.reader.readLine();
            if (title.equals("") && option == 1)
                title = null;
            else if (title.equals("")) {
                System.out.println("A title has to be given.");
                return false;
            }
            // obter a descricao
            System.out.println("Insert the election's description:");
            description = this.reader.readLine();
            if (description.equals("") && option == 1)
                description = null;
            else if (description.equals("")) {
                System.out.println("A description has to be given.");
                return false;
            }
            // obter o departamento
            System.out.println("Insert the election's department:");
            department = this.reader.readLine();
            if (department.equals("") && option == 1)
                department = null;
            else if (department.equals("")) {
                System.out.println("A department has to be given.");
                return false;
            }
            // obter o tipo de eleicao
            if (option == 0) {
                System.out.println("Insert the election's type (0 - student, 1 - teacher, 2 - staff):");
                aux = this.reader.readLine();
                if (aux.equals("")) {
                    System.out.println("A type has to be given.");
                    return false;
                }
                type = Integer.parseInt(aux);
                if (type != 0 && type != 1 && type != 2) {
                    System.out.println("Wrong type value inserted.");
                    return false;
                }
            }
            // obter data de inicio
            System.out.println("Insert the election's starting date (yyyy/MM/dd/hh/mm):");
            aux = this.reader.readLine();
            if (aux.equals("") && option == 1)
                startTime = null;
            else if (aux.equals("")) {
                System.out.println("A starting date has to be given.");
                return false;
            } else {
                date_fields = aux.split("/");
                startTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) - 1,
                        Integer.parseInt(date_fields[2]), Integer.parseInt(date_fields[3]),
                        Integer.parseInt(date_fields[4]));
                // verify if the starting date is valid
                /*
                 * if (startTime.before(Calendar.getInstance())){
                 * System.out.println("The starting date is invalid."); return false; }
                 */
            }
            // obter a data de fim da votacao
            System.out.println("Insert the election's ending date (yyyy/MM/dd/hh/mm):");
            aux = this.reader.readLine();
            if (aux.equals("") && option == 1)
                endTime = null;
            else if (aux.equals("")) {
                System.out.println("A ending date has to be given.");
                return false;
            } else {
                date_fields = aux.split("/");
                endTime.set(Integer.parseInt(date_fields[0]), Integer.parseInt(date_fields[1]) - 1,
                        Integer.parseInt(date_fields[2]), Integer.parseInt(date_fields[3]),
                        Integer.parseInt(date_fields[4]));
                // verify if the starting date is valid
                if (endTime.before(Calendar.getInstance())) {
                    System.out.println("The ending date is invalid.");
                    return false;
                }
            }

            // verify if the endTime is after the startTime
            if (startTime != null && endTime != null) {
                if (endTime.before(startTime)) {
                    System.out.println("The ending date is before the starting date.");
                    return false;
                }
            }
            CopyOnWriteArrayList<VotingListInfo> validDeps = new CopyOnWriteArrayList<>();
            validDeps.add(new VotingListInfo("WEB"));

            if (option == 0)
                response = this.rmiSv.createElection(startTime, endTime, description, title, department, type,
                        validDeps);
            else if (option == 1)
                response = this.rmiSv.updateElection(uid, startTime, endTime, description, title, department);
            else
                response = "Wrong option.";
            if (response.equals(""))
                return true;
            else {
                System.out.println(response);
                return false;
            }
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Show users
     */
    private void showUsers() {
        String department;
        int type;
        ArrayList<Person> users = new ArrayList<Person>();
        try {
            // obter o nome do departamento
            System.out.println("Insert the department name:");
            department = this.reader.readLine();
            // obter o tipo de user
            System.out.println("Insert person's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2) {
                System.out.println("Wrong type value inserted.");
                return;
            }
            users = this.rmiSv.getListUsers(department, type);

            // print the users info
            for (Person p : users)
                System.out.println(p);
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }
    }

    /**
     * Show Elections
     */
    private void showElections() {
        String department;
        int type;
        ArrayList<Election> elections = new ArrayList<Election>();
        try {
            // obter o nome do departamento
            System.out.println("Insert the department name:");
            department = this.reader.readLine();
            // obter o tipo de user
            System.out.println("Insert person's type (0 - student, 1 - teacher, 2 - staff):");
            type = Integer.parseInt(this.reader.readLine());
            if (type != 0 && type != 1 && type != 2) {
                System.out.println("Wrong type value inserted.");
                return;
            }
            elections = this.rmiSv.getListElections(department, type);
            System.out.println("---------------------------------------------");
            // print elections info
            for (Election e : elections) {
                System.out.println(e);
                System.out.println("####################");
            }
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }
    }

    /**
     * Show RealTime Data
     */
    private void realTimeData() {
        // mostrar real time data
        this.showRealTimeData = true;
        try {
            this.reader.readLine();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
        // se o user interagir com a consola desativa esta funcionalidade e volta ao
        // menu
        this.showRealTimeData = false;
    }

    public void showFinishedEletcionData() {
        try {
            System.out.println("Insert election's uid.");
            String aux = this.reader.readLine();
            long electionId = Long.parseLong(aux);
            String response = this.rmiSv.finishedElectionData(electionId);
            if (response.equals("")) {
                System.out.println("Invalid election's uid or the election hasn't ended yet.");
            } else {
                System.out.println(response);
            }
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }
    }

    /**
     * @param mode 0 if addition, 1 if removal
     * @see RMIServer#updateTables(String, long, int)
     */
    private void updateTable(int mode) {
        try {
            // obter o uid da eleicao
            System.out.println("Insert the election's id.");
            long electionId = Long.parseLong(this.reader.readLine());

            // obter o nome da mesa (departamento)
            System.out.println("Insert the table's name (department).");
            String tableDepartment = this.reader.readLine();

            this.rmiSv.updateTables(tableDepartment, electionId, mode);
        } catch (RemoteException e) {
            connectRMI(this.timeoutTime, this);
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    /**
     * Displays the menu for the user to select and option
     */
    private void menu() {
        boolean stop = false;
        int option;
        while (!stop) {
            try {
                System.out.println("---------------------------------------------");
                System.out.println(
                        "Choose an option:\n1 - Register user\n2 - Create election\n3 - Create a voting list\n4 - Show users from a department and a certain type\n5 - Show elections from a department and a certain type\n6 - Update election\n7 - Show finished election's data.\n8 - Show real time data.\n9 - Add a table to an election.\n10 - Remove a table from an election.\n0 - exit");
                option = Integer.parseInt(this.reader.readLine());
                if (option == 1)
                    this.regPerson();
                else if (option == 2)
                    this.regElection(0);
                else if (option == 3)
                    this.regVotingList();
                else if (option == 4)
                    this.showUsers();
                else if (option == 5)
                    this.showElections();
                else if (option == 6)
                    this.regElection(1);
                else if (option == 7)
                    this.showFinishedEletcionData();
                else if (option == 8)
                    this.realTimeData();
                else if (option == 9)
                    this.updateTable(0);
                else if (option == 10)
                    this.updateTable(1);
                else if (option == 0)
                    stop = true;
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Choose one of the numbers.");
            }
        }
        System.exit(0);
    }

    /**
     * Prints remote data on Admin Console
     * 
     * @param s String to be Printed
     * @throws RemoteException if connection cannot be established
     */
    public void printOnConsole(String s) throws RemoteException {
        if (this.showRealTimeData) {
            System.out.println(s);
        }
    }

    /**
     * Class constructor for admin console
     * 
     * @param ip     Ip of the RMI server
     * @param svName Name of the server
     * @param port   Port of the server
     * @param backup Backup Port of the server
     * @throws RemoteException if connection cannot be made
     */
    AdminConsole(String svIP, int svPort, String backupIP, int backupPort, String svName, int timeoutTime)
            throws RemoteException {
        this.svIP = svIP;
        this.svPort = svPort;
        this.backupIP = backupIP;
        this.backupPort = backupPort;
        this.svName = svName;
        this.timeoutTime = timeoutTime;
        this.showRealTimeData = false;

        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);

        connectRMI(this.timeoutTime, this);
        this.menu();

    }

    /**
     * @param args Terminal arguments
     * @throws MalformedURLException if URL is wrong
     * @throws RemoteException       if connection cannot be made
     * @throws NotBoundException     if an attempt is made to lookup or unbind in
     *                               the registry a name that has no associated
     *                               binding.
     */
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
        String svIP = args[0];
        int svPort = Integer.parseInt(args[1]);
        String backupIP = args[2];
        int backupPort = Integer.parseInt(args[3]);
        String svName = args[4];
        // String ip = "localhost";
        int timeoutTime = Integer.parseInt(args[5]);
        new AdminConsole(svIP, svPort, backupIP, backupPort, svName, timeoutTime);
    }

}
