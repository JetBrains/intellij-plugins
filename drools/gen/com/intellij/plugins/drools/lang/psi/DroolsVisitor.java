// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class DroolsVisitor extends PsiElementVisitor {

  public void visitAccumulateFunction(@NotNull DroolsAccumulateFunction o) {
    visitPsiCompositeElement(o);
  }

  public void visitAccumulateFunctionBinding(@NotNull DroolsAccumulateFunctionBinding o) {
    visitPsiCompositeElement(o);
  }

  public void visitAccumulateParameters(@NotNull DroolsAccumulateParameters o) {
    visitPsiCompositeElement(o);
  }

  public void visitAdditiveExpr(@NotNull DroolsAdditiveExpr o) {
    visitExpression(o);
  }

  public void visitAndExpr(@NotNull DroolsAndExpr o) {
    visitExpression(o);
  }

  public void visitAnnotation(@NotNull DroolsAnnotation o) {
    visitPsiCompositeElement(o);
  }

  public void visitArguments(@NotNull DroolsArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitArrayCreatorRest(@NotNull DroolsArrayCreatorRest o) {
    visitPsiCompositeElement(o);
  }

  public void visitArrayInitializer(@NotNull DroolsArrayInitializer o) {
    visitPsiCompositeElement(o);
  }

  public void visitAssignmentExpr(@NotNull DroolsAssignmentExpr o) {
    visitExpression(o);
  }

  public void visitAssignmentOperator(@NotNull DroolsAssignmentOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitAttribute(@NotNull DroolsAttribute o) {
    visitSimpleAttribute(o);
  }

  public void visitBlock(@NotNull DroolsBlock o) {
    visitPsiCompositeElement(o);
  }

  public void visitBooleanLiteral(@NotNull DroolsBooleanLiteral o) {
    visitExpression(o);
  }

  public void visitCastExpr(@NotNull DroolsCastExpr o) {
    visitExpression(o);
  }

  public void visitChunk(@NotNull DroolsChunk o) {
    visitPsiCompositeElement(o);
  }

  public void visitClassCreatorRest(@NotNull DroolsClassCreatorRest o) {
    visitPsiCompositeElement(o);
  }

  public void visitConditionalAndExpr(@NotNull DroolsConditionalAndExpr o) {
    visitExpression(o);
  }

  public void visitConditionalElement(@NotNull DroolsConditionalElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitConditionalExpr(@NotNull DroolsConditionalExpr o) {
    visitExpression(o);
  }

  public void visitConditionalOrExpr(@NotNull DroolsConditionalOrExpr o) {
    visitExpression(o);
  }

  public void visitConsequenceId(@NotNull DroolsConsequenceId o) {
    visitPsiCompositeElement(o);
  }

  public void visitConstraint(@NotNull DroolsConstraint o) {
    visitPsiCompositeElement(o);
  }

  public void visitCreatedQualifiedIdentifier(@NotNull DroolsCreatedQualifiedIdentifier o) {
    visitPsiCompositeElement(o);
  }

  public void visitCreator(@NotNull DroolsCreator o) {
    visitPsiCompositeElement(o);
  }

  public void visitDecimal(@NotNull DroolsDecimal o) {
    visitPsiCompositeElement(o);
  }

  public void visitDeclareStatement(@NotNull DroolsDeclareStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitElementValue(@NotNull DroolsElementValue o) {
    visitPsiCompositeElement(o);
  }

  public void visitElementValueArrayInitializer(@NotNull DroolsElementValueArrayInitializer o) {
    visitPsiCompositeElement(o);
  }

  public void visitElementValuePair(@NotNull DroolsElementValuePair o) {
    visitPsiCompositeElement(o);
  }

  public void visitElementValuePairs(@NotNull DroolsElementValuePairs o) {
    visitPsiCompositeElement(o);
  }

  public void visitEntryPointDeclaration(@NotNull DroolsEntryPointDeclaration o) {
    visitPsiCompositeElement(o);
  }

  public void visitEntryPointName(@NotNull DroolsEntryPointName o) {
    visitPsiCompositeElement(o);
  }

  public void visitEnumDeclaration(@NotNull DroolsEnumDeclaration o) {
    visitPsiClass(o);
  }

  public void visitEnumerative(@NotNull DroolsEnumerative o) {
    visitEnumConstant(o);
  }

  public void visitEqualityExpr(@NotNull DroolsEqualityExpr o) {
    visitExpression(o);
  }

  public void visitExclusiveOrExpr(@NotNull DroolsExclusiveOrExpr o) {
    visitExpression(o);
  }

  public void visitExplicitGenericInvocation(@NotNull DroolsExplicitGenericInvocation o) {
    visitPsiCompositeElement(o);
  }

  public void visitExplicitGenericInvocationSuffix(@NotNull DroolsExplicitGenericInvocationSuffix o) {
    visitPsiCompositeElement(o);
  }

  public void visitExpression(@NotNull DroolsExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitField(@NotNull DroolsField o) {
    visitPsiField(o);
  }

  public void visitFieldName(@NotNull DroolsFieldName o) {
    visitPsiCompositeElement(o);
  }

  public void visitFieldType(@NotNull DroolsFieldType o) {
    visitPsiCompositeElement(o);
  }

  public void visitFilterDef(@NotNull DroolsFilterDef o) {
    visitPsiCompositeElement(o);
  }

  public void visitFromAccumulate(@NotNull DroolsFromAccumulate o) {
    visitPsiCompositeElement(o);
  }

  public void visitFromCollect(@NotNull DroolsFromCollect o) {
    visitPsiCompositeElement(o);
  }

  public void visitFromEntryPoint(@NotNull DroolsFromEntryPoint o) {
    visitPsiCompositeElement(o);
  }

  public void visitFromExpression(@NotNull DroolsFromExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitFromWindow(@NotNull DroolsFromWindow o) {
    visitPsiCompositeElement(o);
  }

  public void visitFunctionStatement(@NotNull DroolsFunctionStatement o) {
    visitFunction(o);
  }

  public void visitGlobalStatement(@NotNull DroolsGlobalStatement o) {
    visitVariable(o);
  }

  public void visitIdentifier(@NotNull DroolsIdentifier o) {
    visitReference(o);
  }

  public void visitIdentifierSuffix(@NotNull DroolsIdentifierSuffix o) {
    visitPsiCompositeElement(o);
  }

  public void visitImportQualifier(@NotNull DroolsImportQualifier o) {
    visitPsiCompositeElement(o);
  }

  public void visitImportStatement(@NotNull DroolsImportStatement o) {
    visitImport(o);
  }

  public void visitInExpr(@NotNull DroolsInExpr o) {
    visitExpression(o);
  }

  public void visitInclusiveOrExpr(@NotNull DroolsInclusiveOrExpr o) {
    visitExpression(o);
  }

  public void visitInnerCreator(@NotNull DroolsInnerCreator o) {
    visitPsiCompositeElement(o);
  }

  public void visitInsertLogicalRhsStatement(@NotNull DroolsInsertLogicalRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitInsertRhsStatement(@NotNull DroolsInsertRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitInstanceOfExpr(@NotNull DroolsInstanceOfExpr o) {
    visitExpression(o);
  }

  public void visitJavaRhsStatement(@NotNull DroolsJavaRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitLabel(@NotNull DroolsLabel o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhs(@NotNull DroolsLhs o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsAccumulate(@NotNull DroolsLhsAccumulate o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsAnd(@NotNull DroolsLhsAnd o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsEval(@NotNull DroolsLhsEval o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsExists(@NotNull DroolsLhsExists o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsExpression(@NotNull DroolsLhsExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsForall(@NotNull DroolsLhsForall o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsNamedConsequence(@NotNull DroolsLhsNamedConsequence o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsNot(@NotNull DroolsLhsNot o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsOr(@NotNull DroolsLhsOr o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsParen(@NotNull DroolsLhsParen o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsPattern(@NotNull DroolsLhsPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsPatternBind(@NotNull DroolsLhsPatternBind o) {
    visitVariable(o);
  }

  public void visitLhsPatternType(@NotNull DroolsLhsPatternType o) {
    visitPsiCompositeElement(o);
  }

  public void visitLhsUnary(@NotNull DroolsLhsUnary o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapEntry(@NotNull DroolsMapEntry o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapExpressionList(@NotNull DroolsMapExpressionList o) {
    visitPsiCompositeElement(o);
  }

  public void visitModifyParExpr(@NotNull DroolsModifyParExpr o) {
    visitExpression(o);
  }

  public void visitModifyRhsStatement(@NotNull DroolsModifyRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitMultiplicativeExpr(@NotNull DroolsMultiplicativeExpr o) {
    visitExpression(o);
  }

  public void visitNameId(@NotNull DroolsNameId o) {
    visitPsiCompositeElement(o);
  }

  public void visitNamespace(@NotNull DroolsNamespace o) {
    visitPsiCompositeElement(o);
  }

  public void visitNonWildcardTypeArguments(@NotNull DroolsNonWildcardTypeArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitNullLiteral(@NotNull DroolsNullLiteral o) {
    visitExpression(o);
  }

  public void visitNumberLiteral(@NotNull DroolsNumberLiteral o) {
    visitExpression(o);
  }

  public void visitOperator(@NotNull DroolsOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitPackageStatement(@NotNull DroolsPackageStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitParExpr(@NotNull DroolsParExpr o) {
    visitExpression(o);
  }

  public void visitParameter(@NotNull DroolsParameter o) {
    visitPsiCompositeElement(o);
  }

  public void visitParameters(@NotNull DroolsParameters o) {
    visitPsiCompositeElement(o);
  }

  public void visitParentRule(@NotNull DroolsParentRule o) {
    visitPsiCompositeElement(o);
  }

  public void visitPatternFilter(@NotNull DroolsPatternFilter o) {
    visitPsiCompositeElement(o);
  }

  public void visitPatternSource(@NotNull DroolsPatternSource o) {
    visitPsiCompositeElement(o);
  }

  public void visitPrimaryExpr(@NotNull DroolsPrimaryExpr o) {
    visitExpression(o);
    // visitPrimaryExprVar(o);
  }

  public void visitPrimitiveType(@NotNull DroolsPrimitiveType o) {
    visitPsiCompositeElement(o);
  }

  public void visitQualifiedIdentifier(@NotNull DroolsQualifiedIdentifier o) {
    visitPsiCompositeElement(o);
  }

  public void visitQualifiedName(@NotNull DroolsQualifiedName o) {
    visitPsiCompositeElement(o);
  }

  public void visitQueryExpression(@NotNull DroolsQueryExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitQueryStatement(@NotNull DroolsQueryStatement o) {
    visitQuery(o);
  }

  public void visitRelationalExpr(@NotNull DroolsRelationalExpr o) {
    visitExpression(o);
  }

  public void visitRelationalOperator(@NotNull DroolsRelationalOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitRetractRhsStatement(@NotNull DroolsRetractRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitRhs(@NotNull DroolsRhs o) {
    visitPsiCompositeElement(o);
  }

  public void visitRuleAttributes(@NotNull DroolsRuleAttributes o) {
    visitPsiCompositeElement(o);
  }

  public void visitRuleName(@NotNull DroolsRuleName o) {
    visitPsiCompositeElement(o);
  }

  public void visitRuleStatement(@NotNull DroolsRuleStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitSelector(@NotNull DroolsSelector o) {
    visitPsiCompositeElement(o);
  }

  public void visitShiftExpr(@NotNull DroolsShiftExpr o) {
    visitExpression(o);
  }

  public void visitSimpleName(@NotNull DroolsSimpleName o) {
    visitPsiCompositeElement(o);
  }

  public void visitSimpleRhsStatement(@NotNull DroolsSimpleRhsStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitSquareArguments(@NotNull DroolsSquareArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitStringId(@NotNull DroolsStringId o) {
    visitPsiCompositeElement(o);
  }

  public void visitStringLiteral(@NotNull DroolsStringLiteral o) {
    visitExpression(o);
  }

  public void visitStringSequence(@NotNull DroolsStringSequence o) {
    visitPsiCompositeElement(o);
  }

  public void visitSuperSuffix(@NotNull DroolsSuperSuffix o) {
    visitPsiCompositeElement(o);
  }

  public void visitSuperType(@NotNull DroolsSuperType o) {
    visitPsiCompositeElement(o);
  }

  public void visitTraitable(@NotNull DroolsTraitable o) {
    visitPsiCompositeElement(o);
  }

  public void visitType(@NotNull DroolsType o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeArgument(@NotNull DroolsTypeArgument o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeArguments(@NotNull DroolsTypeArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeDeclaration(@NotNull DroolsTypeDeclaration o) {
    visitPsiClass(o);
  }

  public void visitTypeName(@NotNull DroolsTypeName o) {
    visitPsiCompositeElement(o);
  }

  public void visitUnary2Expr(@NotNull DroolsUnary2Expr o) {
    visitExpression(o);
  }

  public void visitUnaryAssignExpr(@NotNull DroolsUnaryAssignExpr o) {
    visitExpression(o);
    // visitVariable(o);
  }

  public void visitUnaryExpr(@NotNull DroolsUnaryExpr o) {
    visitExpression(o);
  }

  public void visitUnaryNotPlusMinusExpr(@NotNull DroolsUnaryNotPlusMinusExpr o) {
    visitExpression(o);
  }

  public void visitUnitName(@NotNull DroolsUnitName o) {
    visitPsiCompositeElement(o);
  }

  public void visitUnitStatement(@NotNull DroolsUnitStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitUpdateRhsStatement(@NotNull DroolsUpdateRhsStatement o) {
    visitSimpleRhsStatement(o);
  }

  public void visitVarType(@NotNull DroolsVarType o) {
    visitPsiCompositeElement(o);
  }

  public void visitVariableInitializer(@NotNull DroolsVariableInitializer o) {
    visitPsiCompositeElement(o);
  }

  public void visitWindowDeclaration(@NotNull DroolsWindowDeclaration o) {
    visitPsiCompositeElement(o);
  }

  public void visitWindowId(@NotNull DroolsWindowId o) {
    visitWindowReference(o);
  }

  public void visitEnumConstant(@NotNull DroolsEnumConstant o) {
    visitPsiCompositeElement(o);
  }

  public void visitFunction(@NotNull DroolsFunction o) {
    visitPsiCompositeElement(o);
  }

  public void visitImport(@NotNull DroolsImport o) {
    visitPsiCompositeElement(o);
  }

  public void visitPsiClass(@NotNull DroolsPsiClass o) {
    visitPsiCompositeElement(o);
  }

  public void visitPsiField(@NotNull DroolsPsiField o) {
    visitPsiCompositeElement(o);
  }

  public void visitQuery(@NotNull DroolsQuery o) {
    visitPsiCompositeElement(o);
  }

  public void visitReference(@NotNull DroolsReference o) {
    visitPsiCompositeElement(o);
  }

  public void visitSimpleAttribute(@NotNull DroolsSimpleAttribute o) {
    visitPsiCompositeElement(o);
  }

  public void visitVariable(@NotNull DroolsVariable o) {
    visitPsiCompositeElement(o);
  }

  public void visitWindowReference(@NotNull DroolsWindowReference o) {
    visitPsiCompositeElement(o);
  }

  public void visitPsiCompositeElement(@NotNull DroolsPsiCompositeElement o) {
    visitElement(o);
  }

}
