package script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import graphs.Item;

public class FilterFront {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"usage: input.csv output.csv"
					);
		}
		
		char separator = ' ';

		String input = args[0];
		String output = args[1];
		
		// Read CSV values
		CSVReader funReader = 
				new CSVReader(new FileReader(new File(input)), separator);
		
		List<Item> iterationItems = new ArrayList<Item>();
		iterationItems.addAll(
				FilterFronts.pairItems(funReader.readAll()));
		funReader.close();
		
		List<Item> filtered = FilterFronts.filterFront(iterationItems);
		Item.writeFuns(filtered, output);
	}

}
