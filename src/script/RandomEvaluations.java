package script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.util.ExecuteCalibration.Options;

import graphs.Item;
import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.random.RandomizerUtils;

public class RandomEvaluations {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println(
					"Usage: input_json result_folder "
							+ "{ --start=seedIndex --evaluations=mcIterations }");
			System.exit(1);
		}
		try {
			String config = CSVFileUtils.readFile(args[0]);
			String nonDominatedOutput = args[1]+"/non-dominated.csv";
			
			// Options
			Options customOptions = Options.parseOptions(args);
		
			int maxEvaluations = 10000;
			if (customOptions.evaluations > 0) {
				maxEvaluations = customOptions.evaluations;
			}
			
			int seedIndex = customOptions.repeatedIterationsStart;
			
			Gson gson = new Gson();
			
			//Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
	
//			SimpleABMProblem problem = new SimpleABMProblem(calibrationConfig);
//			WomABMProblem problem = new WomABMProblem(calibrationConfig);
			MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
			
			int iteration = 0;
			
			List<Item> items = new ArrayList<>();
			List<DefaultIntegerDoubleSolution> allTheSolutions = new ArrayList<DefaultIntegerDoubleSolution>();
			
			List<DefaultIntegerDoubleSolution> nonDominated = new ArrayList<DefaultIntegerDoubleSolution>();
			
			JMetalRandom.getInstance().setSeed(RandomizerUtils.PRIME_SEEDS[seedIndex]);
			
			while (iteration<maxEvaluations) {
				
//				DefaultDoubleSolution solution = new DefaultDoubleSolution(problem);
				DefaultIntegerDoubleSolution solution = new DefaultIntegerDoubleSolution(problem);
				problem.evaluate(solution);
				
				allTheSolutions.add(solution);
				nonDominated.add(solution);
				
				Item item = createItem(solution);
				items.add(item);
				
				if(iteration % 500 ==0) {
					System.out.println("Current number of evaluations: "+iteration);
					// Each 500 evaluations we clean the nonDominated by removing new dominated solutions.
					Ranking<DefaultIntegerDoubleSolution> ranking = 
							new DominanceRanking<DefaultIntegerDoubleSolution>();
					nonDominated = 
							ranking.computeRanking(nonDominated).getSubfront(0);
				}
				iteration++;
			}
			
			System.out.println("Reached "+iteration+" evaluations.");
			// Remove any dominated solution from the last iterations. 
			Ranking<DefaultIntegerDoubleSolution> ranking = 
					new DominanceRanking<DefaultIntegerDoubleSolution>();
			nonDominated = 
					ranking.computeRanking(nonDominated).getSubfront(0);
			
			// Write all the solutions
			String fullSolutions = args[1]+"/full-solutions.csv";
			Item.writeItems(items, fullSolutions);
			
									
			//Reset the set
			items = new ArrayList<>();
			for (DefaultIntegerDoubleSolution solution : nonDominated) {
				Item item = createItem(solution);
				items.add(item);
			}
			
			// Write non-dominated
			Item.writeItems(items, nonDominatedOutput);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (CalibrationException e) {
			e.printStackTrace();
		}
	}
	
//	private static Item createItem(DefaultDoubleSolution solution) {
	private static Item createItem(DefaultIntegerDoubleSolution solution) {
		
		int objectives = solution.getNumberOfObjectives();
		int params = solution.getNumberOfVariables();
		
		String[] objs = new String[objectives];
		for (int i=0; i<objectives; i++) {
			objs[i] = String.valueOf(solution.getObjective(i));
		}
		
		String[] variables = new String[params];
		for (int i=0; i<params; i++) {
			variables[i] = String.valueOf(solution.getVariableValue(i));
		}
		
		return new Item(objs, variables);
	}
}
