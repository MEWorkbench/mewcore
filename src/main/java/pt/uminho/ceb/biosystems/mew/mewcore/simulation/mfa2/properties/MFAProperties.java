package pt.uminho.ceb.biosystems.mew.mewcore.simulation.mfa2.properties;

import pt.uminho.ceb.biosystems.mew.mewcore.simulation.components.SimulationProperties;

public class MFAProperties extends SimulationProperties{
	
	public final static String MEASURED_FLUXES = "measuredFluxes";
	public final static String FLUX_RATIO_CONSTRAINTS = "fluxRatioConstraints";
	
	
	public final static String MFA_APPROACH = "mfaApproach";

	public final static String MFA_DETERMINED = "mfaDetermined";
	public final static String MFA_LSQ = "mfaLsq";
	public final static String MFA_WLSQ = "mfaWlsq";
	public final static String MFA_CLASSIC_ALGEBRA = "mfaAlgebra";
	
	public final static String MFA_NULLSPACE = "mfaNullSpace";
	
	public final static String MFA_LP = "mfaLP";
	public final static String MFA_QP = "mfaQP";
	public final static String MFA_PARSIMONIOUS = "mfaParsimonious";
	
	public final static String MFA_TIGHTBOUMDS = "mfaTightBounds";
	public final static String MFA_FVA = "mfaFVA";
	
	public final static String MFA_ROBUSTNESSANALYSIS = "mfaRobustness";
	
	
	public final static String NULLSPACE_COMPUTESENTITIVITY = "computeSensitivity";
	public final static String NULLSPACE_CALCULATEALTERNATIVEFLUXES = "calculateAlternativeFluxes";
	
	
	public final static String FVA_FIXED_FLUX_PROBLEM = "fvaFixedFluxProblem";
	public final static String FVA_FIXED_FLUX_VALUE = "fvaFixedFluxValue";
	public final static String FVA_MIN_PERCENTAGE = "fvaMinimumPercentage";
	public final static String FVA_MIN_PERCENTAGE_FLUX = "fvaMinimumPercentageFlux";
	public final static String FVA_MIN_PERCENTAGE_FLUX_VALUE = "fvaMinimumPercentageFluxValue";
	
	
	public final static String MFA_ALGEBRA_FITTING = "mfaAlgebraFitting";
	public final static String MFA_ALGEBRA_LSQ_FITTING = "mfaAlgebraLSQfitting";
	public final static String MFA_ALGEBRA_WLSQ_FITTING = "mfaAlgebraWLSQfitting";
	
	public final static String MFA_ALGEBRA_WLSQ_ALPHA = "mfaAlgebraWlsqAlpha";
	
	
	public final static String SYSTEM_TYPE = "systemType";
	
	public final static String UNDERDETERMINED_SYSTEM = "Underdetermined System";
	public final static String DETERMINED_SYSTEM = "Determined System";
	public final static String OVERDETERMINED_SYSTEM = "Overdetermined System";

	
	public final static String ROBUSTNESS_SELECTED_FLUXES = "robustSelctdfluxes";
	public final static String ROBUSTNESS_SELECTED_FLUXES_INITVALUES = "robustSelctInitValues";
	public final static String ROBUSTNESS_OBJECTIVE_PROBLEM = "robustObjectiveProblem";
	public final static String ROBUSTNESS_OBJECTIVE_FLUX = "robustObjectiveFlux";
	public final static String ROBUSTNESS_PERCENTAGE_INTERVAL = "robustPercent";
	public final static String ROBUSTNESS_WT_OBJECTIVE_VALUE = "robustWtOfValue";
	
	
	public final static int MEASUREMENTS_DECIMAL_PLACES = 3;

}
