package solution.neo4j;

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
public class GraphDatabaseInserter extends GraphDatabaseHandler {

	private Map<String, Long> configurationNodeIdMap = null;
	private Map<String, Long> classTraceIdMap = null;
	private Map<String, Long> methodTraceIdMap = null;
	private Map<String, Long> fieldTraceIdMap = null;
	private Map<String, Long> importTraceIdMap = null;
	private Map<String, Long> methodRefinementTraceIdMap = null;

	public GraphDatabaseInserter(File dbFolder) throws RuntimeException, IOException {
		super(dbFolder);
		// delete old trace graph
		if (dbFolder.exists()) {
			shutdown();
			FileUtils.deleteDirectory(dbFolder);
			start();
		}
		this.configurationNodeIdMap = new HashMap<>();
		this.classTraceIdMap = new HashMap<>();
		this.methodTraceIdMap = new HashMap<>();
		this.fieldTraceIdMap = new HashMap<>();
		this.importTraceIdMap = new HashMap<>();
		this.methodRefinementTraceIdMap = new HashMap<>();
	}

	public void createConfigurations(List<String> configurations) throws IOException {
		shutdown();
		BatchInserter inserter = null;
		try {
			inserter = BatchInserters.inserter(getDbFolder());
			for (String configuration : configurations) {
				// create (Configuration)
				Long configurationNodeId = inserter.createNode(map("name", configuration, "value", configuration),
						label("Configuration"));
				configurationNodeIdMap.put(configuration, configurationNodeId);
			}
		} finally {
			if (inserter != null) {
				inserter.shutdown();
			}
		}
	}

	public void createTraces(String configuration, Map<String, List<String>> traces) throws IOException {
		shutdown();
		BatchInserter inserter = null;
		try {
			inserter = BatchInserters.inserter(getDbFolder());
			// class traces
			List<String> classTraces = traces.get("class");
			for (String classTrace : classTraces) {
				if (classTraceIdMap.containsKey(classTrace)) {
					// (Configuration)-[:HAS]->(Trace:Class)
					inserter.createRelationship(configurationNodeIdMap.get(configuration),
							classTraceIdMap.get(classTrace), RelationshipType.withName("HAS"), null);
				} else {
					// create (Trace:Class)
					Long classTraceNodeId = inserter.createNode(map("name", classTrace, "value", classTrace),
							label("Trace"), label("Class"));
					// (Configuration)-[:HAS]->(Trace:Class)
					inserter.createRelationship(configurationNodeIdMap.get(configuration), classTraceNodeId,
							RelationshipType.withName("HAS"), null);
					classTraceIdMap.put(classTrace, classTraceNodeId);
				}
			}
			// field traces
			List<String> fieldTraces = traces.get("field");
			for (String fieldTrace : fieldTraces) {
				String[] field = fieldTrace.split("__");
				if (fieldTraceIdMap.containsKey(fieldTrace)) {
					// (Configuration)-[:HAS]->(Trace:Field:ClassRefinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration),
							fieldTraceIdMap.get(fieldTrace), RelationshipType.withName("HAS"), null);
				} else {
					// create (Trace:Field:ClassRefinement)
					Long fieldTraceNodeId = inserter.createNode(
							map("name", field[0] + " Refinement", "value", fieldTrace), label("Trace"),
							label("ClassRefinement"), label("Field"));
					// (Configuration)-[:HAS]->(Trace:Field:Refinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration), fieldTraceNodeId,
							RelationshipType.withName("HAS"), null);
					fieldTraceIdMap.put(fieldTrace, fieldTraceNodeId);
				}
			}
			// import traces
			List<String> importTraces = traces.get("import");
			for (String importTrace : importTraces) {
				String[] imp = importTrace.split("__");
				if (importTraceIdMap.containsKey(importTrace)) {
					// (Configuration)-[:HAS]->(Trace:Import:ClassRefinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration),
							importTraceIdMap.get(importTrace), RelationshipType.withName("HAS"), null);
				} else {
					// create (Trace:Import:ClassRefinement)
					Long importTraceNodeId = inserter.createNode(
							map("name", imp[0] + " Refinement", "value", importTrace), label("Trace"),
							label("ClassRefinement"), label("Import"));
					// (Configuration)-[:HAS]->(Trace:Import:ClassRefinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration), importTraceNodeId,
							RelationshipType.withName("HAS"), null);
					importTraceIdMap.put(importTrace, importTraceNodeId);
				}
			}
			// method traces
			List<String> methodTraces = traces.get("method");
			for (String methodTrace : methodTraces) {
				String[] method = methodTrace.split("__");
				if (methodTraceIdMap.containsKey(methodTrace)) {
					// (Configuration)-[:HAS]->(Trace:Method)
					inserter.createRelationship(configurationNodeIdMap.get(configuration),
							methodTraceIdMap.get(methodTrace), RelationshipType.withName("HAS"), null);
				} else {
					// create (Trace:Method)
					Long methodTraceNodeId = inserter.createNode(
							map("name", method[0] + " " + method[1], "value", methodTrace), label("Trace"),
							label("Method"));
					// (Configuration)-[:HAS]->(Trace:Method)
					inserter.createRelationship(configurationNodeIdMap.get(configuration), methodTraceNodeId,
							RelationshipType.withName("HAS"), null);
					methodTraceIdMap.put(methodTrace, methodTraceNodeId);
				}
			}
			// statement traces
			List<String> statementTraces = traces.get("statement");
			for (String statementTrace : statementTraces) {
				if (methodRefinementTraceIdMap.containsKey(statementTrace)) {
					// (Configuration)-[:HAS]->(Trace:Statement:MethodRefinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration),
							methodRefinementTraceIdMap.get(statementTrace), RelationshipType.withName("HAS"), null);
				} else {
					// create (Trace:Statement)
					String[] statement = statementTrace.split("__");
					Long methodRefinementTraceNodeId = inserter.createNode(
							map("name", statement[0] + " " + statement[1] + " Refinement", "value", statementTrace),
							label("Trace"), label("MethodRefinement"), label("Statement"));
					// (Configuration)-[:HAS]->(Trace:Statement:MethodRefinement)
					inserter.createRelationship(configurationNodeIdMap.get(configuration), methodRefinementTraceNodeId,
							RelationshipType.withName("HAS"), null);
					methodRefinementTraceIdMap.put(statementTrace, methodRefinementTraceNodeId);
				}
			}
		} finally {
			if (inserter != null) {
				inserter.shutdown();
			}
		}
	}
}
