package pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods;

import java.util.ArrayList;
import java.util.List;

import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFAProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.properties.MFASystemType;


public enum MFAApproaches {
	
	classicAlgebra{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.determined);
			systems.add(MFASystemType.overdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_CLASSIC_ALGEBRA;
		}
		
		@Override
		public String toString() {
			return "Algebraic Method";
		}
	},
	
	algebraDetermined {		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_DETERMINED;
		}
	},
	
	algebraLSQ {		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_LSQ;
		}
	},
	
	algebraWLSQ {		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_WLSQ;
		}
	},
	
	linearProgramming{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_LP;
		}
		
		@Override
		public String toString() {
			return "Flux Balance Analysis";
		}
	},
	
	parsimonious{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_PARSIMONIOUS;
		}
		
		@Override
		public String toString() {
			return "Parsimonious Flux Balance Analysis";
		}
	},
	
	tightBounds{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_TIGHTBOUMDS;
		}
		
		@Override
		public String toString() {
			return "Tight Bounds";
		}
	},
	
	fva{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_FVA;
		}
		
		@Override
		public String toString() {
			return "Flux Variability Analysis";
		}
	},
	
	quadraticProgramming{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			systems.add(MFASystemType.determined);
			systems.add(MFASystemType.overdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_QP;
		}
		
		@Override
		public String toString() {
			return "Quadratic Programming";
		}
	},
	
	nullSpace{
		@Override
		public List<MFASystemType> solvingSystems(){
			List<MFASystemType> systems = new ArrayList<MFASystemType>();
			systems.add(MFASystemType.underdetermined);
			systems.add(MFASystemType.determined);
			systems.add(MFASystemType.overdetermined);
			return systems;
		}
		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_NULLSPACE;
		}
		
		@Override
		public String toString() {
			return "Null Space";
		}
	},
	
	robustnessAnalysis{		
		@Override
		public String getPropertyDescriptor(){
			return MFAProperties.MFA_ROBUSTNESSANALYSIS;
		}
		
		@Override
		public String toString() {
			return "Robustness Analysis";
		}
	};
	
	
	public List<MFASystemType> solvingSystems(){
		return new ArrayList<MFASystemType>();
	}
	
	public String getPropertyDescriptor(){
		return null;
	}

}
