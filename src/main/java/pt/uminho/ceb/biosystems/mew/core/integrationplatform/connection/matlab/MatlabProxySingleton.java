package pt.uminho.ceb.biosystems.mew.core.integrationplatform.connection.matlab;

import java.io.File;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.exceptions.MatlabNotFoundException;
import pt.uminho.ceb.biosystems.mew.core.integrationplatform.properties.MatlabProperties;


public class MatlabProxySingleton {

	
	static boolean loadpathdef = true;
	static MatlabProxySingleton instance;
	private static MatlabProxy proxy;
	private static MatlabProperties matprops;
	private static boolean isConnected = false;
	
	private MatlabProxySingleton() throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException {
		this(getMatlabProperties());
	}
	
	private MatlabProxySingleton(MatlabProperties properties) throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException {
		
		File f = new File(properties.getExecutableFile());
		if(!f.exists())
			throw new MatlabNotFoundException();
		
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
			.setProxyTimeout(60000)
			.setUsePreviouslyControlledSession(true)
			.setHidden(properties.getShowConsoleOnly())
			.setMatlabLocation(properties.getExecutableFile())
	        .build();
		
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		proxy = factory.getProxy();
		
		isConnected = true;
	}
	
	public static MatlabProxySingleton getInstance() throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException{
		return getInstance(null);
	}
	
	public static MatlabProxySingleton getInstance(MatlabProperties properties) throws MatlabConnectionException, MatlabInvocationException, MatlabNotFoundException{
		if(instance == null){
			if(properties == null)
				instance = new MatlabProxySingleton();
			else
				instance = new MatlabProxySingleton(properties);
		}
		
		return instance;
	}
	
	public static MatlabProperties getMatlabProperties(){
		MatlabProperties prop;
		if(matprops != null)
			prop = matprops;
		else
			prop = new MatlabProperties();
		
		prop = MatlabProperties.createPropertiesFromFile(MatlabProperties.FILE);
		
		return prop;
	}
	
	
	public void setMatprops(MatlabProperties matprops) {
		this.matprops = matprops;
	}
	
	public MatlabProperties getMatprops() {
		return matprops;
	}
	
	public MatlabProxy getProxy() throws MatlabNotFoundException, MatlabConnectionException, MatlabInvocationException {
		if(!proxy.isConnected())
			instance = new MatlabProxySingleton();
		return proxy;
	}
	
	public static boolean isConnected(){
		if(proxy == null)
			return isConnected;
		return proxy.isConnected();
	}
	
	public MatlabProxy reconnectProxy() throws MatlabNotFoundException, MatlabConnectionException, MatlabInvocationException{
		return getProxy();
	}
	
	public static void main(String[] args) {
		
		MatlabProperties prop = MatlabProperties.createPropertiesFromFile("../optfluxcore3/conf/Properties/matlab.confX1");
		try {
			MatlabProxySingleton.getInstance(prop).getProxy();
		} catch (MatlabNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatlabConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
