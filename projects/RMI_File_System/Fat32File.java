/**
 * HELLO! WELCOME!
 *
 * Pardon the harshness of "fat32_reader.java"
 * the responsibility of running an entire program got to their attitude
 *
 * Make your way over to the constructor
 * your stay here wont be too long
 *
 * Hope you enjoy iy though!
 */



import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class Fat32File {
	protected File file;
	protected int BPB_BytesPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATS, BPB_FATSz32, BPB_RootClus, BPB_RootEntCnt;
	protected int RootDirSectors , FirstDataSector, FirstSectorofCluster, BPB_TotSec32, DataSec, CountOfClusters, BPB_FSInfo;
	protected byte[] data;

	/**
	 * Hey so all we do here is accept a file
	 *
	 * Then we read the bytes go there for more information, I dont know why but
	 * that method enjoys staying at the bottom of the class.
	 * He usually gets his work done first then his motivation for anything else leaves
	 *
	 * catch him before he goes to sleep!
	 *
	 * Now that your acquainted with him, we pass the rest of the work to init
	 *
	 * Go meet the A student in this class!!
	 */
	protected Fat32File(File file)  {
		this.file = file;
		this.data = getBytes(file);
		this.init();
	}

	/**
	 * Hey I carry this entire class, they wouldnt be able to do anything wihtout me
	 *
	 * I have a deep understanding of FAT32 boot sectors
	 * so I use that knowledge to parse all the data for my peers
	 * and save the required information in variables
	 *
	 * lets walk through my method
	 *
	 * then checkout the other methods, they are used by the reader though
	 * but they are important to know!!!
	 *
	 * Then make your way back to the reader, he has been waiting patiently
	 * He told me you should go to where you came from, his constructor
	 */
	private void init(){

		//I find the BPB_BytesPerSec in bytes 11 and 12
		this.BPB_BytesPerSec = Integer.parseInt(String.format("%02X", this.data[12]) + ""
				+ String.format("%02X", this.data[11]), 16);

		//I find the BPB_SecPerClus in byte 13
		this.BPB_SecPerClus =  this.data[13];

		//I find the BPB_RsvdSecCnt in bytes 14 and 15
		this.BPB_RsvdSecCnt = Integer.parseInt(String.format("%02X", this.data[15]) + ""
				+ String.format("%02X", this.data[14]), 16);

		//I find the BPB_NumFATSHex in byte 16
		this.BPB_NumFATS = this.data[16];

		//I find the BPB_RootEntCnt in bytes 17 and 18
		this.BPB_RootEntCnt = Integer.parseInt(String.format("%02X", this.data[18]) + ""
				+ String.format("%02X", this.data[17]), 16);

		//I find the BPB_TotSec32 in bytes 32, 33, 34, and 35
		this.BPB_TotSec32 = Integer.parseInt(String.format("%02X", this.data[35]) + ""
				+ String.format("%02X", this.data[34]) + ""
				+ String.format("%02X", this.data[33]) + ""
				+ String.format("%02X", this.data[32]), 16);

		//I find the BPB_FATSz32 in bytes 36, 37, 38, and 39
		this.BPB_FATSz32 = Integer.parseInt(String.format("%02X", this.data[39]) + ""
				+ String.format("%02X", this.data[38]) + ""
				+ String.format("%02X", this.data[37]) + ""
				+ String.format("%02X", this.data[36]), 16);

		//I find the BPB_RootClus in bytes 44, 45, 46, and 47
		this.BPB_RootClus = Integer.parseInt(String.format("%02X", this.data[47]) + ""
				+ String.format("%02X", this.data[46]) + ""
				+ String.format("%02X", this.data[45]) + ""
				+ String.format("%02X", this.data[44]), 16);

		//I find the BPB_FSInfo in bytes 48 and 49
		this.BPB_FSInfo = Integer.parseInt(String.format("%02X", this.data[49]) + ""
                                            + String.format("%02X", this.data[48]), 16);

		//I get the root directory sector
		this.RootDirSectors  = ((this.BPB_RootEntCnt * 32) + (this.BPB_BytesPerSec - 1)) / this.BPB_BytesPerSec;

		//then I get the first data sector by taking the number of FAT files times the sizes of them
		//then adding the root directory sectors and reserved space
		this.FirstDataSector = this.BPB_RsvdSecCnt + (this.BPB_NumFATS * this.BPB_FATSz32) + this.RootDirSectors;

		//then I get the first sector of the first cluster by calling the respected method
		//you can check that method when we are done here, because there is so little left
		this.FirstSectorofCluster = this.getFirstSectorOfCluster(this.BPB_RootClus);

		//Then get the total data sectors
		this.DataSec = this.BPB_TotSec32 - (this.BPB_RsvdSecCnt + (this.BPB_NumFATS * this.BPB_FATSz32) + this.RootDirSectors);

		//and divide that by the size of each cluster to obtain the amount of clusters
		this.CountOfClusters = this.DataSec / this.BPB_SecPerClus;

	}

	//im just a getter method
	protected int getFirstSectorofCluster() {
		return FirstSectorofCluster;
	}

	/**
	 * Hey
	 * I get the sector number of the first sector in the given cluster
	 *
	 * @param N has to be a valid cluster number
	 * @return first sector of that cluster
	 */
	protected int getFirstSectorOfCluster(int N){
		return (((N - 2) * this.BPB_SecPerClus) + this.FirstDataSector) * this.BPB_BytesPerSec;
	}

	/**
	 * Gets the section number of a given cluster and given entry
	 *in
	 * @param N has to be a valid cluster number
	 *
	 * @param numOfFat is a multiple for obtaining further FAT entries
	 * First FAT = numOfFat = 0
	 * Second FAT = numOfFat = 1
	 * Third FAT = numOfFat = 2
	 * and so on
	 *
	 * @return the sector number of the FAT sector that contains the entry for
	 * cluster N in the first FAT. If you want later entries increment numOfFat.
	 */
	protected int getSectionNumber(int N, int numOfFat){
		//offset for Fat 32
		int FATOffset = N * 4;
		//gets the fat section
		int thisFatSecNum = this.BPB_RsvdSecCnt + (FATOffset / this.BPB_BytesPerSec);
		//gets which fat entry
		int fatEntry = numOfFat * this.BPB_FATSz32;
		//gets the correct sector based on which entry was requested
		return thisFatSecNum + fatEntry;
	}

	/**
	 * Gets the offset of given entry
	 * @param N has to be a valid cluster number
	 * @return the offset of given entry
	 */
	protected int getEntryOffset(int N){
		return (N * 4) % this.BPB_BytesPerSec;
	}

	/**
	 * 	Hey, lets make this quick
	 *
	 * 	I take a file then read all the bytes into an array
	 *	then go back to sleep
	 *
	 * @param file the FAT32 file
	 * @return byte array of the file
	 */
	private byte[] getBytes(File file)  {
		byte[] array = new byte[(int) file.length()];

		try{
			array = Files.readAllBytes(file.toPath());

		}catch (IOException e){
			e.printStackTrace();
		}
		return array;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Fat32File fat32File = (Fat32File) o;
		return BPB_BytesPerSec == fat32File.BPB_BytesPerSec &&
				BPB_SecPerClus == fat32File.BPB_SecPerClus &&
				BPB_RsvdSecCnt == fat32File.BPB_RsvdSecCnt &&
				BPB_NumFATS == fat32File.BPB_NumFATS &&
				BPB_FATSz32 == fat32File.BPB_FATSz32 &&
				BPB_RootClus == fat32File.BPB_RootClus &&
				BPB_RootEntCnt == fat32File.BPB_RootEntCnt &&
				RootDirSectors == fat32File.RootDirSectors &&
				FirstDataSector == fat32File.FirstDataSector &&
				FirstSectorofCluster == fat32File.FirstSectorofCluster &&
				BPB_TotSec32 == fat32File.BPB_TotSec32 &&
				DataSec == fat32File.DataSec &&
				CountOfClusters == fat32File.CountOfClusters &&
				BPB_FSInfo == fat32File.BPB_FSInfo &&
				Objects.equals(file, fat32File.file) &&
				Arrays.equals(data, fat32File.data);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(file, BPB_BytesPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATS, BPB_FATSz32, BPB_RootClus, BPB_RootEntCnt, RootDirSectors, FirstDataSector, FirstSectorofCluster, BPB_TotSec32, DataSec, CountOfClusters, BPB_FSInfo);
		result = 31 * result + Arrays.hashCode(data);
		return result;
	}
}
