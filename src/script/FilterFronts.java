package script;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;

import graphs.Item;

public class FilterFronts {

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
				"0TP", "5TP", "7TP", "10TP", "12TP", "15TP",
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
//				"SMSEMOA", "mIBEA", "SPEA2", "SPEA2-BLX", "NSGAII", "NSGAII-BLX",
//				"MOEAD", "PESA2", "IBEA", "GWASFGA", "MOMBI2"
//				}; 
		String[] methods = {  
				"SMSEMOA", "SPEA2-BLX", "NSGAII-BLX", 
				"MOEAD", "IBEA", "GWASFGA", "MOMBI2",
				"NELDER-MEAD",
				};
		
		int iterations = 30;
		char separator = ' ';
		
		//Initialize the antiideal point at the ideal values.
		String[] antiideal = {"0.0", "0.0"}; 

		List<Item> idealItems = new ArrayList<Item>();
		
		for (String m : methods) {
			
			System.out.println("Current method: "+m);
			
			List<Item> surfaceItems = new ArrayList<Item>();

			for (int i = 0; i < iterations; i++) {
				//Items list
				List<Item> items = new ArrayList<Item>();
				
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
						.addAll(pairItems(funReader.readAll(), varReader.readAll()));

				items.addAll(filterFront(iterationItems));

				funReader.close();
				varReader.close();
				
				funFile = baseDir + m + problemName + "/FUN" + i + "_front.tsv";
				varFile = baseDir + m + problemName + "/VAR" + i + "_front.tsv";
				
				Item.writeFuns(items, funFile);
				Item.writeVars(items, varFile);
				
//				idealItems.addAll(filterFront(iterationItems));
				surfaceItems.addAll(items);
			}

			//Get surface items
			surfaceItems = filterFront(surfaceItems);
			
			String targetFile = baseDir + m + problemName + "/Surface.tsv";
			Item.writeFuns(surfaceItems, targetFile);
			
			//Update antiideal
			updateAntiIdeal(surfaceItems,antiideal);
			
			idealItems.addAll(surfaceItems);
		}
		
		//Save pseudooptimal pareto front
		String outPut = baseDir + "pseudooptimal.tsv";
		Item.writeFuns(filterFront(idealItems), outPut);
		
		//Save antiideal
		outPut = baseDir+"AntiIdealPoint.csv";
		FileWriter fw = new FileWriter(new File(outPut));
		fw.write(Arrays.toString(antiideal));
		fw.close();
	}

	public static List<Item> filterFront(List<Item> items) {
//		Collections.sort(items);
//
//		List<Item> filtered = new ArrayList<Item>();
//
//		Item last = items.remove(0);
//		filtered.add(last);
//
//		for (Item i : items) {
//			if (Item.lessThan(i.secondFun(), last.secondFun())) {
//				filtered.add(i);
//				last = i;
//			}
//		}
//
//		return filtered;
		return filterDominated(items);
	}
	
	public static List<Item> filterDominated(List<Item> items) {
		Collections.sort(items);

		List<Item> filtered = new ArrayList<Item>();

		// XXX Nelder-Mead is finding a single solution in some cases.
		if(items.size()==1) {
			return items;
		}
		
		Item last = items.remove(0);
		filtered.add(last);
		
		for (Item i : items) {
			if (!Item.isDominated(filtered,i)) {
				Item.add(filtered,i);
			}
		}

		return filtered;
	}

	public static List<Item> pairItems(List<String[]> funList, List<String[]> varList) {
		if (funList.size() != varList.size()) {
			throw new IllegalArgumentException("Both list should have the same size.");
		}
		List<Item> list = new ArrayList<Item>();

		int i = 0;
		while (i < funList.size()) {
			list.add(new Item(funList.get(i), varList.get(i)));
			i++;
		}

		return list;
	}
	
	public static List<Item> pairItems(List<String[]> funList) {
		
		List<Item> list = new ArrayList<Item>();

		int i = 0;
		while (i < funList.size()) {
			String[] empty = {};
			list.add(new Item(funList.get(i), empty));
			i++;
		}

		return list;
	}
	
	public static void updateAntiIdeal(List<Item> items, String[] antiideal) {
		for (Item item : items) {
			if(Double.parseDouble(item.fun[0])>Double.parseDouble(antiideal[0])) {
				antiideal[0] = item.fun[0];
			}
			if(Double.parseDouble(item.fun[1])>Double.parseDouble(antiideal[1])) {
				antiideal[1] = item.fun[1];
			}
		}
	}

}
