# Remote Method Invocation File System


# Projects Partners
    
    Zach Hamburger
    Nathaniel Silverman
    
# Files

    fat32_reader.java

This class is passed in a .img FAT32 file and calls the respected class to parse the FAT32 data. The class handles the processing of the 8 file system commands (listed below).

    Fat32File.java 

This class reads the boot sector and parses all the data from the .img file. The class also holds all the logic for getting preceding clusters and FAT Table lookups.

    DIR_Entry.java 
    
This class takes a byte array of a specific file in the FAT32 file scheme. It then parses that files data from its size, next cluster, and all the relevant information. 

    Fat32_driver.java 
        
This class creates and starts up the RMIServer for Remote Method Invocation (RMI) calls.
    
    RMIServerInterface.java
    
This interface provides the description of the 8 file system methods (listed below) that can be invoked by a remote client. The interface extends the Java RMI Remote interface to identify interfaces whose methods may be invoked from a non-local virtual machine. 

    RMIServer.java

This class is serves as the skeleton on the server side and is responsible for communicating and processing requests from a client. This class implements all the methods required by the RMIServerInterface and extends UnicastRemoteObject so that the methods can be invoked by a client on a non-local virtual machine.
        
    ClientRequest.java

This class serves as the stub on the client side. This class communicates with the server skeleton, making remote invocation calls.          
    
    fat32.img 

Image file for testing the fat32_reader.java

    makefile 
    
This compiles and runs the program 
    
# Running the File System Example

    Using Java:
    javac fat32_reader.java Fat32File.java DIR_Entry.java Fat32_driver.java RMIServerInterface.java RMIServer.java ClientRequest.java
    java Fat32_driver fat32.img
    In a separate terminal:
    java ClientRequest {hostName} {portNum}
    
    Using Makefile:
    make
    make run
    In a separate terminal:
    java ClientRequest {hostName} {portNum}

Then the program will display the working directory with a prompt to enter a command. The 8 commands it supports is 

    info                                        prints info of the .img file
    ls      {file/directory}                    prints the list of directories of the given folder
    stat    {file/directory}                    prints the statistics of the given file or folder 
    cd      {file/directory}                    changes the current directory  
    size    {file/directory}                    prints the size of the given file
    open    {file/directory}                    opens a closed file
    close   {file/directory}                    closes an open file
    read    {file/directory}{offset}{numBytes}  prints the ASCII values from the offset to the numbers of bytes passed in

# Challenges 

We faced challenges with displaying error messages when remotely invoking commands from the client.

When running the server on my windows laptop then runnning a client on my windows desktop the server and client worked perfectly. However, when running the server on my windows desktop and client on windows laptop, the IP address was private and didnt work for some reason. 

# Sources Cited 

The main source that stepped us through the Java RMI component of this project
        https://www.geeksforgeeks.org/remote-method-invocation-in-java/
 
