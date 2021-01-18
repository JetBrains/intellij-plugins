/*
 * Copyright 2013 The authors
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
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.injections.JSInXmlLanguagesInjector;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.patterns.StandardPatterns.*;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Adds JavaScript support for Struts UI/jQuery-plugin tags.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
final class TaglibJavaScriptInjector implements MultiHostInjector, DumbAware {
  private static class Holder {
    private static final ElementPattern<XmlAttributeValue> JS_ATTRIBUTE_PATTERN =
      xmlAttributeValue()
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

    // everything with "onXXX"
    private static final ElementPattern<XmlAttributeValue> JS_TAGLIB_PATTERN =
      xmlAttributeValue()
        .withLocalName(
          and(
            string().longerThan(5), // shortest "onXXX" attribute name: 6 characters
            or(string().startsWith("on"),
               string().startsWith("doubleOn")),  // **TransferSelect-tags
            not(string().endsWith("Topics"))));   // exclude jQuery-plugin "onXXXTopics"

    // struts2-jQuery taglib "pseudo" JS-highlighting
    private static final ElementPattern<XmlAttributeValue> JS_JQUERY_PATTERN =
      xmlAttributeValue()
        .withLocalName("effectOptions",
                       // dialog
                       "buttons",
                       // datepicker
                       "showOptions",
                       // grid
                       "filterOptions", "navigatorAddOptions", "navigatorDeleteOptions",
                       "navigatorEditOptions", "navigatorSearchOptions", "navigatorViewOptions",
                       // gridColumn
                       "editoptions", "editrules", "searchoptions",
                       // tabbedPanel
                       "disabledTabs");
  }
  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    final FileType fileType = host.getContainingFile().getFileType();
    if (fileType != StdFileTypes.JSP && fileType != StdFileTypes.JSPX) {
      return;
    }

    if (!Holder.JS_ATTRIBUTE_PATTERN.accepts(host)) {
      return;
    }

    if (Holder.JS_TAGLIB_PATTERN.accepts(host)) {
      JSInXmlLanguagesInjector.injectJSIntoAttributeValue(registrar, (XmlAttributeValue)host, false);
      return;
    }

    // "pseudo" JS
    if (Holder.JS_JQUERY_PATTERN.accepts(host)) {
      registrar.startInjecting(JavaScriptSupportLoader.JAVASCRIPT.getLanguage())
        .addPlace("(", ")", (PsiLanguageInjectionHost)host,
                  TextRange.from(1, host.getTextLength() - 2))
        .doneInjecting();
    }
  }

  @Override
  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(XmlAttributeValue.class);
  }
}