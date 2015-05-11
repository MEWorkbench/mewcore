package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.properties;

import java.io.File;
import java.util.Properties;

import pt.uminho.ceb.biosystems.mew.utilities.io.PropertiesUtils;

public class MatlabProperties {

	public static final String EXECUTABLE_FILE = "Matlab.executableFile";
	public static final String SHOW_CONSOLE_ONLY = "Matlab.showconsole";
	public static final String CLOSE_MATLAB_AFTER_EXIT = "Matlab.closeafterexit";
	
	public static final String GUROBI_FOLDER = "Matlab.gurobifolder";
	
	
	public static final String FILE = "./conf/Properties/matlab.conf";
	
	private Properties properties;
	
	public MatlabProperties() {
		this(null);
	}
	
	public MatlabProperties(Properties properties) {
		this.properties = (properties == null) ? new Properties() : properties;
		
		// Fill possible empty properties
		fillProperties();
	}
	
	private void fillProperties(){
		if(!this.properties.containsKey(EXECUTABLE_FILE))
			setExecutableFile("");
		if(!this.properties.containsKey(SHOW_CONSOLE_ONLY))
			setShowConsoleOnly(true);
		if(!this.properties.containsKey(CLOSE_MATLAB_AFTER_EXIT))
			setCloseAfterExit(false);
		if(!this.properties.containsKey(GUROBI_FOLDER))
			setGurobiFolder("");
	}
	
	public void setShowConsoleOnly(boolean showConsoleOnly) {
		properties.put(SHOW_CONSOLE_ONLY, showConsoleOnly+"");
	}
	
	public void setGurobiFolder(String gurobiFolder) {
		properties.put(GUROBI_FOLDER, gurobiFolder);
	}
	
	public void setExecutableFile(String executableFile) {
		properties.put(EXECUTABLE_FILE, executableFile);
	}
	
	public void setCloseAfterExit(boolean closeAfterExit) {
		properties.put(CLOSE_MATLAB_AFTER_EXIT, closeAfterExit+"");
	}
	
	public String getExecutableFile() {
		return properties.getProperty(EXECUTABLE_FILE);
	}
	
	public boolean getCloseMatlabAfterExit() {
		// return false if null or empty property
		return Boolean.parseBoolean(properties.getProperty(CLOSE_MATLAB_AFTER_EXIT));
	}
	
	public boolean getShowConsoleOnly() {
		// return false if null or empty property
		return Boolean.parseBoolean(properties.getProperty(SHOW_CONSOLE_ONLY));
	}
	
	public String getGurobiFolder() {
		return properties.getProperty(GUROBI_FOLDER);
	}
	
	public String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public void putProperty(Object key, Object value){
		properties.put(key, value);		
	}
	
	public Properties getProperties(){
		return properties;
	}
	
	public void setProperties(Properties properties){
		this.properties = properties;
	}
	
	public static MatlabProperties createPropertiesFromFile(String file){
		if(!new File(file).exists()){
			return new MatlabProperties();
		}
		
		Properties p = PropertiesUtils.readPropertiesFromFile(file);
		return new MatlabProperties(p);
	}
	
	public static void writeMatlabPropertiesFile(MatlabProperties properties, String filePath){
		PropertiesUtils.writePropertiesFromFile(properties.getProperties(), filePath);
	}
	
	public static void main(String[] args) {
		MatlabProperties p = new MatlabProperties();
		
		System.out.println(": "+p.getCloseMatlabAfterExit());
		System.out.println(": "+p.getExecutableFile());
		System.out.println(": "+p.getGurobiFolder());
		System.out.println(": "+p.getShowConsoleOnly());
		
		MatlabProperties.writeMatlabPropertiesFile(p, "../optfluxcore3/conf/Properties/matlab.confX1");
		
		MatlabProperties properties = MatlabProperties.createPropertiesFromFile("../optfluxcore3/conf/Properties/matlab.confX1");
		
		System.out.println(": "+properties.getCloseMatlabAfterExit());
		System.out.println(": "+properties.getExecutableFile());
		System.out.println(": "+properties.getGurobiFolder());
		System.out.println(": "+properties.getShowConsoleOnly());
		
		properties.setGurobiFolder("TEST");
		
		MatlabProperties mat = new MatlabProperties(properties.getProperties());
		
		System.out.println(": "+mat.getCloseMatlabAfterExit());
		System.out.println(": "+mat.getExecutableFile());
		System.out.println(": "+mat.getGurobiFolder());
		System.out.println(": "+mat.getShowConsoleOnly());
	}
}
