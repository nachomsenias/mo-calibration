package graphs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import script.FilterFronts;

public class GraphGenerator {
	
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			throw new IllegalArgumentException(
					"This script requires the base directory. Example:"
							+"/home/ignacio/codingmadafaka/jmetaltest/olimpo/fight/data/"
					);
		}
		
		String baseDir = args[0];

		String problemName = "/MWomABMProblem";
		
		String[] instances = {  
				"0TP", "5TP", "7TP", "10TP", "12TP","15TP",
				"17TP", "20TP", "22TP", "25TP", "30TP", "35TP", 
				"40TP", "45TP", "50TP"
				};

		for (String instance : instances) {
			String file = baseDir + instance + "/WOMM/data/";
			execute(file, problemName);
		}
	}
	
	public static void execute(String baseDir, String problemName) throws IOException {
//		String[] methods = {  
//				"SMSEMOA", "SPEA2", "SPEA2-BLX", "NSGAII-BLX", "NSGAII",
//				"MOEAD", "PESA2", "IBEA", "mIBEA", "GWASFGA", "MOMBI2",
//				};
		String[] methods = {  
				"SMSEMOA", "SPEA2-BLX", "NSGAII-BLX", 
				"MOEAD", "IBEA", "GWASFGA", "MOMBI2",
				"NELDER-MEAD"
				};
		
		int iterations = 30;
		char separator = ' ';
		
		//Initialize the antiideal point at the ideal values.
		String[] antiideal = {"0.0", "0.0"}; 

		for (String m : methods) {
			
			System.out.println("Current method: "+m);
			
			List<Item> items = new ArrayList<Item>();

			for (int i = 0; i < iterations; i++) {
				// Fun files
				String funFile = baseDir + m + problemName + "/FUN" + i + ".tsv";
				String varFile = baseDir + m + problemName + "/VAR" + i + ".tsv";

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
			}

			// Write the parameters of all the solutions of the not filtered
			// fronts
			String outPutParams = baseDir + "PARAMS_" + m + ".tsv";
			Item.writeVars(items, outPutParams);

			// Write all the items
			String outPutFull = baseDir + "ITEMS_" + m + "_FULL.tsv";
			Item.writeItems(items, outPutFull);
					
			// XXX Could the nadir point be update here?
			
			// Generate front and save
			List<Item> paretoFront = FilterFronts.filterFront(items);
			
			//Update nadir
			FilterFronts.updateAntiIdeal(paretoFront,antiideal);


			String outPut = baseDir + "ITEMS_" + m + ".tsv";
			Item.writeItems(paretoFront, outPut);

			// Compute SIM matrix
			double[][] simMatrix = Similarity.computeMatrix(Double.MAX_VALUE, paretoFront);
			
			//### XXX Test Similarities
//			String[][] similaritiesMatrix = Similarity.computeSimilarities(paretoFront);
//			String similaritiesFile = baseDir + m + problemName + "/similarities.txt";
//			CSVWriter simCsvw = new CSVWriter(new FileWriter(new File(similaritiesFile)), ' ',
//					CSVWriter.NO_QUOTE_CHARACTER);
//			simCsvw.writeAll(Similarity.matrixToList(similaritiesMatrix));
//			simCsvw.close();
			//### XXX End - Test Similarities
			
			System.out.println("###########################");
			System.out.println("Prune graph");
			System.out.println("###########################");

			//r = simMatrix.length? 25
			//85 va bien para el problema de 25TP
			double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix, simMatrix.length-1, 85);

//			double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix, simMatrix.length, Integer.MAX_VALUE);
			
			double[][] inverted = Similarity.convertPruned(pruned);

			// Save weights
			String targetFile = baseDir + m + problemName + "/Graph.tsv";
			CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), ' ',
					CSVWriter.NO_QUOTE_CHARACTER);
			csvw.writeAll(Similarity.weightsToCSV(inverted));
			csvw.close();
			System.out.println("Generated: "+targetFile);

			// Display
//			for (double[] rows : inverted) {
//				System.out.println(Arrays.toString(rows));
//			}
//			System.out.println("###########################");
			
			List<String[]> edges = Similarity.toEdges(inverted);

			// Save edges
			targetFile = baseDir + m + problemName + "/edges.csv";
			csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
					CSVWriter.NO_QUOTE_CHARACTER);
			csvw.writeAll(edges);
			csvw.close();			
			
			// Objectives
			targetFile = baseDir + m + problemName + "/Objectives.tsv";
			csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
					CSVWriter.NO_QUOTE_CHARACTER);
			csvw.writeAll(Item.getFuns(paretoFront));
			csvw.close();
		}
		
		//Save antiideal
		String outPut = baseDir+"AntiIdealPoint.csv";
		FileWriter fw = new FileWriter(new File(outPut));
		fw.write(Arrays.toString(antiideal));
		fw.close();
	}
	
	
}
