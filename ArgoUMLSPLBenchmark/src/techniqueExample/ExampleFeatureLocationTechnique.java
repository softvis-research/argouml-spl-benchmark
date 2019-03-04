package techniqueExample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FeatureUtils;
import utils.FileUtils;
import utils.TraceIdUtils;

/**
 * A simplistic example of a feature location technique to illustrate how they
 * can be implemented. This technique, just uses the first name of each feature
 * and visits the variants (those from the scenario containing this feature) to
 * create class traces for the Java classes containing this feature name in the
 * class name. Only the feature traces that are present in all variants
 * containing this feature are kept.
 * 
 * @author jabier.martinez
 */
public class ExampleFeatureLocationTechnique {

	// Modify with the selected scenario. Traditional scenario by default.
	private static final String SELECTED_SCENARIO_PATH = "scenarios/ScenarioTraditionalVariants";

	public static void main(String[] args) {

		// Check
		File variantsFolder = new File(SELECTED_SCENARIO_PATH, "variants");
		if (!variantsFolder.exists()) {
			System.err.println("The variants folder does not exist yet at " + SELECTED_SCENARIO_PATH
					+ "\nYou should build the scenario before. Use the Ant scripts in the scenario folder.");
			return;
		}

		long start = System.currentTimeMillis();

		// Utils for information about features and configurations
		FeatureUtils utils = new FeatureUtils(SELECTED_SCENARIO_PATH);

		// Scenario in the console
		System.out.println("Trying to locate: " + utils.getFeatureIds());
		System.out.println("in the following variants: ");
		for (String config : utils.getConfigurationIds()) {
			System.out.println(config + ": " + utils.getFeaturesOfConfiguration(config));
		}

		// Locate each feature
		for (String featureId : utils.getFeatureIds()) {
			// Get just the first feature name
			String featureName = utils.getFeatureNames(featureId).get(0);
			// remove whitespaces (for example Use Case -> UseCase)
			featureName = featureName.replaceAll(" ", "");

			System.out.println("Locating: " + featureId + " using the word: " + featureName);

			List<String> traces = null;

			// For each variant with this feature
			for (String configurationId : utils.getConfigurationsContainingFeature(featureId)) {

				// Traces for this configuration
				List<String> currentVariantTraces = new ArrayList<String>();
				File variantFolder = utils.getVariantFolderOfConfig(configurationId);
				List<File> javaFiles = FileUtils.getAllJavaFiles(variantFolder);

				// Prepare the parser
				for (File javaFile : javaFiles) {
					ASTParser parser = ASTParser.newParser(AST.JLS8);
					String source = FileUtils.getStringOfFile(javaFile);
					parser.setSource(source.toCharArray());
					parser.setKind(ASTParser.K_COMPILATION_UNIT);
					parser.setBindingsRecovery(true);

					// Get the AST
					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					for (Object type : cu.types()) {
						if (type instanceof TypeDeclaration) {
							TypeDeclaration typeDeclaration = (TypeDeclaration) type;
							String typeName = typeDeclaration.getName().toString();
							// Check if the class contains the feature name
							if (typeName.contains(featureName)) {
								// trace found
								String traceId = TraceIdUtils.getId(typeDeclaration);
								if (!currentVariantTraces.contains(traceId)) {
									currentVariantTraces.add(traceId);
								}
							}
						}
					}
				}

				// it is the first variant, so add all
				if (traces == null) {
					traces = new ArrayList<String>();
					traces.addAll(currentVariantTraces);
				} else {
					// keep only the traces that were already seen in other
					// variants with this feature
					List<String> toBeRemoved = new ArrayList<String>();
					for (String trace : traces) {
						if (!currentVariantTraces.contains(trace)) {
							toBeRemoved.add(trace);
						}
					}
					traces.removeAll(toBeRemoved);
				}
			}
			System.out.println(traces.size() + " traces found");
			createTracesFile(featureId, traces);
		}

		long end = System.currentTimeMillis();
		System.out.println("Finished, check yourResults folder. Time spent (ms): " + (end - start));
	}

	/**
	 * Create a txt file with the traces in each line.
	 * 
	 * @param featureId
	 * @param traces
	 */
	private static void createTracesFile(String featureId, List<String> traces) {
		// If traces were found, write the results in the file
		if (!traces.isEmpty()) {
			File resultsFolder = new File("yourResults");
			File ffile = new File(resultsFolder, featureId + ".txt");
			if (ffile.exists()) {
				ffile.delete();
			}
			for (String trace : traces) {
				try {
					FileUtils.appendToFile(ffile, trace);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
