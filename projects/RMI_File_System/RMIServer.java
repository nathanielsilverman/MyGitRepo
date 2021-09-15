import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    Fat32_driver driver;

    public RMIServer(Fat32_driver driver) throws RemoteException {
        super();
        this.driver = driver;
    }

    @Override
    public String getCurrentDirectory() throws RemoteException {
        return this.driver.getFs().getDirName();
    }

    @Override
    public String getInfo() throws RemoteException{
        return this.driver.info();
    }

    @Override
    public String listDirectories(File file) throws RemoteException{
        return this.driver.ls(file);
    }

    @Override
    public String getStatistics(File file) throws RemoteException{
        return this.driver.stat(file);
    }

    @Override
    public String openFile(File file) throws RemoteException{
        return this.driver.open(file);
    }

    @Override
    public String closeFile(File file) throws RemoteException{
        return this.driver.close(file);
    }

    @Override
    public String getSizeOfFile(File file) throws RemoteException{
        return this.driver.size(file);
    }

    @Override
    public String changeDirectory(File file) throws RemoteException{
        return this.driver.cd(file);
    }

    @Override
    public String readFileForGivenOffset(File file, String start, String end) throws RemoteException{
        return this.driver.read(file, start, end);
    }
}
