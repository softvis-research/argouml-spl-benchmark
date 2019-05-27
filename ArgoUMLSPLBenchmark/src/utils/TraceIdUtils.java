package utils;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Trace Id utils
 * 
 * @author jabier.martinez
 */
public class TraceIdUtils {

	public static String getId(PackageDeclaration packageDeclaration) {
		if (packageDeclaration == null) {
			return "defaultPackage";
		}
		return packageDeclaration.getName().getFullyQualifiedName();
	}

	public static String getId(TypeDeclaration typeDeclaration) {
		StringBuffer qname = new StringBuffer();
		CompilationUnit cu = (CompilationUnit) typeDeclaration.getRoot();
		qname.append(getId(cu.getPackage()));
		qname.append(".");
		// nested and inner classes
		Object parent = typeDeclaration.getParent();
		while(parent != null && parent instanceof TypeDeclaration) {
			qname.append(((TypeDeclaration)parent).getName().getFullyQualifiedName());
			qname.append(".");
			parent = ((TypeDeclaration)parent).getParent();
		}
		qname.append(typeDeclaration.getName().getFullyQualifiedName());
		return qname.toString();
	}

	public static String getId(MethodDeclaration node) {
		StringBuffer qname = new StringBuffer();
		// TODO handle AnonymousClassDeclaration
		if (node.getParent() instanceof TypeDeclaration) {
			TypeDeclaration typeParent = (TypeDeclaration) node.getParent();
			qname.append(getId(typeParent));
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
}
