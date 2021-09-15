import java.io.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Fat32_driver {

	private fat32_reader fs;

	public Fat32_driver(File file){
		this.fs = new fat32_reader(file);

		//sets the current directory
		fs.setDirName("");

		//*******************************************
		RMIServer server = null;
		try {
			server = new RMIServer(this);
			LocateRegistry.createRegistry(4300);
			Naming.bind("rmi://localhost:4300"+"/filesystem", server);
		}catch (Exception e){
			System.out.println("Error: Starting up Server: "+e);
		}
		if(server == null) return;
		System.out.println("Server Started");
		//*******************************************

	}

	/**
	 * Main
	 * HEY YOU OVER HERE THE MAIN METHOD STARTS HERE!
	 *
	 * The main method will display the current path the user is in
	 * Then it will wait for input such as 'ls', 'info' and 'stat'
	 *
	 * When we make it to those methods later you will gain more clarity on what they do
	 * Meanwhile we have to go look at the constructor that instantiates the file system
	 *
	 * That method should be right above you!
	 *
	 * HEY YOU AGAIN!
	 *
	 * Now we can see how the public API works
	 *
	 * see "showStatistics" for stat
	 * see "getInfo" for info
	 * see "listDirectories" for ls
	 *
	 * then we are done here!
	 */
	public static void main(String[]args) {
		new Fat32_driver(new File(args[0]));
	}

	public String info(){
		return fs.getInfo();
	}

	public String ls(File file){
		return fs.listDirectoryContent(file);
	}

	public String stat(File file) {
		return fs.showStatistics(file);
	}

	public String cd(File file) {
		return fs.changeDirectory(file);
	}

	public String size(File file) {
		int size = fs.get_file_size(file);
		if (size == -1) {
			return "Error: "+file+" is not a file";
		}
		else return "Size of " + file + " is " + size + " bytes";
	}

	private String read(File file, int offset, int numBytes) {
		if (offset < 0) {
			return "Error: OFFSET must be a positive value";
		}
		if (numBytes <= 0) {
			return "Error: NUM_BYTES must be greater than zero";
		}
		int size = fs.get_file_size(file);
		if (size == -1) {
			return "Error: "+file+" is not a file";
		}
		DIR_Entry dirEntry = fs.getDirectoryEntry(file);

		if(dirEntry == null) return "";
		if (!dirEntry.isOpen) {
			return "Error: file is not open";
		}
		if (offset+numBytes >= size) {
			return "Error: attempt to read data outside of file bounds";
		}
		if(dirEntry.DIR_FileSize == 0) {
			return "Error: file is empty";
		}

		return fs.read(dirEntry, offset, numBytes);
	}

	public String open(File file) {
		return fs.handleOpeningAndClosingFiles(file, true);
	}

	public String close(File file) {
		return fs.handleOpeningAndClosingFiles(file, false);
	}

	public String read(File file, String argument1, String argument2) {
		int offset;
		int numBytes;
		try{
			offset = Integer.parseInt(argument1);
			numBytes = Integer.parseInt(argument2);
		} catch (NumberFormatException e){
			offset = -1;
			numBytes = -1;
		}
		return read(file, offset, numBytes);
	}

	protected fat32_reader getFs() {
		return this.fs;
	}

}
