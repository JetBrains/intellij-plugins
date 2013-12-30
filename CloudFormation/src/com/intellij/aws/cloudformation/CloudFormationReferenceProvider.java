package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CloudFormationReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (element instanceof JSLiteralExpression) {
      final CloudFormationEntityReference reference = CloudFormationEntityReference.buildFromElement(element);
      if (reference != null) {
        return new PsiReference[]{reference};
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }
}
