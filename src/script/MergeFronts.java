package script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zioabm.beans.CalibrationConfig;

import graphs.Item;
import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class MergeFronts {

	public static void main(String[] args) throws IOException, CalibrationException, JsonSyntaxException {
		if (args.length < 4) {
			System.out.println(
					"Usage: problem.json first_items.csv second_items.csv resulting_items.csv.");
			System.exit(1);
		}
		
		String config = CSVFileUtils.readFile(args[0]);
		Gson gson = new Gson();
		
		//Read configuration from JSON
		CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
		MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
		
		List<Item> firstItems = Item.getItemsFromFile(args[1]);
		List<Item> secondItems = Item.getItemsFromFile(args[2]);
		
		List<Item> combined = new ArrayList<Item>();
		combined.addAll(firstItems);
		combined.addAll(secondItems);
		
		List<IntegerDoubleSolution> sols = new ArrayList<IntegerDoubleSolution>();
		for (Item item : combined) {
			IntegerDoubleSolution solution = item.getSolution(problem.createSolution());
			sols.add(solution);
		}
		
		//Get non dominated
		Ranking<IntegerDoubleSolution> ranking = 
				new DominanceRanking<IntegerDoubleSolution>();
		List<IntegerDoubleSolution> nonDominated = 
				ranking.computeRanking(sols).getSubfront(0);
		
		List<Item> resulting = new ArrayList<Item>();
		for (IntegerDoubleSolution solution : nonDominated) {
			Item item = Item.createItem(solution);
			resulting.add(item);
		}
		
		String nonDominatedOutput = args[3];
		// Write non-dominated
		Item.writeItems(resulting, nonDominatedOutput);
	}

}
