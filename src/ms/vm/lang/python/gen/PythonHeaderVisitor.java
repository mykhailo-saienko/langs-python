// Generated from PythonHeader.g4 by ANTLR 4.6

	package ms.vm.lang.python.gen;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PythonHeaderParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PythonHeaderVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PythonHeaderParser#main}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMain(PythonHeaderParser.MainContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonHeaderParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(PythonHeaderParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonHeaderParser#classHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassHeader(PythonHeaderParser.ClassHeaderContext ctx);
}