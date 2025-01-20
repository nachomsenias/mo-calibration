package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.util.measure.impl.SimplePushMeasure;

public class CustomPopulationMeasure<S> extends SimplePushMeasure<S> {

	/**
	 * Generated
	 */
	private static final long serialVersionUID = -6056805615259968822L;

	private String path;
	private String index;

	private StoreIndividual<S> individual;

	public CustomPopulationMeasure(String path, String index,
			StoreIndividual<S> individual) {
		this.path = path;
		this.index = index;

		register(individual);
		this.individual = individual;
	}

	public void writePopulation() throws IOException {
		List<String> population = individual.getIndividuals();

		File output = new File(path);
		output.mkdirs();
		
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(
						path + "/Pop_" + index + ".csv"
						)
				);

		for (String s : population) {
			bw.write(s);
			bw.newLine();
		}

		bw.close();
	}
}
