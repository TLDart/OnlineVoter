import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.rmi.*;
import java.util.concurrent.TimeUnit;

import javax.swing.text.html.parser.DTD;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

class RequestHandler extends Thread {
    private String ip;
    private int port;
    private long sleep_time;
    private int svPort;
    private int backUpPort;
    private int nTerminals;
    String name;
    private int tHandler;
    private int tNumber;
    private String TableName;
    private CopyOnWriteArrayList<TerminalInfo> tInfo;

    RequestHandler(String ip, int port, int svPort, int backUpIp, int nTerminals,
            CopyOnWriteArrayList<TerminalInfo> tInfo, String name) {
        this.ip = ip;
        this.port = port;
        this.svPort = svPort;
        this.backUpPort = backUpIp;
        this.nTerminals = nTerminals;
        this.tInfo = tInfo;
        this.name = name;
    }

    /*
     * private RMIServerInterface connectRMI(long port1, long port2) { Boolean ok =
     * false; RMIServerInterface rmiSv; while (true) { try { rmiSv =
     * (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", ip, port1,
     * "SV")); rmiSv.heartbeat(); return rmiSv; } catch (Exception e) { System.out.
     * println("Server 1 is not currently Available , switch to backup server" +
     * e.getMessage()); } try { rmiSv = (RMIServerInterface)
     * Naming.lookup(String.format("//%s:%d/%s", ip, port2, "SV"));
     * rmiSv.heartbeat(); return rmiSv; } catch (Exception e) { System.out.
     * println("Server 2 is currently also not available, waiting for connection" +
     * e.getMessage()); rmiSv = null; } try { TimeUnit.SECONDS.sleep(1); } catch
     * (Exception e) { ; } } }
     */

    public void run() {
        MulticastSocket socket = null;
        DatagramPacket packet, packetResponse;
        String message, cc = "", password = "", list = "";
        int curId;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
        String[] tokens, temp;
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
               /*  for (String t : temp) {
                    System.out.println(t);
                } */
                if (temp[0].equals("id") && !temp[1].equals("N")){
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
                        // Password and cc saved here ready to send to RMI to confirm
                       /*  System.out.println(cc);
                        System.out.println(password);
                        System.out.println("bug"); */
                        
                        message = String.format("id|%d;type|list;itemcount|2; listitem1|Y;listitem2|Z",curId);
                        buffer = message.getBytes();
                        packet = new DatagramPacket(buffer, buffer.length, group, this.port);

                        socket.send(packet);
                        // TODO: Confirm with the RMI Server

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
                        tInfo.get(curId).setV(new Vote(TableName, list, cal));    
                        message = String.format("id|%d;type|unlock;",curId);
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
        // String message = "(id | 3 ; type | auth ; cc | X ; password | Y)";
        // .String message = "(id | 3 ; type | vote ; list | test ; time |
        // 2021/12/03-21:42)";

    }

}

class TerminalInfo {
    private Person p;
    private int tNr;
    private Vote v;
    private Boolean state;

    public Person getP() {
        return this.p;
    }

    public void setP(Person p) {
        this.p = p;
    }

    public int getTNr() {
        return this.tNr;
    }

    public void setTNr(int tNr) {
        this.tNr = tNr;
    }

    public Vote getV() {
        return this.v;
    }

    public void setV(Vote v) {
        this.v = v;
    }

    public Boolean getState() {
        return this.state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
    // This mean if the thread is ready or not for scheduling

    TerminalInfo(int tNr) {
        this.tNr = tNr;
        this.v = null;
        this.state = true;
    }

}

public class VotingTable extends Thread {
    // The voting table needs to know:
    /*
     * Self ip Self port Server port Backup Port
     */
    private String ip;
    private int port;
    private long sleep_time;
    private int svPort;
    private int backUpPort;
    private int nTerminals;
    String name;
    private CopyOnWriteArrayList<TerminalInfo> tInfo;
    private InputStreamReader input;
    private BufferedReader reader;
    private RMIServerInterface rmiSV;

    private RMIServerInterface connectRMI(long port1, long port2) {
        Boolean ok = false;
        RMIServerInterface rmiSv;
        while (true) {
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", ip, port1, "SV"));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                System.out.println("Server 1 is not currently Available , switch to backup server" + e.getMessage());
            }
            try {
                rmiSv = (RMIServerInterface) Naming.lookup(String.format("//%s:%d/%s", ip, port2, "SV"));
                rmiSv.heartbeat();
                return rmiSv;
            } catch (Exception e) {
                System.out.println("Server 2 is currently also not available, waiting for connection" + e.getMessage());
                rmiSv = null;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                ;
            }
        }
    }

    public void run() {
        String cc, responseMessage;
        final String availabilityMessage = "id|*;type|availability";
        Info rmiInfo;
        MulticastSocket socket = null;
        byte[] buffer = availabilityMessage.getBytes();
        byte[] response = new byte[1024];
        InetAddress group;
        DatagramPacket packet, packetResponse;
        int curId;
        String[] tokens, temp, temp2;
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        // this.rmiSV = connectRMI(svPort, backUpPort);
        while (true) {
            try {
                System.out.println("Please Identify Yourself");
                System.out.print("CC: ");
                cc = this.reader.readLine();
            } catch (IOException e) {
                System.out.println("There was an error");
            }

            // TODO: RMI Connection
            // rmiInfo = rmiSV.getEligibleElection(cc);
            // Probe for an available Terminal
            try {
                socket = new MulticastSocket(this.port);
                group = InetAddress.getByName(this.ip);
                socket.joinGroup(group);
                while (true) {
                    response = new byte[1024];
                    packetResponse = new DatagramPacket(response, response.length);
                    packet = new DatagramPacket(buffer, buffer.length, group, this.port);
                    System.out.println(this.ip);
                    System.out.println(this.port);
                    socket.send(packet);

                    socket.receive(packetResponse);
                    System.out.println("receid");
                    responseMessage = new String(packetResponse.getData(), 0, packetResponse.getLength());
                    System.out.println(responseMessage);
                    tokens = responseMessage.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\s+", "").split(";");
                    temp = tokens[0].split("\\|");
                    temp2 = tokens[1].split("\\|");
                    System.out.println(temp[1]);
                    if (temp[0].equals("id") && !temp[1].equals("*") && temp2[0].equals("type")
                            && temp2[1].equals("availability")) {
                        curId = Integer.parseInt(temp[1]);
                        System.out.println("Sending " + String.format("id|%d;type|lock", curId));
                        response = String.format("id|%d;type|lock", curId).getBytes();
                        packet = new DatagramPacket(response, response.length, group, this.port);
                        while (true) {
                            socket.send(packet);
                            socket.receive(packetResponse);
                            responseMessage = new String(packetResponse.getData(), 0, packetResponse.getLength());
                            System.out.println(responseMessage);
                            tokens = responseMessage.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\s+", "").split(";");
                            temp = tokens[0].split("\\|");
                            temp2 = tokens[1].split("\\|");
                            if (temp[0].equals("id") && temp[1].equals(String.valueOf(curId)) && temp2[0].equals("type")
                                    && temp2[1].equals("locked"))
                                break;
                            else{
                                System.out.println("welp");
                            }

                        }
                        break;
                    }
                }
                System.out.println("Found One");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }

    VotingTable(String ip, int port, int svPort, int backUpIp, int nTerminals, CopyOnWriteArrayList<TerminalInfo> tInfo,
            String name) {
        this.ip = ip;
        this.port = port;
        this.svPort = svPort;
        this.backUpPort = backUpIp;
        this.nTerminals = nTerminals;
        this.tInfo = tInfo;
        this.name = name;
    }

    public static void main(String args[]) {
        /*
         * String ip = args[0]; int port = Integer.parseInt((args[1])); long sleep_time
         * = Integer.parseInt((args[2])); int svPort = Integer.parseInt((args[3])); int
         * BackupIP = Integer.parseInt((args[4])); int nTerminais =
         * Integer.parseInt((args[5]));
         */

        String ip = "224.3.2.1";
        int port = 4321;
        long sleep_time = 5000;
        int svPort = 3099;
        int BackupIP = 4099;
        int nTerminais = 2;
        String name = "DEI";

        CopyOnWriteArrayList<TerminalInfo> tInfo = new CopyOnWriteArrayList<>();
        for (int i = 0; i < nTerminais + 1; i++) {
            tInfo.add(new TerminalInfo(i));
        }

        RequestHandler r = new RequestHandler(ip, 43210, svPort, BackupIP, nTerminais, tInfo, name);
        VotingTable v = new VotingTable(ip, port, svPort, BackupIP, nTerminais, tInfo, name);

        v.start();
        r.start();

    }
}