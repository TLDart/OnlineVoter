import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Starter {
    private static String OS = null;
    private static String ServerName;
    private static String ServerIP;
    private static String ServerPort;
    private static String BackupIP;
    private static String BackupPort;
    private static String[] MulticastDiscoveryIP;
    private static String[] MulticastDiscoveryPort;
    private static String[] MulticastrequestHandlerPort;
    private static String MulticastTerminalNumber;
    private static String MulticastTerminalStartingIP;
    private static String TimeoutTime;
    private static int nTables;
    private static String[] TableNames;
    private static String[] ConfTemp;
    private static ArrayList<Integer> Configuration = new ArrayList<>();
    private static String nMessagesTimeout;

    public static String getOsName() {
        if (OS == null) {
            OS = System.getProperty("os.name");
        }
        return OS;
    }

    public static void main(String[] args) {
        String path = args[0];
        Properties prop = new Properties();
        getOsName();
        if (OS == null) {
            System.out.println("OS not detected  , exiting...");
            System.exit(1);
        }
        // Parse Data
        try {
            InputStream in = new FileInputStream(path);
            int sum = 0, nTerm = -1;
            prop.load(in);
            in.close();
            String temp;
            ServerName = prop.getProperty("ServerName");
            ServerIP = prop.getProperty("ServerIP");
            ServerPort = prop.getProperty("ServerPort");
            BackupIP = prop.getProperty("BackupIP");
            BackupPort = prop.getProperty("BackupPort");
            temp = prop.getProperty("MulticastDiscoveryIP");
            MulticastDiscoveryIP = temp.split(",");
            temp = prop.getProperty("MulticastDiscoveryPort");
            MulticastDiscoveryPort = temp.split(",");
            temp = prop.getProperty("MulticastrequestHandlerPort");
            MulticastrequestHandlerPort = temp.split(",");
            MulticastTerminalNumber = prop.getProperty("MulticastTerminalNumber");
            MulticastTerminalStartingIP = prop.getProperty("MulticastTerminalStartingIP");
            TimeoutTime = prop.getProperty("TimeoutTime");
            nTables = Integer.parseInt(prop.getProperty("nTables"));
            temp = prop.getProperty("TableNames");
            TableNames = temp.split(",");
            temp = prop.getProperty("Configuration");
            nMessagesTimeout = prop.getProperty("nMessagesTimeout");
            ConfTemp = temp.split(",");
            try {
                for (String el : ConfTemp) {
                    Configuration.add(Integer.parseInt(el));
                    sum += Integer.parseInt(el);
                }
                nTerm = Integer.parseInt(MulticastTerminalNumber);
            } catch (NumberFormatException e) {
                System.out.println("NonInt Detected in configuration file");
                System.exit(0);
            }
            assert sum == nTerm;

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (OS.startsWith("Linux")) {
            int i = 0;
            try {
                Process p = Runtime.getRuntime().exec(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.RMIServer %s %s %s %s %s",
                        ServerIP, ServerPort, BackupIP, BackupPort, ServerName));
                System.out.println(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.RMIServer %s %s %s %s %s",
                        ServerIP, ServerPort, BackupIP, BackupPort, ServerName));;
                TimeUnit.SECONDS.sleep(5);
                p = Runtime.getRuntime().exec(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.RMIServer %s %s %s %s %s",
                        BackupIP, BackupPort, ServerIP, ServerPort, ServerName));
                System.out.println(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.RMIServer %s %s %s %s %s",
                        BackupIP, BackupPort, ServerIP, ServerPort, ServerName));
                TimeUnit.SECONDS.sleep(5);
                p = Runtime.getRuntime().exec(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.AdminConsole %s %s %s %s %s %s",
                        ServerIP, ServerPort, BackupIP, BackupPort, ServerName, TimeoutTime));
                System.out.println(String.format(
                        "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.AdminConsole %s %s %s %s %s %s",
                        ServerIP, ServerPort, BackupIP, BackupPort, ServerName, TimeoutTime));
                TimeUnit.SECONDS.sleep(2);
                for (i = 0; i < nTables; i++) {
                    p = Runtime.getRuntime().exec(String.format(
                            "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.VotingTable %s %s %s %s %s %s %s %s %s %s %s",
                            MulticastDiscoveryIP[i], MulticastDiscoveryPort[i], MulticastrequestHandlerPort[i], ServerIP,
                            ServerPort, BackupIP, BackupPort, ServerName, MulticastTerminalNumber, TableNames[i],
                            TimeoutTime));
                    System.out.println(String.format(
                            "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.VotingTable %s %s %s %s %s %s %s %s %s %s %s",
                            MulticastDiscoveryIP[i], MulticastDiscoveryPort[i], MulticastrequestHandlerPort[i], ServerIP,
                            ServerPort, BackupIP, BackupPort, ServerName, MulticastTerminalNumber, TableNames[i],
                            TimeoutTime));
                    TimeUnit.SECONDS.sleep(2);
                }
                System.out.println(Configuration);
                for (int j = 0; j < Configuration.size(); j++) {
                    for (i = 0; i < Configuration.get(j); i++) {
                        p = Runtime.getRuntime().exec(String.format(
                                "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.VotingTerminal %d %s %s %s %s %s %s",
                                i, MulticastDiscoveryIP[j], MulticastDiscoveryIP[j], MulticastDiscoveryPort[j],
                                MulticastrequestHandlerPort[j], nMessagesTimeout,
                                TimeoutTime));
                        System.out.println(String.format(
                                "/usr/bin/x-terminal-emulator --disable-factory -e java OnlineVoter.VotingTerminal %d %s %s %s %s %s %s",
                                i, MulticastDiscoveryIP[j], MulticastDiscoveryIP[j], MulticastDiscoveryPort[j],
                                MulticastrequestHandlerPort[j], nMessagesTimeout,
                                TimeoutTime));
                        TimeUnit.SECONDS.sleep(2);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } else if (OS.startsWith("Mac OS X")) {
            ;
        }
    }
}