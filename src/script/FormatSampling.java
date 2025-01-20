package script;

import java.io.IOException;

import util.io.CSVFileUtils;

public class FormatSampling {

	public static void main(String[] args) {
		// Target csv file
		String file = args[0];
		
		try {
			double[][] values = CSVFileUtils.readDoubleTwoDimArrayFromCSV(file);
			
//			int[][] valuesInt = new int[230][110];
			int[][] valuesInt = new int[110][230];
			
			for (double[] darray : values) {
				int x = (int)darray[0];
				int y = (int)darray[1];
//				valuesInt[x][y]++;
				valuesInt[y][x]++;
			}
			
			String target = args[1];
			CSVFileUtils.writeHistoryToCSV(target, valuesInt);
			
			
			String target2= args[2];
			
//			int [][] valuesCount = new int [valuesInt.length][valuesInt[0].length][]
//			TIntArrayList list = new TIntArrayList();
			String content = "Avalue,Bvalue,Cvalue\n";
			
			for (int x=0; x<valuesInt.length; x++) {
				for (int y=0; y<valuesInt[x].length; y++) {
					if(valuesInt[x][y]!=0) {
						content +=String.valueOf(x)+","+
								String.valueOf(y)+","+
								String.valueOf(valuesInt[x][y])+"\n";
					}
				}
			}
			
			CSVFileUtils.writeFile(target2, content);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
