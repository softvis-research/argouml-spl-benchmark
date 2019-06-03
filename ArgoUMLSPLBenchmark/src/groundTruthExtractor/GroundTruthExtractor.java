package groundTruthExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import groundTruthExtractor.tests.ExtractorTest;
import utils.FileUtils;
import utils.TraceIdUtils;

/**
 * Extracting the ground truth from the jpp annotations
 * http://www.slashdev.ca/javapp/
 * 
 * @author jabier.martinez
 */
public class GroundTruthExtractor {

	public static final String AND_FEATURES = "_and_";
	private static final String GRANULARITY_PACKAGE = "Package";
	private static final String GRANULARITY_CLASS = "Class";
	private static final String GRANULARITY_METHOD = "Method";
	private static final String GRANULARITY_INTERFACEMETHOD = "InterfaceMethod";

	private static final String GRANULARITY = "//@#$LPS";
	private static final String JPPIFDEFINED = "//#if defined(";
	private static final String JPPELIFDEFINED = "#elif defined(";
	private static final String JPPENDIF = "//#endif";
	private static final String JPPELSE = "//#else";
	private static final String JPPCOMMENT = "//#";

	/**
	 * Go through all ArgoUML projects
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("ArgoUML SPL ground-truth extractor");

		// Launch tests
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(ExtractorTest.class);
		if (result.getFailureCount() > 0) {
			System.err.println("JUnit tests failed. Ground truth extraction cancelled.");
			return;
		}

		// the argoUML SPL projects must be imported in the workspace in the same parent
		// folder of this ArgoUMLSPLBenchmark project
		File argoUMLSPLContainingFolder = new File("../");

		// get all relevant Java files
		List<File> javaFiles = getAllArgoUMLSPLRelevantJavaFiles(argoUMLSPLContainingFolder);

		extractGroundTruth(javaFiles, new File("groundTruth"));
	}

	/**
	 * Get all the Java files of the ArgoUMLSPL projects that can contain
	 * variability annotations
	 * 
	 * @param argoUMLSPLContainingFolder
	 * @return the list of Java files
	 */
	public static List<File> getAllArgoUMLSPLRelevantJavaFiles(File argoUMLSPLContainingFolder) {
		// Go through all files of ArgoUML
		List<File> argoUMLProjects = new ArrayList<File>();
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-app"));
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-core-diagrams-sequence2"));
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-core-infra"));
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-core-model"));
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-core-model-euml"));
		argoUMLProjects.add(new File(argoUMLSPLContainingFolder, "argouml-core-model-mdr"));
		// argouml-core-tools does not contain jpp annotations
		// argoUMLProjects.add(new File("argoUMLSPLContainingFolder,
		// "argouml-core-tools"));
		List<File> javaFiles = new ArrayList<File>();
		for (File project : argoUMLProjects) {
			List<File> files = FileUtils.getAllJavaFilesIgnoringStagingFolder(project);
			javaFiles.addAll(files);
		}
		return javaFiles;
	}

	/**
	 * Extract groundTruth
	 * 
	 * @param allJavaFiles
	 *            files with java extension
	 * @param outputFolder
	 *            for the txt files of the groundtruth
	 */
	public static void extractGroundTruth(List<File> allJavaFiles, File outputFolder) {

		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		} else {
			// clean groundTruth folder
			System.out.println("Cleaning " + outputFolder.getAbsolutePath());
			for (File f1 : outputFolder.listFiles()) {
				if (f1.getName().endsWith(".txt")) {
					System.out.println("Deleting " + f1.getAbsolutePath());
					f1.delete();
				}
			}
		}

		System.out.println("Ground-truth extraction started");
		for (File f : allJavaFiles) {
			Map<String, List<String>> map = parseFile(f);
			for (String feature : map.keySet()) {
				File file = new File(outputFolder, feature + ".txt");
				for (String id : map.get(feature)) {
					try {
						FileUtils.appendToFile(file, id);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Ground-truth extraction finished");
		System.out.println("Feature traces at " + outputFolder.getAbsolutePath());
	}

	/**
	 * Parse a java file to get variability info
	 * 
	 * @param javaFile
	 * @return a map of features to implementation elements
	 */
	public static Map<String, List<String>> parseFile(File javaFile) {

		// This is a stack because we can have #ifdefined(A) for the class and
		// then #ifdefined(B) for the method
		// See for example org.argouml.sequence2.diagram.ActionAddClassifierRole
		Stack<List<String>> currentBlockFeatures = new Stack<List<String>>();
		Stack<Integer> currentBlockStart = new Stack<Integer>();

		List<String> cuRefinements = new ArrayList<String>();

		Map<String, List<String>> featureToImplementationMap = new HashMap<String, List<String>>();
		// Prepare the parser
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String source = FileUtils.getStringOfFile(javaFile);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);

		// Get the AST
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		System.out.println("########################");
		System.out.println(javaFile.getAbsolutePath());

		// Empty list of already added refinements
		cuRefinements.clear();

		// Get a list of methods
		List<MethodDeclaration> methods = getMethods(cu);
		List<String> currentFeatures = new ArrayList<String>();
		// Process the jpp comments
		List<LineComment> jppComments = geJPPComments(cu, source);
		// count CORE LoC when there are jppComments, use source, because
		// cu.toString does not include comments
		int coreLines = getLinesOfCode(source, true);
		if (coreLines > 0) {
			System.out.println("LoC_info;CORE;" + coreLines + ";" + javaFile.getName());
		}
		for (LineComment node : jppComments) {
			int start = node.getStartPosition();
			int end = start + node.getLength();
			String comment = source.substring(start, end);
			currentFeatures = getFeatures(currentBlockFeatures);
			// End of block
			if (comment.startsWith(JPPENDIF) || comment.startsWith(JPPELIFDEFINED) || comment.startsWith(JPPELSE)) {
				// We finished a block
				System.out.println("--------");
				System.out.println("Features: " + currentBlockFeatures);
				String blockText = source.substring(currentBlockStart.peek(), end);
				String granularity = getJPPGranularity(blockText);
				if (granularity == null) {
					granularity = "Undefined";
				}
				System.out.println("Granularity: " + granularity);
				// System.out.println(blockText);

				for (String feature : currentFeatures) {
					try {
						// print loc info, and then we print the id
						System.out.print("LoC_info;" + feature + ";" + getLinesOfCode(blockText, false) + ";"
								+ currentBlockFeatures + ";");
						if (granularity.equals(GRANULARITY_PACKAGE) || granularity.equals(GRANULARITY_CLASS)) {
							List<?> types = cu.types();
							for (Object type : types) {
								String id = TraceIdUtils.getId((TypeDeclaration) type);
								addMapping(featureToImplementationMap, feature, id);
								System.out.println(id);
							}
						} else if (granularity.equals(GRANULARITY_METHOD) || granularity.equals(GRANULARITY_INTERFACEMETHOD)) {
							List<MethodDeclaration> wrappingMethods = getWrappingMethods(methods,
									currentBlockStart.peek(), end);
							if (!wrappingMethods.isEmpty()) {
								for (MethodDeclaration method : wrappingMethods) {
									String id = TraceIdUtils.getId(method);
									addMapping(featureToImplementationMap, feature, id);
									System.out.println(id);
								}
							} else {
								System.err.println("Should not happen");
							}
						} else {
							// It is something else
							MethodDeclaration method = getMethodThatContainsAPosition(methods, currentBlockStart.peek(),
									end);
							if (method != null) {
								// it is inside a method
								String id = TraceIdUtils.getId(method) + " Refinement";
								if (!cuRefinements.contains(feature + " " + id)) {
									cuRefinements.add(feature + " " + id);
									addMapping(featureToImplementationMap, feature, id);
								}
								System.out.println(id);
							} else {
								// it is somewhere in the class (import,
								// variable etc.)
								List<?> types = cu.types();
								for (Object type : types) {
									String id = TraceIdUtils.getId((TypeDeclaration) type) + " Refinement";
									if (!cuRefinements.contains(feature + " " + id)) {
										cuRefinements.add(feature + " " + id);
										addMapping(featureToImplementationMap, feature, id);
									}
									System.out.println(id);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// end of #endif
				currentBlockFeatures.pop();
				currentBlockStart.pop();
			}
			// We started a block
			if (comment.startsWith(JPPIFDEFINED) || comment.startsWith(JPPELIFDEFINED) || comment.startsWith(JPPELSE)) {
				// if it is not an Else
				if (!comment.startsWith(JPPELSE)) {
					List<String> features = getJPPFeatures(comment);
					currentBlockFeatures.push(features);
					currentBlockStart.push(start);
				} else {
					// TODO This is enough for ArgoUML SPL but this can be
					// improved to cover more cases.
					List<String> newFeatures = new ArrayList<String>();
					// it was an else so it is a negation of previous
					for (String f : currentFeatures) {
						newFeatures.add("not_" + f);
					}
					currentBlockFeatures.push(newFeatures);
					currentBlockStart.push(start);
				}
			}
		}
		return featureToImplementationMap;
	}

	public static void addMapping(Map<String, List<String>> featureToImplementationMap, String feature, String id) {
		List<String> current = featureToImplementationMap.get(feature);
		if (current == null) {
			current = new ArrayList<String>();
		}
		current.add(id);
		featureToImplementationMap.put(feature, current);
	}

	/**
	 * Get all methods
	 * 
	 * @param a
	 *            compilation unit
	 * @return list of methods
	 */
	public static List<MethodDeclaration> getMethods(CompilationUnit cu) {
		List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				methods.add(node);
				return true;
			}
		});
		return methods;
	}

	/**
	 * Go comment by comment in the source code to find JPP annotations
	 * 
	 * @param cu
	 * @return list of jpp comments
	 */
	@SuppressWarnings("unchecked")
	public static List<LineComment> geJPPComments(CompilationUnit cu, String source) {
		List<LineComment> jppComments = new ArrayList<LineComment>();
		for (Comment comment : (List<Comment>) cu.getCommentList()) {
			comment.accept(new ASTVisitor() {
				public boolean visit(LineComment node) {
					int start = node.getStartPosition();
					int end = start + node.getLength();
					String comment = source.substring(start, end);
					if (comment.startsWith(JPPCOMMENT)) {
						jppComments.add(node);
					}
					return true;
				}
			});
		}
		return jppComments;
	}

	/**
	 * If we have more than one item in the stack it means that we had nested
	 * #ifdefined. If peek is A, and then we had B as second item, the result will
	 * be A_and_B
	 * 
	 * @param currentBlockFeatures
	 * @return the list of features
	 */
	public static List<String> getFeatures(Stack<List<String>> currentBlockFeatures) {
		// empty
		if (currentBlockFeatures.isEmpty()) {
			return new ArrayList<String>();
		}

		List<String> peek = currentBlockFeatures.peek();
		// General case without nested #ifdefined
		if (currentBlockFeatures.size() == 1) {
			return peek;
		}

		// Nested #ifdefined so we take the peek features to append the _and_
		List<String> features = new ArrayList<String>();
		features.addAll(peek);

		for (int i = 0; i < currentBlockFeatures.size(); i++) {
			List<String> currentFs = currentBlockFeatures.get(i);
			if (currentFs != peek && !currentFs.containsAll(peek)) {
				List<String> toBeAdded = new ArrayList<String>();
				for (String f : features) {
					for (String f2 : currentFs) {
						if (f.equals(f2) || isFPartOfAnAnd(f, f2)) {
							// they are equal or it is contained, it is not
							// added
						} else if (!f.equals(f2)) {
							toBeAdded.add(f + AND_FEATURES + f2);
						}
					}
				}
				features.clear();
				features.addAll(toBeAdded);
			}
		}
		// sort and remove repeated
		List<String> toReturn = new ArrayList<String>();
		for (String f : features) {
			String correct = getUniformFeatureWithAnd(f);
			toReturn.add(correct);
		}

		return toReturn;
	}

	/**
	 * Check if a feature is contained in another. For example, if f is FEATUREA and
	 * f2 is FEATUREA_AND_FEATUREB, it will return true
	 * 
	 * @param f
	 * @param f2
	 * @return whether the f is contained in f2
	 */
	public static boolean isFPartOfAnAnd(String f, String f2) {
		List<String> l = Arrays.asList(f.split(AND_FEATURES));
		List<String> l2 = Arrays.asList(f2.split(AND_FEATURES));
		return l2.containsAll(l);
	}

	/**
	 * In case of feature name with _ands_, it removes duplicates and sort them
	 * alphabetically. For example FEATUREB_and_FEATUREA_and_FEATUREA will return
	 * FEATUREA_and_FEATUREB
	 * 
	 * @param feature
	 * @return correct feature name
	 */
	public static String getUniformFeatureWithAnd(String f) {
		List<String> l = Arrays.asList(f.split(AND_FEATURES));
		// remove duplicated
		l = new ArrayList<>(new HashSet<>(l));
		// sort
		java.util.Collections.sort(l);
		StringBuffer toReturn = new StringBuffer();
		for (String i : l) {
			toReturn.append(i);
			toReturn.append(AND_FEATURES);
		}
		// remove last
		toReturn.setLength(toReturn.length() - AND_FEATURES.length());
		return toReturn.toString();
	}

	/**
	 * Get the method that is between a given start point and end point in the
	 * source code String
	 * 
	 * @param methods
	 * @param currentBlockStart
	 * @param currentBlockEnd
	 * @return methodDeclaration
	 */
	public static List<MethodDeclaration> getWrappingMethods(List<MethodDeclaration> methods, int currentBlockStart,
			int currentBlockEnd) {
		List<MethodDeclaration> wrappingMethods = new ArrayList<MethodDeclaration>();
		// currentBlock is a wrapper of the method
		for (MethodDeclaration method : methods) {
			if (currentBlockStart < method.getStartPosition()) {
				if (currentBlockEnd > method.getStartPosition() + method.getLength()) {
					wrappingMethods.add(method);
				}
			}
		}
		return wrappingMethods;
	}

	/**
	 * Get the method that contains (inside) a given position
	 * 
	 * @param methods
	 * @param currentBlockStart
	 * @param currentBlockEnd
	 * @return
	 */
	public static MethodDeclaration getMethodThatContainsAPosition(List<MethodDeclaration> methods,
			int currentBlockStart, int currentBlockEnd) {
		// currentBlock is inside a method
		for (MethodDeclaration method : methods) {
			if (currentBlockStart >= method.getStartPosition()) {
				if (currentBlockEnd <= method.getStartPosition() + method.getLength()) {
					return method;
				}
			}
		}
		return null;
	}

	public static Pattern betweenParenthesisPattern = Pattern.compile("\\(([^)]+)\\)");

	/**
	 * Get features from JPP annotation
	 * 
	 * @param comment
	 *            the line of the comment containing the JPP annotation #if
	 *            defined(LOGGING) #if defined(COLLABORATIONDIAGRAM) or
	 *            defined(SEQUENCEDIAGRAM) #if defined(COGNITIVE) and
	 *            defined(DEPLOYMENTDIAGRAM)
	 * @return the list of features
	 */
	public static List<String> getJPPFeatures(String comment) {
		List<String> features = new ArrayList<String>();
		Matcher m = betweenParenthesisPattern.matcher(comment);
		while (m.find()) {
			features.add(m.group(1));
		}

		// Feature Interaction GLUE CODE
		// #if defined(COGNITIVE) and defined(DEPLOYMENTDIAGRAM)
		// will be COGNITIVE_DEPLOYMENTDIAGRAM feature
		if (comment.contains(" and ")) {
			StringBuffer featInteraction = new StringBuffer("");
			for (String s : features) {
				featInteraction.append(s);
				featInteraction.append(AND_FEATURES);
			}
			// remove last _
			featInteraction.setLength(featInteraction.length() - AND_FEATURES.length());
			features.clear();
			String uniform = getUniformFeatureWithAnd(featInteraction.toString());
			features.add(uniform);
		}

		return features;
	}

	/**
	 * Get granularity of the JPP annotation
	 * 
	 * @param blockText:
	 *            from the #ifdefined to the #endif
	 * @return the granularity
	 */
	public static String getJPPGranularity(String blockText) {
		String[] lines = blockText.split("\\r?\\n");
		// normally is the second line but not always, get first time it appears
		// check if the current ifdefined does not have a granularity comment before the
		// next ifdefined
		int ifdefinedFound = 0;
		for (String line : lines) {
			// this is the first time
			if (line.contains(JPPIFDEFINED) || line.contains(JPPELIFDEFINED) || line.contains(JPPELSE)) {
				ifdefinedFound++;
				// the first line is the current ifdefined but we stop searching if we find another one
				if (ifdefinedFound>=2) {
					break;
				}
			}
			if (line.contains(GRANULARITY)) {
				return line.substring(line.indexOf(":GranularityType:") + ":GranularityType:".length());
			}
		}
		if (!blockText.startsWith(JPPELSE)) {
			System.err.println("Granularity annotation not found:\n" + blockText + "\n");
		}
		return null;
	}

	/**
	 * Get lines of code, we ignore the lines inside other ifdefined that might
	 * exist.
	 * 
	 * @param text
	 * @param countCoreLines,
	 *            true to count core lines, and false if it is a jppComment
	 * @return the number of lines
	 */
	public static int getLinesOfCode(String text, boolean countCoreLines) {
		String[] lines = text.split("\r\n|\r|\n");
		int counter = 0;
		int insideifdefined = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			// remove whitespaces in the beginning of the string
			line = line.trim();
			// ignore empty lines
			if (line.isEmpty()) {
				continue;
			}
			// it is a jpp comment in the beginning
			if (!countCoreLines && i == 0 && line.startsWith(JPPIFDEFINED)) {
				continue;
			}
			if (line.startsWith(JPPIFDEFINED)) {
				insideifdefined++;
			}
			if (line.startsWith(JPPENDIF)) {
				insideifdefined--;
			}
			// ignore jpp comments and granularity comments
			if (!line.startsWith(JPPCOMMENT) && !line.startsWith(GRANULARITY)) {
				if (insideifdefined == 0) {
					counter++;
				}
			}
		}
		return counter;
	}

}
