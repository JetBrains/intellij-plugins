// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.introduceConstant;

import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseInplaceIntroducer;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler;
import com.intellij.lang.javascript.refactoring.introduceVariable.InplaceSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Mossienko
 */
public class FlexIntroduceConstantHandler extends JSBaseIntroduceHandler<JSElement, FlexIntroduceConstantSettings, FlexIntroduceConstantDialog> {
  private boolean myIntroducingInTheSameClass;
  private boolean myShortRefIsAmbigousOrUnequal;

  @Override
  protected String getRefactoringName() {
    return JavaScriptBundle.message("javascript.introduce.constant.title");
  }

  @Override
  protected String getCannotIntroduceMessagePropertyKey() {
    return "javascript.introduce.constant.error.no.expression.selected";
  }

  @Override
  protected String getCannotIntroduceVoidExpressionTypeMessagePropertyKey() {
    return "javascript.introduce.constant.error.expression.has.void.type";
  }

  @Override
  protected FlexIntroduceConstantDialog createDialog(final Project project,
                                                     final JSExpression expression,
                                                     final JSExpression[] occurrences,
                                                     PsiElement scope) {
    return new FlexIntroduceConstantDialog(project, occurrences, expression, scope);
  }

  @Override
  protected String getDeclText(final BaseIntroduceContext<FlexIntroduceConstantSettings> baseIntroduceContext, JSElement anchor) {
    @NonNls String baseDeclText = "static const " + baseIntroduceContext.settings.getVariableName();

    return prependAccessModifier(baseIntroduceContext, baseDeclText);
  }

  @Override
  protected JSElement findAnchor(final BaseIntroduceContext<FlexIntroduceConstantSettings> context, final boolean replaceAllOccurrences) {
    JSElement element = findClassAnchor(context.expressionDescriptor.first);
    String className = context.settings.getClassName();
    if (!StringUtil.isEmpty(className)) {
      PsiElement qName = JSResolveUtil.findType(className, context.expressionDescriptor.first, true);
      assert qName instanceof JSClass;
      if (element != null && qName == JSResolveUtil.findParent(element)) {
        myIntroducingInTheSameClass = true;
      }
      return (JSElement)qName;
    }

    return element;
  }

  @Override
  protected Pair<JSVarStatement, Boolean> prepareDeclaration(String varDeclText,
                                                             BaseIntroduceContext<FlexIntroduceConstantSettings> context,
                                                             Project project,
                                                             @Nullable JSLanguageDialect languageDialect,
                                                             JSElement anchorStatement,
                                                             Editor editor) throws IncorrectOperationException {
    String qName = context.settings.getClassName();

    if (!StringUtil.isEmpty(qName) &&
        !myIntroducingInTheSameClass &&
        !StringUtil.getPackageName(qName).isEmpty()
      ) {
      myShortRefIsAmbigousOrUnequal = JSResolveUtil.shortReferenceIsAmbiguousOrUnequal(
        JSResolveUtil.getShortTypeName(qName, true), context.expressionDescriptor.first, qName, true);
      if (!myShortRefIsAmbigousOrUnequal) ImportUtils.doImport(context.expressionDescriptor.first, qName, false);
    }
    return super
      .prepareDeclaration(varDeclText, context, project, languageDialect, anchorStatement, editor);
  }

  @Override
  public JSExpression createRefExpr(PsiElement context,
                                       FlexIntroduceConstantSettings settings,
                                       PsiElement scope,
                                       JSLanguageDialect languageDialect) {
    String qName = settings.getClassName();

    if (!StringUtil.isEmpty(qName) && !myIntroducingInTheSameClass) {
      String className = myShortRefIsAmbigousOrUnequal ? qName:JSResolveUtil.getShortTypeName(qName, true);
      return JSPsiElementFactory.createJSExpression(className + "." + settings.getVariableName(), context);
    }
    return super.createRefExpr(context, settings, scope, languageDialect);
  }

  @Override
  protected JSVariable addStatementBefore(final JSElement anchor, final JSVarStatement declaration) throws IncorrectOperationException {
    return addToClassAnchor(anchor, declaration);
  }

  @Override
  protected boolean validateSelectedExpression(@NotNull PsiFile file,
                                               @NotNull Editor editor,
                                               @NotNull Pair<JSExpression, TextRange> expressionDescriptor) {
    if (!super.validateSelectedExpression(file, editor, expressionDescriptor))
      return false;
    final Ref<Boolean> hasAccesibilityProblem = new Ref<>();
    expressionDescriptor.first.accept(new JSElementVisitor() {
      @Override
      public void visitJSReferenceExpression(final @NotNull JSReferenceExpression node) {
        if (node.getQualifier() == null) {
          final PsiElement element = node.resolve();

          if (element instanceof JSAttributeListOwner &&
              !(element instanceof JSClass || JSResolveUtil.isConstructorFunction(element))
            ) {
            final JSAttributeList attributeList = ((JSAttributeListOwner)element).getAttributeList();
            if (attributeList == null || !attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
              hasAccesibilityProblem.set(Boolean.TRUE);
            }
          } else if (element == null) {
            hasAccesibilityProblem.set(Boolean.TRUE);
          }
        }
        super.visitJSReferenceExpression(node);
      }

      @Override
      public void visitJSElement(final @NotNull JSElement node) {
        node.acceptChildren(this);
      }
    });

    if (Boolean.TRUE.equals(hasAccesibilityProblem.get())) {
      showErrorHint(editor, file, JavaScriptBundle.message("javascript.introduce.constant.error.not.constant.expression.selected"));
      return false;
    }
    return true;
  }

  @Override
  protected InplaceSettings<FlexIntroduceConstantSettings> getInplaceSettings(Pair<JSExpression, TextRange> expr,
                                                                              JSExpression[] occurrences,
                                                                              PsiElement scope,
                                                                              OccurrencesChooser.ReplaceChoice choice) {
    return null;  //TODO implement inplace introduce constant
  }

  @Override
  protected JSBaseInplaceIntroducer createInplaceIntroducer(BaseIntroduceContext<FlexIntroduceConstantSettings> context,
                                                            PsiElement scope,
                                                            Editor editor,
                                                            Project project,
                                                            JSExpression[] occurences, Runnable callback) {
    return null;
  }
}
