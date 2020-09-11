package solution.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import solution.neo4j.GraphDatabaseInserter;
import solution.parser.visitor.TypeVisitor;
import utils.FileUtils;

/**
 * @author Richard Müller
 *
 */
public class JavaSourceCodeParser {
	private GraphDatabaseInserter databaseInserter = null;
	private String scenarioPath = null;
	private List<String> variantFolderNames = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public JavaSourceCodeParser(GraphDatabaseInserter databaseInserter, String scenarioPath,
			List<String> variantFolderNames) throws IOException {
		this.databaseInserter = databaseInserter;
		this.scenarioPath = scenarioPath;
		this.variantFolderNames = variantFolderNames;
		initSolver();
	}

	public void scanScenarioVariants() throws IOException, InterruptedException {
		for (String variantFolderName : variantFolderNames) {
			LOGGER.info("Scan variant " + variantFolderName);
			File variantSourceDirectory = new File(scenarioPath + "\\variants\\" + variantFolderName + "\\src");
			scan(FileUtils.getAllJavaFiles(variantSourceDirectory), variantFolderName);
		}
	}

	private void initSolver() throws IOException {
		for (String variantFolderName : variantFolderNames) {
			File variantSourceDirectory = new File(scenarioPath + "\\variants\\" + variantFolderName + "\\src");
			File variantLibDirectory = new File(scenarioPath + "\\variants\\" + variantFolderName + "\\lib");
			// create type solver
			CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
			// add external jar solvers
			if (variantLibDirectory.exists()) {
				int jarCounter = 0;
				for (final File fileEntry : variantLibDirectory.listFiles()) {
					if (fileEntry.isFile() && fileEntry.getName().toLowerCase().endsWith("jar")) {
						combinedTypeSolver.add(new JarTypeSolver(fileEntry.getPath()));
						jarCounter++;
					}
				}
				LOGGER.info("Added " + jarCounter + " jar " + ((jarCounter == 1) ? "file" : "files")
						+ " to solver from '{}'.", variantLibDirectory.getPath());
			}
			// add reflection solver
			combinedTypeSolver.add(new ReflectionTypeSolver());
			// add source solver
			combinedTypeSolver.add(new JavaParserTypeSolver(variantSourceDirectory));
			LOGGER.info("Added source directory to solver " + variantSourceDirectory.getPath());
			// set created type solver globally
			StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
		}
	}

	private void scan(List<File> javaFiles, String configuration) throws IOException {
		Map<String, List<String>> traces = new HashMap<String, List<String>>();
		List<String> classTraces = new ArrayList<String>();
		List<String> fieldTraces = new ArrayList<String>();
		List<String> importTraces = new ArrayList<String>();
		List<String> methodTraces = new ArrayList<String>();
		List<String> statementTraces = new ArrayList<String>();
		traces.put("class", classTraces);
		traces.put("field", fieldTraces);
		traces.put("import", importTraces);
		traces.put("method", methodTraces);
		traces.put("statement", statementTraces);

		for (File javaFile : javaFiles) {
			CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(javaFile));
			if (cu.getPackageDeclaration().isPresent()) {
				cu.getTypes().accept(new TypeVisitor(cu.getPackageDeclaration().get().getNameAsString(), cu), traces);
			}
		}
		LOGGER.info("Found " + traces.get("class").size() + " class traces");
		LOGGER.info("Found " + traces.get("field").size() + " field traces");
		LOGGER.info("Found " + traces.get("import").size() + " import traces");
		LOGGER.info("Found " + traces.get("method").size() + " method traces");
		LOGGER.info("Found " + traces.get("statement").size() + " statement traces");
		databaseInserter.createTraces(configuration, traces);
	}
}
