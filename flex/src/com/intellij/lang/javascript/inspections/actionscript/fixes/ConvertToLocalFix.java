package com.intellij.lang.javascript.inspections.actionscript.fixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.inspections.actionscript.JSFieldCanBeLocalInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class ConvertToLocalFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance(JSFieldCanBeLocalInspection.class);
  private final JSVariable myField;
  private final Map<JSFunction, Collection<PsiReference>> myFunctionToReferences;

  public ConvertToLocalFix(final JSVariable field, Map<JSFunction, Collection<PsiReference>> functionToReferences) {
    myField = field;
    myFunctionToReferences = functionToReferences;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return FlexBundle.message("js.convert.to.local.quick.fix");
  }

  @Override
  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    if (!FileModificationService.getInstance().preparePsiElementForWrite(myField)) return;

    for (Map.Entry<JSFunction, Collection<PsiReference>> entry : myFunctionToReferences.entrySet()) {
      if (!applyFixForFunction(entry.getValue())) return;
    }

    deleteField();
  }

  private boolean applyFixForFunction(final Collection<PsiReference> references) {
    final Project project = myField.getProject();

    final JSBlockStatement anchorBlock = findAnchorBlock(references);
    if (anchorBlock == null) return false;
    final PsiElement firstElement = getFirstElement(references);
    final PsiElement anchorElement = getAnchorElement(anchorBlock, firstElement);

    JSType type = myField.getJSType();
    final String typeString = type == null ? null : type.getTypeText(JSType.TypeTextFormat.CODE);
    StringBuilder text = new StringBuilder("var ").append(myField.getName());
    final boolean assignment = isAssignment(anchorElement, firstElement);
    if (!StringUtil.isEmpty(typeString) && !(DialectDetector.isTypeScript(myField) && assignment)) {
      text.append(":").append(typeString);
    }

    if (assignment) {
      final JSExpression expression = ((JSExpressionStatement)anchorElement).getExpression();
      final JSExpression rOperand = ((JSAssignmentExpression)expression).getROperand();
      text.append("=").append(rOperand.getText());
    }
    else {
      final JSExpression initializer = myField.getInitializer();
      if (initializer != null) {
        text.append("=").append(initializer.getText());
      }
    }

    text.append(JSCodeStyleSettings.getSemicolon(anchorBlock));
    final PsiElement varStatement =
      JSChangeUtil.createJSTreeFromText(project, text.toString(), JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
    if (varStatement == null) return false;

    final PsiElement newDeclaration;
    if (assignment) {
      newDeclaration = anchorElement.replace(varStatement);
    }
    else {
      newDeclaration = anchorBlock.addBefore(varStatement, anchorElement);
    }
    CodeStyleManager.getInstance(project).reformatNewlyAddedElement(anchorBlock.getParent().getNode(), anchorBlock.getNode());

    if (newDeclaration != null) {
      PsiFile psiFile = myField.getContainingFile();
      int offset = newDeclaration.getTextOffset();
      final PsiElement context = psiFile.getContext();
      if (context != null) {
        psiFile = context.getContainingFile();
        offset = InjectedLanguageManager.getInstance(project).injectedToHost(newDeclaration, offset);
      }
      final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor != null) {
        final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (file == psiFile) {
          editor.getCaretModel().moveToOffset(offset);
          editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        }
      }
    }
    return true;
  }

  private void deleteField() {
    final PsiElement varStatement = myField.getParent();
    LOG.assertTrue(varStatement instanceof JSVarStatement);
    final PsiElement cl = varStatement.getParent();
    final PsiElement first = varStatement.getPrevSibling();
    if (first instanceof PsiWhiteSpace) {
      cl.deleteChildRange(first, varStatement);
    }
    else {
      myField.delete();
    }
  }

  private static boolean isAssignment(final PsiElement anchorElement, PsiElement ref) {
    if (anchorElement instanceof JSExpressionStatement expressionStatement) {
      final JSExpression expression = expressionStatement.getExpression();
      if (expression instanceof JSAssignmentExpression) {
        return ((JSAssignmentExpression)expression).getOperationSign() == JSTokenTypes.EQ &&
               PsiTreeUtil.isAncestor(((JSAssignmentExpression)expression).getLOperand(), ref, true);
      }
    }
    return false;
  }

  @Nullable
  private static JSBlockStatement findAnchorBlock(final Collection<PsiReference> references) {
    JSBlockStatement result = null;
    for (PsiReference psiReference : references) {
      final PsiElement element = psiReference.getElement();
      JSBlockStatement block = PsiTreeUtil.getParentOfType(element, JSBlockStatement.class);
      if (result == null || block == null) {
        result = block;
      }
      else {
        final PsiElement commonParent = PsiTreeUtil.findCommonParent(result, block);
        result = PsiTreeUtil.getParentOfType(commonParent, JSBlockStatement.class, false);
      }
    }
    return result;
  }

  @NotNull
  private static PsiElement getFirstElement(final Collection<PsiReference> references) {
    PsiElement firstElement = null;
    for (PsiReference reference : references) {
      final PsiElement element = reference.getElement();
      if (firstElement == null || firstElement.getTextRange().getStartOffset() > element.getTextRange().getStartOffset()) {
        firstElement = element;
      }
    }
    LOG.assertTrue(firstElement != null);
    return firstElement;
  }

  @NotNull
  private static PsiElement getAnchorElement(final JSBlockStatement anchorBlock, @NotNull final PsiElement firstElement) {
    PsiElement element = firstElement;
    while (element != null && element.getParent() != anchorBlock) {
      element = element.getParent();
    }
    LOG.assertTrue(element != null);
    return element;
  }
}
