//package MComSim.MoleculeMovementType;
package jmetal.problems.OptMolCom.MolComSim;
public enum MoleculeMovementType {

	ACTIVE,
	PASSIVE,
	NONE;
	
	public static MoleculeMovementType getMovementType(String stringRep) {
		if(stringRep.equals("ACTIVE")) {
			return ACTIVE;
		} else if(stringRep.equals("PASSIVE")) {
			return PASSIVE;
		} else if(stringRep.equals("NONE")) {
			return NONE;
		} else {
			throw new IllegalArgumentException("Invalid argument: " + stringRep + 
					" to MoleculeMovementType.getMovementType");
		}
	}
}
 