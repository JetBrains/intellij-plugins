/*
 * Copyright 2014 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.jsp;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlLanguageInjector;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.patterns.StandardPatterns.not;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Injects OGNL language into tag attributes.
 *
 * @author Yann C&eacute;bron
 */
final class TaglibOgnlInjector implements MultiHostInjector, DumbAware {
  private static class Holder {
    private static final XmlAttributeValuePattern STRUTS_TAG_ATTRIBUTE = xmlAttributeValue()
      .withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_CHART_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_TREE_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_GRID_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_JQUERY_MOBILE_PLUGIN_URI,
                                                 StrutsConstants.TAGLIB_BOOTSTRAP_PLUGIN_URI
      ))
      .withLocalName(not(string().oneOf(StrutsConstants.TAGLIB_STRUTS_UI_CSS_ATTRIBUTES))); // do not mix with CSS

    // generic attribute containing "%{" (multiple occurrences possible)
    private static final ElementPattern<XmlAttributeValue> OGNL_OCCURRENCE_PATTERN = xmlAttributeValue()
      .withValue(string().longerThan(OgnlLanguage.EXPRESSION_PREFIX.length()).contains(OgnlLanguage.EXPRESSION_PREFIX));

    // attributes always containing expression _without_ prefix
    // <s:iterator> "value"
    private static final ElementPattern<XmlAttributeValue> OGNL_WITHOUT_PREFIX_PATTERN = xmlAttributeValue()
      .withLocalName("value")
      .withSuperParent(2, xmlTag().withLocalName("iterator"));

    // list/map expression
    private static final ElementPattern<XmlAttributeValue> OGNL_LIST_MAP_PATTERN = xmlAttributeValue()
      .withValue(string().andOr(string().startsWith("{"), string().startsWith("#{")));
  }
  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar multiHostRegistrar,
                                   @NotNull final PsiElement psiElement) {
    final FileType fileType = psiElement.getContainingFile().getFileType();
    if (fileType != StdFileTypes.JSP && fileType != StdFileTypes.JSPX) {
      return;
    }

    if (!Holder.STRUTS_TAG_ATTRIBUTE.accepts(psiElement)) {
      return;
    }

    if (!psiElement.isValid() ||
        !psiElement.getContainingFile().isValid()) {
      return;
    }

    if (Holder.OGNL_OCCURRENCE_PATTERN.accepts(psiElement)) {
      OgnlLanguageInjector.injectOccurrences(multiHostRegistrar, (PsiLanguageInjectionHost)psiElement);
      return;
    }

    if (Holder.OGNL_WITHOUT_PREFIX_PATTERN.accepts(psiElement)) {
      OgnlLanguageInjector.injectElementWithPrefixSuffix(multiHostRegistrar, (PsiLanguageInjectionHost)psiElement);
      return;
    }

    if (Holder.OGNL_LIST_MAP_PATTERN.accepts(psiElement)) {
      OgnlLanguageInjector.injectElementWithPrefixSuffix(multiHostRegistrar, (PsiLanguageInjectionHost)psiElement);
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(XmlAttributeValue.class);
  }
}