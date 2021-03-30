import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

public class VotingTerminal{
    private String numeroTerminal;
    private String ipForVoting;
    private int portForVoting;
    private long timeInterval;
    private int nMessagesTimeout;
    private int timeoutTime;

    //ler inputs do user
    private InputStreamReader input;
    private BufferedReader reader;
    
    //thread reponsavel pelo handle das mensagens de discovery do terminal de voto
    private DiscoveryThread discoverThread;

    //ligar ao socket para as mensagens de voto
    private MulticastSocket socket;
    private InetAddress group;


    public VotingTerminal(String [] args, DiscoveryThread discoveryThread){
        this.numeroTerminal = args[0];
        //this.ipForDiscovery = args[1];
        this.ipForVoting = args[2];
        //this.portForDiscovery = Integer.parseInt(args[3]);
        this.portForVoting = Integer.parseInt(args[4]);
        this.timeInterval = 120;
        this.nMessagesTimeout = Integer.parseInt(args[5]);
        this.timeoutTime = Integer.parseInt(args[6]);

        //ler inputs do user
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
        
        //thread reponsavel pelo handle das mensagens de discovery do terminal de voto
        //this.discoverThread = new DiscoveryThread(this.numeroTerminal, this.ipForDiscovery, this.portForDiscovery);
        this.discoverThread = discoveryThread;

        //ligar ao socket para as mensagens de voto
        this.socket = null;
        this.group = null;
        try{
            this.socket = new MulticastSocket(portForVoting);
            this.group = InetAddress.getByName(ipForVoting);
            this.socket.joinGroup(this.group);
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            //nao consegui dar bind ao socket nao pode continuar
            System.exit(0);
        }
    }
    public static void main(String args[]){
        DiscoveryThread discoverThread = new DiscoveryThread(args[0], args[1], Integer.parseInt(args[3]));
        VotingTerminal vt = new VotingTerminal(args, discoverThread);
        while(true){//Estar sempre ativo à espera que lhe seja atribuido um user
            vt.process();
            // try{
            //     Thread.sleep(100);
            // }
            // catch(InterruptedException e){}
        }
    }

    private void process(){
        String message, response;
        byte [] buffer = new byte[1024];
        DatagramPacket datagramReceive, datagramSend;
        byte [] bytesSend;
        ArrayList<String> words;

        //para guardar as possibilidades de voto
        ArrayList<String> choices = null;

        try {
            boolean keep = true;
            while(keep){//espera que o terminal esteja a ser usado
               if (this.discoverThread.getFree() == false) keep = false;
               try{
                   TimeUnit.SECONDS.sleep(1);
               }
               catch (Exception e){
                   ;
               }
            }
            System.out.println("Nepia");
            //obter numero de cc
            System.out.println("Insert you cc number:");
            FutureTask<String> task = new FutureTask<String>(() -> {
                return this.reader.readLine();
            });
            Thread thread = new Thread(task);
            thread.start();
            String cc = task.get(timeInterval, TimeUnit.SECONDS);

            //obter a password
            System.out.println("Insert you password:");
            task = new FutureTask<String>(() -> {
                return this.reader.readLine();
            });
            thread = new Thread(task);
            thread.start();
            String password = task.get(timeInterval, TimeUnit.SECONDS);

            //enviar a mesa de voto para verificacao
            //se receber uma mensagem igual a anterior significa que a mesa nao recebeu -> enviar outra vez
            //se receber a mensagem com as listas, continuar
            keep = true;
            //construir datagrama -> basta faze-lo uma vez
            response = String.format("id|%s;type|auth;cc|%s;password|%s", this.numeroTerminal, cc, password);
            bytesSend = response.getBytes();
            datagramSend = new DatagramPacket(bytesSend, bytesSend.length, this.group, this.portForVoting);
            while(keep){//embrulhar tudo em try catch para voltar a enviar a mensagem
                this.socket.send(datagramSend);
                try{
                    for (int e = 0; e < nMessagesTimeout + 1 && keep; e++){//+1 para a mensagem que o socket envia para o grupo
                        datagramReceive = new DatagramPacket(buffer, buffer.length);
                        this.socket.setSoTimeout(this.timeoutTime);//se não receber mensagens tem de saltar e reenviar a mensagem dele
                        this.socket.receive(datagramReceive);
                        message = new String(datagramReceive.getData(), 0, datagramReceive.getLength());

                        words = splitStr(message);
                        if (words.size() > 3 &&words.get(0).equals("id") && words.get(1).equals(numeroTerminal)){
                            if (words.get(2).equals("type") && words.get(3).equals("error")){
                                //ocorreu um erro na autenticacao
                                discoverThread.setFree(true);
                                return;//acaba o processamento do utilizador atual
                            }
                            else if(words.get(2).equals("type") && words.get(3).equals("list")){//recolher as listas enviadas
                                //array com todas as possibilidades + blank + null
                                choices = new ArrayList<String>();
                                //salta o par item_count|number
                                for (int i = 6; i < words.size(); i += 2){
                                    choices.add(words.get(i));
                                }
                                choices.add("blank");
                                choices.add("null");
                                System.out.println("Choose one of the following:");
                                for (int i = 0; i < choices.size(); i++){
                                    System.out.println(String.format("%d - %s", i, choices.get(i)));
                                }
                                keep = false;
                            }
                        }
                    }
                }
                catch(SocketTimeoutException tOut){
                    //catch da exception, mas quero que continue a correr o loop while
                }
            }

            task = new FutureTask<String>(() -> {
                return reader.readLine();
            });
            thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
            String choice = task.get(timeInterval, TimeUnit.SECONDS);

            //verificar que a escolha e valida
            int aux = 0;
            try{
                aux = Integer.parseInt(choice);
            }
            catch(NumberFormatException e){
                System.out.println("NumberFormatException: " + e.getMessage());
                if(choices != null) aux = choices.size() - 1;//se não for um int é considerado voto nulo
                else System.exit(0);//erro, de alguma maneira as choices ficaram null
            }
            Calendar currentTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd-HH:mm");
            response = String.format("id|%s;type|vote;list|%s;time|%s", this.numeroTerminal, choices.get(aux), sdf.format(currentTime));
            bytesSend = response.getBytes();
            datagramSend = new DatagramPacket(bytesSend, bytesSend.length, this.group, this.portForVoting);

            keep = true;
            while(keep){
                socket.send(datagramSend);
                
                try{
                    for (int e = 0; e < this.nMessagesTimeout + 1 && keep; e++){
                        //esperar o ack id|numeroTerminal;type|unlock
                        datagramReceive = new DatagramPacket(buffer, buffer.length);
                        this.socket.setSoTimeout(this.timeoutTime);
                        this.socket.receive(datagramReceive);
                        message = new String(datagramReceive.getData(), 0, datagramReceive.getLength());

                        words = splitStr(message);
                        if (words.size() > 3 && words.get(0).equals("id") && words.get(1).equals(numeroTerminal)){
                            if (words.get(2).equals("type") && words.get(3).equals("unlock")){
                                keep = false;
                                discoverThread.setFree(true);//terminal desocupado
                            }
                        }
                    }
                }
                catch(SocketTimeoutException tOut){
                    //volta a enviar a mensagem
                }
            }

        } catch (TimeoutException interruptedException) {
            System.out.println("Tempo de voto excedido.");
            //todo
            //?o que fazer quando o tempo de interacao e excedido
            discoverThread.setFree(true);
            //System.exit(0);
        } catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
        // System.out.println("main");
        // this.discoverThread.setFree(!this.discoverThread.getFree());
    }

    private static ArrayList<String> splitStr(String s){
        String [] pairs = s.split(";");
        ArrayList<String> all = new ArrayList<String>();
        for (String str : pairs){
            String [] parts = str.split("\\|");
            for (String p : parts){
                all.add(p);
            }
        }
        return all;
    }
}

class DiscoveryThread implements Runnable{
    private boolean free;
    private String ipForDiscovery;
    private String numeroTerminal;
    private int portForDiscovery;
    public int count;
    Thread thread;

    public DiscoveryThread(String numeroTerminal, String ipForDiscovery, int portForDiscovery){
        this.numeroTerminal = numeroTerminal;
        this.ipForDiscovery = ipForDiscovery;
        this.portForDiscovery = portForDiscovery;
        this.thread = new Thread(this, "discovery");
        this.free = true;//para inicializar como estando livre
        this.thread.start();
        this.count = 1;
    }

    public void run(){
        MulticastSocket socket = null;
        String message, response;
        DatagramPacket datagramReceive, datagramSend;
        byte [] buffer = new byte[1024];
        byte [] bytesSend;
        ArrayList<String> words;
        try{
            socket = new MulticastSocket(this.portForDiscovery);
            InetAddress group = InetAddress.getByName(this.ipForDiscovery);
            socket.joinGroup(group);
            while(true){
                //for (int i = 0; i < this.nMessagesTimeout; i++){//no maximo le nMessagesTimeout sem receber a resposta que quer antes de reenviar mensagem
                    //ler as mensagens do grupo
                    datagramReceive = new DatagramPacket(buffer, buffer.length);
                    System.out.println(this.portForDiscovery);
                    System.out.println(this.ipForDiscovery);
                    socket.receive(datagramReceive);
                    message = new String(datagramReceive.getData(), 0, datagramReceive.getLength());
                    System.out.println(message);
                    
                    words = splitStr(message);
                    System.out.println(words);
                    if (words.size() > 3 && words.get(0).equals("id") && words.get(1).equals("*") && words.get(2).equals("type") && words.get(3).equals("availability") && this.free){
                        response = String.format("id|%s;type|availability", this.numeroTerminal);
                        bytesSend = response.getBytes();
                        datagramSend = new DatagramPacket(bytesSend, bytesSend.length,group, this.portForDiscovery);
                        socket.send(datagramSend);
                        System.out.println("sent");
                    }

                    else if(words.size() > 3 && words.get(1).equals(numeroTerminal) && words.get(2).equals("type") && words.get(3).equals("lock")){//dar lock
                        System.out.println("locked");
                        this.free = false;
                    }
                //}
            }
        }
        catch(IOException e){
            System.out.println("IOException: " + e.getMessage());
            //erro a dar bind do socket
            System.exit(0);
        }
        // while(true){
        //     System.out.println(this.free);
        //     try{
        //         Thread.sleep(100);
        //     }
        //     catch(InterruptedException e){}
        // }
    }

    public boolean getFree(){
        return this.free;
    }

    public void setFree(boolean s){
        this.free = s;
    }

    private static ArrayList<String> splitStr(String s){
        String [] pairs = s.split(";");
        ArrayList<String> all = new ArrayList<String>();
        for (String str : pairs){
            String [] parts = str.split("\\|");
            for (String p : parts){
                all.add(p);
            }
        }
        return all;
    }
}