package pt.uminho.ceb.biosystems.mew.core.simulation.formulations.cobra;
//package metabolic.simulation.formulations.cobra;
//
//import MatlabConnectionException;
//import MatlabInvocationException;
//import MatlabProxy;
//import MatlabProxyFactory;
//import MatlabProxyFactoryOptions;
//
//
//public class MatlabProxySingleton {
//
//	
//	static boolean loadpathdef = true;
//	static MatlabProxySingleton instance;
//	private MatlabProxy proxy;
//	
//	//
//	// MUITO IMPORTANTE TRATAR DESTA PARTE DE VER ONDE ESTa LOCALIZADO O MATLAB
//	//
//	private MatlabProxySingleton() throws MatlabConnectionException, MatlabInvocationException {
//		
//		 MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
//		 .setHidden(true)
//         .setMatlabLocation("C:\\Program Files\\MATLAB\\R2008a\\bin\\matlab.exe")
//         .build();
//		MatlabProxyFactory factory = new MatlabProxyFactory(options);
//		proxy = factory.getProxy();
//		if(loadpathdef) proxy.eval("addpath(pathdef())");
//	}
//	
//	static MatlabProxySingleton getInstance() throws MatlabConnectionException, MatlabInvocationException{
//		if(instance ==null)
//			instance = new MatlabProxySingleton();
//		
//		return instance;
//	}
//	
//	public MatlabProxy getProxy() {
//		return proxy;
//	}
//}
