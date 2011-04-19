/*
 * Copyright 2010 The authors
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
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssSupportLoader;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.virtualFile;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Adds CSS inline support for UI/jQuery-plugin tags in JSP(X).
 *
 * @author Yann C&eacute;bron
 */
public class TaglibCssInlineStyleInjector implements MultiHostInjector {

  private static final ElementPattern<XmlAttributeValue> CSS_ELEMENT_PATTERN =
      xmlAttributeValue()
          .withLocalName(StrutsConstants.TAGLIB_STRUTS_UI_CSS_ATTRIBUTES)
          .inVirtualFile(or(virtualFile().ofType(StdFileTypes.JSP),
                            virtualFile().ofType(StdFileTypes.JSPX)))
          .withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI,
                                                     StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI,
                                                     StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI));

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement context) {
    if (CSS_ELEMENT_PATTERN.accepts(context)) {
      final TextRange range = new TextRange(1, context.getTextLength() - 1);
      registrar.startInjecting(CssSupportLoader.CSS_FILE_TYPE.getLanguage())
          .addPlace("inline.style {", "}", (PsiLanguageInjectionHost) context, range)
          .doneInjecting();
    }
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }

}