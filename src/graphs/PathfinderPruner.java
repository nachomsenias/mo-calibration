package graphs;

import org.apache.commons.math3.stat.StatUtils;

public class PathfinderPruner {

	public static double[][] pruneNetwork(double[][] original, int q, int r) {
		int elements = original.length;
		// Check dimensions
		for (int i = 0; i < elements; i++) {
			if (original[i].length != elements)
				throw new IllegalArgumentException();
		}

		// Matrixes
		double[][][] d = new double[q][elements][elements];
		double[][][] w = new double[q][elements][elements];

		w[0] = original;

		for (int i = 1; i < q; i++) {
			computeWeights(i, w, r);
			computeDistances(i, w, d);
		}

		// W^1 intersection D^q
		for (int j = 0; j < elements; j++) {
			for (int k = 0; k < elements; k++) {
				// If the value at w1 and Dq are different,
				// We prune the edge
				if (j != k && w[0][j][k] != d[q - 1][j][k]) {
					w[0][j][k] = Double.MAX_VALUE;
				}
			}
		}

		return w[0];
	}

	private static void computeWeights(int i, double[][][] w, int r) {
		int elements = w[i].length;
		for (int j = 0; j < elements; j++) {
			for (int k = 0; k < elements; k++) {
				if(j==k) {
					w[i][j][k] = Double.MAX_VALUE;
				} else {
					// Fix j and compute every ((w_jm)^r + (w^i_jk)^r)^1/r
					double[] wm = new double[elements];
					for (int m = 0; m < elements; m++) {
						wm[m] = Math.pow(
								Math.pow(w[0][j][m], r) + Math.pow(w[i - 1][m][k], r),
								1.0 / r);
					}
					w[i][j][k] = StatUtils.min(wm);
				}
			}
		}
	}

	private static void computeDistances(int q, double[][][] w, double[][][] d) {
		int elements = d[0].length;
		for (int j = 0; j < elements; j++) {
			for (int k = 0; k < elements; k++) {
				// Fix j and k: compute Min (w^1_jk, .., w^i_jk) j!=k
				if (j == k) {
//
//					for (int i = 0; i < q; i++) {
//						d[i][j][k] = Double.MAX_VALUE;
//					}
					
					d[q][j][k] = Double.MAX_VALUE;					

				} else {
					double[] di = new double[q];
					for (int i = 0; i < q; i++) {
						di[i] = w[i][j][k];
					}
					d[q][j][k] = StatUtils.min(di);
				}
			}
		}
	}
}
