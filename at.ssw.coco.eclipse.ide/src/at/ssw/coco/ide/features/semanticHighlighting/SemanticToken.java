package at.ssw.coco.ide.features.semanticHighlighting;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Semantic token
 */
public final class SemanticToken {

	/** AST node */
	private SimpleName fNode;
	private Expression fLiteral;

	/** Binding */
	private IBinding fBinding;

	/** AST root */
	private CompilationUnit fRoot;

	/**
	 * @return Returns the binding, can be <code>null</code>.
	 */
	public IBinding getBinding() {
		if (fBinding == null && fNode != null) {
				fBinding= fNode.resolveBinding();
		}

		return fBinding;
	}

	/**
	 * @return the AST node (a {@link SimpleName})
	 */
	public SimpleName getNode() {
		return fNode;
	}

	/**
	 * @return the AST node (a <code>Boolean-, Character- or NumberLiteral</code>)
	 */
	public Expression getLiteral() {
		return fLiteral;
	}

	/**
	 * @return the AST root
	 */
	public CompilationUnit getRoot() {
		if (fRoot == null) {
			fRoot= (CompilationUnit) (fNode != null ? fNode : fLiteral).getRoot();
		}

		return fRoot;
	}

	/**
	 * Update this token with the given AST node.
	 * <p>
	 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
	 * </p>
	 *
	 * @param node the AST simple name
	 */
	void update(SimpleName node) {
		clear();
		fNode= node;
	}

	/**
	 * Update this token with the given AST node.
	 * <p>
	 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
	 * </p>
	 *
	 * @param literal the AST literal
	 */
	void update(Expression literal) {
		clear();
		fLiteral= literal;
	}

	/**
	 * Clears this token.
	 * <p>
	 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
	 * </p>
	 */
	void clear() {
		fNode= null;
		fLiteral= null;
		fBinding= null;
		fRoot= null;
	}
}
