package script.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import gnu.trove.list.array.TDoubleArrayList;
import util.functions.ArrayFunctions;
import util.io.XLSXFileUtils;

public class GenerateMOReports {

	private static class MOReport {
		String pseudooptimal_s;
		String[][] s;
		String[][] cardinality;
		String[][][] ie;
	};
	
	private static class SumReport {
		double[][][] avgUnaries; //Method Problem Metric
		double[][][] stdUnaries; //Method Problem Metric
		
		double[][][] avgIe; //Method Problem Method
		
		public SumReport(int methods, int problems, int uniMetrics) {
			avgUnaries = new double [methods][problems][uniMetrics];
			stdUnaries = new double [methods][problems][uniMetrics];
			
			avgIe = new double [methods][problems][methods];
		}
	};
	
	public static final String[] labels = {
			"P1(25)", "P2(40)", "P3(46)", "P4(55)", "P5(61)", "P6(70)",
			"P7(76)", "P8(85)", "P9(91)", "P10(100)", "P11(115)", "P12(130)",
			"P13(145)", "P14(160)", "P15(175)",
			};
	public static final String[] problems = {
			"0TP", "5TP", "7TP", "10TP", "12TP","15TP", 
			"17TP", "20TP", "22TP", "25TP", "30TP", "35TP",
			"40TP", "45TP", "50TP"
			};
	public static final String[] methods = {  
			"MOEAD", "SPEA2-BLX", "SMSEMOA",  
			"IBEA", "NSGAII-BLX", "GWASFGA", "MOMBI2",
			"NELDER-MEAD"
			}; // Discarded methods:  "mIBEA", "PESA2", "SPEA2", "NSGAII",   
	
	
	public static final String[] uniMetric = {"HVR", "C"};
	
	public static int iterations = 30;
	public static char separator = ';';
	
	public static void main(String[] args) throws IOException {
		String root = args[0];
		
		MOReport[] reports = new MOReport[problems.length];
		SumReport summary = new SumReport(
				methods.length, problems.length, uniMetric.length);
		
		
		//Read values for each problem
		for (int p=0; p<problems.length; p++) {
			reports[p] = loadReportValues(root, problems[p]);
		}
		
		Workbook book = new XSSFWorkbook();
		
		//Write Summary Sheets
		Sheet sheet = book.createSheet("Summary");
		
		//Write Sheets
		for (int p=0; p<problems.length; p++) {
			Sheet problemSheet = book.createSheet(labels[p]);
			
			int rowoffset = 2;
			int coloffset = 1;
			
			Row row = problemSheet.createRow(rowoffset);
			Cell cell = row.createCell(coloffset);
			
			//HVR
			cell.setCellValue("HVR");
			cell = row.createCell(coloffset+1);
			cell.setCellValue("Pseudooptimal");
			
			rowoffset++;
			row = problemSheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			
			cell.setCellValue("S-value");
			cell = row.createCell(coloffset+1);
			cell.setCellValue(reports[p].pseudooptimal_s);
			
			rowoffset+=2;
			
			for (int m=0; m<methods.length; m++) {
				row = problemSheet.createRow(rowoffset);
				cell = row.createCell(coloffset);
				cell.setCellValue(methods[m]);
				
				for (int mc=0; mc<iterations; mc++) {
					cell = row.createCell(coloffset+mc+1);
					cell.setCellValue("MC "+mc); 
				}
				
				rowoffset++;
				row = problemSheet.createRow(rowoffset);
				cell = row.createCell(coloffset);
				
				cell.setCellValue("S-value");
				for (int mc=0; mc<iterations; mc++) {
					cell = row.createCell(coloffset+mc+1);
					cell.setCellValue(reports[p].s[m][mc]);
				}
				cell = row.createCell(coloffset+iterations+2);
				cell.setCellValue("Average");
				
				cell = row.createCell(coloffset+iterations+3);
				cell.setCellValue("Std");
				
				rowoffset++;
				row = problemSheet.createRow(rowoffset);
				cell = row.createCell(coloffset);
				
				cell.setCellValue("Ratio");
				double[] ratio = new double[iterations];
				for (int mc=0; mc<iterations; mc++) {
					cell = row.createCell(coloffset+mc+1);
					ratio[mc] = Double.parseDouble(reports[p].s[m][mc])
							/Double.parseDouble(reports[p].pseudooptimal_s);
					cell.setCellValue(ratio[mc]);
				}
				cell = row.createCell(coloffset+iterations+2);
				double meanValue = StatUtils.mean(ratio);
				cell.setCellValue(meanValue);
				summary.avgUnaries[m][p][0]=meanValue;
				
				cell = row.createCell(coloffset+iterations+3);
				
				StandardDeviation sd = new StandardDeviation();
				double stdValue = sd.evaluate(ratio);
				cell.setCellValue(stdValue);
				summary.stdUnaries[m][p][0]=stdValue;
				
				rowoffset++;
				row = problemSheet.createRow(rowoffset);
				cell = row.createCell(coloffset);
				
				cell.setCellValue("Cardinality");
				double[] cardinality = new double[iterations];
				for (int mc=0; mc<iterations; mc++) {
					cell = row.createCell(coloffset+mc+1);
					cardinality[mc] = Double.parseDouble(reports[p].cardinality[m][mc]);
					cell.setCellValue(cardinality[mc]);
				}
				cell = row.createCell(coloffset+iterations+2);
				meanValue = StatUtils.mean(cardinality);
				cell.setCellValue(meanValue);
				summary.avgUnaries[m][p][1]=meanValue;
				
				cell = row.createCell(coloffset+iterations+3);
				
				sd = new StandardDeviation();
				stdValue = sd.evaluate(cardinality);
				cell.setCellValue(stdValue);
				summary.stdUnaries[m][p][1]=stdValue;
				
				rowoffset+=2;
			}
			
			
			row = problemSheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("I-epsilon");
			
			rowoffset+=2;
			
			//Ie
//			int items = methods.length * (methods.length-1);
			int count = 0;
			
			for (int m =0; m<methods.length; m++) {
				for (int second =0; second<methods.length; second++) {
					if(m==second) {
						continue;
					}
					row = problemSheet.createRow(rowoffset);
					cell = row.createCell(coloffset);
					cell.setCellValue(methods[m] +" vs "+methods[second]);
					
					rowoffset+=2;
					
					row = problemSheet.createRow(rowoffset);
					cell = row.createCell(coloffset);
					cell.setCellValue("MC");

					for (int mc=0; mc<iterations; mc++) {
						cell = row.createCell(coloffset+mc+1);
						cell.setCellValue(mc); 
					}
					
					rowoffset++;
					
					TDoubleArrayList list = new TDoubleArrayList();
					
					for (int mc=0; mc<iterations; mc++) {
						row = problemSheet.createRow(rowoffset);
						cell = row.createCell(coloffset);
						cell.setCellValue(mc);
						
						//Ie values
						for (int it=0; it<iterations; it++) {
							cell = row.createCell(coloffset+it+1);
							double value = Double.parseDouble(reports[p].ie[count][mc][it]);
							cell.setCellValue(value);
							list.add(value);
						}
						rowoffset++;
					}
					
					rowoffset++;
					
					double[] ie_values = list.toArray();
					
					row = problemSheet.createRow(rowoffset);
					cell = row.createCell(coloffset);
					cell.setCellValue("Average");
					cell = row.createCell(coloffset+1);
					
					double mean = StatUtils.mean(ie_values);
					cell.setCellValue(mean);
					summary.avgIe[m][p][second] = mean;
					
					rowoffset++;
					
					row = problemSheet.createRow(rowoffset);
					cell = row.createCell(coloffset);
					cell.setCellValue("Std. Dev.");
					cell = row.createCell(coloffset+1);
					cell.setCellValue(new StandardDeviation().evaluate(ie_values));
					
					rowoffset+=2;

					count++;
				}
				
			}
		}
		
		//Fill summary values
		
		
		// Problem ROW
		int rowoffset = 2;
		int coloffset = 2;
		
		Row row = sheet.createRow(rowoffset);
		
		for (int p=0; p<problems.length; p++) {
			Cell cell = row.createCell(coloffset);
			cell.setCellValue(labels[p]);
			
			int endCol = coloffset+(uniMetric.length*2)-1;
			sheet.addMergedRegion(new CellRangeAddress(rowoffset, rowoffset, coloffset, endCol));
			coloffset = endCol+1;
		}
		
		//Unary KPI ROW
		rowoffset++;
		coloffset = 2;
		
		row = sheet.createRow(rowoffset);
		
		for (int p=0; p<problems.length; p++) {
			
			for (int u=0; u<uniMetric.length; u++) {
				Cell cell = row.createCell(coloffset);
				cell.setCellValue(uniMetric[u]);
				
				int endCol = coloffset+1;
				sheet.addMergedRegion(new CellRangeAddress(rowoffset, rowoffset, coloffset, endCol));
				coloffset = endCol+1;
			}
		}
		
		//Statistic ROW
		rowoffset++;
		coloffset = 2;
		
		row = sheet.createRow(rowoffset);
		
		for (int p=0; p<problems.length; p++) {
			
			for (int u=0; u<uniMetric.length; u++) {
				Cell cell = row.createCell(coloffset);
				cell.setCellValue("Avg");
				coloffset++;
				
				cell = row.createCell(coloffset);
				cell.setCellValue("Std.Dev");
				coloffset++;
			}
		}
		
		//Values row
		rowoffset++;
		for (int m=0; m<methods.length; m++) {
			row = sheet.createRow(rowoffset);
			coloffset = 1;
			
			Cell cell = row.createCell(coloffset);
			cell.setCellValue(methods[m]);
			coloffset++;
			
			for (int p=0; p<problems.length; p++) {
				
				for (int u=0; u<uniMetric.length; u++) {
					cell = row.createCell(coloffset);
					cell.setCellValue(summary.avgUnaries[m][p][u]);
					coloffset++;
					
					cell = row.createCell(coloffset);
					cell.setCellValue(summary.stdUnaries[m][p][u]);
					coloffset++;
				}
			}
			
			rowoffset++;
		}
		
		rowoffset++;
		
		//Binary Matrix
		coloffset = 2;
		
		row = sheet.createRow(rowoffset);
		
		for (int p=0; p<problems.length; p++) {
			Cell cell = row.createCell(coloffset);
			cell.setCellValue(labels[p]);
			
			int endCol = coloffset+(methods.length-1);
			sheet.addMergedRegion(new CellRangeAddress(rowoffset, rowoffset, coloffset, endCol));
			coloffset = endCol+1;
		}
		
		rowoffset++;
		coloffset = 2;
		row = sheet.createRow(rowoffset);
		
		for (int p=0; p<problems.length; p++) {
			for (int m=0; m<methods.length; m++) {
				Cell cell = row.createCell(coloffset);
				cell.setCellValue(methods[m]);
				coloffset++;
			}
		}
		
		//Values Rows
		rowoffset++;
		
		for (int m=0; m<methods.length; m++) {
			row = sheet.createRow(rowoffset);
			coloffset = 1;
			
			Cell cell = row.createCell(coloffset);
			cell.setCellValue(methods[m]);
			coloffset++;
			
			for (int p=0; p<problems.length; p++) {
				
				for (int second =0; second<methods.length; second++) {
					cell = row.createCell(coloffset);
					if(m!=second) {
						cell.setCellValue(summary.avgIe[m][p][second]);
					} else {
						cell.setCellValue("-");
					}
					coloffset++;
				}
			}
			
			rowoffset++;
		}
		
		//Export report values to latex tables
		exportToTex(summary, root);
		
		XLSXFileUtils.saveWorkbook(book, root+"/report");
	}
	
	private static String loadPsudooptimalS(String pseudooptimal) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(pseudooptimal)));
		
		String line = br.readLine();
		boolean end = false;
		
		while(line!=null && !end) {
			if(line.contains("METRICA S:")) {
				br.close();
				return line.split(":")[1];
			} else {
				line = br.readLine();
			}
		}
		br.close();
		throw new IllegalStateException();
	}
	
	private static MOReport loadReportValues(String root, String problem) throws IOException {
		MOReport report = new MOReport();
		
		String folder = root+problem+"/WOMM/data/";//hvr-pseudooptimal.txt";
		
		//Load pseudooptimal
		String pseudooptimal = folder+"hvr-pseudooptimal.txt";
		
		report.pseudooptimal_s = loadPsudooptimalS(pseudooptimal);

		//Load HVR and cardinality
		report.s = new String[methods.length][];
		report.cardinality = new String[methods.length][];
		for (int m =0; m<methods.length; m++) {
			
			String hvr = folder + methods[m] +"_s.csv";
			
			CSVReader csvr = new CSVReader(new FileReader(new File(hvr)), separator);
			List<String[]> lines = csvr.readAll();
			csvr.close();
			report.s[m] = lines.get(0);
			
			String cardinality = folder + methods[m] +"_cardinality.csv";
			
			csvr = new CSVReader(new FileReader(new File(cardinality)), separator);
			lines = csvr.readAll();
			csvr.close();
			report.cardinality[m] = lines.get(0);
		}
		
		//Load Ie
		int items = methods.length * (methods.length-1);
		int count = 0;
		
		report.ie = new String[items][][];
		
		String[][][][] accIe = new String[methods.length][methods.length][][]; 
		
		for (int m =0; m<methods.length; m++) {
			for (int second =0; second<methods.length; second++) {
				if(m==second) {
					continue;
				}
				// NSGAII_vs_SPEA2_ie.csv
				String ie = folder + methods[m] +"_vs_"+methods[second]+"_ie.csv";
				
				CSVReader csvr = new CSVReader(new FileReader(new File(ie)), separator);
				List<String[]> lines = csvr.readAll();
				csvr.close();
				
				String[][] values = lines.toArray(report.cardinality);
				
				accIe[m][second] = values;
				
//				report.ie[count] = lines.toArray(report.cardinality);
				report.ie[count] = values;
				count++;
			}
		}
		
		computeP(folder, accIe);
		
		return report;
	}
	
	private static void computeP(String folder, String[][][][] values) {
		double [][][][] pOcc = new double [methods.length][methods.length][][];
		for (int m=0; m<methods.length; m++) {
			for (int second =0; second<methods.length; second++) {
				if(m==second) {
					continue;
				}
				
				int items = values[m][second].length;
				double p[] = new double[items];
				pOcc[m][second] = new double [items][items];
				
				for (int i=0; i<values[m][second].length; i++) {
					for (int j=0; j<values[m][second][i].length; j++) {
						double p_ab = Double.parseDouble(values[m][second][i][j]);
						double p_ba = Double.parseDouble(values[second][m][i][j]);
						if(p_ab<=1 && p_ba > 1) {
							pOcc[m][second][i][j] = 1.0;
						}
					}
					
					p[i] = StatUtils.sum(pOcc[m][second][i]) / items;
					
				}
				
				try {
					CSVWriter csWriter = new CSVWriter(
							new FileWriter(new File(folder+"p_"+methods[m]+"vs"+methods[second]+".csv")), ',',
							CSVWriter.NO_QUOTE_CHARACTER);
					csWriter.writeNext(XLSXFileUtils.getNamesMC(items),false);
					csWriter.writeNext(ArrayFunctions.doubleToString(p),false);
					csWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static void exportToTex(SumReport summary, String root) {
		
		boolean[][][] maxValues = getMaxValues(summary.avgUnaries);
		
		// Unary to TEX file
		for (int uniIndex = 0; uniIndex <uniMetric.length; uniIndex++) {
			DecimalFormat df;
			DecimalFormat devFormat = new DecimalFormat("#.##");;
			switch (uniIndex) {
			case 0: // HVR
				df = new DecimalFormat("#.###");
				break;
			case 1: // Cardinality
				df = new DecimalFormat("#.#");
				break;

			default:
				df = new DecimalFormat("#.####");
			}
			
			ArrayList<String> unaryLines = new ArrayList<>();
			unaryLines.add(getLatexRow("\\textbf{"+uniMetric[uniIndex]+"}", methods));
			
			for (int p=0; p<problems.length; p++) {
				String[] values = new String[methods.length];
				for (int m=0; m<methods.length; m++) {
					//Use bold fond for max values
					if(maxValues[m][p][uniIndex]) {
						values[m] = "\\textbf{" + df.format(summary.avgUnaries[m][p][uniIndex])
								+ "} (" + devFormat.format(summary.stdUnaries[m][p][uniIndex])+ ")";
					} else {
						values[m] = df.format(summary.avgUnaries[m][p][uniIndex])
								+ " (" + devFormat.format(summary.stdUnaries[m][p][uniIndex])+ ")";
					}
				}
				unaryLines.add(getLatexRow(labels[p], values));
			}
			
			String[] dummyArray = {};		
			writeTex(unaryLines.toArray(dummyArray), root+"/"+uniMetric[uniIndex]+".tex");
			
			ArrayList<String> trasposedLines = new ArrayList<>();
			trasposedLines.add(getLatexRow("\\textbf{"+uniMetric[uniIndex]+"}", toTiny(labels)));
			
			for (int m=0; m<methods.length; m++) {
				String[] values = new String[problems.length];
				String[] stdValues = new String[problems.length];
				for (int p=0; p<problems.length; p++) {
					//Use bold fond for max values
//					if(maxValues[m][p][uniIndex]) {
//						values[p] = "\\textbf{" + df.format(summary.avgUnaries[m][p][uniIndex])
//								+ "} (" + df.format(summary.stdUnaries[m][p][uniIndex])+ ")";
//					} else {
//						values[p] = df.format(summary.avgUnaries[m][p][uniIndex])
//								+ " (" + df.format(summary.stdUnaries[m][p][uniIndex])+ ")";
//					}
					if(maxValues[m][p][uniIndex]) {
						values[p] = "\\textbf{" + df.format(summary.avgUnaries[m][p][uniIndex])
								+ "}";
					} else {
						values[p] = df.format(summary.avgUnaries[m][p][uniIndex]);
					}
					stdValues[p] = "(" +devFormat.format(summary.stdUnaries[m][p][uniIndex])+ ")";
				}
				trasposedLines.add(getLatexRow("\\multirow{2}{*}{\\tiny{"+methods[m]+"}}", values));
				//trasposedLines.add(getLatexRow("", stdValues));
			}
			
			writeTex(trasposedLines.toArray(dummyArray), root+"/"+uniMetric[uniIndex]+"_trasposed.tex");
		}
		
		
		//I-epsilon* values
		DecimalFormat df = new DecimalFormat("#.####");
		ArrayList<String> iepsilonLines = new ArrayList<>();
		
		String[] methodLables = getMethodLables(methods);
		
		for (int p=0; p<problems.length; p++) {
			iepsilonLines.add(getLatexRow("\\textbf{"+labels[p]+"}", methodLables));
			for (int m1=0; m1<methods.length; m1++) {
				String[] values = new String[methods.length];
				for (int m2=0; m2<methods.length; m2++) {
					if(m1 == m2) {
						values[m2] = "-";
					} else {
						//Use bold font for best pair
						//Ie(m1,m2) < Ie(m2,m1)
						if(summary.avgIe[m1][p][m2] < summary.avgIe[m2][p][m1]) {
							values[m2] = "\\textbf{" +df.format(summary.avgIe[m1][p][m2])+ "}";
						} else {
							values[m2] = df.format(summary.avgIe[m1][p][m2]);
						}
					}
				}
				iepsilonLines.add(getLatexRow(methodLables[m1], values));
			}
		}
		
		String[] dummyArray = {};		
		writeTex(iepsilonLines.toArray(dummyArray), root+"/Iepsilon.tex");
		
		
		
//		private static class SumReport {
//			double[][][] avgUnaries; //Method Problem Metric
//			double[][][] stdUnaries; //Method Problem Metric
//			
//			double[][][] avgIe; //Method Problem Method
//			
//			public SumReport(int methods, int problems, int uniMetrics) {
//				avgUnaries = new double [methods][problems][uniMetrics];
//				stdUnaries = new double [methods][problems][uniMetrics];
//				
//				avgIe = new double [methods][problems][methods];
//			}
//		};
		
	}
	
	private static String[] getMethodLables(String[] methods) {
		String[] labels = new String[methods.length];
		for (int m=0; m<methods.length; m++) {
			String name = methods[m];
			switch (name) {
			case "SPEA2-BLX":
			case "SMSEMOA":
			case "NSGAII-BLX":	
			case "GWASFGA":	
				labels[m] = "{\\tiny "+name+"}";
				break;

			default:
				labels[m] = name;
				break;
			}
		}
		return labels;

	}
	
	private static String getLatexRow(String header, String[] columns) {
		String row = header + " & " + columns[0];
		for (int r=1; r<columns.length; r++) {
			row += " & " + columns[r];
		}
		row += " \\\\ \n";
		return row;
	}
	
	private static String[] toTiny(String[] original) {
		String[] tiny = new String[original.length];
		for (int i=0; i<tiny.length; i++) {
			tiny[i] = "\\tiny{"+original[i]+"}";
		}
		return tiny;
	}
	
	private static void writeTex(String[] lines, String file) {
		try {
			FileWriter fw = new FileWriter(file);
			for (String line : lines) {
				fw.write(line);
				fw.write("\\hline \n");
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("Error writing the file: "+file);
			e.printStackTrace();
		}
	}
	
	private static boolean [][][] getMaxValues(double[][][] values) {
		boolean[][][] bValues = new boolean[methods.length][problems.length][uniMetric.length];
		
		for (int uniIndex = 0; uniIndex <uniMetric.length; uniIndex++) {
			for (int p=0; p<problems.length; p++) {
				double[] mValues = new double[methods.length];
				for (int m=0; m<methods.length; m++) {
					mValues[m] = values[m][p][uniIndex];
				}
				double max = StatUtils.max(mValues);
				int maxIndex = ArrayUtils.indexOf(mValues, max);
				bValues[maxIndex][p][uniIndex] = true;
			}
		}
		
		return bValues;
	}
}
