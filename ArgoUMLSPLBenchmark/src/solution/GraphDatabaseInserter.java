package solution;

import static org.neo4j.graphdb.Label.label;
import static org.neo4j.helpers.collection.MapUtil.map;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

/**
 * Neo4j database batch inserter to create the trace graph.
 * 
 * @author Richard Mueller
 *
 */
public class GraphDatabaseInserter extends GraphDatabaseHandler{

	private Map<String, Long> configurationNodeIdMap = null;
	private Map<String, Long> featureNodeIdMap = null;
	private Map<String, Long> classTraceIdMap = null;
	private Map<String, Long> methodTraceIdMap = null;
	
	public GraphDatabaseInserter(File dbFolder) throws RuntimeException, IOException {
		super(dbFolder);
		// delete old trace graph
		if (dbFolder.exists()) {
			shutdown();
			FileUtils.deleteDirectory(dbFolder);
			start();
		}
		this.configurationNodeIdMap = new HashMap<>();
		this.featureNodeIdMap = new HashMap<>();
		this.classTraceIdMap = new HashMap<>();
		this.methodTraceIdMap = new HashMap<>();
	}
	
	/**
	 * Create a trace graph.
	 * 
	 * @param configurationId
	 * @param currentVariantTraces
	 * @param label
	 * @param refinement
	 * @throws IOException
	 */
	public void createTraceGraph(String configurationId, List<String> currentVariantTraces, String label,
			boolean refinement) throws IOException {
		shutdown();
		BatchInserter inserter = null;
		try {
			inserter = BatchInserters.inserter(getDbFolder());
			for (String currentVariantTrace : currentVariantTraces) {
				if (label.equals("Class")) {
					if (classTraceIdMap.containsKey(currentVariantTrace)) {
						// (Configuration)-[:HAS]->(Trace:Class)
						inserter.createRelationship(configurationNodeIdMap.get(configurationId),
								classTraceIdMap.get(currentVariantTrace), RelationshipType.withName("HAS"), null);
					} else {
						// create (Trace:Class)
						Long classTraceNodeId = inserter.createNode(map("value", currentVariantTrace), label("Trace"),
								label(label));
						// (Configuration)-[:HAS]->(Trace:Class)
						inserter.createRelationship(configurationNodeIdMap.get(configurationId), classTraceNodeId,
								RelationshipType.withName("HAS"), null);
						classTraceIdMap.put(currentVariantTrace, classTraceNodeId);
					}
				} else if (label.equals("Method")) {
					String classTrace = currentVariantTrace.split(" ")[0];
					if (classTraceIdMap.containsKey(classTrace)) {
						if (methodTraceIdMap.containsKey(currentVariantTrace)) {
							// (Configuration)-[:HAS]->(Trace:Method)
							inserter.createRelationship(configurationNodeIdMap.get(configurationId),
									methodTraceIdMap.get(currentVariantTrace), RelationshipType.withName("HAS"), null);
						} else {
							// create (Trace:Method)
							Long methodTraceNodeId = inserter.createNode(map("value", currentVariantTrace),
									label("Trace"), label(label));
							// (Configuration)-[:HAS]->(Trace:Method)
							inserter.createRelationship(configurationNodeIdMap.get(configurationId), methodTraceNodeId,
									RelationshipType.withName("HAS"), null);
							methodTraceIdMap.put(currentVariantTrace, methodTraceNodeId);
							// (Trace:Class)-[:DECLARES]->(Trace:Method)
							inserter.createRelationship(classTraceIdMap.get(classTrace), methodTraceNodeId,
									RelationshipType.withName("DECLARES"), null);
						}
					} else {
						// create (Trace:Class)
						Long classTraceNodeId = inserter.createNode(map("value", currentVariantTrace), label("Trace"),
								label(label));
						// (Configuration)-[:HAS]->(Trace:Class)
						inserter.createRelationship(configurationNodeIdMap.get(configurationId), classTraceNodeId,
								RelationshipType.withName("HAS"), null);
						classTraceIdMap.put(currentVariantTrace, classTraceNodeId);
						// create (Trace:Method)
						Long methodTraceNodeId = inserter.createNode(map("value", currentVariantTrace), label("Trace"),
								label(label));
						// (Configuration)-[:HAS]->(Trace:Method)
						inserter.createRelationship(configurationNodeIdMap.get(configurationId), methodTraceNodeId,
								RelationshipType.withName("HAS"), null);
						methodTraceIdMap.put(currentVariantTrace, methodTraceNodeId);
						// (Trace:Class)-[:DECLARES]->(Trace:Method)
						inserter.createRelationship(classTraceIdMap.get(classTrace), methodTraceNodeId,
								RelationshipType.withName("DECLARES"), null);
					}
				}
			}
		} finally {
			if (inserter != null) {
				inserter.shutdown();
			}
		}
	}

	/**
	 * Create configuration node.
	 * 
	 * @param configurationId
	 * @param featureIds
	 * @throws IOException
	 */
	public void createConfigurationNode(String configurationId, List<String> featureIds) throws IOException {
		shutdown();
		BatchInserter inserter = null;
		try {
			inserter = BatchInserters.inserter(getDbFolder());
			Long configurationNodeId = inserter.createNode(
					map("name", configurationId, "value",
							featureIds.toString().replaceAll(", ", "_and_").replace("[", "").replace("]", "")),
					label("Configuration"));
			configurationNodeIdMap.put(configurationId, configurationNodeId);
			// (Configuration)-[:HAS]->(Feature)
			for (String featureId : featureIds) {
				inserter.createRelationship(configurationNodeIdMap.get(configurationId),
						featureNodeIdMap.get(featureId), RelationshipType.withName("HAS"), null);
			}
		} finally {
			if (inserter != null) {
				inserter.shutdown();
			}
		}
	}

	/**
	 * Create feature nodes.
	 * 
	 * @param featureIds
	 * @throws IOException
	 */
	public void createFeatureNodes(List<String> featureIds) throws IOException {
		shutdown();
		BatchInserter inserter = null;
		try {
			inserter = BatchInserters.inserter(getDbFolder());
			for (String featureId : featureIds) {
				Long featureNode = inserter.createNode(map("name", featureId), label("Feature"));
				featureNodeIdMap.put(featureId, featureNode);
			}
			
		} finally {
			if (inserter != null) {
				inserter.shutdown();
			}
		}

	}
}
