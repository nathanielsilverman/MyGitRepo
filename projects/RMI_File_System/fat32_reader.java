/**
 * Welcome to Zachary Hamburger and Nathaniel Silvermans FAT32 file systems!
 *
 * Make your way over to the main method to begin reading the execution of the program
 * which resides under the constructor for the file system
 *
 * Hope you enjoy your stay!!
 */

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class fat32_reader {

	Fat32File file;
	//HashMap<String, DIR_Entry> table = new HashMap<>(); //maps dirs and file names to there dir entry
	DIR_Entry currentDirectory;
	String dirName;
	Stack<DIR_Entry> parents = new Stack<>();
//	LinkedList<DIR_Entry> pathToCurrentDir = new LinkedList<>();
	DIR_Entry root;
	/**
	 * I was right, it was right above you!
	 *
	 * This constructor is passed in a File
	 *
	 * PLEASE COME WITH ME OVER TO THE FAT32 FILE CLASS
	 * OTHERWISE THE OTHER METHODS WONT MAKE MUCH SENSE!!!!
	 *
	 * Sorry for the extra emphasis, its just very important we continue there
	 * you'll be instructed to come back here, so ill see you later
	 * Have a fun time over there!
	 *
	 *
	 * NOW GO! its called "FAT32File.java"
	 *
	 * WELCOME BACK! How was your trip?
	 * I bet it was great, I took a power nap and feel much better
	 *
	 * Make you way to read directories method
	 * then come back when instructed
	 *
	 *
	 * WELCOME BACK AGAIN!!
	 * lets now check out what map directories does!
	 *
	 *
	 *
	 * GO BACK TO MAIN NOW WE ARE FINALLY DONE
	 */
	public fat32_reader(File isoFile) {
		this.file = new Fat32File(isoFile);

		DIR_Entry root = this.makeEntry(this.file.getFirstSectorofCluster(), this.file.getFirstSectorofCluster()+this.file.BPB_BytesPerSec);
		this.root = root;
		//gets directories under root
		List<DIR_Entry> dirs = this.readDirectories(this.file.getFirstSectorofCluster(), this.file.getFirstSectorofCluster()+this.file.BPB_BytesPerSec, -1, root);

		//if the root has more
		this.mapSubDirectories(root, false);

		//add dot
		root.setName(".");
		dirs.add(root);

		//add dotdot
		DIR_Entry dotdot = this.makeEntry(this.file.getFirstSectorofCluster(), this.file.getFirstSectorofCluster()+this.file.BPB_BytesPerSec);
		dotdot.setName("..");
		dirs.add(dotdot);

		//gonna go through dirs and map them to a table
		root.subDirs = this.mapDirs(dirs);
		this.currentDirectory = root;
		this.parents.push(this.currentDirectory);
	}

	/**********************************************
	 *
	 * Prints info about the size of the disk file
	 *
	 **********************************************/
	protected String getInfo(){
		StringBuilder builder = new StringBuilder();

		builder.append("BPB_BytesPerSec is ").append(formatHex(this.file.BPB_BytesPerSec)).append(", ").append(this.file.BPB_BytesPerSec).append("\n");
		builder.append("BPB_SecPerClus is ").append(formatHex(this.file.BPB_SecPerClus)).append(", ").append(this.file.BPB_SecPerClus).append("\n");
		builder.append("BPB_RsvdSecCnt is ").append(formatHex(this.file.BPB_RsvdSecCnt)).append(", ").append(this.file.BPB_RsvdSecCnt).append("\n");;
		builder.append("BPB_NumFATs is ").append(formatHex(this.file.BPB_NumFATS)).append(", ").append(this.file.BPB_NumFATS).append("\n");;
		builder.append("BPB_FATSz32 is ").append(formatHex(this.file.BPB_FATSz32)).append(", ").append(this.file.BPB_FATSz32);

		return builder.toString();
	}

	/**********************************************
	 *
	 * Prints the contents of the given directory
	 *
	 **********************************************/
	protected String listDirectoryContent(File fileDirName){
		StringBuilder sb = new StringBuilder();
		if(fileDirName.getPath().length() == 0){
			String[] names = this.getNames(this.currentDirectory.subDirs);
			for (String s : names){
				sb.append(s).append("  ");
			}
			return (sb.toString());
		}

		String[] subDirs = this.getDirectoryPath(fileDirName);

		//if its .. then it gets previous directory
		//if its . it gets the current one
		//if its neither it gets the directory asked for

		HashMap<String, DIR_Entry> map = this.getMap(subDirs, subDirs.length);

		Stack<DIR_Entry> temp = new Stack<>();
		for (String subDir : subDirs) {
			if (!subDir.equals("..")) continue;
			DIR_Entry parent = this.parents.pop();
			temp.push(parent);
			map = parent.subDirs;
		}

		while(!temp.isEmpty()){
			this.parents.push(temp.pop());
		}

		if(map == null){
			return ("Error: "+fileDirName+" is not a directory");
		}

		String[] names = this.getNames(map);
		if(names.length == 0) {
			return "Error: "+fileDirName+" is not a directory";
		}
		for (String s : names){
			sb.append(s).append("  ");
		}
		return sb.toString();
	}

	/***************************************************
	 *
	 * Prints the statistics of given directory or file
	 *
	 ***************************************************/
	protected String showStatistics(File fileDirName) {
		String[] subDirs = this.getDirectoryPath(fileDirName);
		HashMap<String, DIR_Entry> map = this.getMap(subDirs, subDirs.length-1);
		if(map == null || !map.containsKey(subDirs[subDirs.length-1])){
			return ("Error: "+fileDirName+" does not exist");
		}
		return (map.get(subDirs[subDirs.length-1]).toString());
	}

	/***************************************************
	 *
	 * Changes to given directory
	 *
	 ***************************************************/
	protected String changeDirectory(File fileDirName){
		String[] subs = this.getDirectoryPath(fileDirName);
		//loop through all the given sub directories
		for(String dir : subs) {
			//get the current directory in the path
			DIR_Entry dirEntry = this.currentDirectory.subDirs.get(dir);
			//null check
			if (dirEntry == null) {
				return "Error: " + fileDirName + " is not a directory";

			}
			if (!dirEntry.isDirectory() && !dirEntry.getDir_Name().equals(".") && !dirEntry.getDir_Name().equals("..")) {
				return "Error: " + fileDirName + " is not a directory";
			}
			//if we are stepping backward
			if (dirEntry.getDir_Name().equals("..")) {
				//if we are at the root, LEAVE!!!!
				if (this.currentDirectory == root) return null;
				//get the previous directory
				this.currentDirectory = parents.pop();
				//if it is the parent add it back
				if (this.currentDirectory == root) this.parents.push(root);

				//update the directory name by removing the current name in the directory
				StringBuilder newDir = new StringBuilder();
				//append the current name
				newDir.append(this.dirName);
				//loop through until the file separator and delete all the given characters
				for (int i = newDir.length() - 1; i >= 0 && newDir.charAt(i) != File.separatorChar; i--) {
					newDir.deleteCharAt(i);
				}
				//delete the file separator
				newDir.deleteCharAt(newDir.length() - 1);
				//make this the new directory
				this.dirName = newDir.toString();
				continue;
			//if they want the current directory
			} else if (dirEntry.getDir_Name().equals(".")) {
				this.currentDirectory = dirEntry;
				continue;
			}

			//if it was a valid directory
			//push the parent one
			this.parents.push(this.currentDirectory);
			//make the current directory the global current
			this.currentDirectory = dirEntry;
			//append the name to the path
			this.setDirName(dirName + File.separator + dirEntry.getDir_Name());
		}
		return null;
	}

	protected int get_file_size(File file) {
		DIR_Entry entry = getDirectoryEntry(file);
		if(entry == null) return -1;
		if (entry.ATTR_DIRECTORY) {
			return -1;
		}
		else {
			return entry.DIR_FileSize;
		}
	}

	protected String handleOpeningAndClosingFiles(File file, boolean shouldOpen){
		DIR_Entry entry = getDirectoryEntry(file);
		if(entry == null) {
			return "Error: "+file+" is not a file";
		}
		if (entry.ATTR_DIRECTORY) {
			return "Error: "+file+" is not a file";
		}

		if (entry.isOpen && shouldOpen) {
			return "Error: "+file+" is open";
		}

		if(!entry.isOpen && !shouldOpen) {
			return "Error: "+file+" is not open";
		}

		if (shouldOpen) {
			entry.open();
			return file + " is open";
		} else {
			entry.close();
			return file + " is closed";
		}

	}

	//gets sorted file names
	private String[] getNames(HashMap<String, DIR_Entry> map){
		String[] names = new String[map.size()];
		int i = 0;
		for (String s : map.keySet()) {
			names[i] = s;
			i++;
		}
		if(names.length == 0) return names;
		//stable sort, first sort by names then by attribute
		Arrays.sort(names);
		Arrays.sort(names, (o1, o2) -> Boolean.compare(map.get(o1).ATTR_HIDDEN, map.get(o2).ATTR_HIDDEN));
		return names;
	}

	//gets the directory path of a file
	private String[] getDirectoryPath(File fileDirName){
		return fileDirName.getPath().split(Pattern.quote(File.separator));
	}

	//gets the map that the file resides in
	private HashMap<String, DIR_Entry> getMap(String[] subDirs, int end){
		HashMap<String, DIR_Entry> map = this.currentDirectory.subDirs;
		Stack<HashMap<String, DIR_Entry>> maps = new Stack<>();
		maps.push(this.currentDirectory.subDirs);
		for (int i = 0; i < end; i++) {
			if(!map.containsKey(subDirs[i])) return null;

			if(subDirs[i].equals("..")) {
				if(maps.isEmpty()) maps.push(this.currentDirectory.subDirs);
				map = maps.pop();
				continue;
			}
			if(subDirs[i].equals(".")) continue;

			maps.push(map);
			map = map.get(subDirs[i]).subDirs;
		}
		return map;
	}

	protected boolean authenticateFile(String[] subDirs, HashMap<String, DIR_Entry> map, File file){
		if(map == null || !map.containsKey(subDirs[subDirs.length-1])){
			System.out.println("Error: "+file+" is not a file");
			return false;
		}
		return true;
	}

	protected DIR_Entry getDirectoryEntry(File file){
		String[] subDirs = this.getDirectoryPath(file);
		HashMap<String, DIR_Entry> map = this.getMap(subDirs, subDirs.length-1);
		if(map == null) return null;
		if(!authenticateFile(subDirs, map, file)) return null;
		return map.get(subDirs[subDirs.length-1]);
	}

	protected String read(DIR_Entry dirEntry, int offset, int numberOfBytes){
		StringBuilder contents = new StringBuilder();
		int start = (dirEntry.getNext_Cluster_Addr() + offset);
		for (int i = start, end = start + numberOfBytes; (i < end) && (i < (start + dirEntry.DIR_FileSize)); i++) {

			contents.append((!Character.isISOControl(this.file.data[i])) ? (char)this.file.data[i] : "");
		}
		return contents.toString();
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	/**
	 * Hey lets walk through the method
	 *
	 * @param start where to start reading bytes in the file
	 * @param end where to end reading bytes in the file
	 * @param lookup the lookup number from the FAT32 lookup (check that out mid method, when i tell you to)
	 * @return a list of Directory entries
	 */
	private List<DIR_Entry> readDirectories(int start, int end, int lookup, DIR_Entry parent){

		// the list that will hold the directories under the given directory
		List<DIR_Entry> entries = new ArrayList<>();

		//go from the start to the end and skip over the sizes of the files
		for (int i = start; i < end; i+=this.file.BPB_RsvdSecCnt) {

			//if we hit a empty file
			if(((Byte.toUnsignedInt(this.file.data[i]) & 0xFF) == 229) || this.file.data[i] == 0) continue;
			//see if its a long dir or not
			boolean isLong = this.file.data[i] == 65;
			//find the size of the data
			int size = ((isLong) ? this.file.BPB_RsvdSecCnt * 2 : this.file.BPB_RsvdSecCnt);

			//***Important to check this method out after finishing this one***
			DIR_Entry entry = makeEntry(i, size);

			//if we need a lookup
			if(lookup != -1) entry.setLookup(lookup);

			if(entry.ATTR_ARCHIVE || entry.ATTR_DIRECTORY) entries.add(entry);

			//skip the extra 32 bits if its a long file
			i += (isLong) ? this.file.BPB_RsvdSecCnt : 0;
		}
		return entries;
	}

	/**
	 * Welcome we just copy the files bytes for a specific file then create a given entry
	 *
	 * We will be going on another field trip over here, dont worry its the last one
	 * Make your way over to the file called "DIR_Entry.java"
	 *
	 * you can then make your way back to the constructor
	 *
	 * @param index the given index of that file
	 * @param size the size of the file
	 * @return the entry for those given bytes
	 */
	private DIR_Entry makeEntry(int index, int size){
		byte[] dir = Arrays.copyOfRange(this.file.data, index, index + size);
		DIR_Entry entry = new DIR_Entry(dir);
		int nextCluster = this.file.getFirstSectorOfCluster(entry.getNext_Cluster());
		entry.setNext_Cluster_Addr(nextCluster);
		return entry;
	}

	/**
	 * We map each directory name to its given directory structure
	 * Its not that much work
	 *
	 * However mapping sub directories does most of the work we do
	 * go check them out!!
	 *
	 * Then return to the constructor
	 *
	 * @param entries a list of entries of the root directory
	 * @return a map of teir names to objects
	 */
	private HashMap<String, DIR_Entry> mapDirs(List<DIR_Entry> entries){
		HashMap<String, DIR_Entry> map = new HashMap<>();
		for(DIR_Entry entry : entries){
			//****errors when reading hidden files*****
			if(entry.isDirectory() && !entry.ATTR_HIDDEN){
				this.mapSubDirectories(entry, true);
			}

			map.put(entry.getDir_Name(), entry);
		}
		return map;
	}

	/**
	 * Yes my big sibling makes me do most of the work, buts its not too bad
	 *
	 * The only annoying thing is sometimes i get recursive and start calling myself when I find a directory
	 * Its a sore subject, just read my code very carefully please!!!
	 *
	 * @param entry maps sub dirs of given entry
	 */
	private void mapSubDirectories(DIR_Entry entry, boolean notRoot){
		//get next cluster checkout the lookup method!! its right below and very quick
		int lookup = FATLookup(entry.getNext_Cluster());
		//get the address
		int found = this.file.getFirstSectorOfCluster(lookup);
		//get the stopping point
		int stop = this.file.data[entry.getNext_Cluster_Addr()];
		//set the current entry
		DIR_Entry current = entry;
		current.setLookup(lookup);

		//holds sub directories
		List<DIR_Entry> subs = readDirectories(entry.getNext_Cluster_Addr(), entry.getNext_Cluster_Addr()+(this.file.BPB_BytesPerSec), -1, current);

		//while we arent at the stopping point
		while(lookup <  0x0FFFFFF8) {

			//get the address for the next cluster
			found = this.file.getFirstSectorOfCluster(lookup);

			//if(this.file.data[found] == (stop | 0x40)) break;

			//otherwise make the new entry
			DIR_Entry newEntry = this.makeEntry(found, this.file.BPB_RsvdSecCnt * 2);

			//set the address from the lookup table
			newEntry.setLookup(lookup);

			//start iterating and adding the directories from that spot
			int start = (found+(this.file.BPB_RsvdSecCnt * 2));
			subs.addAll(readDirectories(start, (start+(this.file.BPB_BytesPerSec - (this.file.BPB_RsvdSecCnt * 2))), lookup, current));
			subs.add(newEntry);

			//otherwise go to the next one
			//get the lookup for the next cluster
			lookup = FATLookup(current.getLookup());
			current = newEntry;
		}
		if(notRoot) {
			//if the sub contents are directories then repeat the action on them
			//also start at 2 because we don't need '.' '..' directories
			for (int i = 2; i < subs.size(); i++) {
				if (subs.get(i).isDirectory() && !subs.get(i).ATTR_HIDDEN) {
					//this is where I call myself
					this.mapSubDirectories(subs.get(i), true);
				}
			}
		}
		//add all the sub directories to the table in its parent directory
		subs.forEach(entry::addSubDirectory);
	}

	/**
	 * Checks the fat table for the preceding directory/files for a specific cluster
	 *
	 * @param N a valid cluster
	 * @return the next section of that cluster
	 */
	private int FATLookup(int N){
		//its not a stop character
		if(N >=  0x0FFFFFF8) return -1;
		//get the offset of given cluster
		int offSet = this.file.getEntryOffset(N);
		//get the section number as well
		int section = this.file.getSectionNumber(N, 0);
		//multiply it by the size and add the offset
		int index = section * this.file.BPB_BytesPerSec + offSet;

		//get the unsigned integer over there
		return Integer.parseInt(String.format("%02X", this.file.data[index+3]) + ""
				+ String.format("%02X", this.file.data[index+2]) + ""
				+ String.format("%02X", this.file.data[index+1]) + ""
				+ String.format("%02X", this.file.data[index]), 16);
	}

	/**
	 * Formats decimal to hex
	 */
	private String formatHex(int i) {
		return "0x" + Integer.toHexString(i);
	}
}
