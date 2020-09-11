package solution.technique;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solution.neo4j.GraphDatabaseHandler;
import solution.neo4j.GraphDatabaseInserter;
import solution.parser.JavaSourceCodeParser;
import utils.FeatureUtils;

/**
 * Creates software graphs for variants of a scenario, a trace graph, queries
 * feature traces, and writes identified feature traces in the folder
 * yourResults.
 * 
 * @author Richard Mueller
 *
 */
public class GraphBasedFeatureLocationTechnique {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Map<String, List<String>> featureTraceMap = new LinkedHashMap<String, List<String>>();
	private FeatureUtils utils = null;
	private GraphDatabaseHandler scenarioHandler = null;
	private List<String> featuresToLocate = null;
	private String scenarioPath = null;
	private static final String JQASSISTANT_DATABASE_FOLDER = "/jqassistant/store";
	private Path graphDbFolder = null;

	public GraphBasedFeatureLocationTechnique(String scenarioPath, List<String> featuresToLocate)
			throws RuntimeException, IOException, InterruptedException {
		// check if variants folder exists
		File variantsFolder = new File(scenarioPath, "variants");
		if (!variantsFolder.exists()) {
			throw new IOException("The variants folder does not exist yet at " + scenarioPath
					+ "\nYou should build the scenario before. Use the Ant scripts in the scenario folder.");
		}
		this.utils = new FeatureUtils(scenarioPath);
		this.featuresToLocate = featuresToLocate;
		this.scenarioPath = scenarioPath;
		this.graphDbFolder = Paths.get(new File(scenarioPath).getAbsolutePath() + JQASSISTANT_DATABASE_FOLDER);
	}

	public void createTraceGraph() throws RuntimeException, IOException, InterruptedException {
		if (Files.notExists(graphDbFolder)) {
			LOGGER.info("Create trace graph for scenario " + scenarioPath);
			GraphDatabaseInserter databaseInserter = new GraphDatabaseInserter(graphDbFolder.toFile());
			// create a node for each variant
			databaseInserter.createConfigurations(utils.getConfigurationIds());
			// scan variants
			JavaSourceCodeParser scanner = new JavaSourceCodeParser(databaseInserter, scenarioPath,
					utils.getConfigurationIds());
			scanner.scanScenarioVariants();
			databaseInserter.shutdown();
		} else { // source code of scenario was scanned
			LOGGER.info("Scenario " + scenarioPath + " has already been scanned.");
		}
	}

	public void computeTraces() {
		LOGGER.info("Compute feature traces for " + featuresToLocate);
		scenarioHandler = new GraphDatabaseHandler(graphDbFolder.toFile());
		Map<String, List<String>> elementarySetTraceMap = new HashMap<String, List<String>>();
		for (String feature : featuresToLocate) {
			List<String> elementarySets = utils.getElementarySetsOfFeature(feature);
			List<String> featureTraces = new ArrayList<String>();
			for (String elementarySet : elementarySets) {
				if (elementarySetTraceMap.containsKey(elementarySet)) {
					featureTraces.addAll(elementarySetTraceMap.get(elementarySet));
				} else {
					List<String> elementaryClassTraces = new ArrayList<String>();
					List<String> elementaryClassRefinementTraces = new ArrayList<String>();
					List<String> elementaryMethodTraces = new ArrayList<String>();
					List<String> elementaryMethodRefinementTraces = new ArrayList<String>();
					List<String> elementarySetTraces = new ArrayList<String>();
					List<String> minuends = utils.getMinuendsOfElementarySet(elementarySet);
					List<String> subtrahends = utils.getSubtrahendsOfElementarySet(elementarySet);
					// remove missing configs
					minuends.removeIf(Objects::isNull);
					subtrahends.removeIf(Objects::isNull);

					if (!minuends.isEmpty() && !subtrahends.isEmpty()) {
						List<String> classAndMethodTraces = new ArrayList<String>();
						
						elementaryClassTraces.addAll(applySetOperations(minuends, subtrahends, "Class"));
						classAndMethodTraces.addAll(elementaryClassTraces);
						elementaryClassTraces
								.removeIf(trace -> classAndMethodTraces.stream().anyMatch(t -> trace.startsWith(t)
										&& trace.length() > t.length() && (trace.charAt(t.length()) == '.')));

						elementaryClassRefinementTraces
								.addAll(applySetOperations(minuends, subtrahends, "ClassRefinement"));
						elementaryClassRefinementTraces
								.removeIf(trace -> classAndMethodTraces.contains(trace.replace(" Refinement", "")));

						elementaryMethodTraces.addAll(applySetOperations(minuends, subtrahends, "Method"));
						classAndMethodTraces.addAll(elementaryMethodTraces);
						elementaryMethodTraces.removeIf(trace -> classAndMethodTraces.contains(trace.split(" ")[0]));

						elementaryMethodRefinementTraces
								.addAll(applySetOperations(minuends, subtrahends, "MethodRefinement"));
						elementaryMethodRefinementTraces
								.removeIf(trace -> classAndMethodTraces.contains(trace.split(" ")[0])
										|| classAndMethodTraces.contains(trace.replace(" Refinement", "")));

						elementarySetTraces = Stream
								.of(elementaryClassTraces, elementaryClassRefinementTraces, elementaryMethodTraces,
										elementaryMethodRefinementTraces)
								.flatMap(Collection::stream).collect(Collectors.toList());
						elementarySetTraceMap.put(elementarySet, elementarySetTraces);
						featureTraces.addAll(elementarySetTraces);
					}
				}
			}
			setFeatureTraces(feature, featureTraces);
			LOGGER.info("Found " + getFeatureTraces(feature).size() + " traces for " + feature);
		}
		scenarioHandler.shutdown();
	}

	public List<String> getFeatureTraces(String feature) {
		if (featureTraceMap.get(feature) != null) {
			return featureTraceMap.get(feature);
		} else {
			return new ArrayList<>();
		}
	}

	private void setFeatureTraces(String feature, List<String> traces) {
		List<String> featureTraces = featureTraceMap.get(feature);
		if (featureTraces == null) {
			featureTraces = new ArrayList<String>();
			featureTraceMap.put(feature, featureTraces);
		}
		for (String trace : traces) {
			if (!featureTraces.contains(trace)) {
				featureTraces.add(trace);
			}
		}
	}

	private List<String> applySetOperations(List<String> minuendConfigs, List<String> subtrahendConfigs, String label) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("subtrahendConfigs", subtrahendConfigs);

		// union of subtrahends
		scenarioHandler
				.executeQuery("MATCH (config:Configuration)-[:HAS]->(trace:" + label + ") "
						+ "WHERE config.name IN {subtrahendConfigs} "
						+ "RETURN collect(DISTINCT ID(trace)) AS subtrahendTraceIds", params)
				.forEachRemaining(result -> {
					params.put("subtrahendTraceIds", (List<String>) result.get("subtrahendTraceIds"));
				});

		// intersection of minuends
		for (String minuendConfig : minuendConfigs) {
			if (params.get("traceIds") == null) {
				scenarioHandler.executeQuery("MATCH (config:Configuration{name:'" + minuendConfig
						+ "'})-[:HAS]->(trace:" + label + ") " + "WHERE NOT ID(trace) IN {subtrahendTraceIds} "
						+ "RETURN collect(DISTINCT ID(trace)) AS traceIds", params).forEachRemaining(result -> {
							params.put("traceIds", (List<String>) result.get("traceIds"));
						});
			} else if (!((List<String>) params.get("traceIds")).isEmpty()) {
				scenarioHandler
						.executeQuery(
								"MATCH (config:Configuration{name:'" + minuendConfig + "'})-[:HAS]->(trace:" + label
										+ ") " + "WHERE NOT ID(trace) IN {subtrahendTraceIds} "
										+ "WITH collect(DISTINCT ID(trace)) AS minuendTraceIds "
										+ "RETURN apoc.coll.intersection({traceIds}, minuendTraceIds) AS traceIds",
								params)
						.forEachRemaining(result -> {
							params.put("traceIds", (List<String>) result.get("traceIds"));
						});
			} else {
				return new ArrayList<String>();
			}
		}
		// transform id into trace
		List<String> traces = new ArrayList<String>();
		scenarioHandler.executeQuery("MATCH (trace:Trace) " + "WHERE ID(trace) IN {traceIds} "
				+ "RETURN collect(DISTINCT trace.name) AS traces", params).forEachRemaining(result -> {
					traces.addAll((List<String>) result.get("traces"));
				});
		return traces;
	}
}
