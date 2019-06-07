package solution;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.FeatureUtils;
import utils.FileUtils;
import utils.JQAssistantUtils;
import utils.TraceIdUtils;

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
	private GraphDatabaseHandler variantHandler = null;
	private GraphDatabaseInserter scenarioHandler = null;
	private List<String> featuresToLocate = null;
	private String scenarioPath = null;
	final static List<String> SINGLE_FEATURES = Arrays
			.asList(new String[] { "ACTIVITYDIAGRAM", "COGNITIVE", "COLLABORATIONDIAGRAM", "DEPLOYMENTDIAGRAM",
					"LOGGING", "SEQUENCEDIAGRAM", "STATEDIAGRAM", "USECASEDIAGRAM" });

	public GraphBasedFeatureLocationTechnique(String scenarioPath, List<String> featuresToLocate)
			throws RuntimeException, IOException, InterruptedException {
		// check if variants folder exists
		File variantsFolder = new File(scenarioPath, "variants");
		if (!variantsFolder.exists()) {
			throw new IOException("The variants folder does not exist yet at " + scenarioPath
					+ "\nYou should build the scenario before. Use the Ant scripts in the scenario folder.");
		}
		// init feature utils
		this.utils = new FeatureUtils(scenarioPath);
		// set features to be located
		this.featuresToLocate = featuresToLocate;
		// set scenario path
		this.scenarioPath = scenarioPath;

		// scan variant folders
		createSoftwareGraph();

		// create trace graph for scenario
		createTraceGraph();

		// do set operations
		queryFeatureTraces();

		// write out located traces for each feature
		for (String featureId : featuresToLocate) {
			createTracesFile(featureId);
		}
	}

	private void createSoftwareGraph() throws IOException, InterruptedException {
		// scan source code of variants with jqassistant if not already done
		for (String configurationId : utils.getConfigurationIds()) {
			JQAssistantUtils.scanVariantFolder(utils.getVariantFolderOfConfig(configurationId));
		}
	}

	private void createTraceGraph() throws IOException {
		// show all configurations of scenario
		List<String> availableFeaturesInScenario = new ArrayList<>();
		for (String config : utils.getConfigurationIds()) {
			List<String> featuresInVariant = utils.getFeaturesOfConfiguration(config);
			for (String featureInVariant : featuresInVariant) {
				if (!availableFeaturesInScenario.contains(featureInVariant)) {
					availableFeaturesInScenario.add(featureInVariant);
				}
			}
			LOGGER.info(config + ": " + featuresInVariant);
		}

		// init scenario database for trace database
		scenarioHandler = new GraphDatabaseInserter(
				new File(scenarioPath + JQAssistantUtils.getJQAssistantDatabaseFolder()));
		scenarioHandler.createFeatureNodes(availableFeaturesInScenario);

		// traces for this configuration
		List<String> currentClassTraces = new ArrayList<String>();
		List<String> currentMethodTraces = new ArrayList<String>();

		for (String configurationId : utils.getConfigurationIds()) {
			File variantFolder = utils.getVariantFolderOfConfig(configurationId);

			LOGGER.info("Creating trace graph from variant: " + variantFolder.getPath());

			// start Neo4j instance for variant
			File dbDir = new File(variantFolder.getAbsolutePath() + JQAssistantUtils.getJQAssistantDatabaseFolder());
			variantHandler = new GraphDatabaseHandler(dbDir);

			// query class and method traces
			currentClassTraces = queryClassQualifiedNameTraces();
			currentMethodTraces = queryMethodQualifiedNameTraces();

			// stop Neo4j instance
			variantHandler.shutdown();

			// create trace graph
			scenarioHandler.createConfigurationNode(configurationId, utils.getFeaturesOfConfiguration(configurationId));
			scenarioHandler.createTraceGraph(configurationId, currentClassTraces, "Class", false);
			scenarioHandler.createTraceGraph(configurationId, currentMethodTraces, "Method", false);
		}
	}

	private void queryFeatureTraces() {
		LOGGER.info("Querying feature traces.");

		// query core traces
		scenarioHandler.start();
		scenarioHandler.executeQuery("MATCH (:Configuration{name:'P01_AllDisabled.config'})-[:HAS]->(coreTrace:Trace) "
				+ "SET coreTrace:Core " + "RETURN count(coreTrace)").forEachRemaining(result -> {
					LOGGER.info("Found " + result.get("count(coreTrace)") + " core traces");
				});

		// query feature traces
		scenarioHandler
				.executeQuery("MATCH (coreTrace:Core) " + "WITH collect(coreTrace) as coreTraces "
						+ "MATCH (:Configuration{name:'P02_AllEnabled.config'})-[:HAS]->(featureAndCoreTrace:Trace) "
						+ "WITH coreTraces, collect(featureAndCoreTrace) as featureAndCoreTraces "
						+ "WITH apoc.coll.subtract(featureAndCoreTraces, coreTraces) as featureTraces "
						+ "FOREACH (f in featureTraces | SET  f:FeatureTrace ) " + "RETURN size(featureTraces)")
				.forEachRemaining(result -> {
					LOGGER.info("Found " + result.get("size(featureTraces)") + " feature traces");
				});

		// query exclusive and interaction feature traces
		for (String feature : SINGLE_FEATURES) {
			scenarioHandler.executeQuery("MATCH (featureTrace:FeatureTrace) "
					+ "WITH collect(featureTrace) as allFeatureTraces " + "MATCH (notFeatureNode:Feature{name:'not_"
					+ feature + "'})<-[:HAS]-(:Configuration)-[:HAS]->(notFeatureTrace:FeatureTrace) "
					+ "WITH allFeatureTraces, collect(notFeatureTrace) as notFeatureTraces "
					+ "WITH apoc.coll.subtract(allFeatureTraces, notFeatureTraces) as featureTraces "
					+ "WITH featureTraces, [(mt:Method)<-[DECLARES]-(ct:Class) WHERE NOT ct in featureTraces AND mt in featureTraces | mt] as featureMethodTraces, [ct IN featureTraces WHERE ct:Class| ct] as featureClassTraces "
					+ "MATCH (featureNode:Feature {name:'" + feature + "'}) "
					+ "WITH (featureMethodTraces + featureClassTraces) as filteredfeatureTraces, featureNode "
					+ "FOREACH (ft in (filteredfeatureTraces) |  CREATE (featureNode)-[:HAS{value:'EXCLUSIVE'}]->(ft)) "
					+ "RETURN size(filteredfeatureTraces)").forEachRemaining(result -> {
						LOGGER.info("Found " + result.get("size(filteredfeatureTraces)") + " " + feature + " traces");
					});
		}
		// separate interaction traces
		for (int i = 0; i < SINGLE_FEATURES.size(); i++) {
			for (int j = i + 1; j < SINGLE_FEATURES.size(); j++) {
				String feature1 = SINGLE_FEATURES.get(i);
				String feature2 = SINGLE_FEATURES.get(j);
				scenarioHandler.executeQuery("MATCH (f1:Feature{name:'" + feature1
						+ "'})-[r1:HAS]->(trace:FeatureTrace) " + "MATCH (f2:Feature{name:'" + feature2
						+ "'})-[r2:HAS]->(trace:FeatureTrace) " + "WITH trace, r1, r2 "
						+ "SET r1.value = 'AND', r2.value = 'AND'  " + "RETURN count(trace)")
						.forEachRemaining(result -> {
							LOGGER.info("Found " + result.get("count(trace)") + " interaction " + feature1 + " and "
									+ feature2 + " traces");
						});
			}
		}

		// write out traces
		for (String featureId : featuresToLocate) {
			List<String> traces = new ArrayList<String>();
			if (!utils.isCombinedFeature(featureId) && !featureId.contains("not")) {
				scenarioHandler
						.executeQuery("MATCH (featureNode:Feature{name:'" + featureId
								+ "'})-[r:HAS]->(featureTrace:FeatureTrace) "
								+ "WHERE r.value='OR' OR r.value='EXCLUSIVE'" + "RETURN featureTrace.value")
						.forEachRemaining(result -> {
							traces.add(result.get("featureTrace.value").toString());
						});
			} else if (utils.isCombinedFeature(featureId)) {
				List<String> singleFeatures = Arrays.asList(featureId.split("_and_"));

				if (singleFeatures.size() == 2) {
					scenarioHandler.executeQuery("MATCH (:Feature{name:'" + singleFeatures.get(0)
							+ "'})-[r1:HAS{value:'AND'}]->(featureTrace:FeatureTrace) " + "MATCH (:Feature{name:'"
							+ singleFeatures.get(1) + "'})-[r2:HAS{value:'AND'}]->(featureTrace:FeatureTrace) "
							+ "RETURN DISTINCT featureTrace.value").forEachRemaining(result -> {
								traces.add(result.get("featureTrace.value").toString());
							});
				} else {
					scenarioHandler.executeQuery("MATCH (:Feature{name:'" + singleFeatures.get(0)
							+ "'})-[r1:HAS{value:'AND'}]->(featureTrace:FeatureTrace) " + "MATCH (:Feature{name:'"
							+ singleFeatures.get(1) + "'})-[r2:HAS{value:'AND'}]->(featureTrace:FeatureTrace) "
							+ "MATCH (:Feature{name:'" + singleFeatures.get(2)
							+ "'})-[r3:HAS{value:'AND'}]->(featureTrace:FeatureTrace) "
							+ "RETURN DISTINCT featureTrace.value").forEachRemaining(result -> {
								traces.add(result.get("featureTrace.value").toString());
							});
				}
			}
			setFeatureTraces(featureId, traces);
		}
		scenarioHandler.shutdown();
	}

	private List<String> queryClassQualifiedNameTraces() {
		List<String> currentVariantTraces = new ArrayList<>();
		String typeNameQuery = "MATCH (type:Type)" + " WHERE type.fqn STARTS WITH 'org.argouml'"
				+ " AND NOT (:Type)-[:DECLARES]->(type)" + " AND NOT type.fqn CONTAINS '$'"
				+ " AND NOT type.fqn CONTAINS 'Anonymous'" + " AND NOT size(type.name) = 1"
				+ " RETURN DISTINCT type.fqn as type";

		variantHandler.executeQuery(typeNameQuery).forEachRemaining((record) -> {
			currentVariantTraces.add(TraceIdUtils.getId(record));

		});
		LOGGER.info("Found " + currentVariantTraces.size() + " qualified class name traces.");
		return currentVariantTraces;
	}

	private List<String> queryMethodQualifiedNameTraces() throws IOException {
		List<String> currentVariantTraces = new ArrayList<>();
		String methodNameQuery = "MATCH (type:Type)-[:DECLARES]->(method:Method)"
				+ " WHERE type.fqn STARTS WITH 'org.argouml'" + " AND NOT (:Type)-[:DECLARES]->(type)"
				+ " AND NOT type.fqn CONTAINS '$'" + " AND NOT type.fqn CONTAINS 'Anonymous'"
				+ " AND NOT size(type.name) = 1"
				+ " RETURN DISTINCT type.fqn as type, method.name as method, method.signature as signature";

		variantHandler.executeQuery(methodNameQuery).forEachRemaining((record) -> {
			currentVariantTraces.add(TraceIdUtils.getId(record));

		});
		LOGGER.info("Found " + currentVariantTraces.size() + " qualified method name traces.");
		return currentVariantTraces;
	}

	private void setFeatureTraces(String featureId, List<String> featureTraces) {
		featureTraceMap.put(featureId, featureTraces);
	}

	private List<String> getFeatureTraces(String featureId) {
		if (featureTraceMap.get(featureId) != null) {
			return featureTraceMap.get(featureId);
		} else {
			return new ArrayList<>();
		}
	}

	private void createTracesFile(String featureId) {
		// if traces were found, write the results in the file
		if (!getFeatureTraces(featureId).isEmpty()) {
			List<String> traces = getFeatureTraces(featureId);
			Collections.sort(traces);
			File resultsFolder = new File("yourResults");
			File ffile = new File(resultsFolder, featureId + ".txt");
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
}
