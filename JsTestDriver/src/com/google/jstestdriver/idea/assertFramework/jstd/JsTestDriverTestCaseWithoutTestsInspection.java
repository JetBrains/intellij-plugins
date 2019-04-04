package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public class JsTestDriverTestCaseWithoutTestsInspection extends JSInspection {

  @Override
  public boolean isEnabledByDefault() {
    return false;
  }

  @NotNull
  @Override
  protected PsiElementVisitor createVisitor(ProblemsHolder holder, LocalInspectionToolSession session) {
    JSFile jsFile = ObjectUtils.tryCast(holder.getFile(), JSFile.class);
    if (jsFile == null) {
      return JSElementVisitor.NOP_ELEMENT_VISITOR;
    }
    Project project = holder.getProject();
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      if (!JstdSettingsUtil.areJstdConfigFilesInProjectCached(project)) {
        return JSElementVisitor.NOP_ELEMENT_VISITOR;
      }
    }
    VirtualFile virtualFile = jsFile.getVirtualFile();
    if (virtualFile != null) {
      boolean inScope = JstdLibraryUtil.isFileInJstdLibScope(project, virtualFile);
      if (!inScope) {
        return JSElementVisitor.NOP_ELEMENT_VISITOR;
      }
    }
    JstdTestFileStructure testFileStructure = JstdTestFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    for (JstdTestCaseStructure structure : testFileStructure.getTestCaseStructures()) {
      if (structure.getTestCount() == 0) {
        JSCallExpression callExpression = structure.getEnclosingCallExpression();
        if (callExpression.isValid()) {
          JSReferenceExpression methodExpression = ObjectUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
          if (methodExpression != null) {
            int startOffset = methodExpression.getStartOffsetInParent();
            TextRange rangeInElement = TextRange.create(
              startOffset,
              startOffset + methodExpression.getTextLength()
            );
            holder.registerProblem(
              callExpression,
              "TestCase has no tests. Tests names should have 'test' prefix.",
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
              rangeInElement,
              LocalQuickFix.EMPTY_ARRAY
            );
          }
        }
      }
    }
    return JSElementVisitor.NOP_ELEMENT_VISITOR;
  }

}
