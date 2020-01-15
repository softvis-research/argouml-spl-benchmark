
package solution.parser.visitor;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

/**
 * @author Richard Müller
 *
 */
public class StatementVisitor extends AbstractVisitor {

	public StatementVisitor(String parent) {
		super(parent);
	}

	@Override
	public void visit(BinaryExpr binaryExpr, Map<String, List<String>> traces) {
		List<String> statementTraces = traces.get("statement");
		if (!binaryExpr.getLeft().isBinaryExpr() && !binaryExpr.getRight().isBinaryExpr()) {
			String statementTrace = getParent() + "__"
					+ binaryExpr.removeComment().toString().replaceAll(" ", "").replaceAll("\\n", "");
			statementTraces.add(statementTrace);
		}
		statementTraces.add(getParent() + "__" + binaryExpr.getOperator().asString());
		super.visit(binaryExpr, traces);
	}

	@Override
	public void visit(MethodCallExpr methodCallExpr, Map<String, List<String>> traces) {
		List<String> statementTraces = traces.get("statement");
		if (!(methodCallExpr.getParentNode().get() instanceof MethodCallExpr)) {
			String statementTrace = getParent() + "__"
					+ methodCallExpr.removeComment().toString().replaceAll(" ", "").replaceAll("\\n", "") + " "
					+ methodCallExpr.getBegin().get().column;
			statementTraces.add(statementTrace);
		}
		super.visit(methodCallExpr, traces);
	}

	@Override
	public void visit(InstanceOfExpr instanceOfExpr, Map<String, List<String>> traces) {
		List<String> statementTraces = traces.get("statement");
		String statementTrace = getParent() + "__"
				+ instanceOfExpr.removeComment().toString().replaceAll(" ", "").replaceAll("\\n", "");
		statementTraces.add(statementTrace);
		super.visit(instanceOfExpr, traces);
	}

	@Override
	public void visit(ObjectCreationExpr objectCreationExpr, Map<String, List<String>> traces) {
		List<String> statementTraces = traces.get("statement");
		String statementTrace = getParent() + "__" + objectCreationExpr.getTypeAsString();
		statementTraces.add(statementTrace);
		super.visit(objectCreationExpr, traces);
	}

	@Override
	public void visit(ClassExpr classExpr, Map<String, List<String>> traces) {
		List<String> statementTraces = traces.get("statement");
		String statementTrace = getParent() + "__" + classExpr.getTypeAsString();
		statementTraces.add(statementTrace);
		super.visit(classExpr, traces);
	}
}
