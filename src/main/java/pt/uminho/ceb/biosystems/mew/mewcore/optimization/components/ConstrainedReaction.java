package pt.uminho.ceb.biosystems.mew.mewcore.optimization.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConstrainedReaction {
	
	private String reactionID;
	private double constraintValue;
	private String constraintSense; //'G'|'>' greater; 'E'|'=' equal; 'L'|'<' less
	
	private static final Map<String, String> senseMap;
    static {
        Map<String, String> senseM = new HashMap<String, String>();
        senseM.put(">", "G");
        senseM.put("=", "E");
        senseM.put("<", "L");
        senseMap = Collections.unmodifiableMap(senseM);
    }
	
	public ConstrainedReaction(String reactionID, double constraintValue, String constraintSense) {
		this.reactionID = reactionID;
		this.constraintValue = constraintValue;
		this.constraintSense = constraintSense;
	}
	
	public String getReactionID() {
		return reactionID;
	}
	
	public double getConstraintValue() {
		return constraintValue;
	}
	
	public String getConstraintSense() {
		return constraintSense;
	}
	
	public static ConstrainedReaction parseConstrainedReaction(String data){
		String[] constrainedReaction = data.split(" ");
		if(constrainedReaction.length != 3)
			return null;
		
		return new ConstrainedReaction(constrainedReaction[0], Double.parseDouble(constrainedReaction[2]), constrainedReaction[1]);
	}
	
	public static String parseConstraintSense(ConstrainedReaction constrainedReaction){
		System.out.println(constrainedReaction.getConstraintSense());
		System.out.println(senseMap.size());
		return senseMap.get(constrainedReaction.getConstraintSense());
	}

}
