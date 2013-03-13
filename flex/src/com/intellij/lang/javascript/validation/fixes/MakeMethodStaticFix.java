package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MakeMethodStaticFix implements IntentionAction, LocalQuickFix {

  @NotNull
  @Override
  public String getText() {
    return JSBundle.message("make.method.static");
  }

  @NotNull
  @Override
  public String getName() {
    return getText();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    invoke(descriptor.getPsiElement());
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (editor != null) {
      PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
      return element != null && element.isValid() && PsiTreeUtil.getNonStrictParentOfType(element, JSFunction.class) != null;
    }
    return false;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (editor != null) {
      PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
      invoke(element);
    }
  }

  private static void invoke(@NotNull PsiElement element) {
    final JSFunction function = PsiTreeUtil.getNonStrictParentOfType(element, JSFunction.class);
    final Collection<JSReferenceExpression> usages = Collections.synchronizedCollection(new HashSet<JSReferenceExpression>());
    ReferencesSearch.search(function, function.getUseScope()).forEach(new Processor<PsiReference>() {
      @Override
      public boolean process(PsiReference psiReference) {
        PsiElement element = psiReference.getElement();
        if (!(element instanceof JSReferenceExpression)) {
          return true;
        }
        JSReferenceExpression refExpr = (JSReferenceExpression)element;
        if (refExpr.getQualifier() == null) {
          return true;
        }
        usages.add(refExpr);
        return true;
      }
    });

    Collection<PsiElement> elementsToWrite = new ArrayList<PsiElement>();
    elementsToWrite.add(element);
    elementsToWrite.addAll(usages);
    if (!CodeInsightUtilBase.preparePsiElementsForWrite(elementsToWrite)) {
      return;
    }

    final JSClass clazz = JSUtils.getMemberContainingClass(function);
    AccessToken l = WriteAction.start();
    try {
      JSAttributeListWrapper wrapper = new JSAttributeListWrapper(function.getAttributeList());
      wrapper.overrideModifier(JSAttributeList.ModifierType.STATIC, true);
      wrapper.overrideModifier(JSAttributeList.ModifierType.FINAL, false);
      wrapper.applyTo(function);

      List<FormatFixer> formatters = new ArrayList<FormatFixer>();
      Collection<PsiFile> filesToOptimizeImports = new HashSet<PsiFile>();
      final boolean actionScript = JSUtils.isActionScript(element);
      for (JSReferenceExpression refExpr : usages) {
        JSClass currentClass = JSResolveUtil.getClassOfContext(refExpr);
        if (currentClass != null && currentClass.isEquivalentTo(clazz)) {
          JSRefactoringUtil.makeQualified(refExpr, null, false);
        }
        else {
          if (actionScript) {
            if (ImportUtils.needsImport(JSResolveUtil.getPackageNameFromPlace(refExpr), clazz)) {
              FormatFixer formatter = ImportUtils.insertImportStatements(refExpr, Collections.singletonList(clazz.getQualifiedName()));
              ContainerUtil.addIfNotNull(formatter, formatters);
            }
            filesToOptimizeImports.add(refExpr.getContainingFile());
          }
          JSRefactoringUtil.makeQualified(refExpr, clazz, true);
        }
      }

      for (PsiFile file : filesToOptimizeImports) {
        formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(file));
      }
      FormatFixer.fixAll(formatters);
    }
    finally {
      l.finish();
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
