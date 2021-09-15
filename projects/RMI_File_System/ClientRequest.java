import java.io.File;
import java.rmi.*;
import java.util.Scanner;

public class ClientRequest {
    public static void main(String[] args)
    {
        try {

        	if (args.length != 2) {
        		System.out.println("Error: hostname and port number required");
        		return;
			}

        	String hostName = args[0];
        	Integer portNum = null;
        	try{
        		portNum = Integer.parseInt(args[1]);
			} catch (NumberFormatException a) {
        		System.out.println("Error: please enter a number");
        		throw new NumberFormatException();
			}

            RMIServerInterface access = null;
			try{
				access = (RMIServerInterface) Naming.lookup("rmi://"+ hostName +":"+ portNum +"/filesystem");
			} catch (Exception a) {
				System.out.println("Error: Server cannot be found at " + hostName +":"+portNum);
				throw new Exception();
			}
            boolean isRunning = true;
            Scanner scanner = new Scanner(System.in);
            String dirName = File.separator;
    		//the shell program
    		while(isRunning) {

				dirName = access.getCurrentDirectory();
				if (dirName.equals("")) {
					dirName = File.separator;
				}

    			//displays the directory
    			System.out.print(dirName+"]");

    			//waits for input
    			String input = scanner.nextLine();

    			//Split inputs into command, file, and args
    			String[] inputs = input.split(" ");

    			String command = "";
    			String filePath = "";
    			String argument1 = "";
    			String argument2 = "";

    			try {
    				command = inputs[0];
    				filePath = inputs[1];
    				if (inputs.length == 4) {
    					argument1 = inputs[2];
    					argument2 = inputs[3];
    				}
    			} catch (Exception e){}

    			File file1 = new File(filePath);

    			//handles different inputs
    			switch (command){
    				case "info":
    					System.out.println(access.getInfo());
    					break;
    				case "ls":
    					System.out.println(access.listDirectories(file1));
    					break;
    				case "stat":
						System.out.println(access.getStatistics(file1));
    					break;
    				case "open":
						System.out.println(access.openFile(file1));
    					break;
    				case "close":
						System.out.println(access.closeFile(file1));
    					break;
    				case "size":
						System.out.println(access.getSizeOfFile(file1));
    					break;
    				case "cd":
                        String returned = access.changeDirectory(file1);
    					if (returned != null) {
							System.out.println(returned);
						}
    					break;
    				case "read":
						System.out.println(access.readFileForGivenOffset(file1, argument1, argument2));
    					break;
    				case "exit":
    					isRunning = false;
    					break;
    				default:
    					System.out.println("Error: '" + input + "' is an invalid command");
    			}
    		}
        }
        catch(Exception e) {
            System.out.println("Error: Client: "+e);
        }
    }
}
