package com.intellij.aws.cloudformation.references;

import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class CloudFormationReferenceBase extends PsiReferenceBase<JSLiteralExpression> {
  public CloudFormationReferenceBase(@NotNull JSLiteralExpression element) {
    super(element);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return getCompletionVariants();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final JSArrayLiteralExpression arrayElement =
      (JSArrayLiteralExpression)JSChangeUtil.createJSTreeFromText(
        myElement.getProject(), "[\"" + newElementName + "\"]",
        JSONLanguageDialect.JSON).getPsi();
    final JSExpression newElement = arrayElement.getExpressions()[0];
    return myElement.replace(newElement);
  }

  @NotNull
  public abstract String[] getCompletionVariants();
}
