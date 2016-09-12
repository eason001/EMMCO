package jmetal.problems.BodyCloudSensor;

import java.util.Random;

public class request {
	
	private int id;
	private int request_time;
	private int request_sensor;
	private int iniT=0;
	private int endT;
	
	public request(int i){
		id = i;
		request_time = new Random().nextInt(BodyCloudOptimization.total_time);
		request_sensor = new Random().nextInt(BodyCloudOptimization.n_shimmer * BodyCloudOptimization.n_node);
		if(request_time ==0){
			request_time=1;
		}
		switch (request_sensor%4) {
		case 0:
			endT = new Random().nextInt(request_time);
			if (endT > BodyCloudOptimization.max_winPressure){ 
			iniT = new Random().nextInt(BodyCloudOptimization.max_winPressure) + (endT - BodyCloudOptimization.max_winPressure);	
			}else if (endT > 0){
			iniT = new Random().nextInt(endT);
			}
			break;
		case 1:
			endT = new Random().nextInt(request_time);
			if (endT > BodyCloudOptimization.max_winAccel){ 
			iniT = new Random().nextInt(BodyCloudOptimization.max_winAccel) + (endT - BodyCloudOptimization.max_winAccel);	
			}else if (endT > 0){
			iniT = new Random().nextInt(endT);
			}
			break;
		case 2:
			endT = new Random().nextInt(request_time);
			if (endT > BodyCloudOptimization.max_winAccel){ 
			iniT = new Random().nextInt(BodyCloudOptimization.max_winAccel) + (endT - BodyCloudOptimization.max_winAccel);	
			}else if (endT > 0){
			iniT = new Random().nextInt(endT);
			}
			break;
		case 3:
			endT = new Random().nextInt(request_time);
			if (endT > BodyCloudOptimization.max_winECG){ 
			iniT = new Random().nextInt(BodyCloudOptimization.max_winECG) + (endT - BodyCloudOptimization.max_winECG);	
			}else if (endT > 0){
			iniT = new Random().nextInt(endT);
			}
			break;	
		default: 
			endT = new Random().nextInt(request_time);
			if (endT > BodyCloudOptimization.max_winECG){ 
			iniT = new Random().nextInt(BodyCloudOptimization.max_winECG) + (endT - BodyCloudOptimization.max_winECG);	
			}else if (endT > 0){
			iniT = new Random().nextInt(endT);
			}
			break;
		}
		
	}
	
	public int getID(){
		return id;
	}
	
	public int getRT(){
		return request_time;
	}
	
	public int getRS(){
		return request_sensor;
	}
	
	public int getIniT(){
		return iniT;
	}
	
	public int getEndT(){
		return endT;
	}
	
}
