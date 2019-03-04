package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature Utils
 * 
 * @author jabier.martinez
 */
public class FeatureUtils {

	// Features information
	List<String> featureIds = new ArrayList<String>();
	Map<String, List<String>> mapIdNames = new LinkedHashMap<String, List<String>>();
	Map<String, String> mapIdDescriptions = new LinkedHashMap<String, String>();

	// Configurations information
	List<String> configIds = new ArrayList<String>();
	List<File> variantFolders = new ArrayList<File>();
	Map<String, List<String>> mapConfigFeatures = new LinkedHashMap<String, List<String>>();
	Map<String, List<String>> mapFeatureConfigs = new LinkedHashMap<String, List<String>>();
	Map<String, File> mapConfigVariantFolder = new LinkedHashMap<String, File>();

	/**
	 * We get all the info in the constructor
	 * 
	 * @param scenarioFolderPath
	 */
	public FeatureUtils(String scenarioFolderPath) {

		// Get features information
		List<String> lines = FileUtils.getLinesOfFile(new File("featuresInfo/features.txt"));
		for (String line : lines) {
			String[] parts = line.split(";");
			String featureId = parts[0];
			featureIds.add(featureId);
			String[] nameSynonyms = parts[1].split(",");
			List<String> featureNames = new ArrayList<String>();
			for (String name : nameSynonyms) {
				featureNames.add(name);
			}
			mapIdNames.put(featureId, featureNames);
			String featureDescription = parts[2];
			mapIdDescriptions.put(featureId, featureDescription);
		}

		// Get configurations information
		File scenarioFolder = new File(scenarioFolderPath);
		if (!scenarioFolder.exists()) {
			System.err.println(scenarioFolderPath + " does not exist");
			return;
		}
		File configsFolder = new File(scenarioFolder, "configs");
		if (!configsFolder.exists()) {
			System.err.println(configsFolder.getAbsolutePath() + " does not contain a configs folder");
			return;
		}

		File variantsFolder = new File(scenarioFolder, "variants");
		if (!variantsFolder.exists() || variantsFolder.listFiles().length == 0) {
			System.err.println(variantsFolder.getAbsolutePath()
					+ " does not exist. You should build the scenario before. Use the Ant scripts in the scenario folder.");
			return;
		}

		// Go through all the configs
		for (File config : configsFolder.listFiles()) {
			// check that it is a config file
			if (config.getName().endsWith(".config")) {
				configIds.add(config.getName());
				File variant = new File(variantsFolder, config.getName());
				if (!variant.exists()) {
					System.err.println(variant.getAbsolutePath()
							+ " does not exist. You should build the scenario before. Use the Ant scripts in the scenario folder.");
					return;
				}
				variantFolders.add(variant);
				mapConfigVariantFolder.put(config.getName(), variant);
				List<String> features = FileUtils.getLinesOfFile(config);
				mapConfigFeatures.put(config.getName(), features);
				for (String feature : features) {
					List<String> confs = mapFeatureConfigs.get(feature);
					if (confs == null) {
						confs = new ArrayList<String>();
					}
					confs.add(config.getName());
					mapFeatureConfigs.put(feature, confs);
				}
			}
		}
	}

	/**
	 * Get the feature ids
	 * 
	 * @return the list of feature ids
	 */
	public List<String> getFeatureIds() {
		return featureIds;
	}

	/**
	 * Get the name and synonyms of a given feature
	 * 
	 * @param featureId
	 * @return the name and synonyms
	 */
	public List<String> getFeatureNames(String featureId) {
		return mapIdNames.get(featureId);
	}

	/**
	 * Get the description of a feature
	 * 
	 * @param featureId
	 * @return the feature description
	 */
	public String getFeatureDescription(String featureId) {
		return mapIdDescriptions.get(featureId);
	}

	/**
	 * Get the list of configurations of the scenario
	 * 
	 * @return the list of configurations
	 */
	public List<String> getConfigurationIds() {
		return configIds;
	}

	/**
	 * Get the list of configurations that contains a feature
	 * 
	 * @param featureId
	 * @return the list of configurations
	 */
	public List<String> getConfigurationsContainingFeature(String featureId) {
		return mapFeatureConfigs.get(featureId);
	}

	/**
	 * Get the list of features of a given configuration
	 * 
	 * @param configurationId
	 * @return the list of features
	 */
	public List<String> getFeaturesOfConfiguration(String configurationId) {
		return mapConfigFeatures.get(configurationId);
	}
	
	/**
	 * Get variant folder of a given configuration
	 * 
	 * @param configurationId
	 * @return the folder of this variant
	 */
	public File getVariantFolderOfConfig(String configurationId){
		return mapConfigVariantFolder.get(configurationId);
	}
}
