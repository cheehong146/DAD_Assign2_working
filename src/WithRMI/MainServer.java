package WithRMI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

/**
 * Created by User on 21/10/2017.
 */
public class MainServer implements Runnable {
    TreeMap<String, String> userTreeMap;
    TreeMap<String, String> agentTreeMap;
    TreeMap<Integer, Boolean> availablePort; //portnumber; boolean true if the port is not taken by by agent, false if it is
    TreeMap<Integer, Boolean> udpPort;
    //TODO MAKE PORT 7777 UNAVAILABLE

    int counter = 0;

    @Override
    public void run() {
        initializeUser();//initialize up to 4 dummy user
        initializePort();//initialize the tree as object with no port yet
        initializeAgent();//initialize 2 dummy agent
        initializeUdpPort();

        try {
            ServerSocket serverSocket = new ServerSocket(7777);
            Socket socket;

            while(true){
                socket = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(socket);
                connectionHandler.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public class ConnectionHandler extends Thread{
        Socket socket;

        ConnectionHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String type;
                type = in.readUTF();

                if(type.equals("agent")){
                    String username = in.readUTF();
                    String password = in.readUTF();
                    System.out.println("main received " + username + " " + password);
                    if(agentTreeMap.containsKey(username) && agentTreeMap.get(username).equals(password)){
                        out.writeBoolean(true);

                        if(!is2PortAvailable()){
                            System.out.println("2 port not available. Gonna create new port");
                            createPort();
                        }

                        if(is2PortAvailable()){
                            System.out.println("2 port available. Gonna send to agent");
                            int portC1 = getAvailablePortAgent();
                            int portC2 = getAvailablePortAgent();

                            System.out.println("Sending port: " + portC1 + " " + portC2 + " to agent");
                            out.writeInt(portC1);
                            out.writeInt(portC2);
                        }

                        boolean isVoice = in.readBoolean();
                        if(isVoice){
                            System.out.println("get another port");
                            createUdpPort();
                            int port3 = getUdpPortAgent();
                            int port4 = getUdpPortAgent();
                            out.writeInt(port3);
                            out.writeInt(port4);
                        }

                    }else{
                        out.writeBoolean(false);
                    }

                }else if(type.equals("client")){
                    String username = in.readUTF();
                    String password = in.readUTF();
                    System.out.println("main received " + username + " " + password);
                    if(userTreeMap.containsKey(username) && userTreeMap.get(username).equals(password)){
                        System.out.println("Server authenticated " + username);
                        out.writeBoolean(true);

                        //send available port to client
                        int port = getAvailablePortClient();
                        System.out.println("sending to client port " + port);
                        out.writeInt(port);

                        boolean isVoice = in.readBoolean();
                        if(isVoice){
                            int extraPort = getAvailableUdpPortClient();
                            out.writeInt(extraPort);
                        }
                    }else{
                        out.writeBoolean(false);
                    }
                }else{
                    System.out.println("Invalid input");
                }
            }catch (IOException e){
                System.out.println("ConnectionHandler run() socket failed.");
                e.printStackTrace();
            }
        }
    }

    public void initializeUser(){
        userTreeMap = new TreeMap<>();
        userTreeMap.put("client1", "123");
        userTreeMap.put("client2", "123");
        userTreeMap.put("client3", "123");
        userTreeMap.put("client4", "123");
    }

    public void initializePort(){
        availablePort = new TreeMap<>();
    }

    public boolean is2PortAvailable(){
        int counter = 0;
        for (int port :
                availablePort.keySet()) {
            if (availablePort.get(port) == true){
                counter++;
            }
        }

        if(counter<2){
            return false;
        }else{
            return true;
        }
    }

    public int getAvailablePortClient(){
        int portTaken = -1;
        for (int port :
                availablePort.keySet()) {
            if(availablePort.get(port) == false){
                availablePort.remove(port);     //remove that port now that the client have taken it
                return port;
            }
        }
        return portTaken;
    }
    

    public void createPort(){
        int counter = 0;

        if(availablePort.isEmpty()){                    //if empty, create 2 port
            counter = 2;
        }

        for (int portNum:                               //if only 1 port is available, increment counter to create new port
                availablePort.keySet()) {
            if(availablePort.get(portNum) == true){
                counter++;
                break;
            }else if(availablePort.get(portNum) == false){  //if 2 of the port in available post is set to false(assigned to another agent)
                counter++;                                  //counter wil be 2 to create another 2 new port for new agent
                if(counter == 2){
                    break;
                }
            }
        }

        for(int i = 0; i < counter; i++){               //this loop this new port number if not enough or no port is available
            int randomPort = (int)(Math.random()* 50000 + 1023 );
            while(!availablePort.containsKey(randomPort) && !udpPort.containsKey(randomPort)) {
                System.out.println("New Random port created: " + randomPort);
                availablePort.put(randomPort, true);
            }
        }
    }

    public int getAvailablePortAgent(){
        for (int port :
                availablePort.keySet()) {
            if(availablePort.get(port) == true){
                System.out.println(port + " is available.");
                availablePort.put(port, false);         //set the available port to not available;
                return port;
            }
        }
        return -1;
    }

    public void initializeAgent(){
        agentTreeMap = new TreeMap<>();
        agentTreeMap.put("agent", "123");
        agentTreeMap.put("agent1", "123");
    }

    public void createAgent(){
        int rand = (int)Math.random()*10 +1;
        String agentUsername = "agent" + Integer.toString(rand);
        agentTreeMap.put(agentUsername, "123");
        System.out.println(agentUsername + " created.");
    }

    public void initializeUdpPort(){
        udpPort = new TreeMap<>();
    }

    public int getUdpPortAgent(){
        for (int port :
                udpPort.keySet()) {
            if(udpPort.get(port) == true){
                System.out.println(port + " is available.");
                udpPort.put(port, false);         //set the available port to not available;
                return port;
            }
        }
        return -1;
    }

    public void createUdpPort(){
        int counter = 2;
        for(int i = 0; i < counter; i++){               //this loop this new port number if not enough or no port is available
            int randomPort = (int)(Math.random()* 50000 + 1023 );
            while(!udpPort.containsKey(randomPort) && !availablePort.containsKey(randomPort)) {
                System.out.println("New Random port created: " + randomPort);
                udpPort.put(randomPort, true);
            }
        }
    }

    public int getAvailableUdpPortClient(){
        for (int port :
                udpPort.keySet()) {
            if(udpPort.get(port) == false){
                if(counter == 2){
                    udpPort.remove(port);
                    counter = 0;
                    return port;
                }else{
                    counter ++;
                    return port;
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        MainServer mainServer = new MainServer();
        mainServer.run();
    }

}
