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

public class RunSimilarity {

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("This script requires one argument: "
					+ "the folder containing the resulting Pareto fronts.");
		}
		
		// /home/ignacio/codingmadafaka/jmetaltest/olimpo/final-mograms/25TP/WOMM/data/SPEA2/MWomABMProblem/
		String baseDir = args[0];
		
		// Indexes are now included as a file. each row should be a number identifying the execution id.
		String indexFile = args[0]+"index.dat";
					
		try {
			CSVReader reader = new CSVReader(new FileReader(new File(indexFile)),
					' ');
			List<String[]> indexes = reader.readAll();
			reader.close();
			
			for (String[] line : indexes) {
				int index = Integer.valueOf(line[0]);
				run(baseDir, index);
			}
		} catch (Exception e) {
			//Do something.
			e.printStackTrace();
		}	
	}
	
	private static void run(String baseDir, int index) throws IOException {
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
				
				final double nullvalue = 0;
				
				// Compute SIM matrix
				double[][] trueSimMatrix = Similarity.computeMatrix(nullvalue,iterationItems);
				
				// Generate Pajek format
				String targetFile = baseDir + "Matrix"+index+".tsv";
				CSVWriter csvw = new CSVWriter(new FileWriter(new File(targetFile)), ' ',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(Similarity.weightsToCSV(trueSimMatrix));
				csvw.close();
				
				targetFile = baseDir + "Pajek"+index+".tsv";
				List<String> content = Similarity.toPajek(trueSimMatrix);
				BufferedWriter bfw = new BufferedWriter(new FileWriter(targetFile));
				for (String line : content) {
					bfw.write(line);
					bfw.write("\n");
				}
				bfw.close();
				
				List<String[]> funs = Item.getFuns(iterationItems);
				
				// Objectives
				targetFile = baseDir + "Objectives" + index + ".tsv";
				csvw = new CSVWriter(new FileWriter(new File(targetFile)), '	',
						CSVWriter.NO_QUOTE_CHARACTER);
				csvw.writeAll(funs);
				csvw.close();
				
	}
}
