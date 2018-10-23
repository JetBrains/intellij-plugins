// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.Consumer;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.jetbrains.annotations.NotNull;

import static org.angular2.Angular2DecoratorUtil.SELECTOR_PROP;

public class Angular2PomDeclarationSearcher extends PomDeclarationSearcher {

  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, Consumer<PomTarget> consumer) {
    if (element.getNode().getElementType() == JSTokenTypes.STRING_LITERAL
        && DialectDetector.isTypeScript(element)
        && element.getParent() instanceof JSLiteralExpression
        && element.getParent().getParent() instanceof JSProperty
        && SELECTOR_PROP.equals(((JSProperty)element.getParent().getParent()).getName())) {
      for (PsiReference ref : element.getParent().getReferences()) {
        if (ref.getRangeInElement().contains(offsetInElement)) {
          PsiElement resolved = ref.resolve();
          if (resolved instanceof Angular2DirectiveSelectorPsiElement) {
            consumer.consume((PomTarget)resolved);
          }
        }
      }
    }
  }
}
