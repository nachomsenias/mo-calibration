package graphs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Similarity {

	public static List<String[]> matrixToList(String [][] values) {
		List<String[]> csv = new ArrayList<String []>(values.length);
		
		for (String [] array : values) {
			csv.add(array);
		}
		
		return csv;
	}
	
	
	public static List<String[]> weightsToCSV(double [][] weights) {
		List<String[]> csv = new ArrayList<String []>(weights.length);
		
		for (double [] array : weights) {
			int elements = array.length;
			String[] formated = new String [elements];
			for (int i=0; i<elements; i++) {
				if(array[i]==Double.MAX_VALUE) {
					formated[i] = "-1";
				} else {
					formated[i] = String.valueOf(array[i]);
				}
			}
			csv.add(formated);
		}
		
		return csv;
	}
	
	public static double [][] convertPruned(double [][] pruned) {

		double [][] converted = new double [pruned.length][];
		
		for (int i=0; i<pruned.length; i++) {
			converted[i] = new double [pruned[i].length];
			for (int j=0; j<pruned[i].length; j++) {
				if(pruned[i][j]==Double.MAX_VALUE) {
//				if(pruned[i][j]>1 || pruned[i][j]<0) {
					converted[i][j] = -1; 
				} else {
					converted[i][j] = 1 - pruned[i][j];
				}
			}
		}
		
		return converted;
	}
	
	public static List<String[]> toEdges(double [][] simMatrix) {

		List<String[]> edges = new ArrayList<String[]>();
		boolean[][] visited = new boolean[simMatrix.length][simMatrix.length];
		for (int x=0; x<simMatrix.length; x++) {
			for (int y=0; y<simMatrix.length; y++) {
				if(simMatrix[x][y]!=-1 && !visited[x][y]) {
					String[] edge = {
							String.valueOf(x),
							String.valueOf(y),
							String.valueOf(simMatrix[x][y])
									};
					visited[y][x] = true;
					edges.add(edge);
				}
			}
		}
		
		return edges;
	}
	
	public static double [][] computeMatrix(double nullValue, List<Item> paretoFront) {
		int items = paretoFront.size();
		double [][] matrix = new double [items][items];
		
		for (int i=0; i<items; i++) {
			for (int j=0; j<items; j++) {
				if(i==j) {
					matrix[i][j] = nullValue;
				} else {
					Item itI = paretoFront.get(i);
					Item itJ = paretoFront.get(j);
					
					matrix[i][j] = computeSim(itI, itJ);
				}
			}
		}
		
		return matrix;
	}
	
	public static String [][] computeSimilarities(List<Item> paretoFront) {
		int items = paretoFront.size();
		String [][] matrix = new String [items][items];
		
		DecimalFormat df = new DecimalFormat("#.####");
		
		for (int i=0; i<items; i++) {
			for (int j=0; j<items; j++) {
				if(i==j) {
					matrix[i][j] = "1.0";
				} else {
					Item itI = paretoFront.get(i);
					Item itJ = paretoFront.get(j);
					
					matrix[i][j] = df.format(pureSimilarityValue(itI, itJ));
				}
			}
		}
		
		return matrix;
	}
	
	private static double pureSimilarityValue(Item itI, Item itJ) {
		String[] var1 = itI.var;
		String[] var2 = itJ.var;
		
		if(var1.length!=var2.length) {
			throw new IllegalArgumentException();
		}
		
		double value = 0.0;
		//Skip empty String
		int items = var1.length-1;
		
		for (int i=0; i<items; i++) {
			if(var1[i].equals(var2[i])) { 
					continue;
			} else {
				value += Math.pow(
						Double.parseDouble(var1[i]) - Double.parseDouble(var2[i]), 
						2);
			}
		}
		
		return 1-Math.sqrt(value/items);
	}
	
	private static double computeSim(Item itI, Item itJ) {
		String[] var1 = itI.var;
		String[] var2 = itJ.var;
		
		if(var1.length!=var2.length) {
			throw new IllegalArgumentException();
		}
		
		double value = 0.0;
		//Skip empty String
		int items = var1.length-1;
		
		for (int i=0; i<items; i++) {
			if(var1[i].equals(var2[i])) { 
					continue;
			} else {
				value += Math.pow(
						Double.parseDouble(var1[i]) - Double.parseDouble(var2[i]), 
						2);
			}
//			if(var1[i].equals(var2[i])) { 
//					continue;
//			} else {
//				value += Math.abs(Double.parseDouble(var1[i]) - Double.parseDouble(var2[i]));
//			}
		}
		
		return 1-Math.sqrt(value/items);
//		return Math.sqrt(value/items);
//		return value/items;
	}
	
	public static double max(double[] values) {
		double max = Double.MIN_VALUE;
		for (double value : values) {
			if(value != Double.MAX_VALUE) {
				max = Double.max(max, value);
			}
		}
		return max;
	}
	
	public static List<String> toPajek(double[][] matrix) {
		// Example of the format
//		*vertices 5
//		1 "1"
//		2 "2"
//		3 "3"
//		4 "4"
//		5 "5"
//		*matrix
//		0 1 4 2 2
		
		List<String> lines = new ArrayList<String>();
		String text ="*vertices "+String.valueOf(matrix.length);
		lines.add(text);
		for (int i=1; i<=matrix.length; i++) {
			String line = String.format("%d \"%d\"", i,i);
			lines.add(line);
		}
		
		text = "*matrix";
		lines.add(text);
		for (int i=0; i<matrix.length; i++) {
			String line = String.valueOf(matrix[i][0]);
			for (int j=1; j<matrix[i].length; j++) {
				line+= " " + String.valueOf(matrix[i][j]);
			}
			lines.add(line);
		}
		
		return lines;
	}
}
