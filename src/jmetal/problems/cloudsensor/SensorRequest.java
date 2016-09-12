//SensorRequest.java
//  Authors: Dung Phan
//  <phdung@cs.ubm.edu>

package jmetal.problems.cloudsensor;

public class SensorRequest {
	private int id_;
	private int sensor_id_;
	private int sensor_type_;
	private double d_;
	private double t_;
	private double h_;
	
	//Temporary variable, for calculation only
	public int data_yield_=1;
	public int atLevel_=0;//0: cloud, 1: edge, 2: sensor- data_yield=1 in this case 
	public SensorRequest(int id, int sensor_id, int sensor_type, int hop,double t, double d){
		id_=id;
		sensor_id_=sensor_id;
		sensor_type_=sensor_type;
		t_=t;
		d_=d;
		h_=hop;
	}
	public int getID(){
		return id_;
	}
	public int getSensorID(){
		return sensor_id_;
	}
	public int getSensorType(){
		return sensor_type_;
	}
	public double getRequestTime()
	{
		return t_;
	}
	public double getRequestTimeWindow(){
		return d_;
	}
	public double getHopCount()
	{
		return h_;
	}
}
