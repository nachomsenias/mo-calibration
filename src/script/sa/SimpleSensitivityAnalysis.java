package script.sa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import graphs.Item;
import problem.WomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class SimpleSensitivityAnalysis {

	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		//Output folder
		String folder = args[1];
		
		String config = CSVFileUtils.readFile(jsonFile);
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		//Compute Hold-out error
		calibrationConfig.setHoldOut(0.25);
		
		WomABMProblem problem = new WomABMProblem(calibrationConfig);
		
		
		List<DefaultDoubleSolution> solutions = new ArrayList<DefaultDoubleSolution>();	
		
		//Solution values 0.000	0.000 0.000	0.047	0.000	0.000	0.000
		double[] values = {0.0, 0.0, 0.0, 0.047, 0.0, 0.0, 0.0};
		DefaultDoubleSolution solution = createSolution(problem, values);
		problem.evaluate(solution);
		solutions.add(solution);
		
		double inc = 0.001;
		
		do {
			values[3]-=inc;
			solution = createSolution(problem, values);
			problem.evaluate(solution);
			solutions.add(solution);
		}
		while(values[3]>=inc);

		List<Item> items = createItems(solutions);
		
		String output = folder+"individuals.tsv"; 
		Item.writeItems(items, output);

	}
	
	private static DefaultDoubleSolution createSolution(
			WomABMProblem problem, double[] params) {
		DefaultDoubleSolution solution = new DefaultDoubleSolution(problem);
		
		for (int v = 0; v<solution.getNumberOfVariables(); v++) {
			solution.setVariableValue(v, params[v]);
		}
		return solution;
	}
	
	public static List<Item> createItems(List<DefaultDoubleSolution> solutions) {
		List<Item> items = new ArrayList<Item>();
		
		for (DefaultDoubleSolution solution:solutions) {
			double[] objs = solution.getObjectives();
			String[] funs = new String[objs.length];
			for (int o=0; o<objs.length; o++) {
				funs[o] = String.valueOf(objs[o]);
			}
			List<Double> values = solution.getVariables();			
			int params = solution.getNumberOfVariables();
			String[] vars = new String[params];
			
			for (int v = 0; v<params; v++) {
				vars[v] = String.valueOf(values.get(v));
			}
			
			items.add(new Item(funs,vars));
		}
		
		return items;
	}

}
