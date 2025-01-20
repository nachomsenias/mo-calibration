package script.generation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import model.touchpoints.TouchPointOwned.InvestmentType;
import util.StringBean;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class GenerateNewTouchPoints {

	private static double[] reductions = { 0.15, 0.3, 0.45, 0.6 };
	private static double[] increments = { 1.0, 2.0, 3.0, 4.0 };

	private static Randomizer random = RandomizerFactory.createRandomizer(
			RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[0]);

	public static void main(String[] args) {
		if (args.length == 2) {
			try {
				String config = CSVFileUtils.readFile(args[0]);

				Gson gson = new Gson();

//				int[] newTouchpoints = { 5, 10, 15, 20, 25 };
				int[] newTouchpoints = { 7, 12, 17, 22, 30, 35, 40, 45, 50};

				for (int tp : newTouchpoints) {

					// Read configuration from JSON
					CalibrationConfig calibrationConfig = gson.fromJson(config,
							CalibrationConfig.class);

					includeTouchpoints(calibrationConfig, tp);

					modifyAwarenessHistory(calibrationConfig);
					modifyWOMHistory(calibrationConfig);

					CSVFileUtils.writeFile(args[1] + "_new" + tp + "touchpoints.json",
							gson.toJson(calibrationConfig, CalibrationConfig.class));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: input_json modified_base_name");
			System.exit(1);
		}
	}

	private static void includeTouchpoints(CalibrationConfig cc, int newTouchpoints) {
		int brands = cc.getSimConfig().getnBrands();
		int tps = cc.getSimConfig().getnTp();

		// Generate GRP
		double[][][][] originalGRPs = cc.getSimConfig().getTouchPointsGRPMarketingPlan();
		double[][][][] newGRPs = new double[newTouchpoints][brands][][];
		
		double[][][][] tpEmphasis = new double[newTouchpoints][brands][][];
		double[][][][] tpQuality = new double[newTouchpoints][brands][][];

		// Rest of parameters
		double[][] touchPointsWeeklyReachMax = new double[newTouchpoints][1];
		double[][] touchPointsAnnualReachMax = new double[newTouchpoints][1];
		double[][] touchPointsAnnualReachSpeed = new double[newTouchpoints][1];

		double[][] touchPointsPerceptionPotential = new double[newTouchpoints][1];
		double[][] touchPointsPerceptionSpeed = new double[newTouchpoints][1];
		double[][] touchPointsPerceptionDecay = new double[newTouchpoints][1];
		
		double[][] touchPointsAwarenessImpact = new double[newTouchpoints][1];
		double[][] touchPointsDiscusionHeatImpact = new double[newTouchpoints][1];
		double[][] touchPointsDiscusionHeatDecay = new double[newTouchpoints][1];

		StringBean[] newBeans = new StringBean[newTouchpoints * 3];
		int beancounter = 0;

		String description = "";

		for (int ntp = 0; ntp < newTouchpoints; ntp++) {
			int chosenTp = random.nextInt(tps);

			description += "Touchpoint " + (ntp + tps)
					+ " is a modification of Touchpoint " + chosenTp + ".\n";

			// Generate GRP
			for (int b = 0; b < brands; b++) {
				boolean increment = random.nextBoolean();
				int variant = random.nextInt(increments.length);
				if (increment) {
					newGRPs[ntp][b] = MatrixFunctions.scaleCopyOfDoubleMatrix(
							originalGRPs[chosenTp][b], 1.0 + increments[variant]);
					description += "* Brand " + b
							+ " increments the original brand investment in Touchpoint "
							+ chosenTp + " by " + toPercentage(increments[variant])
							+ "%.\n";
				} else {
					newGRPs[ntp][b] = MatrixFunctions.scaleCopyOfDoubleMatrix(
							originalGRPs[chosenTp][b], 1.0 - reductions[variant]);
					description += "* Brand " + b
							+ " reduces the original brand investment in Touchpoint "
							+ chosenTp + " by " + toPercentage(reductions[variant])
							+ "%.\n";
				}
			}

			// Rest of parameters
			touchPointsWeeklyReachMax[ntp] = cc.getSimConfig()
					.getTouchPointsWeeklyReachMax()[chosenTp];
			touchPointsAnnualReachMax[ntp] = cc.getSimConfig()
					.getTouchPointsAnnualReachMax()[chosenTp];
			touchPointsAnnualReachSpeed[ntp] = cc.getSimConfig()
					.getTouchPointsAnnualReachSpeed()[chosenTp];

			touchPointsPerceptionPotential[ntp] = cc.getSimConfig()
					.getTouchPointsPerceptionPotential()[chosenTp];
			touchPointsPerceptionSpeed[ntp] = cc.getSimConfig()
					.getTouchPointsPerceptionSpeed()[chosenTp];
			touchPointsPerceptionDecay[ntp] = cc.getSimConfig()
					.getTouchPointsPerceptionDecay()[chosenTp];
			
			touchPointsAwarenessImpact[ntp] = cc.getSimConfig()
					.getTouchPointsAwarenessImpact()[chosenTp];
			touchPointsDiscusionHeatImpact[ntp] = cc.getSimConfig()
					.getTouchPointsDiscusionHeatImpact()[chosenTp];
			touchPointsDiscusionHeatDecay[ntp] = cc.getSimConfig()
					.getTouchPointsDiscusionHeatDecay()[chosenTp];

			tpEmphasis[ntp] = cc.getSimConfig().getTouchPointsEmphasis()[chosenTp];
			tpQuality[ntp] = cc.getSimConfig().getTouchPointsQuality()[chosenTp];
			
			newBeans[beancounter] = new StringBean(
					"TouchPointAwarenessImpact_" + (ntp + tps) + "_0", "0.0,1,0.001");
			beancounter++;
			newBeans[beancounter] = new StringBean(
					"TouchPointDiscusionHeatImpact_" + (ntp + tps) + "_0", "0.0,1,0.001");
			beancounter++;
			newBeans[beancounter] = new StringBean(
					"TouchPointDiscusionHeatDecay_" + (ntp + tps) + "_0", "0.0,1,0.001");
			beancounter++;
		}

		// Merge arrays and modify base model values
		cc.getSimConfig().setTouchPointsGRPMarketingPlan(
				MatrixFunctions.pasteDouble4DMatrixes(originalGRPs, newGRPs));

		cc.getSimConfig()
				.setTouchPointsWeeklyReachMax(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsWeeklyReachMax(),
						touchPointsWeeklyReachMax));
		cc.getSimConfig()
				.setTouchPointsAnnualReachMax(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsAnnualReachMax(),
						touchPointsAnnualReachMax));
		cc.getSimConfig()
				.setTouchPointsAnnualReachSpeed(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsAnnualReachSpeed(),
						touchPointsAnnualReachSpeed));

		cc.getSimConfig()
				.setTouchPointsPerceptionPotential(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsPerceptionPotential(),
						touchPointsPerceptionPotential));
		cc.getSimConfig()
				.setTouchPointsPerceptionSpeed(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsPerceptionSpeed(),
						touchPointsPerceptionSpeed));
		cc.getSimConfig()
				.setTouchPointsPerceptionDecay(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsPerceptionDecay(),
						touchPointsPerceptionDecay));
		
		cc.getSimConfig()
				.setTouchPointsAwarenessImpact(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsAwarenessImpact(),
						touchPointsAwarenessImpact));
		cc.getSimConfig()
				.setTouchPointsDiscusionHeatImpact(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsDiscusionHeatImpact(),
						touchPointsDiscusionHeatImpact));
		cc.getSimConfig()
				.setTouchPointsDiscusionHeatDecay(MatrixFunctions.pasteDoubleMatrixes(
						cc.getSimConfig().getTouchPointsDiscusionHeatDecay(),
						touchPointsDiscusionHeatDecay));

		cc.getSimConfig().setTouchPointsEmphasis(MatrixFunctions.pasteDouble4DMatrixes(
				cc.getSimConfig().getTouchPointsEmphasis(), tpEmphasis));
		cc.getSimConfig().setTouchPointsEmphasis(MatrixFunctions.pasteDouble4DMatrixes(
				cc.getSimConfig().getTouchPointsQuality(), tpQuality));
		
		cc.getSimConfig().setnTp(tps + newTouchpoints);
		cc.setDescription(description);
		cc.setCalibrationModelParameters(
				pasteBeans(cc.getCalibrationModelParameters(), newBeans));
		
		InvestmentType[] investment = new InvestmentType[tps + newTouchpoints];
		Arrays.fill(investment, InvestmentType.GRP);
		cc.getSimConfig().setTouchPointsInvestment(investment);
	}

	private static void modifyAwarenessHistory(CalibrationConfig cc) {
		String description = cc.getDescription();
		int brands = cc.getSimConfig().getnBrands();

		double[] variation = { 2.0, 5.0, 8.0, 10.0 };
		double[][] awarenessHistory = cc.getTargetAwareness();

		description += "Awareness history is modified as following:\n";

		for (int b = 0; b < brands; b++) {
			double modifier = variation[random.nextInt(variation.length)];
			boolean increase = random.nextBoolean();
			if (increase) {
				description += "* Brand " + b
						+ " increments the original brand awareness value by "
						+ toPercentage(modifier) + "%.\n";
				for (int w = 0; w < awarenessHistory[b].length; w++) {
					awarenessHistory[b][w] = truncate100(awarenessHistory[b][w],
							modifier);
				}
			} else {
				description += "* Brand " + b
						+ " decreases the original brand awareness value by "
						+ toPercentage(modifier) + "%.\n";
				for (int w = 0; w < awarenessHistory[b].length; w++) {
					awarenessHistory[b][w] = truncate0(awarenessHistory[b][w], modifier);
				}
			}
		}

		cc.setTargetAwareness(awarenessHistory);
		cc.setDescription(description);
	}

	private static void modifyWOMHistory(CalibrationConfig cc) {
		String description = cc.getDescription();
		int brands = cc.getSimConfig().getnBrands();

		double[] variation = { 1000.0, 2000.0, 4000.0, 6000.0 };
		double[][] womHistory = cc.getTargetWOMVolumen();

		description += "WOM history is modified as following:\n";

		for (int b = 0; b < brands; b++) {
			double modifier = variation[random.nextInt(variation.length)];
			boolean increase = random.nextBoolean();
			if (increase) {
				description += "* Brand " + b
						+ " increments the original brand wom volume by " + modifier
						+ ".\n";
				for (int w = 0; w < womHistory[b].length; w++) {
					womHistory[b][w] = womHistory[b][w] + modifier;
				}
			} else {
				description += "* Brand " + b
						+ " decreases the original brand wom volume by " + modifier
						+ ".\n";
				for (int w = 0; w < womHistory[b].length; w++) {
					womHistory[b][w] = truncate0(womHistory[b][w], modifier);
				}
			}
		}

		cc.setTargetWOMVolumen(womHistory);
		cc.setDescription(description);
	}

	private static double truncate100(double original, double increment) {
		double value = original + increment;
		if (value > 100.0)
			return 100.0;
		else
			return value;
	}

	private static double truncate0(double original, double increment) {
		double value = original - increment;
		if (value < 1.0)
			return 1.0;
		else
			return value;
	}

	private static String toPercentage(double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(value * 100);
	}

	private static StringBean[] pasteBeans(StringBean[] first, StringBean[] second) {
		StringBean[] result = new StringBean[first.length + second.length];
		for (int i = 0; i < first.length; i++) {
			result[i] = first[i];
		}
		for (int j = first.length; j < first.length + second.length; j++) {
			result[j] = second[j - first.length];
		}
		return result;
	}
}
