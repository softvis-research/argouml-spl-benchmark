package solution.parser.visitor;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.ImportDeclaration;

/**
 * @author Richard Müller
 *
 */
public class ImportVisitor extends AbstractVisitor {

	public ImportVisitor(String parent) {
		super(parent);
	}

	@Override
	public void visit(ImportDeclaration importDeclaration, Map<String, List<String>> traces) {
		if (!importDeclaration.isStatic() && !importDeclaration.isAsterisk()) {
			List<String> importTraces = traces.get("import");
			importTraces.add(getParent() + "__" + importDeclaration.getNameAsString());
		}
	}

}
