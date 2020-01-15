package solution;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import metricsCalculation.MetricsCalculation;
import utils.FileUtils;

/**
 * Graph-based feature location technique using set theory.
 * 
 * @author Richard Mueller
 */
public class GraphBasedFeatureLocationMain {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final static List<String> RELEVANT_FEATURES = Arrays.asList(new String[] { "ACTIVITYDIAGRAM", "COGNITIVE",
			"COLLABORATIONDIAGRAM", "DEPLOYMENTDIAGRAM", "LOGGING", "not_LOGGING", "not_COGNITIVE", "SEQUENCEDIAGRAM",
			"STATEDIAGRAM", "USECASEDIAGRAM", "ACTIVITYDIAGRAM_and_LOGGING", "ACTIVITYDIAGRAM_and_STATEDIAGRAM",
			"COGNITIVE_and_DEPLOYMENTDIAGRAM", "COGNITIVE_and_LOGGING", "COGNITIVE_and_SEQUENCEDIAGRAM",
			"COLLABORATIONDIAGRAM_and_LOGGING_and_SEQUENCEDIAGRAM", "COLLABORATIONDIAGRAM_and_LOGGING",
			"COLLABORATIONDIAGRAM_and_SEQUENCEDIAGRAM", "DEPLOYMENTDIAGRAM_and_LOGGING",
			"DEPLOYMENTDIAGRAM_and_USECASEDIAGRAM", "LOGGING_and_SEQUENCEDIAGRAM", "LOGGING_and_STATEDIAGRAM",
			"LOGGING_and_USECASEDIAGRAM", "SEQUENCEDIAGRAM_and_STATEDIAGRAM" });

	private final static List<String> SCENARIO_PATHS = Arrays.asList(new String[] { "scenarios/ScenarioOriginalVariant",
			"scenarios/ScenarioPairWiseVariants", "scenarios/ScenarioRandom002Variants",
			"scenarios/ScenarioRandom003Variants", "scenarios/ScenarioRandom004Variants",
			"scenarios/ScenarioRandom005Variants", "scenarios/ScenarioRandom006Variants",
			"scenarios/ScenarioRandom007Variants", "scenarios/ScenarioRandom008Variants",
			"scenarios/ScenarioRandom009Variants", "scenarios/ScenarioRandom010Variants",
			"scenarios/ScenarioRandom050Variants", "scenarios/ScenarioRandom100Variants",
			"scenarios/ScenarioTraditionalVariants", "scenarios/ScenarioAllVariants" });

	public static void main(String[] args) {
		try {
			for (String scenarioPath : SCENARIO_PATHS) {
				// create trace graph
				long startScan = System.currentTimeMillis();
				GraphBasedFeatureLocationTechnique gbfl = new GraphBasedFeatureLocationTechnique(scenarioPath,
						RELEVANT_FEATURES);
				gbfl.createTraceGraph();
				long endScan = System.currentTimeMillis();

				// compute traces
				long startComputation = System.currentTimeMillis();
				gbfl.computeTraces();
				long endComputation = System.currentTimeMillis();

				// write out traces
				String scenarioName = new File(scenarioPath).getName();
				for (String feature : RELEVANT_FEATURES) {
					writeOutTraces(scenarioName, feature, gbfl.getFeatureTraces(feature));
				}
				writeOutMetrics(scenarioName, (endScan - startScan), (endComputation - startComputation));
				LOGGER.info("Finished " + scenarioPath + ", check yourResults folder. Time spent for scan: "
						+ (endScan - startScan) / 1000 + "s, computation: " + (endComputation - startComputation) / 1000
						+ "s");
			}
		} catch (IOException ioe) {
			LOGGER.error(ioe.getMessage());
		} catch (InterruptedException ie) {
			LOGGER.error(ie.getMessage());
		} catch (RuntimeException re) {
			LOGGER.error(re.getMessage());
		}
	}

	private static void writeOutTraces(String scenarioName, String feature, List<String> traces) throws IOException {
		if (!traces.isEmpty()) {
			Collections.sort(traces);
			File resultsFolder = new File("yourResults", scenarioName);
			if (!resultsFolder.exists()) {
				resultsFolder.mkdir();
			}
			File ffile = new File(resultsFolder, feature + ".txt");
			if (ffile.exists()) {
				ffile.delete();
			}
			for (String trace : traces) {
				try {
					FileUtils.appendToFile(ffile, trace);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	private static void writeOutMetrics(String scenarioName, long scanTime, long computationTime) {
		File groundTruth = new File("groundTruth");
		File yourResults = new File("yourResults", scenarioName);
		if (!yourResults.exists()) {
			yourResults.mkdir();
		}
		File yourResultsMetrics = new File("yourResultsMetrics", scenarioName);
		if (!yourResultsMetrics.exists()) {
			yourResultsMetrics.mkdir();
		}
		String results = MetricsCalculation.getResults(groundTruth, yourResults);
		// long current = System.currentTimeMillis();
		File resultsFile = new File(yourResultsMetrics, "resultPrecisionRecall.csv");
		File timeFile = new File(yourResultsMetrics, "time.csv");
		File plotFile = new File(yourResultsMetrics, "plot.txt");
		try {
			// metrics
			FileUtils.writeFile(resultsFile, results);
			// time
			FileUtils.appendToFile(timeFile, "Scenario,Scan,Computation");
			FileUtils.appendToFile(timeFile,
					scenarioName + "," + String.valueOf(scanTime) + "," + String.valueOf(computationTime));
			// gnu plot script
			FileUtils.appendToFile(plotFile,
					"cd '" + resultsFile.getAbsoluteFile().getParentFile().getAbsolutePath() + "'");
			FileUtils.appendToFile(plotFile, "set style data boxplot");
			FileUtils.appendToFile(plotFile, "set datafile sep ','");
			FileUtils.appendToFile(plotFile, "set style boxplot outliers pointtype 6");
			FileUtils.appendToFile(plotFile, "set style fill empty");
			FileUtils.appendToFile(plotFile, "set xtics ('Names' 1, 'Precision' 2, 'Recall' 3, 'FScore' 4) scale 0.0");
			FileUtils.appendToFile(plotFile, "set yrange [-0.04:1.04]");
			FileUtils.appendToFile(plotFile, "set title \"Actual features where nothing was retrieved= "
					+ (int) MetricsCalculation.failedToRetrieve_counter
					+ " out of 24\\nInexistent features where something was retrieved= "
					+ MetricsCalculation.retrievedInexistentFeature_counter + "\\nMetrics for actual features:\"");
			// [i=2:4] because the first column is for names
			// every ::1::24 to ignore the last row with the global results
			FileUtils.appendToFile(plotFile, "plot for [i=2:4] '" + resultsFile.getName()
					+ "' every ::1::24 using (i):i notitle pointsize .8 lc rgb 'black'");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

//	private static void checkTraces(String feature, String scenarioName) {
//		File resultsFolder = new File("yourResults/" + scenarioName);
//		File groundTruthFolder = new File("groundTruth");
//		List<String> foundTraces = FileUtils.getLinesOfFile(new File(resultsFolder, feature + ".txt"));
//		List<String> trueTraces = FileUtils.getLinesOfFile(new File(groundTruthFolder, feature + ".txt"));
//		System.out.println(feature);
//		for (String trueTrace : trueTraces) {
//			if (foundTraces.contains(trueTrace)) {
//				foundTraces.remove(trueTrace);
//			} else {
//				System.out.println("Missing: " + trueTrace);
//			}
//		}
//		for (String foundTrace : foundTraces) {
//			System.out.println("Wrong: " + foundTrace);
//		}
//
//	}
}
