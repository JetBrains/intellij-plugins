package com.google.jstestdriver.idea.assertFramework.jasmine.codeInsight;

import com.google.jstestdriver.idea.assertFramework.codeInsight.AbstractJsGenerateAction;
import com.google.jstestdriver.idea.assertFramework.codeInsight.GenerateActionContext;
import com.google.jstestdriver.idea.assertFramework.codeInsight.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructure;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineSpecStructure;
import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JasmineGenerateNewSpecAction extends AbstractJsGenerateAction {

  @NotNull
  @Override
  public String getHumanReadableDescription() {
    return "Jasmine Spec";
  }

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    return generator != null;
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    if (generator != null) {
      generator.run();
    }
  }

  @Nullable
  private static Runnable createGenerator(final @NotNull GenerateActionContext context) {
    JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
    JasmineFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    return createGenerator(context, fileStructure);
  }

  @Nullable
  private static Runnable createGenerator(final @NotNull GenerateActionContext context, @NotNull final JasmineFileStructure fileStructure) {
    if (fileStructure.hasJasmineSymbols()) {
      final PsiElement psiElementUnderCaret = context.getPsiElementUnderCaret();
      if (psiElementUnderCaret == null) {
        return null;
      }
      return new Runnable() {
        @Override
        public void run() {
          final int caretOffset = context.getDocumentCaretOffset();
          PsiElement element = getPrecedingPsiElement(fileStructure, psiElementUnderCaret, caretOffset);
          int suitableCaretOffset = JsGeneratorUtils.findSuitableOffsetForNewStatement(element, caretOffset);
          context.getCaretModel().moveToOffset(suitableCaretOffset);
          Template template = JsGeneratorUtils.createDefaultTemplate("it(\"${spec name}\", function() {|});");
          context.startTemplate(template);
        }
      };
    }
    return null;
  }

  @NotNull
  private static PsiElement getPrecedingPsiElement(@NotNull JasmineFileStructure fileStructure,
                                                   @NotNull PsiElement psiElementUnderCaret,
                                                   final int caretOffset) {
    JasmineSpecStructure specStructure = fileStructure.findSpecContainingOffset(caretOffset);
    if (specStructure != null) {
      return specStructure.getEnclosingCallExpression();
    }
    return psiElementUnderCaret;
  }
}
