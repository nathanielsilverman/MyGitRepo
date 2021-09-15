import java.io.File;
import java.rmi.*;
public interface RMIServerInterface extends Remote {

    public String getCurrentDirectory()throws RemoteException;
    public String getInfo()throws RemoteException;
    public String listDirectories(File file) throws RemoteException;
    public String getStatistics(File file) throws RemoteException;
    public String openFile(File file) throws RemoteException;
    public String closeFile(File file) throws RemoteException;
    public String getSizeOfFile(File file) throws RemoteException;
    public String changeDirectory(File file) throws RemoteException;
    public String readFileForGivenOffset(File file, String start, String end) throws RemoteException;
}
