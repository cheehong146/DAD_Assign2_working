package WithRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UDPInterface extends Remote {
    void stop() throws Exception;
    void captureAudio() throws RemoteException;
    void runVOIP() throws RemoteException;
    int getServerSocketPort() throws RemoteException;
    void setServerSocketPort(int serverSocketPort) throws RemoteException;
    int getSendPort() throws RemoteException;
    void setSendPort(int sendPort) throws RemoteException;

}
