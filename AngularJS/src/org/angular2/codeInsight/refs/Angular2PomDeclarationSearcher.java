// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.Angular2DecoratorUtil.SELECTOR_PROP;

public class Angular2PomDeclarationSearcher extends PomDeclarationSearcher {

  @SuppressWarnings("BoundedWildcard")
  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, @NotNull Consumer<PomTarget> consumer) {
    Angular2DirectiveSelector directiveSelector = null;
    if (element.getNode().getElementType() == JSTokenTypes.STRING_LITERAL
        && DialectDetector.isTypeScript(element)
        && element.getParent() instanceof JSLiteralExpression
        && element.getParent().getParent() instanceof JSProperty
        && SELECTOR_PROP.equals(((JSProperty)element.getParent().getParent()).getName())) {
      directiveSelector = doIfNotNull(Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator.class)),
                                      dir -> dir.getSelector());
      element = element.getParent();
    }
    else if (element instanceof Angular2HtmlNgContentSelector) {
      directiveSelector = ((Angular2HtmlNgContentSelector)element).getSelector();
    }
    if (directiveSelector != null) {
      for (Angular2DirectiveSelector.SimpleSelectorWithPsi selector : directiveSelector.getSimpleSelectorsWithPsi()) {
        Angular2DirectiveSelectorPsiElement selectorPart = selector.getElementAt(offsetInElement);
        if (selectorPart != null && selectorPart.getParent() == element) {
          consumer.consume(selectorPart);
        }
      }
    }
  }
}
