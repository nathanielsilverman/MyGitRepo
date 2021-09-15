/**
 * Welcome we keep all the information for a specific directory or file entry
 *
 * Read our variables first to get an idea what we store
 *
 * Then go to the constructor for the rest
 */

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DIR_Entry {
	//the byte data
	protected byte[] data;
	//the attributes of the files
	protected String [] attributes = new String[6];
	//a map of all the sub directories
	protected HashMap<String, DIR_Entry> subDirs = new HashMap<>();
	//our name
	protected String Dir_Name;
	//byte data will be explained in the init method
	protected int lookup, DIR_Attr, DIR_FstClusHI, DIR_FstClusLO, DIR_FileSize, Next_Cluster, Next_Cluster_Addr;
	//if people need to know what kind of files we are
	protected boolean isLong, ATTR_READ_ONLY, ATTR_HIDDEN, ATTR_SYSTEM, ATTR_VOLUME_ID, ATTR_DIRECTORY, ATTR_ARCHIVE;
	protected boolean isOpen;

	/**
	 * I figure out what size we are then let init do all the work
	 */
	protected DIR_Entry(byte[] data) {
		this.data = data;
		this.isLong = (this.data[0] == 65);
		this.init();
	}

	/**
	 * Hi there again! Yup im in two classes! Yup I carry both of them!
	 * Pretty similar as before just walk through and ill explain as we go!
	 *
	 * After reading "makeEntry" told you to go back to the constructor of fat32_reader.java
	 */
	private void init(){

		//I get the DIR_Attr at bytes 43 or 11
		this.DIR_Attr = Integer.parseInt(String.format("%02X", this.data[(isLong) ? 43 : 11]), 16);

		//I get the DIR_FstClusHI at bytes 52 and 53 or 20 and 21
		this.DIR_FstClusHI = Integer.parseInt(String.format("%02X", this.data[(isLong) ? 53 : 21]) + ""
				+ String.format("%02X", this.data[(isLong) ? 52 : 20]), 16);

		//I get the DIR_FstClusLO at bytes 58 and 59 or 28 and 27
		this.DIR_FstClusLO = Integer.parseInt(String.format("%02X", this.data[(isLong) ? 59 : 27]) + ""
				+ String.format("%02X", this.data[(isLong) ? 58 : 26]), 16);


		//I get the DIR_FileSize at the below bytes :) i got lazy writing them down
		this.DIR_FileSize = Integer.parseInt(String.format("%02X", this.data[(isLong) ? 63 : 31]) + ""
				+ String.format("%02X", this.data[(isLong) ? 62 : 30]) + ""
				+ String.format("%02X", this.data[(isLong) ? 61 : 29]) + ""
				+ String.format("%02X", this.data[(isLong) ? 60 : 28]), 16);

		//calculate the next cluster
		this.Next_Cluster = this.DIR_FstClusLO + this.DIR_FstClusHI;

		//set the attributes, go check that out!!
		this.attrToString();

		//I extract the name from the naming scheme, go check that out as well!!
		this.Dir_Name = this.extractName();
	}

	/**
	 * sets the file attribute
	 */
	private void attrToString() {
		int x = this.DIR_Attr;

		if (x >= 32) {
			x -= 32;
			ATTR_ARCHIVE = true;
			this.attributes[0] = "ATTR_ARCHIVE";
		}
		if (x >= 16) {
			x -= 16;
			ATTR_DIRECTORY = true;
			this.attributes[1] = "ATTR_DIRECTORY";
		}
		if (x >= 8) {
			x -= 8;
			ATTR_VOLUME_ID = true;
			this.attributes[2] = "ATTR_VOLUME_ID";
		}
		if (x >= 4) {
			x -= 4;
			ATTR_SYSTEM = true;
			this.attributes[3] = "ATTR_SYSTEM";
		}
		if (x >= 2) {
			x -= 2;
			ATTR_HIDDEN = true;
			this.attributes[4] = "ATTR_HIDDEN";
		}
		if (x >= 1) {
			x -= 1;
			ATTR_READ_ONLY = true;
			this.attributes[5] = "ATTR_READ_ONLY";
		}
	}

	/**
	 * parses the 1st through 11th bytes too get the dir name
	 * @return the directory name of that given file/directory
	 */
	private String extractName(){
		//name of the dir
		StringBuilder name = new StringBuilder();

		//get the first part of the name
		for (int i = (this.isLong) ? 1 : 0; i < ((isLong) ? 10 : 11); i++) {
			name.append((!Character.isISOControl(this.data[i]) && (Byte.toUnsignedInt(this.data[i]) != 0xFF) && (char)this.data[i] != ' ') ? (char)this.data[i] : "");
		}

		if(!isLong){
			return name.toString();
		}

		//get the second part of long name
		for (int i = 14; i < 26; i++) {
			//append the english letters to the dir
			name.append((!Character.isISOControl(this.data[i]) && (Byte.toUnsignedInt(this.data[i]) != 0xFF) && (char)this.data[i] != ' ') ? (char)this.data[i] : "");
		}

		//get third part of long name
		for (int i = 28; i < 32; i++) {
			//append the english letters to the dir
			name.append((!Character.isISOControl(this.data[i]) && (Byte.toUnsignedInt(this.data[i]) != 0xFF) && (char)this.data[i] != ' ') ? (char)this.data[i] : "");
		}

		return name.toString();
	}

	/**
	 * A bunch of getters and setters that do the rote work
	 */
	protected String getDir_Name(){
		return this.Dir_Name;
	}

	protected int getNext_Cluster() {
		return Next_Cluster;
	}

	protected int getNext_Cluster_Addr() {
		return Next_Cluster_Addr;
	}

	protected void setNext_Cluster_Addr(int next_Cluster_Addr) {
		Next_Cluster_Addr = next_Cluster_Addr;
	}

	protected boolean isDirectory(){
		return this.ATTR_DIRECTORY;
	}

	protected void setLookup(int lookup){
		this.lookup = lookup;
	}

	protected int getLookup(){
		return this.lookup;
	}

	protected void addSubDirectory(DIR_Entry entry){
		this.subDirs.put(entry.getDir_Name(), entry);
	}

	protected void setName(String name){
		this.Dir_Name = name;
	}

	protected boolean open() {
		if (this.isOpen) return false;
		else {
			this.isOpen = true;
			return true;
		}
	}

	protected boolean close(){
		if (!this.isOpen) return false;
		else {
			this.isOpen = false;
			return true;
		}
	}

	protected String getData(int offset, int numBytes) {
		if (offset < 0 || numBytes <= 0) {
			return "";
		}
		if (numBytes > (this.DIR_FileSize - offset)) {
			return "Error: attempt to read data outside of file bounds";
		}
		return new String(this.data, offset, numBytes, Charset.forName("UTF-8"));
	}

	/*************************
	 * Formats decimal to hex
	 *************************/
	private String formatHex(int i) {
		return "0x" + Integer.toHexString(i);
	}

	@Override
	public String toString() {
		StringBuilder list = new StringBuilder();
		for(String s : this.attributes){
			if(s == null) continue;
			list.append(s).append(" ");
		}
		list.append("\n");
		return  "Size is "+this.DIR_FileSize+"\n"+
				"Attributes "+ list.toString()+
				"Next cluster number is "+this.formatHex(this.Next_Cluster);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DIR_Entry)) return false;
		DIR_Entry dir_entry = (DIR_Entry) o;
		return DIR_Attr == dir_entry.DIR_Attr &&
				DIR_FstClusHI == dir_entry.DIR_FstClusHI &&
				DIR_FstClusLO == dir_entry.DIR_FstClusLO &&
				DIR_FileSize == dir_entry.DIR_FileSize &&
				Next_Cluster == dir_entry.Next_Cluster &&
				Next_Cluster_Addr == dir_entry.Next_Cluster_Addr &&
				isLong == dir_entry.isLong &&
				ATTR_READ_ONLY == dir_entry.ATTR_READ_ONLY &&
				ATTR_HIDDEN == dir_entry.ATTR_HIDDEN &&
				ATTR_SYSTEM == dir_entry.ATTR_SYSTEM &&
				ATTR_VOLUME_ID == dir_entry.ATTR_VOLUME_ID &&
				ATTR_DIRECTORY == dir_entry.ATTR_DIRECTORY &&
				ATTR_ARCHIVE == dir_entry.ATTR_ARCHIVE &&
				Arrays.equals(data, dir_entry.data) &&
				Arrays.equals(attributes, dir_entry.attributes) &&
				Objects.equals(subDirs, dir_entry.subDirs) &&
				Objects.equals(Dir_Name, dir_entry.Dir_Name);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(subDirs, Dir_Name, DIR_Attr, DIR_FstClusHI, DIR_FstClusLO, DIR_FileSize, Next_Cluster, Next_Cluster_Addr, isLong, ATTR_READ_ONLY, ATTR_HIDDEN, ATTR_SYSTEM, ATTR_VOLUME_ID, ATTR_DIRECTORY, ATTR_ARCHIVE);
		result = 31 * result + Arrays.hashCode(data);
		result = 31 * result + Arrays.hashCode(attributes);
		return result;
	}
}
