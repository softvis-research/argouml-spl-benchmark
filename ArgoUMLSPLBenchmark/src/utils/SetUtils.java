package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Richard Müller
 *
 */
public class SetUtils {
	int numberOfFeatures = 0;
	int numberOfVariants = 0;
	private List<String> elementarySets = null;
	private Map<String, List<String>> variants = null;
	private Map<String, List<String>> minuendSets = null;
	private Map<String, List<String>> subtrahendSets = null;

	public SetUtils(int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
		this.numberOfVariants = (int) Math.pow(2, this.numberOfFeatures);
		this.elementarySets = new ArrayList<String>();
		this.variants = new HashMap<String, List<String>>();
		this.minuendSets = new HashMap<String, List<String>>();
		this.subtrahendSets = new HashMap<String, List<String>>();

		createElementarySets();
		createConfigurations();
		createCalculationsOfElementarySets();
	}

	public List<String> getMinuendsOfElementarySet(String elementarySet) {
		return minuendSets.get(elementarySet);
	}

	public List<String> getSubtrahendsOfElementarySet(String elementarySet) {
		return subtrahendSets.get(elementarySet);
	}

	public List<String> getElementarySetsOfFeature(String feature) {
		List<String> elementarySets = new ArrayList<String>();
		if (feature.contains("_and_")) {
			// and
			elementarySets.add(feature);
			if (feature.equals("1_and_8") || feature.equals("2_and_8")) {
				elementarySets.add("1_or_2_and_8");
				minuendSets.put("1_or_2_and_8", Arrays.asList(new String[] { "1_or_8", "2_or_8" }));
				subtrahendSets.put("1_or_2_and_8", Arrays.asList(new String[] { "8", "1", "2", "1_or_2" }));
			}
		} else if (feature.startsWith("not_")) {
			// not
			elementarySets.add(feature);
		} else {
			// complete
			variants.keySet().forEach(variant -> {
				if (variant.contains(feature) && variant.split("_or_").length <= 2) {
					elementarySets.add(variant);
				}
			});
		}
		return elementarySets;
	}

	private void createElementarySets() {
		for (int i = 0; i < numberOfVariants; i++) {
			StringBuilder orSet = new StringBuilder();
			int andCounter = 0;
			for (int j = 0; j < numberOfFeatures; j++) {
				if ((i & (1 << j)) > 0) {
					orSet.append(j + 1 + "_or_");
					andCounter++;
				}
			}
			if (orSet.length() != 0) {
				orSet.setLength(orSet.length() - 4);
			}
			elementarySets.add(orSet.toString());
			if (andCounter >= 2) {
				String andSet = orSet.toString().replaceAll("_or_", "_and_");
				elementarySets.add(andSet);
			}
		}
		for (int i = 0; i < numberOfFeatures; i++) {
			elementarySets.add("not_" + (i + 1));
		}
	}

	private void createConfigurations() {
		for (int i = 0; i < elementarySets.size(); i++) {
			List<String> variant = new ArrayList<String>();
			String elementarySetElement = elementarySets.get(i);
			if (elementarySets.get(i).contains("and") || elementarySets.get(i).contains("not")) {
				// leave out sets with and as well as not
				continue;
			}
			for (int k = 0; k < elementarySets.size(); k++) {
				String lookupSetElement = elementarySets.get(k);
				if (contains(elementarySetElement, lookupSetElement) && !variant.contains(lookupSetElement)) {
					variant.add(lookupSetElement);
				}
			}
			variants.put(elementarySetElement, variant);
		}
	}

	private void createCalculationsOfElementarySets() {

		for (int i = 0; i < elementarySets.size(); i++) {
			String elementarySetElement = elementarySets.get(i);
			List<String> minuendSet = new ArrayList<String>();
			List<String> subtrahendSet = new ArrayList<String>();
			variants.entrySet().forEach(variant -> {
				if (variant.getValue().contains(elementarySetElement) && !minuendSet.contains(variant.getKey())) {
					minuendSet.add(variant.getKey());
				} else if (!subtrahendSet.contains(variant.getKey())) {
					subtrahendSet.add(variant.getKey());
				}
			});
			minuendSets.put(elementarySetElement, minuendSet);
			subtrahendSets.put(elementarySetElement, subtrahendSet);
		}
	}

	private boolean contains(String set1, String set2) {
		List<String> set1Elements = Arrays.asList(set1.split("_or_|_and_"));
		List<String> set2Elements = Arrays.asList(set2.split("_or_|_and_"));

		if (set2.contains("not")) {

			return !contains(set1, set2.replace("not_", ""));

		} else if (set1Elements.size() == 1) {
			// pure set
			if (set2Elements.size() == 1) {
				return set1Elements.equals(set2Elements);
			} else if (set2.contains("and")) {
				return false;
			} else if (set2.contains("or")) {
				return set2Elements.contains(set1Elements.get(0));
			}
		} else if (set1.contains("or")) {
			// or set
			if (set2Elements.size() == 1) {
				return set1Elements.contains(set2);
			} else if (set2.contains("and")) {
				return set1Elements.containsAll(set2Elements);
			} else if (set2.contains("or")) {
				for (String set1Element : set1Elements) {
					if (set2Elements.contains(set1Element)) {
						return true;
					}
				}
				return false;
			}
		} else if (set1.contains("and")) {
			// and set
			if (set2Elements.size() == 1) {
				return set1Elements.contains(set2Elements.get(0));
			} else if (set2.contains("and")) {
				if (set1Elements.size() > set2Elements.size()) {
					for (String set2Element : set2Elements) {
						if (set1Elements.contains(set2Element)) {
							return true;
						}
					}
				} else {
					return set1.equals(set2);
				}
			} else if (set2.contains("or")) {
				return set2Elements.equals(set1Elements);
			}
		}
		return false;
	}
}
