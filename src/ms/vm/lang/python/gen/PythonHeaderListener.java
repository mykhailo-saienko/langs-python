// Generated from PythonHeader.g4 by ANTLR 4.6

	package ms.vm.lang.python.gen;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PythonHeaderParser}.
 */
public interface PythonHeaderListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PythonHeaderParser#main}.
	 * @param ctx the parse tree
	 */
	void enterMain(PythonHeaderParser.MainContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonHeaderParser#main}.
	 * @param ctx the parse tree
	 */
	void exitMain(PythonHeaderParser.MainContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonHeaderParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(PythonHeaderParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonHeaderParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(PythonHeaderParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonHeaderParser#classHeader}.
	 * @param ctx the parse tree
	 */
	void enterClassHeader(PythonHeaderParser.ClassHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonHeaderParser#classHeader}.
	 * @param ctx the parse tree
	 */
	void exitClassHeader(PythonHeaderParser.ClassHeaderContext ctx);
}