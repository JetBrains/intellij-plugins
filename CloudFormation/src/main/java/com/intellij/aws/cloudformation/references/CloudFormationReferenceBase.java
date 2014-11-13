package com.intellij.aws.cloudformation.references;

import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class CloudFormationReferenceBase extends PsiReferenceBase<JsonLiteral> {
  public CloudFormationReferenceBase(@NotNull JsonLiteral element) {
    super(element);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return getCompletionVariants();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final JsonStringLiteral newElement = new JsonElementGenerator(myElement.getProject()).createStringLiteral(newElementName);
    return myElement.replace(newElement);
  }

  @NotNull
  public abstract String[] getCompletionVariants();
}
