package graphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.solution.IntegerDoubleSolution;

import com.opencsv.CSVWriter;

import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerUtils;

public class Item implements Comparable<Item>{

	public static char SEPARATOR = ' '; 
	
	public String[] fun;
	public String[] var;
	
	public Item(String[] fun, String[] var) {
		super();
		this.fun = fun;
		this.var = var;
	}
	
	public String createString() {
		return stringize(fun) + ";" + stringize(var)+ "\n"; 
	}
	
	private String stringize(String[] arg) {
		String base = arg[0];
		
		int i = 1;
		while (i<arg.length) {
			base += SEPARATOR + arg[i];
			i++;
		}
		
		return base;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fun);
		result = prime * result + Arrays.hashCode(var);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (!Arrays.equals(fun, other.fun))
			return false;
		//Using only fun
//		if (!Arrays.equals(var, other.var))
//			return false;
		return true;
	}
	
	public String[] secondFun() {
		int size = fun.length-1;
		String[] second = new String[size];
		
		if(size>0) {
			for (int j=0; j<size; j++) {
				second[j] = fun[j+1];
			}
		}
		
		return second;
	}
	
	@Override
	public int compareTo(Item o) {
		if(o==null) throw new NullPointerException();
		if(this.equals(o)) return 0;
		else if (lessThan(this.fun,o.fun)) {
			return -1;
		} else return 1;
	}
	
	public IntegerDoubleSolution getSolution(IntegerDoubleSolution solution) {
		
		for (int i=0; i<fun.length; i++) {
			solution.setObjective(i, Double.parseDouble(fun[i]));
		}
		
		for (int i=0; i<var.length; i++) {
			try {
				solution.setVariableValue(i, NumberFormat.getInstance().parse(var[i]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return solution;
	}
	
	/****************
	 **Static methods
	 ****************
	 */
	
	public static boolean lessThan(String[] fun1, String[] fun2) {
		if(fun1.length != fun2.length) throw new IllegalArgumentException();
		String empty = "";
		for (int i=0; i<fun1.length; i++) {
			
			if(fun1[i].equals(empty) || fun2[i].equals(empty)) {
				return false;
			}
			
			double first = Double.parseDouble(fun1[i]);
			double second = Double.parseDouble(fun2[i]);
			
			int compare = Double.compare(first, second);
			if (compare<0) {
				return true;
			} else if(compare == 0) {
				continue;
			} else {
				return false;
			}
		}
		return false;
	}

	public static List<String[]> getFuns(List<Item> items) {
		List<String[]> funs = new ArrayList<String[]> (items.size());
		
		for (Item item : items) {
			funs.add(filterFun(item.fun));
		}
		
		return funs;
	}
	
	public static List<Item> sample(List<Item> items, int numSamples) {
		List<Item> funs = new ArrayList<Item> (numSamples);
		
		//Sample best and worst
		funs.add(items.get(0));
		funs.add(items.get(items.size()-1));
		
		Randomizer r = RandomizerFactory.createDefaultRandomizer(
				RandomizerUtils.PRIME_SEEDS[0]);
		
		for (int i=0; i<numSamples-2; i++) {
			int next = r.nextInt(items.size());
			funs.add(items.get(next));
		}
		
		return funs;
	}
	
	public static void writeVars(List<Item> items, String paramsFile) throws IOException {
		
		CSVWriter csvw = new CSVWriter(
				new FileWriter(new File(paramsFile)), 
				' ', 
				CSVWriter.NO_QUOTE_CHARACTER);
		
		for (Item item : items) {
			csvw.writeNext(item.var);
		}
		
		csvw.close();
	}
	
	public static void writeFuns(List<Item> items, String paramsFile) throws IOException {
		
		CSVWriter csvw = new CSVWriter(
				new FileWriter(new File(paramsFile)), 
				' ', 
				CSVWriter.NO_QUOTE_CHARACTER);
		
		for (Item item : items) {
			csvw.writeNext(item.fun);
		}
		
		csvw.close();
	}
	
	public static void writeItems(List<Item> items, String output) throws IOException {
		String text = "";
		for (Item i : items) {
			text += i.createString();
		}
		FileWriter writer = new FileWriter(output);
		writer.write(text);
		writer.close();
	}
	
	private static String[] filterFun(String[] fun) {
		if(fun[fun.length-1].equals("")) {
			String[] resized = Arrays.copyOfRange(fun, 0, fun.length-1);
			return resized;
		} else return fun;
	}
	
	public static List<Item> getItemsFromFile(String path) {
		
		List<Item> items = new ArrayList<Item>();
		
		try {
			BufferedReader bfr = new BufferedReader(new FileReader(path));
			String line = bfr.readLine();
			while (line!=null) {
				String[] main = line.split(";");
				String[] fun = main[0].split(" ");
				String[] var = main[1].split(" ");
				
				Item newItem = new Item(fun, var);
				items.add(newItem);
				
				line = bfr.readLine();
			}
			bfr.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Error reading file, returning empty set!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading file, returning empty set!");
			e.printStackTrace();
		}
		
		return items;
	}
	
	public static Item createItem(IntegerDoubleSolution solution) {
		
		int objectives = solution.getNumberOfObjectives();
		int params = solution.getNumberOfVariables();
		
		String[] objs = new String[objectives];
		for (int i=0; i<objectives; i++) {
			objs[i] = String.valueOf(solution.getObjective(i));
		}
		
		String[] variables = new String[params];
		for (int i=0; i<params; i++) {
			variables[i] = String.valueOf(solution.getVariableValue(i));
		}
		
		return new Item(objs, variables);
	}

	// Dominance methods
	public static boolean isDominated(List<Item> items, Item item) {
		if(items.isEmpty()) return false;
		for (Item listItem : items) {
			if(listItem.dominance(item)) {
				return true;
			}
		}
		return false;
	}
	
	public static void add(List<Item> items, Item item) {
		for (Item listItem : items) {
			if(item.dominance(listItem)) {
				items.remove(listItem);
			}
		}
		
		items.add(item);
	}
			
	private boolean dominance(Item otherItem) {
		
		if(otherItem.fun.length != this.fun.length) throw new IllegalArgumentException();
		String empty = "";
		int dominance = 0;
		
		//If they are the same point, it count as dominated.
		if(this.equals(otherItem)) return true;
		
		for (int i=0; i<this.fun.length; i++) {
		//Point dimension fixed at 2.
//		int dimension = 2;
//		for (int i=0; i<dimension; i++) {
			
//			if(otherItem.fun[i].equals(empty) || this.fun[i].equals(empty)) {
//				throw new IllegalStateException("Empty strings!");
//			}
			
			if(otherItem.fun[i].equals(empty) && this.fun[i].equals(empty)) {
				continue;
			}
			
			double first = Double.parseDouble(otherItem.fun[i]);
			double second = Double.parseDouble(this.fun[i]);
			
			int compare = Double.compare(first, second);
			
			// if component ith is greater, it can dominate this
			if (compare>0) {
				if(dominance == 1) {
					return false;
				} else if (dominance == 0) {
					dominance = -1;
				}
			// if it is lower in component ith, could be dominated
			} else if (compare<0) {
				// now, non-dominated
				if(dominance == -1) {
					return false;
				// up to know, it is dominated
				} else if (dominance == 0) {
					dominance = 1;
				}
			}
		}
		
		if(dominance == 0) {
			return false;
		} else return true;
	}
}
