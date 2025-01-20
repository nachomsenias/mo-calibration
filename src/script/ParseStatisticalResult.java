package script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVWriter;

import util.io.XLSXFileUtils;

public class ParseStatisticalResult {

	public static final String[] labels = {
			"P1(25)", "P2(40)", "P3(46)", "P4(55)", "P5(61)", "P6(70)",
			"P7(76)", "P8(85)", "P9(91)", "P10(100)", "P11(115)", "P12(130)",
			"P13(145)", "P14(160)", "P15(175)"
			};
	public static final String[] problems = {
			"0TP", "5TP", "7TP", "10TP", "12TP","15TP", 
			"17TP", "20TP", "22TP", "25TP", "30TP", "35TP",
			"40TP", "45TP", "50TP"
			};
	public static final String[] methods = {  
			"MOEAD", "SPEA2-BLX", "SMSEMOA",  
			"IBEA", "NSGAII-BLX", "GWASFGA", "MOMBI2", "NELDER-MEAD"  
			}; // Discarded methods:  "mIBEA", "PESA2", "SPEA2", "NSGAII",   
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException(
					"This script requires the base directory. Example:"
							+"/home/ignacio/codingmadafaka/jmetaltest/olimpo/fight/data/"
					);
		}
		
		String baseDir = args[0];

		String problemName = "/MWomABMProblem";   
		
		String[][][] pValues = new String[problems.length][][];
		
		
		for (int p=0; p<problems.length; p++) {
			String file = baseDir + problems[p] + "/WOMM/data/";
			pValues[p]=execute(file, problemName);
		}
		
		exportToXLS(pValues,baseDir);
	}
	
	public static String[][] execute(String baseDir, String problemName) throws IOException {
		
		String[][] values = new String[methods.length][methods.length];
		for (String[] line : values) {
			Arrays.fill(line,"-");
		}
		
		for (int algo1 =0; algo1<methods.length; algo1++) {
			for (int algo2 = algo1+1; algo2<methods.length; algo2++) {
				if(algo1==algo2) continue;
				
				System.out.println("Current pair: "+methods[algo1]+" and "+methods[algo2]+".");

				String resultFile = baseDir+"sum_"+methods[algo1]+"vs"+methods[algo2]+".out";
				Double[] comparison = getValues(resultFile);
				values[algo1][algo2] = formatValue(comparison[0]);
				values[algo2][algo1] = formatValue(comparison[1]);
			}
		}
		
		
		//Export CSV
		String outPut = baseDir + "p_values.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(outPut), ',');
		for (String[] line : values) {
			writer.writeNext(line);
		}
		writer.close();
		
		//Export Tex
		outPut = baseDir + "p_values.tex";
		FileWriter fw = new FileWriter(outPut);
		for (String[] line : values) {
			fw.write(line[0]);
			for (int i = 1; i<line.length; i++) {
				fw.write(" & ");
				fw.write(line[i]);
			}
			fw.write(" \\\\ \n");
		}
		fw.close();
		
		return values;
	}
	
	public static String formatValue(Double value) {
		DecimalFormat df = new DecimalFormat("#.####");
		if(value < 0.0001) {
			return String.format("%6.3e",value);
		} else return df.format(value);
	}
	
	public static Double[] getValues(String resultFile) throws IOException {
		Double[] values = new Double[2];
		
		int line1vs2 = 3;
		int line2vs1 = 10;
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(resultFile));
		
		String line = lnr.readLine();
		while(line!=null && lnr.getLineNumber()!=line1vs2) {
			line=lnr.readLine();
		}
		
		values[0]= Double.valueOf(line);
		
		lnr.readLine();
		while(line!=null && lnr.getLineNumber()!=line2vs1) {
			line=lnr.readLine();
		}
		
		values[1]= Double.valueOf(line);
		
		lnr.close();
		
		return values;
	}
	
	// XXX Alternate version of the "getValues" method using checkpoints (compatible with multiple values).
	@SuppressWarnings("unused")
	private static String[] readFile(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		int[] checkPoints = {3, 10};
		String[] values = new String[checkPoints.length];
		
		int line = 0;
		for (int check : checkPoints) {
			String txt = "";
			while(line<check) {
				txt = br.readLine();
				line++;
			}
			values[ArrayUtils.indexOf(checkPoints, check)] = txt;
			
		}
		
		br.close();
		
		return values;
	}
	
	public static void exportToXLS(String[][][] pValues, String root) {
		Workbook book = new XSSFWorkbook();
		
		//Write Summary Sheets
		Sheet sheet = book.createSheet("p-values");
		
		int rowoffset = 2;
		int coloffset = 1;
		
		Row row = sheet.createRow(rowoffset);
		Cell cell = row.createCell(coloffset);
		
		cell.setCellValue("p-values");
		
		rowoffset+=2;
		
		for (int m =0; m<methods.length; m++) {
			
			row = sheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue(methods[m]);
			
			rowoffset+=2;
			
			row = sheet.createRow(rowoffset);
			cell = row.createCell(coloffset);
			cell.setCellValue("P / M");

			int methodOffset = 0;
			for (int mheader=0; mheader<methods.length; mheader++) {
				if(mheader==m) {
					continue;
				}
				
				cell = row.createCell(coloffset+methodOffset+1);
				cell.setCellValue(methods[mheader]);
				
				methodOffset++;
			}
			
			rowoffset++;
			
			for (int p =0; p<problems.length; p++) {
				row = sheet.createRow(rowoffset);
				cell = row.createCell(coloffset);
				cell.setCellValue(labels[p]);
				
				methodOffset = 0;
				for (int second =0; second<methods.length; second++) {
					if(second==m) {
						continue;
					}
					
					cell = row.createCell(coloffset+methodOffset+1);
					cell.setCellValue(pValues[p][m][second]);
					
					methodOffset++;
				}
				
				rowoffset++;
			}
			
			rowoffset+=2;
		}

		XLSXFileUtils.saveWorkbook(book, root+"/p-values");
	}
}
