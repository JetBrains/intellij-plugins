// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs;

import com.intellij.javascript.web.css.refs.CssClassInJSLiteralOrIdentifierReferenceProvider;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2CssReferencesContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, Angular2Language.INSTANCE,
                                                              Angular2Binding.class, Angular2AttributeValueProvider::isNgClassAttribute);
    registrar.registerReferenceProvider(CSS_CLASS_PATTERN_IN_ATTRIBUTE, new Angular2CssClassInAttributeReferenceProvider());
  }

  private static final PsiElementPattern.Capture<XmlAttribute> CSS_CLASS_PATTERN_IN_ATTRIBUTE =
    PlatformPatterns.psiElement(XmlAttribute.class)
      .and(new FilterPattern(new ElementFilter() {
        @Override
        public boolean isAcceptable(Object element, @Nullable PsiElement context) {
          Angular2AttributeNameParser.AttributeInfo info;
          if (context instanceof Angular2HtmlPropertyBinding
              || (context instanceof XmlAttribute
                  && !context.getLanguage().is(Angular2Language.INSTANCE))) {
            info = Angular2AttributeNameParser.parse(((XmlAttribute)context).getName(), ((XmlAttribute)context).getParent());
            return (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
                    && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.CLASS);
          }
          return false;
        }

        @Override
        public boolean isClassAcceptable(Class hintClass) {
          return true;
        }
      }));
}
