/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jQAssistant utils
 * 
 * @author Richard Mueller
 *
 */
public class JQAssistantUtils {
	private static final String JQASSISTANT_DATABASE_FOLDER = "/jqassistant/store";
	private static final String JQASSISTANT_COMMANDLINE_TOOL_WINDOWS = "resources/jqassistant-commandline-neo4jv3-1.7.0-MS3/bin/jqassistant.cmd";
	private static final String JQASSISTANT_COMMANDLINE_TOOL_NOT_WINDOWS = "resources/jqassistant-commandline-neo4jv3-1.7.0-MS3/bin/jqassistant.sh";
	private static final String JQASSISTANT_SCAN_TASK = " scan -f java:src::./src/, java:src::./tests/";
	private static final String JQASSISTANT_RESET_TASK = " reset";
	private static final String JQASSISTANT_PROPERTY_PARAMETER = " -p ";
	private static final String JQASSISTANT_PROPERTY_FILE = "resources/jqassistant.properties";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	

	/**
	 * Scan Java source code in variant folder and create software graph.
	 * 
	 * @param variantFolder
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void scanVariantFolder(File variantFolder) throws IOException, InterruptedException {
			Path graphDbFolder = Paths.get(variantFolder.getAbsolutePath() + JQASSISTANT_DATABASE_FOLDER);
			if (Files.notExists(graphDbFolder)) {
				// source code of variant was not scanned
				long start = System.currentTimeMillis();
				LOGGER.info("Scan variant folder " + variantFolder.getPath());
				executeCommand(createCommand(JQASSISTANT_SCAN_TASK), variantFolder);
				long end = System.currentTimeMillis();
				LOGGER.info("Finished scan. Time spent (ms): " + (end - start));
			} else { // source code of variant was scanned
				LOGGER.info("Variant folder " + variantFolder.getPath() + " has already been scanned.");
			}
	}
	

	/**
	 * Return path to software graph database.
	 * 
	 * @return graph database path
	 */
	public static String getJQAssistantDatabaseFolder() {
		return JQASSISTANT_DATABASE_FOLDER;
	}


	private static String[] createCommand(String task) {
		String taskCommand = "";
		if (task.equals(JQASSISTANT_SCAN_TASK)) {
			taskCommand = JQASSISTANT_SCAN_TASK + JQASSISTANT_PROPERTY_PARAMETER
					+ new File(JQASSISTANT_PROPERTY_FILE).getAbsolutePath();
		} else if (task.equals(JQASSISTANT_RESET_TASK)) {
			taskCommand = JQASSISTANT_RESET_TASK;
		}
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		String[] command = new String[3];
		if (isWindows) {
			command[0] = "cmd.exe";
			command[1] = "/c";
			command[2] = new File(JQASSISTANT_COMMANDLINE_TOOL_WINDOWS).getAbsolutePath() + taskCommand;

		} else {
			command[0] = "sh";
			command[1] = "-c";
			command[2] = new File(JQASSISTANT_COMMANDLINE_TOOL_NOT_WINDOWS).getAbsolutePath() + taskCommand;
		}
		return command;
	}

	private static void executeCommand(String[] command, File variantFolder) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command(command);
		builder.directory(variantFolder);
		builder.redirectErrorStream(true);
		Process process;
		process = builder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		int exitCode = process.waitFor();
		assert exitCode == 0;
	}
}
