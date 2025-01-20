package script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import graphs.Item;

public class FilterSingleAlgorithm {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException(
					"This script requires the base directory. Example:"
							+"/home/ignacio/proyectos/abm4marketing/ziocommon/"
							+ "dummy_output/toy/result/WOMM/data/NSGAII/MWomABMProblem/"
					);
		}
		
		String baseDir = args[0];
		
		int iterations = 20;
		char separator = ' ';
		
		List<Item> idealItems = new ArrayList<Item>();
		
		for (int i = 0; i < iterations; i++) {
			//Items list
			List<Item> items = new ArrayList<Item>();
			
			// Fun files
			String funFile = baseDir + "FUN" + i + ".tsv";
			String varFile = baseDir + "VAR" + i + ".tsv";

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
			
			funFile = baseDir + "FUN" + i + "_front.tsv";
			varFile = baseDir + "VAR" + i + "_front.tsv";
			
			Item.writeFuns(items, funFile);
			Item.writeVars(items, varFile);
			
			idealItems.addAll(FilterFronts.filterFront(iterationItems));
		}
		
		String outPut = baseDir + "FUN"+iterations+".tsv";
		List<Item> filtered = FilterFronts.filterFront(idealItems);
		Item.writeFuns(filtered, outPut);
		
		outPut = baseDir + "VAR"+iterations+".tsv";
		Item.writeVars(filtered, outPut);
		
		outPut = baseDir + "Items.tsv";
		Item.writeItems(filtered, outPut);
	}
}
