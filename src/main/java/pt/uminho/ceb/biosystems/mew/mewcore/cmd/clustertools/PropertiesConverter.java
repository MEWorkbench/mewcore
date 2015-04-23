package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConverter {
	
	public static void convertXMLPropertiesToText(String propertiesFile, String newFile) throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		prop.loadFromXML(new FileInputStream(propertiesFile));
		FileOutputStream fos = new FileOutputStream(new File(newFile));		
		prop.store(fos,null);
//		prop.storeToXML(fos,null);
		fos.flush();
		fos.close();
	}
	
	public static void main(String... args){
		try {
			convertXMLPropertiesToText("files/iAF1260_orig_simpG/iAF1260_SimpG.conf", "files/iAF1260_orig_simpG/iAF1260.conf");
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} finally{
			System.out.println("Done");
		}
	}
}
