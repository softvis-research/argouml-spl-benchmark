package solution.neo4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import apoc.load.Xml;
import apoc.refactor.GraphRefactoring;

/**
 * Neo4j database handler to execute queries.
 * 
 * @author Richard Mueller
 *
 */
public class GraphDatabaseHandler {
	private GraphDatabaseService graphDb = null;
	private File dbFolder = null;

	/**
	 * Start an existing Neo4j instance in a certain directory.
	 * 
	 * @param dbDir
	 */
	public GraphDatabaseHandler(File dbFolder) throws RuntimeException {
		setDbFolder(dbFolder);
		start();
	}

	/**
	 * Start Neo4j instance and register APOC.
	 */
	public void start() {
		this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(getDbFolder())
				.setConfig(GraphDatabaseSettings.read_only, "false").newGraphDatabase();
		registerApocProcedure(graphDb);
	}

	/**
	 * Shutdown the Neo4j instance.
	 */
	public void shutdown() {
		graphDb.shutdown();
	}

	/**
	 * Execute a Cypher query.
	 * 
	 * @param params
	 * @param query
	 * @return query result
	 */
	public Result executeQuery(String query) {
		return graphDb.execute(query);
	}

	/**
	 * Execute a Cypher query with paramters.
	 * 
	 * @param query
	 * @param params
	 * @return query result
	 */
	public Result executeQuery(String query, Map<String, Object> params) {
		return graphDb.execute(query, params);
	}

	/**
	 * Get folder of Neo4j database.
	 * 
	 * @return database folder
	 */
	public File getDbFolder() {
		return dbFolder;
	}

	/**
	 * Set folder of Neo4j database.
	 * 
	 * @param database folder
	 */
	public void setDbFolder(File dbFolder) {
		this.dbFolder = dbFolder;
	}

	private static void registerApocProcedure(GraphDatabaseService graphDB) throws IllegalArgumentException {
		// register APOC procedures
		Procedures procedures = ((GraphDatabaseAPI) graphDB).getDependencyResolver()
				.resolveDependency(Procedures.class);
		List<Class<?>> apocProcedures = Arrays.asList(Xml.class, GraphRefactoring.class, apoc.coll.Coll.class);
		apocProcedures.forEach((proc) -> {
			try {
				procedures.registerFunction(proc);
				procedures.registerProcedure(proc);

			} catch (KernelException e) {
				throw new RuntimeException("Error registering " + proc, e);
			}
		});
	}
}
