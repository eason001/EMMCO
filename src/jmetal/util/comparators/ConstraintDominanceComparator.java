package jmetal.util.comparators;

/**
 * ConstraintDominanceComparator.java
 * 
 * @author Dung Phan
 */
import java.util.*;

import jmetal.core.Solution;
/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on the number of violated constraints.
 */
public class ConstraintDominanceComparator implements Comparator{
    
 /**
  * Compares two solutions.
  * @param o1 Object representing the first <code>Solution</code>.
  * @param o2 Object representing the second <code>Solution</code>.
  * @return -1, or 0, or 1 2 if o1 is less than, equal, greater than o2, or both of them are feasible
  * respectively.
  */
  public int compare(Object o1, Object o2) {
    Solution solution1 = (Solution) o1;
    Solution solution2 = (Solution) o2;
   // System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX 2:"+solution2.numberOfConstraints());
  //  System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX 1:"+solution1.numberOfConstraints());
    
   // if(solution2.numberOfConstraints()!=2) System.exit(0);
    
    int dominate1 ; // dominate1 indicates if some objective of solution1 
    // dominates the same objective in solution2. dominate2
	int dominate2 ; // is the complementary of dominate1.
	int flag;
	
	
	dominate1 = 0 ; 
	dominate2 = 0 ;
	boolean feasible=true;
	  double value1, value2;
	    for (int i = 0; i < solution1.numberOfConstraints(); i++) {
	      value1 = solution1.getConstraint(i);
	      value2 = solution2.getConstraint(i);
	      if(value1!=0||value2!=0) feasible=false;
	      if (value1 < value2) {
	        flag = -1;
	      } else if (value1 > value2) {
	        flag = 1;
	      } else {
	        flag = 0;
	      }
	      
	      if (flag == -1) {
	        dominate1 = 1;
	      }
	      
	      if (flag == 1) {
	        dominate2 = 1;           
	      }
	    }
	            
	    if (dominate1 == dominate2) {
	      if(feasible==false)
	    	  return 0; //No one dominate the other
	      else return 2;// both of them are feasible
	    }
	    if (dominate1 == 1) {
	      return -1; // solution1 constraint dominate
	    }
	    
	    
	    return 1;    // solution2 constraint dominate 
                     
  } // compare 
} // ViolatedConstraintComparator
