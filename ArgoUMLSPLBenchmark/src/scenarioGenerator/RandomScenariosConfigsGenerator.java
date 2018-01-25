package scenarioGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import utils.FileUtils;

/**
 * Random generator using a specific seed. We randomly select configurations
 * from All possible configurations without repeating them. Then we check if
 * they cover all the features. If the random selection does not cover all
 * features, we repeat again the process.
 * 
 * @author jabier.martinez
 */
public class RandomScenariosConfigsGenerator {

	static Random random = new Random();

	public static void main(String[] args) {
		random.setSeed(1);
		getRandom(100);
		getRandom(50);
		getRandom(10);
		getRandom(9);
		getRandom(8);
		getRandom(7);
		getRandom(6);
		getRandom(5);
		getRandom(4);
		getRandom(3);
		getRandom(2);
	}

	public static void getRandom(int numberOfVariants) {
		System.out.println("Creating Random Scenario with " + numberOfVariants + " variants.");
		List<String> features = new ArrayList<String>();
		features.add("COGNITIVE");
		features.add("LOGGING");
		features.add("ACTIVITYDIAGRAM");
		features.add("STATEDIAGRAM");
		features.add("SEQUENCEDIAGRAM");
		features.add("USECASEDIAGRAM");
		features.add("COLLABORATIONDIAGRAM");
		features.add("DEPLOYMENTDIAGRAM");

		File configs = new File("scenarios/ScenarioAllVariants/configs");

		boolean found = false;
		List<Integer> selected = null;

		while (!found) {
			// Get random indexes
			selected = new ArrayList<Integer>();
			while (selected.size() != numberOfVariants) {
				Integer integer = random.nextInt(configs.listFiles().length);
				if (!selected.contains(integer)) {
					selected.add(integer);
				}
			}

			// Check that all features are covered
			List<String> coveredFeatures = new ArrayList<String>();
			for (Integer sel : selected) {
				File selFile = configs.listFiles()[sel];
				List<String> selFileFeatures = FileUtils.getLinesOfFile(selFile);
				for (String f : selFileFeatures) {
					if (!coveredFeatures.contains(f)) {
						coveredFeatures.add(f);
						if (coveredFeatures.containsAll(features)) {
							found = true;
							break;
						}
					}
				}
			}
		}

		// We have the selected
		// Create folder
		File scenario = new File("scenarios/ScenarioRandom" + getNumberWithZeros(numberOfVariants, 256) + "Variants");
		if (!scenario.exists()) {
			scenario.mkdirs();
		}

		File confFolder = new File(scenario, "configs");
		if (!confFolder.exists()) {
			confFolder.mkdirs();
		}

		int i = 1;
		for (Integer s : selected) {
			File sourceFile = configs.listFiles()[s];
			File destinationFile = new File(confFolder, getNumberWithZeros(i, 99999) + ".config");
			FileUtils.copyFile(sourceFile, destinationFile);
			i++;
		}
		System.out.println("Finished");
	}

	public static String getNumberWithZeros(int number, int maxNumber) {
		String _return = String.valueOf(number);
		for (int zeros = _return.length(); zeros < String.valueOf(maxNumber).length(); zeros++) {
			_return = "0" + _return;
		}
		return _return;
	}

}
