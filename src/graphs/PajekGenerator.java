package graphs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import script.FilterFronts;

public class PajekGenerator {

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
				"MOEAD", "IBEA", "GWASFGA", "MOMBI2" //"PESA2"
				};
		
		int iterations = 30;
		char separator = ' ';

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

			
			// Generate front and save
			List<Item> paretoFront = FilterFronts.filterFront(items);

			final double nullvalue = 0;
			
			// Compute SIM matrix
			double[][] trueSimMatrix = Similarity.computeMatrix(nullvalue, paretoFront);
			
			// Generate Pajek format
			String targetFile = baseDir + m + problemName +"/Matrix.tsv";
			CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), ' ',
					CSVWriter.NO_QUOTE_CHARACTER);
			csvw.writeAll(Similarity.weightsToCSV(trueSimMatrix));
			csvw.close();
			
			targetFile = baseDir + m + problemName +"/Pajek.tsv";
			List<String> content = Similarity.toPajek(trueSimMatrix);
			BufferedWriter bfw = new BufferedWriter(new FileWriter(targetFile));
			for (String line : content) {
				bfw.write(line);
				bfw.write("\n");
			}
			bfw.close();
			
			List<String[]> funs = Item.getFuns(paretoFront);
			
			// Objectives
			targetFile = baseDir + m + problemName +"/Objectives.tsv";
			csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
					CSVWriter.NO_QUOTE_CHARACTER);
			csvw.writeAll(funs);
			csvw.close();
		}
		
	}
	
}
