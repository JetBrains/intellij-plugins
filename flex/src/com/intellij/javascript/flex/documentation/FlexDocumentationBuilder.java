package com.intellij.javascript.flex.documentation;

import com.intellij.lang.javascript.documentation.JSDocumentationBuilder;
import com.intellij.lang.javascript.documentation.JSDocumentationProvider;
import com.intellij.psi.PsiElement;

/**
 * @author Konstantin.Ulitin
 */
public class FlexDocumentationBuilder extends JSDocumentationBuilder {
  FlexDocumentationBuilder(PsiElement element,
                           PsiElement _contextElement,
                           JSDocumentationProvider provider) {
    super(element, _contextElement, provider);
  }

  @Override
  public void fillEvaluatedType() {
  }

  @Override
  public boolean showDoc() {
    return false;
  }
}
