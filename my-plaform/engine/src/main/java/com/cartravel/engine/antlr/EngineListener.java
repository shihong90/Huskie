// Generated from Engine.g4 by ANTLR 4.5.3

package com.cartravel.engine.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EngineParser}.
 */
public interface EngineListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link EngineParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(EngineParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(EngineParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#sql}.
	 * @param ctx the parse tree
	 */
	void enterSql(EngineParser.SqlContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#sql}.
	 * @param ctx the parse tree
	 */
	void exitSql(EngineParser.SqlContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#overwrite}.
	 * @param ctx the parse tree
	 */
	void enterOverwrite(EngineParser.OverwriteContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#overwrite}.
	 * @param ctx the parse tree
	 */
	void exitOverwrite(EngineParser.OverwriteContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#append}.
	 * @param ctx the parse tree
	 */
	void enterAppend(EngineParser.AppendContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#append}.
	 * @param ctx the parse tree
	 */
	void exitAppend(EngineParser.AppendContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#errorIfExists}.
	 * @param ctx the parse tree
	 */
	void enterErrorIfExists(EngineParser.ErrorIfExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#errorIfExists}.
	 * @param ctx the parse tree
	 */
	void exitErrorIfExists(EngineParser.ErrorIfExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#ignore}.
	 * @param ctx the parse tree
	 */
	void enterIgnore(EngineParser.IgnoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#ignore}.
	 * @param ctx the parse tree
	 */
	void exitIgnore(EngineParser.IgnoreContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(EngineParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(EngineParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterBooleanExpression(EngineParser.BooleanExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitBooleanExpression(EngineParser.BooleanExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(EngineParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(EngineParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#ender}.
	 * @param ctx the parse tree
	 */
	void enterEnder(EngineParser.EnderContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#ender}.
	 * @param ctx the parse tree
	 */
	void exitEnder(EngineParser.EnderContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#format}.
	 * @param ctx the parse tree
	 */
	void enterFormat(EngineParser.FormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#format}.
	 * @param ctx the parse tree
	 */
	void exitFormat(EngineParser.FormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#path}.
	 * @param ctx the parse tree
	 */
	void enterPath(EngineParser.PathContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#path}.
	 * @param ctx the parse tree
	 */
	void exitPath(EngineParser.PathContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#db}.
	 * @param ctx the parse tree
	 */
	void enterDb(EngineParser.DbContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#db}.
	 * @param ctx the parse tree
	 */
	void exitDb(EngineParser.DbContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(EngineParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(EngineParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(EngineParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(EngineParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#col}.
	 * @param ctx the parse tree
	 */
	void enterCol(EngineParser.ColContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#col}.
	 * @param ctx the parse tree
	 */
	void exitCol(EngineParser.ColContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(EngineParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(EngineParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(EngineParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(EngineParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(EngineParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(EngineParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(EngineParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(EngineParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterStrictIdentifier(EngineParser.StrictIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitStrictIdentifier(EngineParser.StrictIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifier(EngineParser.QuotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifier(EngineParser.QuotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link EngineParser#numPartition}.
	 * @param ctx the parse tree
	 */
	void enterNumPartition(EngineParser.NumPartitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EngineParser#numPartition}.
	 * @param ctx the parse tree
	 */
	void exitNumPartition(EngineParser.NumPartitionContext ctx);
}