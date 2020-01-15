package solution.parser.visitor;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

/**
 * @author Richard Müller
 *
 */
public class FieldVisitor extends AbstractVisitor {

	public FieldVisitor(String parent) {
		super(parent);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration, Map<String, List<String>> traces) {
		List<String> fieldTraces = traces.get("field");
		fieldTraces.add(getParent() + "__" + fieldDeclaration.getVariable(0).getNameAsString());
	}

	@Override
	public void visit(EnumConstantDeclaration enumConstantDeclaration, Map<String, List<String>> traces) {
		List<String> fieldTraces = traces.get("field");
		fieldTraces.add(getParent() + "__" + enumConstantDeclaration.getNameAsString());
	}
}
