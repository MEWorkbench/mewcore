package pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.connection.matlab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.exceptions.MatlabNotFoundException;
import pt.uminho.ceb.biosystems.mew.mewcore.integrationplatform.properties.MatlabProperties;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;


public class MatlabProxySingleton {

	
	static boolean loadpathdef = true;
	static MatlabProxySingleton instance;
	private MatlabProxy proxy;
	private MatlabProperties matprops;
	
	//
	// 
	//
	private MatlabProxySingleton() throws MatlabConnectionException, MatlabInvocationException {
		
		MatlabProperties prop;
		if(getMatprops() != null)
			prop = getMatprops();
		else
			prop = new MatlabProperties();
		try {
			prop = MatlabProperties.createProperties(MatlabProperties.FILE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File f = new File(prop.getExecutableFile());
//		if(!f.exists())
//			throw new MatlabNotFoundException();
		 MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
		 .setHidden(prop.getShowConsoleOnly())
         //.setMatlabLocation("C:\\Program Files\\MATLAB\\R2013a\\bin\\matlab.exe")
		 .setMatlabLocation(prop.getExecutableFile())//"C:\\Program Files\\MATLAB\\R2013a\\bin\\matlab.exe")
         .build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		proxy = factory.getProxy();
		//if(loadpathdef) proxy.eval("addpath(pathdef())");
	}
	
	static MatlabProxySingleton getInstance() throws MatlabConnectionException, MatlabInvocationException{
		if(instance ==null)
			instance = new MatlabProxySingleton();
		
		return instance;
	}
	
	
	public void setMatprops(MatlabProperties matprops) {
		this.matprops = matprops;
	}
	
	public MatlabProperties getMatprops() {
		return matprops;
	}
	
	
	public MatlabProxy getProxy() {
		if(!proxy.isConnected())
			try {
				instance = new MatlabProxySingleton();
			} catch (MatlabConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MatlabInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return proxy;
	}
}
