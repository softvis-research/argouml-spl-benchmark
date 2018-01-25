package scenarioGenerator;

import java.io.File;
import java.util.List;

import utils.FileUtils;

/**
 * It creates the build.xml files of each scenario based on the configs folder
 * of each scenario
 * 
 * @author jabier.martinez
 */
public class ScenarioBuildXMLFilesGenerator {

	public static void main(String[] args) {
		generateBuildXML(new File("scenarios/ScenarioOriginalVariant"));
		generateBuildXML(new File("scenarios/ScenarioPairWiseVariants"));
		generateBuildXML(new File("scenarios/ScenarioAllVariants"));
		generateBuildXML(new File("scenarios/ScenarioTraditionalVariants"));

		generateBuildXML(new File("scenarios/ScenarioRandom002Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom003Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom004Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom005Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom006Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom007Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom008Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom009Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom010Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom050Variants"));
		generateBuildXML(new File("scenarios/ScenarioRandom100Variants"));
	}

	public static void generateBuildXML(File scenarioFolder) {
		System.out.println("Generating build files at " + scenarioFolder.getAbsolutePath());
		File configs = new File(scenarioFolder, "configs");

		// We split in parts of 50 variants to avoid memory problems
		int totalConfigs = configs.listFiles().length;
		int numberOfParts = (int) Math.ceil(totalConfigs / 50);
		if (totalConfigs % 50 != 0) {
			numberOfParts += 1;
		}
		for (int part = 0; part < numberOfParts; part++) {
			StringBuffer buildXML = new StringBuffer();
			buildXML.append(
					"<project basedir=\"../../../org.splevo.casestudy.argoumlspl.generator\" default=\"generateScenario\" name=\"Scenario generator\">\n");
			buildXML.append("<include file=\"../../../org.splevo.casestudy.argoumlspl.generator/build.xml\"/>\n");
			buildXML.append("<include file=\"../build.xml\"/>\n");
			buildXML.append("<target name=\"generateScenario\">\n");

			for (int i = part * 50; i < part * 50 + 50 && i < totalConfigs; i++) {
				File config = configs.listFiles()[i];
				buildXML.append("\t<generate-variant project.path=\"ArgoUMLSPLBenchmark/scenarios/");
				buildXML.append(scenarioFolder.getName());
				buildXML.append("/variants/");
				buildXML.append(config.getName());
				buildXML.append("\" jpp-features=\"");
				List<String> lines = FileUtils.getLinesOfFile(config);
				for (String line : lines) {
					buildXML.append(line);
					buildXML.append("=on${line.separator}");
				}
				buildXML.append("\" />\n");
			}
			buildXML.append("</target>\n");
			buildXML.append("</project>\n");

			// Saving the file
			String name = "build";
			if (numberOfParts > 1) {
				name += "_part" + (part + 1);
			}
			name += ".xml";
			try {
				FileUtils.writeFile(new File(scenarioFolder, name), buildXML.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished");
	}
}
