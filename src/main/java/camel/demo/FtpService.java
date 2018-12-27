package camel.demo;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FtpService {
	
	public static long getFtpFileCount() {
		
		String host = "localhost";
		int port = 21;
		String username = "test";
		String password = "test";
		 
		FTPClient ftpClient = new FTPClient();
		try {
		    ftpClient.connect(host, port);
		    ftpClient.login(username, password);
		    ftpClient.enterLocalPassiveMode();
		 
		    String remoteDirPath = "/";
		 
		    long[] dirInfo = calculateDirectoryInfo(ftpClient, remoteDirPath, "");
		 
		    System.out.println("Total dirs: " + dirInfo[0]);
		    System.out.println("Total files: " + dirInfo[1]);
		    System.out.println("Total size: " + dirInfo[2]);           
		 
		    ftpClient.logout();
		    ftpClient.disconnect();
		    
		    return dirInfo[1];
		    
		} catch (IOException ex) {
		    System.err.println(ex);
		}
		
		return 0;		
	}

	public static long[] calculateDirectoryInfo(FTPClient ftpClient,
	        String parentDir, String currentDir) throws IOException {
	    long[] info = new long[3];
	    long totalSize = 0;
	    int totalDirs = 0;
	    int totalFiles = 0;
	 
	    String dirToList = parentDir;
	    if (!currentDir.equals("")) {
	        dirToList += "/" + currentDir;
	    }
	 
	    try {
	        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
	 
	        if (subFiles != null && subFiles.length > 0) {
	            for (FTPFile aFile : subFiles) {
	                String currentFileName = aFile.getName();
	                if (currentFileName.equals(".")
	                        || currentFileName.equals("..")) {
	                    // skip parent directory and the directory itself
	                    continue;
	                }
	 
	                if (aFile.isDirectory()) {
	                    totalDirs++;
	                    long[] subDirInfo =
	                            calculateDirectoryInfo(ftpClient, dirToList, currentFileName);
	                    totalDirs += subDirInfo[0];
	                    totalFiles += subDirInfo[1];
	                    totalSize += subDirInfo[2];
	                } else {
	                    totalSize += aFile.getSize();
	                    totalFiles++;
	                }
	            }
	        }
	 
	        info[0] = totalDirs;
	        info[1] = totalFiles;
	        info[2] = totalSize;
	 
	        return info;
	    } catch (IOException ex) {
	        throw ex;
	    }
	}
	
	
	
}
