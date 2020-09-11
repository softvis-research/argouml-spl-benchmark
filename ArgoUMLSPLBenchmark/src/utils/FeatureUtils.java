package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import solution.set.SetCalculator;

/**
 * Feature Utils
 * 
 * @author jabier.martinez, Richard Müller
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
	Map<String, String> mapFeatureIds = new LinkedHashMap<String, String>();
	Map<String, String> mapConfigIds = new LinkedHashMap<String, String>();
	SetCalculator setUtils = null;

	/**
	 *
	 * We get all the info in the constructor**
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

		// create feature id mapping
		int i = 1;
		for (String feature : featureIds) {
			mapFeatureIds.put(feature, String.valueOf(i));
			i++;
		}

		// create config id mapping
		for (String config : configIds) {
			List<String> features = getFeaturesOfConfiguration(config);
			List<String> featuresIds = new ArrayList<String>();
			for (String feature : features) {
				featuresIds.add(getIdOfFeature(feature));
			}
			StringBuilder configId = new StringBuilder();
			Collections.sort(featuresIds);
			for (String feature : featuresIds) {
				configId.append(feature + "_or_");
			}
			if (configId.length() != 0) {
				configId.setLength(configId.length() - 4);
			}
			mapConfigIds.put(configId.toString(), config);
		}

		// solve scenario
		setUtils = new SetCalculator(getFeatureIds().size());
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
	public File getVariantFolderOfConfig(String configurationId) {
		return mapConfigVariantFolder.get(configurationId);
	}

	public boolean isCombinedFeature(String featureId) {
		return featureId.contains("_and_");
	}

	public List<String> getSingleFeatures(String combinedFeatureId) {
		if (isCombinedFeature(combinedFeatureId)) {
			return Arrays.asList(combinedFeatureId.split("_and_"));
		} else {
			return new ArrayList<String>();
		}
	}

	public String getFeatureNegation(String featureId) {
		if (featureId.startsWith("not_")) {
			return featureId.replace("not_", "");
		} else {
			return "not_" + featureId;
		}
	}

	public List<String> getMinuendsOfElementarySet(String elementarySet) {
		List<String> configIds = setUtils.getMinuendsOfElementarySet(elementarySet);
		List<String> minuends = new ArrayList<String>();
		for (String configId : configIds) {
			minuends.add(getIdOfConfiguration(configId));
		}
		return minuends;
	}

	public List<String> getSubtrahendsOfElementarySet(String elementarySet) {
		List<String> configIds = setUtils.getSubtrahendsOfElementarySet(elementarySet);
		List<String> subtrahends = new ArrayList<String>();
		for (String configId : configIds) {
			subtrahends.add(getIdOfConfiguration(configId));
		}
		return subtrahends;
	}

	public List<String> getElementarySetsOfFeature(String feature) {
		return setUtils.getElementarySetsOfFeature(getIdOfFeature(feature));
	}

	private String getIdOfFeature(String feature) {
		if (mapFeatureIds.containsKey(feature)) {
			return mapFeatureIds.get(feature);
		} else {
			if (feature.contains("_and_")) {
				List<String> singleFeatures = Arrays.asList(feature.split("_and_"));
				List<String> featuresIds = new ArrayList<String>();
				for (String singleFeature : singleFeatures) {
					featuresIds.add(getIdOfFeature(singleFeature));
				}
				Collections.sort(featuresIds);
				StringBuilder id = new StringBuilder();
				for (String featureId : featuresIds) {
					id.append(featureId + "_and_");
				}
				if (id.length() != 0) {
					id.setLength(id.length() - 5);
				}
				mapFeatureIds.put(feature, id.toString());
				return id.toString();
			} else if (feature.startsWith("not_")) {
				StringBuilder id = new StringBuilder();
				id.append("not_");
				id.append(getIdOfFeature(getFeatureNegation(feature)));
				mapFeatureIds.put(feature, id.toString());
				return id.toString();
			} else {
				return "";
			}
		}
	}

	private String getIdOfConfiguration(String configurationId) {
		return mapConfigIds.get(configurationId);
	}
}
