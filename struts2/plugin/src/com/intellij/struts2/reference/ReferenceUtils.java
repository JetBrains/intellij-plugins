/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.reference;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.patterns.ElementPattern;
import static com.intellij.patterns.StandardPatterns.and;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.*;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NonNls;

/**
 * Various utility methods for working with {@link PsiReferenceProvider}s.
 *
 * @author Yann C&eacute;bron
 */
class ReferenceUtils {

  /**
   * JSP custom tags names.
   */
  @NonNls
  static final String[] TAGLIB_UI_FORM_TAGS = new String[]{
      "a",
      "checkbox",
      "checkboxlist",
      "combobox",
      "component",
      "debug",
      "div",
      "doubleselect",
      "head",
      "fielderror",
      "file",
      "form",
      "hidden",
      "inputtransferselect",
      "label",
      "optiontransferselect",
      "optgroup",
      "password",
      "radio",
      "reset",
      "select",
      "submit",
      "textarea",
      "textfield",
      "token",
      "updownselect"
  };

  /**
   * Struts UI taglib pattern (JSP(X)).
   */
  static final ElementPattern<XmlAttributeValue> TAGLIB_STRUTS_UI =
      xmlAttributeValue()
          .inVirtualFile(or(virtualFile().ofType(StdFileTypes.JSP),
                            virtualFile().ofType(StdFileTypes.JSPX)))
          .withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI));

  /**
   * struts.xml pattern.
   */
  static final ElementPattern<XmlAttributeValue> STRUTS_XML =
      xmlAttributeValue()
          .inVirtualFile(virtualFile().ofType(StdFileTypes.XML))
          .withSuperParent(2, xmlTag().withNamespace(string().oneOf(StrutsConstants.STRUTS_DTDS)));

  /**
   * Register the given provider on the given XmlAttribute/Namespace/XmlTag(s) combination.
   *
   * @param provider         Provider to install.
   * @param attributeName    Attribute name.
   * @param namespacePattern Namespace for tag(s).
   * @param registrar        Registrar instance.
   * @param tagNames         Tag name(s).
   */
  static void registerTags(final PsiReferenceProvider provider,
                           @NonNls final String attributeName,
                           final ElementPattern<XmlAttributeValue> namespacePattern,
                           final PsiReferenceRegistrar registrar,
                           @NonNls final String... tagNames) {
    registrar.registerReferenceProvider(
        and(
            xmlAttributeValue()
                .withLocalName(attributeName)
                .withSuperParent(2, xmlTag().withLocalName(string().oneOf(tagNames))),
            namespacePattern
        ),
        provider);
  }

}