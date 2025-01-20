package junit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.opencsv.CSVReader;

import graphs.Item;
import graphs.PathfinderPruner;
import graphs.Similarity;
import script.FilterFronts;

@RunWith(Parameterized.class)
public class PrunnerTest {

	private double[][] simMatrix;
	private List<String[]> edges;
	private double[][] inverted;

	public PrunnerTest(double[][] simMatrix, double[][] prunnedMatrix) {
		this.simMatrix = simMatrix;
		this.inverted = Similarity.convertPruned(prunnedMatrix);
		this.edges = Similarity.toEdges(this.inverted);
	}

	@Parameters
	public static Collection<Object[]> data() throws IOException {
		ArrayList<Object[]> list = new ArrayList<Object[]>();

		String baseDir = "/home/ignacio/codingmadafaka/jmetaltest/olimpo/womM/WOMM/data/";

		String problemName = "/MWomABMProblem";

		String[] methods = { "NSGAII", "SPEA2", "MOEAD" };
		int iterations = 10;
		char separator = ' ';

		for (String m : methods) {
			List<Item> items = new ArrayList<Item>();

			for (int i = 0; i < iterations; i++) {
				// Fun files
				String funFile = baseDir + m + problemName + "/FUN" + i + ".tsv";
				String varFile = baseDir + m + problemName + "/VAR" + i + ".tsv";

				// Read CSV values
				CSVReader funReader = new CSVReader(new FileReader(new File(funFile)),
						separator);
				CSVReader varReader = new CSVReader(new FileReader(new File(varFile)),
						separator);

				List<Item> iterationItems = new ArrayList<Item>();
				iterationItems.addAll(FilterFronts.pairItems(funReader.readAll(),
						varReader.readAll()));

				items.addAll(FilterFronts.filterFront(iterationItems));

				funReader.close();
				varReader.close();
			}

			// Generate front and save
			List<Item> paretoFront = FilterFronts.filterFront(items);

			System.out.println("###########################");

			// Compute SIM matrix
			double[][] simMatrix = Similarity.computeMatrix(Double.MAX_VALUE, paretoFront);

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;

			for (double[] rows : simMatrix) {
				double rmin = StatUtils.min(rows);
				double rmax = Similarity.max(rows);
				if (rmin < min) {
					min = rmin;
				}
				if (rmax > max) {
					max = rmax;
				}
				// System.out.println(Arrays.toString(rows));
			}
			System.out.println("Overal Matrix min value: " + min);
			System.out.println("Overal Matrix max value: " + max);

			System.out.println("Similarity values: " + (1 - min) + " and " + (1 - max));

			double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix, simMatrix.length,
					simMatrix.length);

			// double[][] pruned = PathfinderPruner.pruneNetwork(simMatrix,
			// simMatrix.length, Integer.MAX_VALUE);

			double[][] inverted = Similarity.convertPruned(pruned);

			Object[] filePath = { inverted, pruned };
			list.add(filePath);

			System.out.println("Prunned " + m + " fronts");
		}

		return list;
	}

	@Test
	public void maxTest() {
		boolean error = false;
		for (int nodei = 0; nodei < simMatrix.length; nodei++) {
			if (simMatrix[nodei].length != simMatrix.length) {
				error = true;
			}
			double maxValue = StatUtils.max(simMatrix[nodei]);

			int maxIndex = ArrayUtils.indexOf(simMatrix[nodei], maxValue);

			if (inverted[nodei][maxIndex] == -1) {
				error = true;
			}
		}
		Assert.assertFalse(error);
	}

	@Test
	public void edgeTest() {
		for (String[] edge : edges) {
			int x = Integer.parseInt(edge[0]);
			int y = Integer.parseInt(edge[1]);
			double value = Double.parseDouble(edge[2]);
			if (value != inverted[x][y]) {
				Assert.fail();
			}
		}
	}

	@Test
	public void prunnedTest() {
		double[][] visited = new double[inverted.length][inverted.length];
		for (double[] row : visited) {
			Arrays.fill(row, -1);
		}
		for (String[] edge : edges) {
			int x = Integer.parseInt(edge[0]);
			int y = Integer.parseInt(edge[1]);
			double value = Double.parseDouble(edge[2]);

			visited[x][y] = value;
			visited[y][x] = value;
		}
		Assert.assertTrue(Arrays.deepEquals(visited, inverted));
	}
}
