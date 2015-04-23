package pt.uminho.ceb.biosystems.mew.mewcore.cmd.clustertools;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.mewcore.simulation.formulations.turnover.TurnOverProperties;

public enum SimulationMethodsEnum {
	
	FBA{
		public String getSimulationProperty() {
			return SimulationProperties.FBA;
		}
	},
	PFBA{		
		public String getSimulationProperty() {
			return SimulationProperties.PARSIMONIUS;
		}
	},
	MOMA{
		public String getSimulationProperty() {
			return SimulationProperties.MOMA;
		}		
	},
	LMOMA{
		public String getSimulationProperty() {			
			return SimulationProperties.LMOMA;
		}		
	},
	NLMOMA{
		public String getSimulationProperty() {
			return SimulationProperties.NORM_LMOMA;
		}
	},
	ROOM{
		public String getSimulationProperty() {		
			return SimulationProperties.ROOM;
		}
	},
	MIMBL{		
		public String getSimulationProperty() {
			return TurnOverProperties.MIMBL;
		}		
	};
	
	public abstract String getSimulationProperty();
	
	public static String getFromString(String method){
		return SimulationMethodsEnum.valueOf(method.toUpperCase()).getSimulationProperty();
	}

}
