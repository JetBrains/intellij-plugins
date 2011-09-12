package com.google.jstestdriver.idea.assertFramework.jasmine.codeInsight;

import com.google.jstestdriver.idea.assertFramework.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.codeInsight.JsCallTemplateContextProvider;
import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

/**
 * @author Sergey Simonchik
 */
public class JasmineSuiteCallTemplateContextProvider implements JsCallTemplateContextProvider {
  @Override
  public String getCalledFunctionName() {
    return "describe";
  }

  @Override
  public String getTailText() {
    return "(description, specs)";
  }

  @Override
  public String getTypeText() {
    return "Jasmine suite";
  }

  @Override
  public Template getTemplate() {
    return JsGeneratorUtils.createDefaultTemplate("describe(\"${suite name}\", function() {|});");
  }

  @Override
  public boolean isInContext(CompletionParameters parameters) {
    PsiElement element = parameters.getOriginalPosition();
    if (element == null) {
      return false;
    }
    final JSExpressionStatement head = JstdConfigFileUtils.getVerifiedHierarchyHead(
      element,
      new Class[] {
        LeafPsiElement.class,
        JSReferenceExpression.class
      },
      JSExpressionStatement.class
    );
    return head != null;
  }
}
