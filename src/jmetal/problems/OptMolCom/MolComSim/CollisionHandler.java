/**
 * Processes collisions based on what type of molecules
 * are colliding and the needs of the particular
 * simulation
 *
 */
package jmetal.problems.OptMolCom.MolComSim;
public interface CollisionHandler {

	public Position handlePotentialCollisions(Molecule mol, Position nextPosition, MolComSim simulation);

} 
