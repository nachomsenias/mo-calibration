package script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ParseNadirPoint {

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

		String[][] nadirPoints = new String[instances.length][]; 
		
		for (int i=0; i<instances.length; i++) {
			String file = baseDir + instances[i] + "/WOMM/data/";
			nadirPoints[i]=execute(file, problemName);
		}
		
		String file = baseDir + "nadir-set.csv";
		BufferedWriter fw = new BufferedWriter(new FileWriter(file));
		for (int i=0; i<instances.length; i++) {
			fw.write(nadirPoints[i][0]+" "+nadirPoints[i][1]+"\n");
		}
		fw.close();
		
		file = baseDir + "nadir-set_transposed.csv";
		fw = new BufferedWriter(new FileWriter(file));
		int row = 0;
		while(row<instances.length) {
			fw.write(nadirPoints[row][0]+" ");
			row++;
		}
		fw.write("\n");
		
		row = 0;
		while(row<instances.length) {
			fw.write(nadirPoints[row][1]+" ");
			row++;
		}
		fw.write("\n");
		
		fw.close();
	}
	
	public static String[] execute(String baseDir, String problemName) throws IOException {
		//Save antiideal
		String file = baseDir+"AntiIdealPoint.csv";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		//Remove brackets (Next we show an example)
		// [133.16851758397868, 497.91948263446625]
		line = line.substring(1, line.length()-1);
		br.close();
		return line.split(", ");
	}
}
