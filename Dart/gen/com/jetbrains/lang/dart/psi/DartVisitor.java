// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiLanguageInjectionHost;

public class DartVisitor extends PsiElementVisitor {

  public void visitAdditiveExpression(@NotNull DartAdditiveExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitAdditiveOperator(@NotNull DartAdditiveOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitArgumentList(@NotNull DartArgumentList o) {
    visitPsiCompositeElement(o);
  }

  public void visitArguments(@NotNull DartArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitArrayAccessExpression(@NotNull DartArrayAccessExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitAsExpression(@NotNull DartAsExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitAssertStatement(@NotNull DartAssertStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitAssignExpression(@NotNull DartAssignExpression o) {
    visitExpression(o);
  }

  public void visitAssignmentOperator(@NotNull DartAssignmentOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitAwaitExpression(@NotNull DartAwaitExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitBitwiseExpression(@NotNull DartBitwiseExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitBitwiseOperator(@NotNull DartBitwiseOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitBlock(@NotNull DartBlock o) {
    visitIDartBlock(o);
  }

  public void visitBreakStatement(@NotNull DartBreakStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitCallExpression(@NotNull DartCallExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitCascadeReferenceExpression(@NotNull DartCascadeReferenceExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitCatchPart(@NotNull DartCatchPart o) {
    visitPsiCompositeElement(o);
  }

  public void visitClassBody(@NotNull DartClassBody o) {
    visitPsiCompositeElement(o);
  }

  public void visitClassDefinition(@NotNull DartClassDefinition o) {
    visitClass(o);
  }

  public void visitClassMembers(@NotNull DartClassMembers o) {
    visitExecutionScope(o);
  }

  public void visitCompareExpression(@NotNull DartCompareExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitComponentName(@NotNull DartComponentName o) {
    visitNamedElement(o);
  }

  public void visitConstObjectExpression(@NotNull DartConstObjectExpression o) {
    visitExpression(o);
  }

  public void visitConstantPattern(@NotNull DartConstantPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitContinueStatement(@NotNull DartContinueStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitDefaultCase(@NotNull DartDefaultCase o) {
    visitPsiCompositeElement(o);
  }

  public void visitDefaultFormalNamedParameter(@NotNull DartDefaultFormalNamedParameter o) {
    visitPsiCompositeElement(o);
  }

  public void visitDoWhileStatement(@NotNull DartDoWhileStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitElement(@NotNull DartElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitEnumConstantDeclaration(@NotNull DartEnumConstantDeclaration o) {
    visitComponent(o);
  }

  public void visitEnumDefinition(@NotNull DartEnumDefinition o) {
    visitClass(o);
  }

  public void visitEqualityOperator(@NotNull DartEqualityOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitExportStatement(@NotNull DartExportStatement o) {
    visitImportOrExportStatement(o);
  }

  public void visitExpression(@NotNull DartExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitExpressionList(@NotNull DartExpressionList o) {
    visitPsiCompositeElement(o);
  }

  public void visitExtensionDeclaration(@NotNull DartExtensionDeclaration o) {
    visitPsiCompositeElement(o);
  }

  public void visitFactoryConstructorDeclaration(@NotNull DartFactoryConstructorDeclaration o) {
    visitComponent(o);
  }

  public void visitFieldFormalParameter(@NotNull DartFieldFormalParameter o) {
    visitPsiCompositeElement(o);
  }

  public void visitFieldInitializer(@NotNull DartFieldInitializer o) {
    visitPsiCompositeElement(o);
  }

  public void visitFinallyPart(@NotNull DartFinallyPart o) {
    visitPsiCompositeElement(o);
  }

  public void visitForElement(@NotNull DartForElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitForInPart(@NotNull DartForInPart o) {
    visitPsiCompositeElement(o);
  }

  public void visitForLoopParts(@NotNull DartForLoopParts o) {
    visitPsiCompositeElement(o);
  }

  public void visitForLoopPartsInBraces(@NotNull DartForLoopPartsInBraces o) {
    visitPsiCompositeElement(o);
  }

  public void visitForStatement(@NotNull DartForStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitFormalParameterList(@NotNull DartFormalParameterList o) {
    visitPsiCompositeElement(o);
  }

  public void visitFunctionBody(@NotNull DartFunctionBody o) {
    visitPsiCompositeElement(o);
  }

  public void visitFunctionDeclarationWithBody(@NotNull DartFunctionDeclarationWithBody o) {
    visitComponent(o);
  }

  public void visitFunctionDeclarationWithBodyOrNative(@NotNull DartFunctionDeclarationWithBodyOrNative o) {
    visitComponent(o);
  }

  public void visitFunctionExpression(@NotNull DartFunctionExpression o) {
    visitExpression(o);
  }

  public void visitFunctionExpressionBody(@NotNull DartFunctionExpressionBody o) {
    visitPsiCompositeElement(o);
  }

  public void visitFunctionFormalParameter(@NotNull DartFunctionFormalParameter o) {
    visitComponent(o);
  }

  public void visitFunctionTypeAlias(@NotNull DartFunctionTypeAlias o) {
    visitComponent(o);
  }

  public void visitGetterDeclaration(@NotNull DartGetterDeclaration o) {
    visitComponent(o);
  }

  public void visitHideCombinator(@NotNull DartHideCombinator o) {
    visitPsiCompositeElement(o);
  }

  public void visitId(@NotNull DartId o) {
    visitPsiCompositeElement(o);
  }

  public void visitIdentifierPattern(@NotNull DartIdentifierPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitIfElement(@NotNull DartIfElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitIfNullExpression(@NotNull DartIfNullExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitIfStatement(@NotNull DartIfStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitImportStatement(@NotNull DartImportStatement o) {
    visitImportOrExportStatement(o);
  }

  public void visitIncompleteDeclaration(@NotNull DartIncompleteDeclaration o) {
    visitPsiCompositeElement(o);
  }

  public void visitInitializers(@NotNull DartInitializers o) {
    visitPsiCompositeElement(o);
  }

  public void visitInterfaces(@NotNull DartInterfaces o) {
    visitPsiCompositeElement(o);
  }

  public void visitIsExpression(@NotNull DartIsExpression o) {
    visitExpression(o);
  }

  public void visitLabel(@NotNull DartLabel o) {
    visitComponent(o);
  }

  public void visitLibraryComponentReferenceExpression(@NotNull DartLibraryComponentReferenceExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitLibraryId(@NotNull DartLibraryId o) {
    visitReference(o);
  }

  public void visitLibraryNameElement(@NotNull DartLibraryNameElement o) {
    visitPsiCompositeElement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitLibraryReferenceList(@NotNull DartLibraryReferenceList o) {
    visitPsiCompositeElement(o);
  }

  public void visitLibraryStatement(@NotNull DartLibraryStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitListLiteralExpression(@NotNull DartListLiteralExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitListPattern(@NotNull DartListPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitListPatternElement(@NotNull DartListPatternElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitLiteralExpression(@NotNull DartLiteralExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitLogicAndExpression(@NotNull DartLogicAndExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitLogicOrExpression(@NotNull DartLogicOrExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitLogicalAndPattern(@NotNull DartLogicalAndPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitLogicalOrPattern(@NotNull DartLogicalOrPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitLongTemplateEntry(@NotNull DartLongTemplateEntry o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapEntry(@NotNull DartMapEntry o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapPattern(@NotNull DartMapPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitMapPatternEntry(@NotNull DartMapPatternEntry o) {
    visitPsiCompositeElement(o);
  }

  public void visitMetadata(@NotNull DartMetadata o) {
    visitPsiCompositeElement(o);
  }

  public void visitMethodDeclaration(@NotNull DartMethodDeclaration o) {
    visitComponent(o);
  }

  public void visitMixinApplication(@NotNull DartMixinApplication o) {
    visitPsiCompositeElement(o);
  }

  public void visitMixinDeclaration(@NotNull DartMixinDeclaration o) {
    visitClass(o);
  }

  public void visitMixins(@NotNull DartMixins o) {
    visitPsiCompositeElement(o);
  }

  public void visitMultiplicativeExpression(@NotNull DartMultiplicativeExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitMultiplicativeOperator(@NotNull DartMultiplicativeOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitNamedArgument(@NotNull DartNamedArgument o) {
    visitPsiCompositeElement(o);
  }

  public void visitNamedConstructorDeclaration(@NotNull DartNamedConstructorDeclaration o) {
    visitComponent(o);
  }

  public void visitNewExpression(@NotNull DartNewExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitNormalFormalParameter(@NotNull DartNormalFormalParameter o) {
    visitPsiCompositeElement(o);
  }

  public void visitNormalParameterType(@NotNull DartNormalParameterType o) {
    visitPsiCompositeElement(o);
  }

  public void visitObjectPattern(@NotNull DartObjectPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitOnMixins(@NotNull DartOnMixins o) {
    visitPsiCompositeElement(o);
  }

  public void visitOnPart(@NotNull DartOnPart o) {
    visitPsiCompositeElement(o);
  }

  public void visitOptionalFormalParameters(@NotNull DartOptionalFormalParameters o) {
    visitPsiCompositeElement(o);
  }

  public void visitOptionalParameterTypes(@NotNull DartOptionalParameterTypes o) {
    visitPsiCompositeElement(o);
  }

  public void visitParameterNameReferenceExpression(@NotNull DartParameterNameReferenceExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitParameterTypeList(@NotNull DartParameterTypeList o) {
    visitPsiCompositeElement(o);
  }

  public void visitParenthesizedExpression(@NotNull DartParenthesizedExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitParenthesizedPattern(@NotNull DartParenthesizedPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitPartOfStatement(@NotNull DartPartOfStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitPartStatement(@NotNull DartPartStatement o) {
    visitUriBasedDirective(o);
  }

  public void visitPatternAssignment(@NotNull DartPatternAssignment o) {
    visitPsiCompositeElement(o);
  }

  public void visitPatternField(@NotNull DartPatternField o) {
    visitPsiCompositeElement(o);
  }

  public void visitPatternVariableDeclaration(@NotNull DartPatternVariableDeclaration o) {
    visitPsiCompositeElement(o);
  }

  public void visitPrefixExpression(@NotNull DartPrefixExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitPrefixOperator(@NotNull DartPrefixOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecord(@NotNull DartRecord o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordField(@NotNull DartRecordField o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordPattern(@NotNull DartRecordPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordType(@NotNull DartRecordType o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordTypeField(@NotNull DartRecordTypeField o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordTypeNamedField(@NotNull DartRecordTypeNamedField o) {
    visitPsiCompositeElement(o);
  }

  public void visitRecordTypeNamedFields(@NotNull DartRecordTypeNamedFields o) {
    visitPsiCompositeElement(o);
  }

  public void visitRedirection(@NotNull DartRedirection o) {
    visitPsiCompositeElement(o);
  }

  public void visitReferenceExpression(@NotNull DartReferenceExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitRelationalOperator(@NotNull DartRelationalOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitRelationalPattern(@NotNull DartRelationalPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitRestPattern(@NotNull DartRestPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitRethrowStatement(@NotNull DartRethrowStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitReturnStatement(@NotNull DartReturnStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitReturnType(@NotNull DartReturnType o) {
    visitPsiCompositeElement(o);
  }

  public void visitSetOrMapLiteralExpression(@NotNull DartSetOrMapLiteralExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitSetterDeclaration(@NotNull DartSetterDeclaration o) {
    visitComponent(o);
  }

  public void visitShiftExpression(@NotNull DartShiftExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitShiftOperator(@NotNull DartShiftOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitShortTemplateEntry(@NotNull DartShortTemplateEntry o) {
    visitPsiCompositeElement(o);
  }

  public void visitShowCombinator(@NotNull DartShowCombinator o) {
    visitPsiCompositeElement(o);
  }

  public void visitSimpleFormalParameter(@NotNull DartSimpleFormalParameter o) {
    visitComponent(o);
  }

  public void visitSimpleType(@NotNull DartSimpleType o) {
    visitPsiCompositeElement(o);
  }

  public void visitSpreadElement(@NotNull DartSpreadElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitStatements(@NotNull DartStatements o) {
    visitExecutionScope(o);
  }

  public void visitStringLiteralExpression(@NotNull DartStringLiteralExpression o) {
    visitExpression(o);
    // visitPsiLanguageInjectionHost(o);
  }

  public void visitSuffixExpression(@NotNull DartSuffixExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitSuperCallOrFieldInitializer(@NotNull DartSuperCallOrFieldInitializer o) {
    visitPsiCompositeElement(o);
  }

  public void visitSuperExpression(@NotNull DartSuperExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitSuperclass(@NotNull DartSuperclass o) {
    visitPsiCompositeElement(o);
  }

  public void visitSwitchCase(@NotNull DartSwitchCase o) {
    visitPsiCompositeElement(o);
  }

  public void visitSwitchExpression(@NotNull DartSwitchExpression o) {
    visitExpression(o);
  }

  public void visitSwitchExpressionCase(@NotNull DartSwitchExpressionCase o) {
    visitPsiCompositeElement(o);
  }

  public void visitSwitchExpressionWrapper(@NotNull DartSwitchExpressionWrapper o) {
    visitPsiCompositeElement(o);
  }

  public void visitSwitchStatement(@NotNull DartSwitchStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitSwitchStatementOrExpression(@NotNull DartSwitchStatementOrExpression o) {
    visitExpression(o);
  }

  public void visitSymbolLiteralExpression(@NotNull DartSymbolLiteralExpression o) {
    visitExpression(o);
  }

  public void visitTernaryExpression(@NotNull DartTernaryExpression o) {
    visitExpression(o);
  }

  public void visitThisExpression(@NotNull DartThisExpression o) {
    visitExpression(o);
    // visitReference(o);
  }

  public void visitThrowExpression(@NotNull DartThrowExpression o) {
    visitExpression(o);
  }

  public void visitTryStatement(@NotNull DartTryStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitType(@NotNull DartType o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeArguments(@NotNull DartTypeArguments o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeList(@NotNull DartTypeList o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypeParameter(@NotNull DartTypeParameter o) {
    visitComponent(o);
  }

  public void visitTypeParameters(@NotNull DartTypeParameters o) {
    visitPsiCompositeElement(o);
  }

  public void visitTypedFunctionType(@NotNull DartTypedFunctionType o) {
    visitPsiCompositeElement(o);
  }

  public void visitUnaryPattern(@NotNull DartUnaryPattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitUntypedFunctionType(@NotNull DartUntypedFunctionType o) {
    visitPsiCompositeElement(o);
  }

  public void visitUriElement(@NotNull DartUriElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitUserDefinableOperator(@NotNull DartUserDefinableOperator o) {
    visitPsiCompositeElement(o);
  }

  public void visitValueExpression(@NotNull DartValueExpression o) {
    visitExpression(o);
  }

  public void visitVarAccessDeclaration(@NotNull DartVarAccessDeclaration o) {
    visitComponent(o);
  }

  public void visitVarDeclarationList(@NotNull DartVarDeclarationList o) {
    visitPsiCompositeElement(o);
  }

  public void visitVarDeclarationListPart(@NotNull DartVarDeclarationListPart o) {
    visitComponent(o);
  }

  public void visitVarInit(@NotNull DartVarInit o) {
    visitPsiCompositeElement(o);
  }

  public void visitVariablePattern(@NotNull DartVariablePattern o) {
    visitPsiCompositeElement(o);
  }

  public void visitVoidTypeFunctionType(@NotNull DartVoidTypeFunctionType o) {
    visitPsiCompositeElement(o);
  }

  public void visitWhileStatement(@NotNull DartWhileStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitYieldEachStatement(@NotNull DartYieldEachStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitYieldStatement(@NotNull DartYieldStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitClass(@NotNull DartClass o) {
    visitPsiCompositeElement(o);
  }

  public void visitComponent(@NotNull DartComponent o) {
    visitPsiCompositeElement(o);
  }

  public void visitExecutionScope(@NotNull DartExecutionScope o) {
    visitPsiCompositeElement(o);
  }

  public void visitImportOrExportStatement(@NotNull DartImportOrExportStatement o) {
    visitPsiCompositeElement(o);
  }

  public void visitNamedElement(@NotNull DartNamedElement o) {
    visitPsiCompositeElement(o);
  }

  public void visitReference(@NotNull DartReference o) {
    visitPsiCompositeElement(o);
  }

  public void visitUriBasedDirective(@NotNull DartUriBasedDirective o) {
    visitPsiCompositeElement(o);
  }

  public void visitIDartBlock(@NotNull IDartBlock o) {
    visitElement(o);
  }

  public void visitPsiCompositeElement(@NotNull DartPsiCompositeElement o) {
    visitElement(o);
  }

}
