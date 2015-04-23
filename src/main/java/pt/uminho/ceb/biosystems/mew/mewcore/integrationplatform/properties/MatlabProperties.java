package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MatlabProperties {

	public static final String EXECUTABLE_FILE = "Matlab.executableFile";
	public static final String SHOW_CONSOLE_ONLY = "Matlab.showconsole";
	
	public static final String GUROBI_FOLDER = "Matlab.gurobifolder";
	
	
	public static final String FILE = "./conf/Properties/matlab.conf";
	//public static final String FILE = "/home/vmsilico/OptFlux-3.2.4_COBRA/conf/Properties/matlab.conf";
//	public static final String FILE = "C:/Users/Programador/Desktop/OptFlux/optfluxcore3/conf/Properties/matlab.conf";
	
	private boolean showConsoleOnly;
	private String executableFile;
	private String gurobiFolder;
	
	public MatlabProperties() {
		showConsoleOnly = true;
		executableFile = "";
		gurobiFolder = "";
	}
	
	public void setShowConsoleOnly(boolean showConsoleOnly) {
		this.showConsoleOnly = showConsoleOnly;
	}
	
	public boolean getShowConsoleOnly() {
		return showConsoleOnly;
	}
	
	public void setExecutableFile(String executableFile) {
		this.executableFile = executableFile;
	}
	
	public String getExecutableFile() {
		return executableFile;
	}
	
	public void setGurobiFolder(String gurobiFolder) {
		this.gurobiFolder = gurobiFolder;
	}
	
	public String getGurobiFolder() {
		return gurobiFolder;
	}
	
	public static MatlabProperties createProperties(String file) throws FileNotFoundException, IOException{
		Properties p = new Properties();
		
		File f = new File(file);
		if(!f.exists())
			return new MatlabProperties();
		
		p.load(new FileInputStream(file));
		
		boolean showConsoleOnly;
		MatlabProperties properties = new MatlabProperties();
		try{
			showConsoleOnly = Boolean.parseBoolean(p.getProperty(SHOW_CONSOLE_ONLY));
		}catch(Exception e){
			showConsoleOnly = true;
		}
		
		String executableFile;
		try{
			executableFile = p.getProperty(EXECUTABLE_FILE);
		}catch(Exception e){
			executableFile = "";
		}
		
		String gurobiFolder;
		try{
			gurobiFolder = p.getProperty(GUROBI_FOLDER);
		}catch(Exception e){
			gurobiFolder = "";
		}
		properties.setShowConsoleOnly(showConsoleOnly);
		properties.setExecutableFile(executableFile);
		properties.setGurobiFolder(gurobiFolder);
		
		return properties;
	}
}
