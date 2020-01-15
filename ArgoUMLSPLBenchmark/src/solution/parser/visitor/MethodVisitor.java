package solution.parser.visitor;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * @author Richard Müller
 *
 */
public class MethodVisitor extends AbstractVisitor {

	public MethodVisitor(String parent) {
		super(parent);
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration, Map<String, List<String>> traces) {
		List<String> methodTraces = traces.get("method");
		StringBuilder methodTrace = new StringBuilder();
		methodTrace.append(getParent());
		methodTrace.append("__");
		methodTrace.append(getSolvedSignature(methodDeclaration));
		methodTraces.add(methodTrace.toString());
		addStatements(methodDeclaration, methodTrace.toString(), traces);

	}

	@Override
	public void visit(ConstructorDeclaration constructorDeclaration, Map<String, List<String>> traces) {
		List<String> methodTraces = traces.get("method");
		StringBuilder methodTrace = new StringBuilder();
		methodTrace.append(getParent());
		methodTrace.append("__");
		methodTrace.append(getSolvedSignature(constructorDeclaration));
		methodTraces.add(methodTrace.toString());
		addStatements(constructorDeclaration, methodTrace.toString(), traces);
	}

	private void addStatements(CallableDeclaration bodyDeclaration, String methodTrace,
			Map<String, List<String>> traces) {
		if (bodyDeclaration.isMethodDeclaration()) {
			bodyDeclaration.asMethodDeclaration().getBody().ifPresent(body -> {
				body.getStatements().accept(new StatementVisitor(methodTrace), traces);
			});
		} else if (bodyDeclaration.isConstructorDeclaration()) {
			bodyDeclaration.asConstructorDeclaration().getBody().getStatements()
					.accept(new StatementVisitor(methodTrace), traces);
		}
	}
}
