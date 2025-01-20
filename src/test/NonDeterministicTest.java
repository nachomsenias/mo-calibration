package test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class NonDeterministicTest {

	private String config = "instances/0TP/fast/input_aw_wom_fast.json";
	private MWomABMProblem problem;
	
	private DefaultIntegerDoubleSolution solution;
	
	private final int iterations = 100; 
	
	public NonDeterministicTest() {
		try {
			String content = CSVFileUtils.readFile(config);
			
			Gson gson = new Gson();
			
			//Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(content, CalibrationConfig.class);

			problem = new MWomABMProblem(calibrationConfig);
			
			solution = new DefaultIntegerDoubleSolution(problem);
			
		} catch (CalibrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String createKey(DefaultIntegerDoubleSolution solution) {
		String key = String.valueOf(solution.getObjective(0))
				+" "+String.valueOf(solution.getObjective(1));
		
		return key;
	}
	
	@Test
	public void randomTest() {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for (int it=0; it<iterations; it++) {
			problem.evaluate(solution);
					
			String key = createKey(solution);
			if(!map.containsKey(key)) {
				map.put(key, it);
			} else {
				System.out.println("Fail! Solution already simulated in iteration "+map.get(key));
				fail();
			}
		}
	}
}
