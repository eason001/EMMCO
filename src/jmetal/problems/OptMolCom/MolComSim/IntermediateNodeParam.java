/**
 * Parameters for a NanoMachine that has both a Transmitter and a Receiver
 */
package jmetal.problems.OptMolCom.MolComSim;
import java.util.ArrayList;
import java.util.Scanner;


public class IntermediateNodeParam extends NanoMachineParam {
	private Position infoMolReleasePoint;
	private Position ackMolReleasePoint;
	ArrayList<MoleculeParams> ackParams = new ArrayList<MoleculeParams>();
	ArrayList<MoleculeParams> infoParams = new ArrayList<MoleculeParams>();
	
	public IntermediateNodeParam(Position cntr, int r, Position infoPoint, Position ackPoint) {
		super(cntr, r, cntr);
		infoMolReleasePoint = infoPoint;
		ackMolReleasePoint = ackPoint;
	}
	
	public IntermediateNodeParam(Scanner readParams) {
		super(readParams);
		infoMolReleasePoint = super.getMolReleasePoint();
		if(readParams.hasNext()) {
			ackMolReleasePoint = new Position(readParams);
		} else {
			ackMolReleasePoint = new Position(super.getCenter().getX(), super.getCenter().getY(), super.getCenter().getZ());
		}
		
		infoParams.add(new MoleculeParams(readParams));
		ackParams.add(new MoleculeParams(readParams));
	}

	public Position getInfoMolReleasePoint() {
		return infoMolReleasePoint;
	}
	
	public Position getAckMolReleasePoint() {
		return ackMolReleasePoint;
	}
	
	public ArrayList<MoleculeParams> getAckParams(){
		return ackParams;
	}
	
	public ArrayList<MoleculeParams> getInfoParams(){
		return infoParams;
	}
	
	
}
