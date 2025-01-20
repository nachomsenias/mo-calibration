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

public class UnfilteredGraph {

	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException("This script requires two arguments: "
					+ "the folder containing the resulting Pareto fronts and the index of"
					+ "the desired solution.");
		}
		
		// /home/ignacio/codingmadafaka/jmetaltest/olimpo/final-mograms/25TP/WOMM/data/SPEA2/MWomABMProblem/
		String baseDir = args[0];
		String rvalue = args[1];
		
		// Indexes are now included as a file. each row should be a number identifying the execution id.
		String indexFile = args[0]+"index.dat";
					
		try {
			CSVReader reader = new CSVReader(new FileReader(new File(indexFile)),
					' ');
			List<String[]> indexes = reader.readAll();
			reader.close();
			
			for (String[] line : indexes) {
				int index = Integer.valueOf(line[0]);
				run(baseDir, index, Integer.valueOf(rvalue));
			}
		} catch (Exception e) {
			//Do something.
			e.printStackTrace();
		}	
	}
	
	private static void run(String baseDir, int index, int rvalue) throws IOException {
		// XXX MM MO results use \t, but old-fashion mo-calibration results uses ' '
				char separator = '	';
				
				String funFile = baseDir + "FUN" + index + ".dat";
				String varFile = baseDir + "VAR" + index + ".dat";
				
				// Read CSV values
				CSVReader funReader = new CSVReader(new FileReader(new File(funFile)),
						separator);
				CSVReader varReader = new CSVReader(new FileReader(new File(varFile)),
						separator);

				List<Item> iterationItems = new ArrayList<Item>();
				iterationItems
						.addAll(FilterFronts.pairItems(funReader.readAll(), varReader.readAll()));

				// XXX Because we are using MM MO, the Frontier is not filtered.
				// items.addAll(FilterFronts.filterFront(iterationItems));

				funReader.close();
				varReader.close();
				
				// Compute SIM matrix
				double[][] simMatrix = Similarity.computeMatrix(Double.MAX_VALUE, iterationItems);
				
				System.out.println("###########################");
				System.out.println("Prune graph");
				System.out.println("###########################");

//				int r = 50;
//				int r = 100;
				double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix, simMatrix.length-1, rvalue);
				
				// XXX This is required if similarity is computed as simple euclidean distance.
				double[][] inverted = Similarity.convertPruned(pruned);

				// Save weights
				String targetFile = baseDir + "Graph"+index+".tsv";
				CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), ' ',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(Similarity.weightsToCSV(inverted));
				csvw.close();
				
				// Save edges
				List<String[]> edges = Similarity.toEdges(inverted);
				targetFile = baseDir + "edges"+index+".csv";
				csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(edges);
				csvw.close();
				
				List<String[]> funs = Item.getFuns(iterationItems);
				
				// Objectives
				targetFile = baseDir + "Objectives" + index + ".tsv";
				csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(funs);
				csvw.close();
				
				//Compute selected
				int tradeOff = SelectTradeOffSolution.getTradeOffIndex(funs);
				
				List<Item> sampled = Item.sample(iterationItems, 100);
				
				List<String[]> sampledFuns = Item.getFuns(FilterFronts.filterFront(sampled));
				targetFile = baseDir + "Sampled" + index + ".tsv";
				csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(sampledFuns);
				csvw.close();
				
				List<Item> selected = new ArrayList<Item>();
				selected.add(iterationItems.get(0));
				selected.add(iterationItems.get(tradeOff));
				selected.add(iterationItems.get(iterationItems.size()-1));
				
				targetFile = baseDir + "Selected" + index + ".tsv";
				Item.writeItems(selected, targetFile);
				
				tradeOff = SelectTradeOffSolution.getTradeOffIndex(sampledFuns);
				selected = new ArrayList<Item>();
				selected.add(iterationItems.get(0));
				selected.add(iterationItems.get(tradeOff));
				selected.add(iterationItems.get(iterationItems.size()-1));
				
				targetFile = baseDir + "Sampled-Selected" + index + ".tsv";
				Item.writeItems(selected, targetFile);
	}

}
