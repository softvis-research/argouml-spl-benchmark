package solution;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graph-based feature location technique using set theory.
 * 
 * @author Richard Mueller
 */
public class GraphBasedFeatureLocationMain {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	// Modify with the selected scenario. Traditional scenario by default.
	private static final String SELECTED_SCENARIO_PATH = "scenarios/ScenarioTraditionalVariants";
	private final static List<String> RELEVANT_FEATURES = Arrays.asList(new String[] { "ACTIVITYDIAGRAM", "COGNITIVE",
			"COLLABORATIONDIAGRAM", "DEPLOYMENTDIAGRAM", "LOGGING", "not_LOGGING", "not_COGNITIVE", "SEQUENCEDIAGRAM",
			"STATEDIAGRAM", "USECASEDIAGRAM", "ACTIVITYDIAGRAM_and_LOGGING", "ACTIVITYDIAGRAM_and_STATEDIAGRAM",
			"COGNITIVE_and_DEPLOYMENTDIAGRAM", "COGNITIVE_and_LOGGING", "COGNITIVE_and_SEQUENCEDIAGRAM",
			"COLLABORATIONDIAGRAM_and_LOGGING_and_SEQUENCEDIAGRAM", "COLLABORATIONDIAGRAM_and_LOGGING",
			"COLLABORATIONDIAGRAM_and_SEQUENCEDIAGRAM", "DEPLOYMENTDIAGRAM_and_LOGGING",
			"DEPLOYMENTDIAGRAM_and_USECASEDIAGRAM", "LOGGING_and_SEQUENCEDIAGRAM", "LOGGING_and_STATEDIAGRAM",
			"LOGGING_and_USECASEDIAGRAM", "SEQUENCEDIAGRAM_and_STATEDIAGRAM" });

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();

			GraphBasedFeatureLocationTechnique gbfl = new GraphBasedFeatureLocationTechnique(SELECTED_SCENARIO_PATH,
					RELEVANT_FEATURES);

			long end = System.currentTimeMillis();
			LOGGER.info("Finished, check yourResults folder. Time spent (ms): " + (end - start));

		} catch (IOException ioe) {
			LOGGER.error(ioe.getMessage());
		} catch (InterruptedException ie) {
			LOGGER.error(ie.getMessage());
		} catch (RuntimeException re) {
			LOGGER.error(re.getMessage());
		}
	}
}
