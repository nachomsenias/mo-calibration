package script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;

import com.opencsv.CSVReader;

import util.functions.ArrayFunctions;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class SelectTradeOffSolution {

	public static void main(String[] args) throws IOException {
		String pareto = args[0];
		char separator = ' ';
		char altSeparator = '	';

		CSVReader funReader = new CSVReader(new FileReader(new File(pareto)),
				separator);
		
		List<String[]> points = funReader.readAll();
		//Check separator
		if(points.get(0).length<2) {
			funReader.close();
			funReader = new CSVReader(new FileReader(new File(pareto)), altSeparator);
			points = funReader.readAll();
		}
		
		
		getTradeOffIndex(points);
		
//		List<double[]> dPoints = getDoublePoints(points);
//		
//		double[] tradeOffValues = computeTradeOff(dPoints);
//		
//		double min =StatUtils.min(tradeOffValues);
//		
//		System.out.println("Best trade off: "+ min);
//		
//		int finalSolution = ArrayUtils.indexOf(tradeOffValues, min);
//		
//		System.out.println("Index of the solution: "+ finalSolution);
//		System.out.println("Objective values: "+ 
//				Arrays.toString(dPoints.get(finalSolution)));
		
		funReader.close();
	}
	
	public static int getTradeOffIndex(List<String[]> points) {
		List<double[]> dPoints = getDoublePoints(points);
		
		double[] tradeOffValues = computeTradeOff(dPoints);
		
		double min =StatUtils.min(tradeOffValues);
		
		System.out.println("Best trade off: "+ min);
		
		int finalSolution = ArrayUtils.indexOf(tradeOffValues, min);
		
		System.out.println("Index of the solution: "+ finalSolution);
		System.out.println("Objective values: "+ 
				Arrays.toString(dPoints.get(finalSolution)));
		return finalSolution;
	}
	
	private static List<double[]> getDoublePoints(List<String[]> points) {
		List<double[]> dPoints = new ArrayList<double[]>(points.size());
		
		for (String[] point : points) {
			int length = point.length;
			if(point[point.length-1]=="") {
				length = point.length-1;
			}
			String[] subPoint = ArrayUtils.subarray(point,0,length);
			dPoints.add(ArrayFunctions.stringToDouble(subPoint));
		}
		
		return dPoints;
	}
	
	private static double computeAlpha(List<double[]> points) {
		double alpha = 0;
		
		for (double[] point : points) {
			if(point[0]!=0) 
				alpha += point[1]/point[0];
		}
		
		alpha/= points.size();
		
		return alpha;
	}

	private static double[] computeTradeOff(List<double[]> points) {
		int items = points.size();
		double[] f = new double[items];
		int weights = 1000;
		
		Randomizer random = RandomizerFactory.createRandomizer(
				RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
				RandomizerUtils.PRIME_SEEDS[0]);
		
		double alpha = computeAlpha(points);
		System.out.println("Computed alpha: "+ alpha);
		
		for (int s=0; s<items; s++) {
			for (int w=0; w<weights; w++) {
				double weightValue = random.nextDouble();
				
				f[s]+= alpha*weightValue*points.get(s)[0]+(1-weightValue)*points.get(s)[1];
			}
			f[s]/=weights;
		}
		
		return f;
	}
}
