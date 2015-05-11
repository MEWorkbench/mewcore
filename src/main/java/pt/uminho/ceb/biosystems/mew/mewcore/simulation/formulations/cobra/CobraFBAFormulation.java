package pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.cobra;
//package metabolic.simulation.formulations.cobra;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import MatlabConnectionException;
//import MatlabInvocationException;
//import metabolic.model.steadystatemodel.ISteadyStateModel;
//import metabolic.simulation.components.SimulationProperties;
//
//public class CobraFBAFormulation extends ConnectionFormulation{
//
//	public CobraFBAFormulation(ISteadyStateModel model) throws MatlabConnectionException, MatlabInvocationException {
//		super(model, new CobraMatlabConnection());
//		// TODO Auto-generated constructor stub
//	}
//	
//	@Override
//	protected Map<String, Object> createConverterParameteres() {
//		
//		Map<String, Object> prop = new HashMap<String, Object>();
//		prop.put(SimulationProperties.IS_MAXIMIZATION, properties.get(SimulationProperties.IS_MAXIMIZATION));
//		
//		return prop;
//	}
//
//	@Override
//	protected void initPropsKeys() {
//		super.initPropsKeys();
//		mandatoryProps.add(SimulationProperties.IS_MAXIMIZATION);
//		possibleProperties.add(SimulationProperties.OBJECTIVE_FUNCTION);
//	}
////
////	@Override
////	public Set<String> getMandatoryProperties() {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
//}
