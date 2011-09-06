package com.google.jstestdriver.idea.assertFramework.qunit.generate;

import com.google.jstestdriver.idea.assertFramework.AbstractJsGenerateAction;
import com.google.jstestdriver.idea.assertFramework.GenerateActionContext;
import com.google.jstestdriver.idea.assertFramework.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructure;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitModuleStructure;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitTestMethodStructure;
import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QUnitGenerateNewTestAction extends AbstractJsGenerateAction {
  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "QUnit Test";
  }

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
    QUnitFileStructure fileStructure = builder.buildTestFileStructure(context.getJsFile());
    Runnable generator = createGenerator(context, fileStructure);
    return generator != null;
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
    QUnitFileStructure fileStructure = builder.buildTestFileStructure(context.getJsFile());
    Runnable generator = createGenerator(context, fileStructure);
    if (generator != null) {
      generator.run();
    }
  }

  @Nullable
  private static Runnable createGenerator(final @NotNull GenerateActionContext context, @NotNull final QUnitFileStructure fileStructure) {
    if (fileStructure.hasQUnitSymbols()) {
      final PsiElement psiElement = context.getPsiElementUnderCaret();
      if (psiElement == null) {
        return null;
      }
      return new Runnable() {
        @Override
        public void run() {
          final int caretOffset = context.getDocumentCaretOffset();
          QUnitTestMethodStructure testMethodStructure = fileStructure.findTestMethodStructureContainingOffset(caretOffset);
          PsiElement element = psiElement;
          if (testMethodStructure != null) {
            element = testMethodStructure.getCallExpression();
          }
          QUnitModuleStructure moduleStructureUnderCaret = fileStructure.findModuleStructureContainingOffset(caretOffset);
          if (moduleStructureUnderCaret != null) {
            element = moduleStructureUnderCaret.getEnclosingCallExpression();
          }
          int suitableCaretOffset = JsGeneratorUtils.findSuitableOffsetForNewStatement(element, caretOffset);
          context.getCaretModel().moveToOffset(suitableCaretOffset);
          Template template = JsGeneratorUtils.createDefaultTemplate("test(\"${test name}\", function() {|});");
          context.startTemplate(template);
        }
      };
    }
    return null;
  }
}
