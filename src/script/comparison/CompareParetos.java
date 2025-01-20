package script.comparison;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

public class CompareParetos {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException("This script requires a two arguments.");
		}
		
		String first = args[0];
		String second = args[1];
		char separator = '	';
		
		CSVReader firstReader = new CSVReader(new FileReader(new File(first)),
				separator);
		List<String[]> firstFuns = firstReader.readAll();
		firstReader.close();
		
		separator = ' ';
		CSVReader secondReader = new CSVReader(new FileReader(new File(second)),
				separator);
		List<String[]> secondFuns = secondReader.readAll();
		secondReader.close();
		
		if(firstFuns.size()!=secondFuns.size()) {
			System.err.println("Warning::"
					+ "These paretos do not have the same cardinality.");
		}
		
		List<String[]> notMatching = new ArrayList<String[]>();
		
		for (String[] funs : firstFuns) {
//			int index = secondFuns.indexOf(funs);
//			if(index == -1) {
//				notMatching.add(funs);
//			}
			boolean found = false;
			for (String[] candidate : firstFuns) {
				if (funs.equals(candidate)) {
					found = true;
					break;
				}
			}
			if(!found) {
				notMatching.add(funs);
			}
		}
		
		if(notMatching.isEmpty()) {
			System.out.println("Nice!! Paretos have the same elements!");
		} else {
			System.out.println("Paretos do not match!");
			System.out.println("The following elements missmatch:");
			for (String[] funs : notMatching) {
				System.out.println(Arrays.toString(funs));
			}
		}
	}
}
