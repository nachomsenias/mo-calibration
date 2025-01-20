package script;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GeneralizedSpread;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import com.opencsv.CSVWriter;

public class ComputeQualityIndicators {
	
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
//				"SMSEMOA", "mIBEA", "SPEA2", "SPEA2-BLX", "NSGAII", "NSGAII-BLX",
//				"MOEAD", "PESA2", "IBEA", "GWASFGA", "MOMBI2"
//				}; 
		String[] methods = {  
				"SMSEMOA", "SPEA2-BLX", "NSGAII-BLX", 
				"MOEAD", "IBEA", "GWASFGA", "MOMBI2", "NELDER-MEAD"
				};
		
		int iterations = 30;
		char separator = ' ';

		String pseudoFile = baseDir + "pseudooptimal.tsv";
		Front referenceFront = new ArrayFront(pseudoFile);
		
		FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
        
        List<GenericIndicator<PointSolution>> indicators = 
        		new ArrayList<GenericIndicator<PointSolution>>();
		indicators.add(new PISAHypervolume<PointSolution>(normalizedReferenceFront));
		indicators.add(new WFGHypervolume<PointSolution>(normalizedReferenceFront));
		indicators.add(new Epsilon<PointSolution>(normalizedReferenceFront));
		indicators.add(new GenerationalDistance<PointSolution>(normalizedReferenceFront));
		indicators.add(new InvertedGenerationalDistance<PointSolution>(normalizedReferenceFront));
		indicators.add(new InvertedGenerationalDistancePlus<PointSolution>(normalizedReferenceFront));
		indicators.add(new Spread<PointSolution>(normalizedReferenceFront));
		indicators.add(new GeneralizedSpread<PointSolution>(normalizedReferenceFront));
		
		List<String[]> summary = new ArrayList<String[]>();
		String[] summaryHeader = new String[indicators.size()+1];
		summaryHeader[0] = "Method";
		for (int i=0; i<indicators.size(); i++) {
			summaryHeader[i+1]=indicators.get(i).getName();
		}
		summary.add(summaryHeader);
		
		for (String m : methods) {
			
			System.out.println("Current method: "+m);

			String[] summaryLine = new String[indicators.size()+1];
			summaryLine[0] = m;
			
			List<String[]> lines = new ArrayList<String[]>();
			lines.add(getHeader(iterations));
			
			for (int ind=0; ind<indicators.size(); ind++) {
				GenericIndicator<PointSolution> indicator = indicators.get(ind);
				
				String[] line = new String[iterations+2];
				line[0] = indicator.getName();
				
				double[] values = new double[iterations]; 
				
				for (int i = 0; i < iterations; i++) {
					
					String frontIt = baseDir + m + problemName + "/FUN" + i + "_front.tsv";
					Front front = new ArrayFront(frontIt);
					
					Front normalized = frontNormalizer.normalize(front);
					
					List<PointSolution> normalizedPopulation = 
							FrontUtils.convertFrontToSolutionList(normalized);
					
					Double indicatorValue = indicator.evaluate(
							normalizedPopulation);
					
					values[i] = indicatorValue;
					line[i+1] = String.valueOf(indicatorValue); 
				}
			
				String avg = String.valueOf(StatUtils.mean(values));
				line[iterations+1] = avg;
				lines.add(line);
				
				summaryLine[ind+1] = avg;
			}
			
			summary.add(summaryLine);
			
			String outputFile = baseDir + m + problemName + "/Indicators.csv";
			CSVWriter writer = new CSVWriter(new FileWriter(outputFile), separator);
			writer.writeAll(lines);
			writer.close();
		}
		
		String outputFile = baseDir + "/SummaryIndicators.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile), separator);
		writer.writeAll(summary);
		writer.close();

	}
	
	private static String[] getHeader(int iterations) {
		String[] header = new String[iterations+2];
		header[0]="KPI";
		for (int it=0; it<iterations; it++) {
			header[it+1]="MC"+String.valueOf(it);
		}
		header[iterations+1]="Avg";
		return header;
	}
}
