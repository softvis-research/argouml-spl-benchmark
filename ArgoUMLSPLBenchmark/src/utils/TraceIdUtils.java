package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Trace Id utils
 * 
 * @author jabier.martinez, Richard Mueller
 */
public class TraceIdUtils {
	private static final String SIGNATURE_PARAMETER_REGEX = "(([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$\\[\\]]*)|<\\1*.*?>|\\?[extends\\s]*\\1*";

	public static String getId(PackageDeclaration packageDeclaration) {
		if (packageDeclaration == null) {
			return "defaultPackage";
		}
		return packageDeclaration.getName().getFullyQualifiedName();
	}

	public static String getId(TypeDeclaration typeDeclaration) {
		CompilationUnit cu = (CompilationUnit) typeDeclaration.getRoot();
		return getId(cu.getPackage()) + "." + typeDeclaration.getName().getFullyQualifiedName();
	}

	public static String getId(MethodDeclaration node) {
		StringBuffer qname = new StringBuffer();
		// TODO handle AnonymousClassDeclaration
		if (node.getParent() instanceof TypeDeclaration) {
			qname.append(getId((TypeDeclaration) node.getParent()));
			qname.append(" ");
			qname.append(node.getName().getIdentifier());
			// add parameters to signature
			qname.append("(");
			List<?> parameters = node.parameters();
			for (Object parameter : parameters) {
				if (parameter instanceof SingleVariableDeclaration) {
					qname.append(((SingleVariableDeclaration) parameter).getType());
					qname.append(",");
				}
			}
			// remove last comma
			if (!parameters.isEmpty()) {
				qname.setLength(qname.length() - 1);
			}
			qname.append(")");
		} else {
			System.err.println("IdUtils: Not handled");
		}
		return qname.toString();
	}

	/**
	 * Transform Cypher results into challenge format.
	 * 
	 * @param record
	 * @return formatted traces
	 */
	public static String getId(Map<String, Object> record) {
		if (record.containsKey("type") && record.containsKey("method") && record.containsKey("signature")) {
			StringBuffer variantTrace = new StringBuffer();
			String type = record.get("type").toString();
			if (type.contains("$")) {
				// remove $ from inner class names
				type = type.substring(0, type.indexOf("$"));
			}
			variantTrace.append(type);
			variantTrace.append(" ");
			String method = record.get("method").toString();
			if (method.equals("<init>")) {
				// set type name as constructor name
				variantTrace.append(type.substring(type.lastIndexOf(".") + 1));
			} else {
				variantTrace.append(method);
			}
			variantTrace.append(reformatSignature(record.get("signature").toString(), '(', ')'));
			return variantTrace.toString();
		} else if (record.containsKey("type")) {
			String type = record.get("type").toString();
			if (type.contains("$")) {
				// remove $ from inner class names
				return type.substring(0, type.indexOf("$"));
			} else {
				return type;
			}
		} else {
			System.err.println("Unhandled record: " + record);
			return "";
		}
	}

	private static String reformatSignature(String signature, char open, char close) {
		StringBuffer reformattedSignature = new StringBuffer();
		String parameters = signature.substring(signature.indexOf(open) + 1, signature.indexOf(close));
		if (parameters.isEmpty()) {
			// there are no parameters
			reformattedSignature.append(open).append(close);
		} else {
			// reformat parameters
			List<String> parameterList = new ArrayList<String>();
			Pattern pattern = Pattern.compile(SIGNATURE_PARAMETER_REGEX);
			Matcher matcher = pattern.matcher(parameters);
			while (matcher.find()) {
				parameterList.add(matcher.group());
			}
			reformattedSignature.append(open);
			for (String parameter : parameterList) {
				if (!parameter.contains("<")) {
					reformattedSignature.append(parameter.substring(parameter.lastIndexOf(".") + 1));
					if (!parameter.contains("extends")) {
						reformattedSignature.append(",");
					}
				} else {
					// remove last comma
					reformattedSignature.setLength(reformattedSignature.length() - 1);
					String parameterTypeName = parameter.substring(0, parameter.indexOf("<"));

					reformattedSignature.append(parameterTypeName.substring(parameterTypeName.lastIndexOf(".") + 1));
					reformattedSignature.append(reformatSignature(parameter, '<', '>'));
					reformattedSignature.append(",");
				}
			}
			// remove last comma
			reformattedSignature.setLength(reformattedSignature.length() - 1);
			reformattedSignature.append(close);
		}
		return reformattedSignature.toString();
	}
}
