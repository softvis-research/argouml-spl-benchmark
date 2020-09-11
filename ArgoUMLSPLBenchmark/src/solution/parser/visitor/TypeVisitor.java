package solution.parser.visitor;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithMembers;

/**
 * @author Richard Müller
 *
 */
public class TypeVisitor extends AbstractVisitor {
	private CompilationUnit cu = null;

	public TypeVisitor(String parent, CompilationUnit cu) {
		super(parent);
		this.cu = cu;
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Map<String, List<String>> traces) {
		List<String> classTraces = traces.get("class");
		if (getParent().startsWith("org.argouml")) {
			String typeName = getParent() + "." + classOrInterfaceDeclaration.getNameAsString();
			classTraces.add(typeName);
			if (!classOrInterfaceDeclaration.isNestedType()) {
				addImports(typeName, traces);
			}
			addFields(classOrInterfaceDeclaration, typeName, traces);
			addConstructors(classOrInterfaceDeclaration, typeName, traces);
			addMethods(classOrInterfaceDeclaration, typeName, traces);
			addInnerClasses(classOrInterfaceDeclaration, typeName, traces);
		}
	}

	@Override
	public void visit(EnumDeclaration enumDeclaration, Map<String, List<String>> traces) {
		List<String> classTraces = traces.get("class");
		if (getParent().startsWith("org.argouml")) {
			String typeName = getParent() + "." + enumDeclaration.getNameAsString();
			classTraces.add(typeName);
			if (!enumDeclaration.isNestedType()) {
				addImports(typeName, traces);
			}
			addEnumConstants(enumDeclaration, typeName, traces);
			addConstructors(enumDeclaration, typeName, traces);
			addMethods(enumDeclaration, typeName, traces);
		}
	}

	@Override
	public void visit(AnnotationDeclaration annotationDeclaration, Map<String, List<String>> traces) {
		List<String> classTraces = traces.get("class");
		if (getParent().startsWith("org.argouml")) {
			String typeName = getParent() + "." + annotationDeclaration.getNameAsString();
			classTraces.add(typeName);
			addImports(typeName, traces);
		}
	}

	private void addImports(String typeName, Map<String, List<String>> traces) {
		cu.getImports().accept(new ImportVisitor(typeName), traces);
	}

	private void addFields(Node nodeWithFields, String typeName, Map<String, List<String>> traces) {
		((NodeWithMembers<?>) nodeWithFields).getFields().forEach(field -> {
			field.accept(new FieldVisitor(typeName), traces);
		});
	}

	private void addEnumConstants(EnumDeclaration enumDeclaration, String typeName, Map<String, List<String>> traces) {
		enumDeclaration.getEntries().forEach(entry -> {
			entry.accept(new FieldVisitor(typeName), traces);
		});
	}

	private void addConstructors(Node node, String typeName, Map<String, List<String>> traces) {
		((NodeWithMembers<?>) node).getConstructors().forEach(constructor -> {
			constructor.accept(new MethodVisitor(typeName), traces);
		});
	}

	private void addMethods(Node nodeWithMembers, String typeName, Map<String, List<String>> traces) {
		((NodeWithMembers<?>) nodeWithMembers).getMethods().forEach(method -> {
			method.getParentNode().ifPresent(parentNode -> {
				// filter methods of anonymous inner classes
				if (!(parentNode instanceof ObjectCreationExpr)) {
					method.accept(new MethodVisitor(typeName), traces);
				}
			});
		});
	}

	private void addInnerClasses(TypeDeclaration<?> typeDeclaration, String typeName,
			Map<String, List<String>> traces) {
		typeDeclaration.findAll(TypeDeclaration.class, isDirectChildAndNoEnum(typeDeclaration)).forEach(innerType -> {
			innerType.accept(new TypeVisitor(getParent() + "." + typeDeclaration.getNameAsString(), cu), traces);
		});

	}

	private static Predicate<TypeDeclaration> isDirectChildAndNoEnum(TypeDeclaration parent) {
		return child -> {
			if (child.getParentNode().isPresent() && !child.isEnumDeclaration()) {
				return child.getParentNode().get().equals(parent);
			} else {
				return false;
			}
		};
	}
}
