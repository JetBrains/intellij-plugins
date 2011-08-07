/*
 * Copyright 2011 The authors
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
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.virtualFile;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Injects OGNL language into tag attributes.
 *
 * @author Yann C&eacute;bron
 */
public class TaglibOgnlInjector implements MultiHostInjector {

  // OGNL expression patterns
  private static final ElementPattern<XmlAttributeValue> OGNL_ELEMENT_PATTERN =
      xmlAttributeValue()
          .inVirtualFile(or(virtualFile().ofType(StdFileTypes.JSP),
                            virtualFile().ofType(StdFileTypes.JSPX)))
          .withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI,
                                                     StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI))
          .withValue(string().startsWith("%{"));
  // TODO "{ a, b, c}" - expressions

  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar multiHostRegistrar,
                                   @NotNull final PsiElement psiElement) {
    if (OGNL_ELEMENT_PATTERN.accepts(psiElement)) {
      final TextRange range = new TextRange(1, psiElement.getTextLength() - 1);
      multiHostRegistrar.startInjecting(OgnlLanguage.INSTANCE)
                        .addPlace(null, null, (PsiLanguageInjectionHost) psiElement, range)
                        .doneInjecting();
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }

}