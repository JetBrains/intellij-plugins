package com.google.jstestdriver.idea.assertFramework.jasmine.codeInsight;

import com.google.jstestdriver.idea.assertFramework.AbstractJsGenerateAction;
import com.google.jstestdriver.idea.assertFramework.GenerateActionContext;
import com.google.jstestdriver.idea.assertFramework.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructure;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineSuiteStructure;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
abstract class JasmineGenerateMissingLifecycleMethodAction extends AbstractJsGenerateAction {

  @NotNull
  @Override
  public abstract String getHumanReadableDescription();

  public abstract String getMethodName();

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    return generator != null;
  }

  protected Runnable createGenerator(@NotNull GenerateActionContext context) {
    JasmineSuiteStructure suiteStructure = findSuiteStructure(context);
    if (suiteStructure == null) {
      return null;
    }
    return createGenerator(context, suiteStructure);
  }

  private Runnable createGenerator(@NotNull final GenerateActionContext context, @NotNull final JasmineSuiteStructure suiteStructure) {
    final PsiElement elementUnderCaret = context.getPsiElementUnderCaret();
    if (elementUnderCaret == null) {
      return null;
    }
    return new Runnable() {
      @Override
      public void run() {
        JSFunctionExpression specDefinitions = suiteStructure.getSpecDefinitions();
        PsiElement leftBrace = JsPsiUtils.getFunctionLeftBrace(specDefinitions);
        if (leftBrace == null) {
          return;
        }
        if (elementUnderCaret.getParent() != leftBrace.getParent()) {
          context.getCaretModel().moveToOffset(leftBrace.getTextRange().getEndOffset());
        }
        Template template = JsGeneratorUtils.createDefaultTemplate(getMethodName() + "(function() {|});");
        context.startTemplate(template);
      }
    };
  }

  private static JasmineSuiteStructure findSuiteStructure(GenerateActionContext context) {
    JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
    JasmineFileStructure fileStructure = builder.buildTestFileStructure(context.getJsFile());
    return fileStructure.findLowestSuiteStructureContainingOffset(context.getDocumentCaretOffset());
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    if (generator != null) {
      generator.run();
    }
  }

}
