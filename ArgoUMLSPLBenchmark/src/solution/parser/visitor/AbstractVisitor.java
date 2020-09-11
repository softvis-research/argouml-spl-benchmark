package solution.parser.visitor;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;

/**
 * @author Richard Müller
 *
 */
public abstract class AbstractVisitor extends VoidVisitorAdapter<Map<String, List<String>>> {
	private String parent = null;

	protected AbstractVisitor(String parent) {
		this.parent = parent;
	}

	protected String getParent() {
		return parent;
	}

	protected String getSolvedSignature(CallableDeclaration callableDeclaration) {

		try {
			if (callableDeclaration.isMethodDeclaration()) {
				return callableDeclaration.asMethodDeclaration().resolve().getSignature()
						.replaceAll("[a-zA-z|0-9|_]*[.]", "").replaceAll(", ", ",");
			} else {
				return callableDeclaration.asConstructorDeclaration().resolve().getSignature()
						.replaceAll("[a-zA-z|0-9|_]*[.]", "").replaceAll(", ", ",");
			}
		} catch (UnsolvedSymbolException use) {
			return callableDeclaration.getSignature().asString().replaceAll(", ", ",");
		}
	}
}
