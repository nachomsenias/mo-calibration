package graphs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import script.FilterFronts;
import script.SelectTradeOffSolution;

public class GenerateSingleGraph {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException("This script requires two arguments: "
					+ "the folder containing the resulting Pareto fronts and the index of"
					+ "the desired solution.");
		}
		
		// /home/ignacio/codingmadafaka/jmetaltest/olimpo/final-mograms/25TP/WOMM/data/SPEA2/MWomABMProblem/
		String baseDir = args[0];
		int index = Integer.parseInt(args[1]);
		char separator = ' ';
		
		List<Item> items = new ArrayList<Item>();
		
		String funFile = baseDir + "FUN" + index + ".tsv";
		String varFile = baseDir + "VAR" + index + ".tsv";
		
		// Read CSV values
		CSVReader funReader = new CSVReader(new FileReader(new File(funFile)),
				separator);
		CSVReader varReader = new CSVReader(new FileReader(new File(varFile)),
				separator);

		List<Item> iterationItems = new ArrayList<Item>();
		iterationItems
				.addAll(FilterFronts.pairItems(funReader.readAll(), varReader.readAll()));

		items.addAll(FilterFronts.filterFront(iterationItems));

		funReader.close();
		varReader.close();
		
		// Compute SIM matrix
//		double[][] simMatrix = Similarity.computeMatrix(items);
//		
//		System.out.println("###########################");
//		System.out.println("Prune graph");
//		System.out.println("###########################");
//
//		double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix, simMatrix.length-1, 85);
//		
//		double[][] inverted = Similarity.convertPruned(pruned);

//		// Save weights
//		String targetFile = baseDir + "Graph"+index+".tsv";
//		CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), ' ',
//				CSVWriter.NO_QUOTE_CHARACTER);
//		csvw.writeAll(Similarity.weightsToCSV(inverted));
//		csvw.close();
//		
//		// Save edges
//		List<String[]> edges = Similarity.toEdges(inverted);
//		targetFile = baseDir + "edges"+index+".csv";
//		csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
//				CSVWriter.NO_QUOTE_CHARACTER);
//		csvw.writeAll(edges);
//		csvw.close();
		
		List<String[]> funs = Item.getFuns(items);
		
		// Objectives
		String targetFile = baseDir + "Objectives" + index + ".tsv";
		CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
				CSVWriter.NO_QUOTE_CHARACTER);
		csvw.writeAll(funs);
		csvw.close();
		
		//Compute selected
		int tradeOff = SelectTradeOffSolution.getTradeOffIndex(funs);
		
		List<Item> sampled = Item.sample(items, 100);
		
		List<String[]> sampledFuns = Item.getFuns(FilterFronts.filterFront(sampled));
		targetFile = baseDir + "Sampled" + index + ".tsv";
		csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
				CSVWriter.NO_QUOTE_CHARACTER);
		csvw.writeAll(sampledFuns);
		csvw.close();
		
		List<Item> selected = new ArrayList<Item>();
		selected.add(items.get(0));
		selected.add(items.get(tradeOff));
		selected.add(items.get(items.size()-1));
		
		targetFile = baseDir + "Selected" + index + ".tsv";
		Item.writeItems(selected, targetFile);
		
		tradeOff = SelectTradeOffSolution.getTradeOffIndex(sampledFuns);
		selected = new ArrayList<Item>();
		selected.add(items.get(0));
		selected.add(items.get(tradeOff));
		selected.add(items.get(items.size()-1));
		
		targetFile = baseDir + "Sampled-Selected" + index + ".tsv";
		Item.writeItems(selected, targetFile);
	}

}
