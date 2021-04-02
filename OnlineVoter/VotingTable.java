package OnlineVoter;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.rmi.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The RequestHandler Class is an helper class to the Voting Table, used to manage a secondary Thread.
 * This class Handles Voting Terminal Messaging
 * 
 * @see RequestHandler#run()
 * 
 * @author Duarte Dias
 * @author Gabriel Fernandes
 */
class RequestHandler extends Thread {
    private String ip;
    private int port;
    private String svIP;
    private int svPort;
    private String backupIP;
    private int backupPort;
    private String svName;
    private String name;
    private CopyOnWriteArrayList<TerminalInfo> tInfo;
    private long timeoutTime;
    private InputStreamReader input;
    private BufferedReader reader;
    private RMIServerInterface rmiSV;
    /**
     * Class Constructor for the Voting Table Thread
     * @param ip The Ip of the voting table
     * @param port The port of the voting table
     * @param svIP The Ip of the RMI Server
     * @param svPort The Port of the RMI Server
     * @param backupIP The Ip of the backup RMI Server
     * @param backupPort The Port of the backup RMI Server
     * @param svName The name of the RMI Server
     * @param nTerminals The number of terminals that are instantiated
     * @param tInfo A structure that hold information about the Terminals
     * @param name The name of the voting table
     * @param timeoutTime The timeout Time of the RMI server (In case of failed connection)
     * @see RMIServer
     * @see VotingTerminal
     */
    RequestHandler(String ip, int port, String svIP, int svPort, String backupIP,int backupPort, String svName, int nTerminals, CopyOnWriteArrayList<TerminalInfo> tInfo,
            String name, long timeoutTime) {
        this.ip = ip;
        this.port = port;
        this.svIP = svIP;
        this.svPort = svPort;
        this.backupIP = backupIP;
        this.backupPort = backupPort;
        this.svName = svName;
        this.tInfo = tInfo;
        this.name = name;
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        this.timeoutTime = timeoutTime;
    }

    /**
     * Bridges a connection with RMI serves, works as Wrapper for the selectServerTool Specifying a Timeout Time
     * @param timeoutTime Time until the Connection Fails and execution is ended
     */
    private void connectRMI(long timeoutTime) {
        try {
            FutureTask<RMIServerInterface> task = new FutureTask<RMIServerInterface>(() -> {
                return selectServer();
            });
            Thread thread = new Thread(task);
            thread.start();
            this.rmiSV = task.get(timeoutTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("There are no servers available, exiting");
            System.exit(1);
        }
    }
    /**
     * Server selector tool
     * <p> Logic : Try to connect to the main Server, if that fails, try to connect to the second one if that fails, wait 1 second
     * @return Returns an instance of a Remote object 
     */
    RMIServerInterface selectServer() {
        RMIServerInterface rmiSv;
        while (true) {
            System.out.println("Loading");
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", this.svIP, this.svPort, this.svName));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                // System.out.println("Server 1 is not currently Available , switch to backup
                // server" + e.getMessage());
            }
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", this.backupIP, this.backupPort, this.svName));
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
     * Runs the Main Thread
     * In this thread, this thread works as a Request-Response Handler
     * <ol>
     *      <li> If the the VotingTerminal asks for authentication, the thread verfies the CC and Password Provided
     *      <li> Processes the Vote ensuring that no vote is lost, nor it is duplicated
     */
    public void run() {
        MulticastSocket socket = null;
        DatagramPacket packet, packetResponse;
        String message, cc = "", password = "", list = "";
        int curId;
        Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
        String[] tokens, temp;

        connectRMI(this.timeoutTime);

        try {
            socket = new MulticastSocket(this.port); // create socket and bind it
            InetAddress group = InetAddress.getByName(this.ip);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[1024];
                packetResponse = new DatagramPacket(buffer, buffer.length);
                socket.receive(packetResponse);

                System.out.println("Received packet from " + packetResponse.getAddress().getHostAddress() + ":"
                        + packetResponse.getPort() + " with message:");
                message = new String(packetResponse.getData(), 0, packetResponse.getLength());
                System.out.println(message);
                tokens = message.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\s+", "").split(";");
                temp = tokens[0].split("\\|");
                /*
                 * for (String t : temp) { System.out.println(t); }
                 */
                if (temp[0].equals("id") && !temp[1].equals("N")) {
                    curId = Integer.parseInt(temp[1]);
                    temp = tokens[1].split("\\|");

                    if (temp[0].equals("type") && temp[1].equals("auth")) {
                        temp = tokens[2].split("\\|");
                        if (temp[0].equals("cc")) {
                            cc = temp[1];
                        }
                        temp = tokens[3].split("\\|");
                        if (temp[0].equals("password")) {
                            password = temp[1];
                        }
                        Person ptemp = this.tInfo.get(curId).getP();
                        System.out.println(ptemp.getCcNr());
                        System.out.println(ptemp.getPassword());
                        if (ptemp.getCcNr() == Integer.parseInt(cc) && ptemp.getPassword().equals(password)) {
                            String listElements = "";// TODO: Mb verify is list has atleast 1 element
                            int i = 1;
                            for (VotingList v : this.tInfo.get(curId).getValidElections()
                                    .get(this.tInfo.get(curId).getOption()).getLists()) {
                                listElements += String.format("listitem%d|%s", i, v.getName());
                                if (i < this.tInfo.get(curId).getValidElections().get(this.tInfo.get(curId).getOption())
                                        .getLists().size())
                                    listElements += ";";
                                i++;
                            }
                            message = String.format("id|%d;type|list;itemcount|%d;%s", curId,
                                    this.tInfo.get(curId).getValidElections().size(), listElements);
                        } else
                            message = String.format("id|%d;type|error", curId);
                        // Password and cc saved here ready to send to RMI to confirm
                        /*
                         * System.out.println(cc); System.out.println(password);
                         * System.out.println("bug");
                         */
                        System.out.println(message);

                        buffer = message.getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, this.port);

                        socket.send(packet);

                    }
                    if (temp[0].equals("type") && temp[1].equals("vote")) {
                        temp = tokens[2].split("\\|");
                        if (temp[0].equals("list")) {
                            list = temp[1];
                        }
                        temp = tokens[3].split("\\|");
                        if (temp[0].equals("time")) {
                            System.out.println(temp[1]);
                            try {
                                cal.setTime(sdf.parse(temp[1]));
                            } catch (ParseException e) {
                                System.out.println("Something went wrong");
                            }
                        }
                        System.out.println(cal.getTime());
                        System.out.println(tInfo.size());
                        tInfo.get(curId).setV(new Vote(this.tInfo.get(curId).getValidElections()
                                .get(this.tInfo.get(curId).getOption()).getUid(), this.name, list, cal));

                        System.out.println(tInfo.get(curId).getState());
                        if (tInfo.get(curId).getState() == false) {// Avoid Voting twice
                            tInfo.get(curId).setState(true);
                            while (true) {
                                try {
                                    this.rmiSV.processVote(tInfo.get(curId));
                                    break;
                                } catch (RemoteException e) {
                                    connectRMI(this.timeoutTime);

                                }
                            }
                        }

                        message = String.format("id|%d;type|unlock;", curId);
                        buffer = message.getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, this.port);
                        socket.send(packet);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}
    /**
     * The class voting tables start the execution of the program by starting 2 threads, a Discovery Thread and a Message Handling Thread.
     * The Discovery Thread handles users and Voting Terminals
     * The Handling Threads manages votes and Terminal Multicast Messaging
     * 
     * @see VotingTable#run()
     * 
     * @author Duarte Dias
     * @author Gabriel Fernandes
     */
public class VotingTable extends Thread {
    private String ip;
    private int port;
    private String svIP;
    private int svPort;
    private String backupIP;
    private int backupPort;
    private String svName;
    String name;
    private CopyOnWriteArrayList<TerminalInfo> tInfo;
    private long timeoutTime;
    private InputStreamReader input;
    private BufferedReader reader;
    private RMIServerInterface rmiSV;
    /**
     * Bridges a connection with RMI serves, works as Wrapper for the selectServerTool Specifying a Timeout Time
     * @param timeoutTime Time until the Connection Fails and execution is ended
     */
    private void connectRMI(long timeoutTime) {
        try {
            FutureTask<RMIServerInterface> task = new FutureTask<RMIServerInterface>(() -> {
                return selectServer();
            });
            Thread thread = new Thread(task);
            thread.start();
            this.rmiSV = task.get(timeoutTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("There are no servers available, exiting");
            System.exit(1);
        }
    }
    /**
     * Server selector tool
     * <p> Logic : Try to connect to the main Server, if that fails, try to connect to the second one if that fails, wait 1 second
     * @return Returns an instance of a Remote object 
     */
    RMIServerInterface selectServer() {
        RMIServerInterface rmiSv;
        while (true) {
            System.out.println("Loading");
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", this.svIP, this.svPort, this.svName));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                // System.out.println("Server 1 is not currently Available , switch to backup
                // server" + e.getMessage());
            }
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", this.backupIP, this.backupPort, this.svName));
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
     * Simple String parser
     * @param s String to be parsed
     * @return Parsed word list
     */
    private static ArrayList<String> splitStr(String s) {
        String[] pairs = s.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\s+", "").split(";");
        ArrayList<String> all = new ArrayList<String>();
        for (String str : pairs) {
            String[] parts = str.split("\\|");
            for (String p : parts) {
                all.add(p);
            }
        }
        return all;
    }
    /**
     * Runs the Main Thread
     * In this thread:
     * <ol>
     *      <li> If there is a person at the voting table, ask for CC
     *      <li> If the CC is valid and there is a Running election, ask the user which election they want to vote
     *      <li> Probes the Voting Terminals for a free Terminal
     *      <li> Links the User, Election and Voting Terminal
     */
    public void run() {
        final String availabilityMessage = "id|*;type|availability";
        String responseMessage;
        int cc = -1;
        TerminalInfo tempInfo = new TerminalInfo(-1);
        MulticastSocket socket = null;
        byte[] buffer;
        byte[] response;
        InetAddress group;
        DatagramPacket packet, packetResponse;
        int curId;
        ArrayList<String> wordlist;

        connectRMI(this.timeoutTime);

        while (true) {
            // Read the IO from the User
            try {
                System.out.print("Insert you CC:");
                cc = Integer.parseInt(this.reader.readLine());
            } catch (IOException e) {
                System.out.println("There was an error");
            }

            // Get the info from the RMI
            while (true) {
                try {
                    tempInfo = this.rmiSV.getPersonInfo(cc, this.name);
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    connectRMI(this.timeoutTime);
                }
            }
            if (tempInfo.getValidElections().size() == 0) {
                System.out.println("There are no valid elections for this user to vote on");
            } else {
                int i = 0, option = -1;
                Scanner scan = new Scanner(System.in);
                System.out.println(String.format("Select a voting list (0 - %d)", tempInfo.getValidElections().size()));
                do {
                    for (Election e : tempInfo.getValidElections()) {
                        System.out.println(String.format("%d - %s", i, e.getTitle()));
                        i++;
                    }
                    option = scan.nextInt();
                } while (option < 0 || option > tempInfo.getValidElections().size());
                tempInfo.setOption(option);

                try {
                    // Create the socket
                    socket = new MulticastSocket(this.port);
                    group = InetAddress.getByName(this.ip);
                    socket.joinGroup(group);
                    while (true) {
                        // Send an availability message
                        buffer = availabilityMessage.getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, this.port);
                        socket.send(packet);

                        // Receive an availability message
                        response = new byte[1024];
                        packetResponse = new DatagramPacket(response, response.length);
                        socket.receive(packetResponse);
                        responseMessage = new String(packetResponse.getData(), 0, packetResponse.getLength());
                        wordlist = splitStr(responseMessage);

                        // Parse the availabitilty message
                        for(String s : wordlist){
                            System.out.println(s);
                        }
                        if (wordlist.get(0).equals("id") && !wordlist.get(1).equals("*")
                                && wordlist.get(2).equals("type") && wordlist.get(3).equals("availability")) {
                            curId = Integer.parseInt(wordlist.get(1));
                            // System.out.println("Sending " + String.format("id|%d;type|lock", curId));
                            response = String.format("id|%d;type|lock", curId).getBytes();
                            packet = new DatagramPacket(response, response.length, group, this.port);
                            while (true) {
                                socket.send(packet);
                                socket.receive(packetResponse);
                                responseMessage = new String(packetResponse.getData(), 0, packetResponse.getLength());

                                wordlist = splitStr(responseMessage);
                                if (wordlist.get(0).equals("id") && wordlist.get(1).equals(String.valueOf(curId))
                                        && wordlist.get(2).equals("type") && wordlist.get(3).equals("locked")) {
                                    tempInfo.setTNr(curId);
                                    tInfo.set(curId, tempInfo);
                                    break;
                                } else {// Ignore all other messages that are not lock
                                    ;
                                }
                            }
                            break;
                        }
                        System.out.println("found it");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            }

        }
    }
    /**
     * Class Constructor for the Voting Table Thread
     * @param ip The Ip of the voting table
     * @param port The port of the voting table
     * @param svIP The Ip of the RMI Server
     * @param svPort The Port of the RMI Server
     * @param backupIP The Ip of the backup RMI Server
     * @param backupPort The Port of the backup RMI Server
     * @param svName The name of the RMI Server
     * @param nTerminals The number of terminals that are instantiated
     * @param tInfo A structure that hold information about the Terminals
     * @param name The name of the voting table
     * @param timeoutTime The timeout Time of the RMI server (In case of failed connection)
     * @see RMIServer
     * @see VotingTerminal
     */
    VotingTable(String ip, int port, String svIP, int svPort, String backupIP,int backupPort, String svName, int nTerminals, CopyOnWriteArrayList<TerminalInfo> tInfo,
            String name, long timeoutTime) {
        this.ip = ip;
        this.port = port;
        this.svIP = svIP;
        this.svPort = svPort;
        this.backupIP = backupIP;
        this.backupPort = backupPort;
        this.svName = svName;
        this.tInfo = tInfo;
        this.name = name;
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        this.timeoutTime = timeoutTime;
    }
    /**
     * Initializes the Voting Table (2 Threads)
     * @param args Command line arguments that specify
     * <ul>
     *      <li> IP </li>
     *      <li> port </li>
     *      <li> svIP </li>
     *      <li> svPort </li>
     *      <li> backupIP</li> 
     *      <li> backupPort </li>
     *      <li> svName </li>
     *      <li> nTerminals </li>
     *      <li> tInfo </li>
     *      <li> name </li>
     *      <li> timeoutTime </li>
     * </ul>
     * 
     */
    public static void main(String args[]) {
        /*
         * String ip = args[0]; int port = Integer.parseInt((args[1])); long sleep_time
         * = Integer.parseInt((args[2])); int svPort = Integer.parseInt((args[3])); int
         * BackupIP = Integer.parseInt((args[4])); int nTerminais =
         * Integer.parseInt((args[5]));
         */

        String ip = "224.3.2.1";
        int port = 4321;
        String svIP = "localhost";
        int svPort = 3099;
        String backupIP = "localhost";
        int BackupIP = 4099;
        String svName = "SV";
        int nTerminais = 2;
        String name = "DEI";
        long timeoutTime = 5;

        CopyOnWriteArrayList<TerminalInfo> tInfo = new CopyOnWriteArrayList<>();
        for (int i = 0; i < nTerminais + 1; i++) {
            tInfo.add(new TerminalInfo(i));
        }

        RequestHandler r = new RequestHandler(ip, 43210,svIP, svPort,backupIP, BackupIP,svName, nTerminais, tInfo, name, timeoutTime);
        VotingTable v = new VotingTable(ip, port,svIP, svPort,backupIP, BackupIP,svName, nTerminais, tInfo, name, timeoutTime);

        v.start();
        r.start();

    }
}