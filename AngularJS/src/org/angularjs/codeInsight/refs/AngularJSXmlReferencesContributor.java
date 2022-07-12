// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.refs;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angularjs.codeInsight.DirectiveUtil.normalizeAttributeName;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSXmlReferencesContributor extends PsiReferenceContributor {
  public static final PsiElementPattern.Capture<XmlAttributeValue> UI_VIEW_REF = xmlAttributePattern("uiSref");
  public static final PsiElementPattern.Capture<XmlAttributeValue> NG_APP_REF = xmlAttributePattern("ngApp");

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(UI_VIEW_REF, new AngularJSUiRouterStatesReferencesProvider());
    registrar.registerReferenceProvider(NG_APP_REF, new AngularJSNgAppReferencesProvider());
  }

  private static PsiElementPattern.Capture<XmlAttributeValue> xmlAttributePattern(final @NotNull String directiveName) {
    return PlatformPatterns.psiElement(XmlAttributeValue.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        final XmlAttributeValue attributeValue = (XmlAttributeValue)element;
        final PsiElement parent = attributeValue.getParent();
        if (parent instanceof XmlAttribute && directiveName.equals(normalizeAttributeName(((XmlAttribute)parent).getName()))) {
          return AngularIndexUtil.hasAngularJS(attributeValue.getProject());
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }
}
