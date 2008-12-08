/*
 * Copyright 2007 The authors
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

import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.patterns.ElementPattern;
import static com.intellij.patterns.StandardPatterns.and;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import com.intellij.psi.*;
import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.reference.jsp.ActionReferenceProvider;
import com.intellij.struts2.reference.jsp.NamespaceReferenceProvider;
import com.intellij.struts2.reference.jsp.ThemeReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Registers all {@link PsiReferenceProvider}s.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsReferenceContributor extends PsiReferenceContributor {

  @NonNls
  private static final String[] TAGLIB_UI_FORM_TAGS = new String[]{
      "autocompleter",
      "checkbox",
      "checkboxlist",
      "combobox",
      "doubleselect",
      "head",
      "file",
      "form",
      "hidden",
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

  private static final StaticStringValuesReferenceProvider BOOLEAN_VALUE_REFERENCE_PROVIDER =
      new StaticStringValuesReferenceProvider(false, "false", "true");

  private static final ActionReferenceProvider ACTION_REFERENCE_PROVIDER = new ActionReferenceProvider();

  private static final PsiReferenceProvider RELATIVE_PATH_PROVIDER = new PsiReferenceProviderBase() {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                                 @NotNull final ProcessingContext context) {
      return PathReferenceManager.getInstance().createReferences(element, false, false, true);
    }
  };

  /**
   * Struts UI taglib namespace pattern.
   */
  private static final ElementPattern<XmlAttributeValue> TAGLIB_STRUTS_UI_NAMESPACE =
      xmlAttributeValue().withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI));

  /**
   * struts.xml namespace pattern.
   */
  private static final ElementPattern<XmlAttributeValue> STRUTS_XML_NAMESPACE =
      xmlAttributeValue().withSuperParent(2, xmlTag().withNamespace(
          string().oneOf(StrutsConstants.STRUTS_2_0_DTD_ID, StrutsConstants.STRUTS_2_0_DTD_URI)));

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {

    registerUITags(registrar);

    registerStrutsXmlTags(registrar);
  }

  private static void registerStrutsXmlTags(final PsiReferenceRegistrar registrar) {

    // <result> "name" common values
    registerTags(new StaticStringValuesReferenceProvider("error", "input", "login", "success"),
                 "name", STRUTS_XML_NAMESPACE, registrar,
                 "result");
  }

  private static void registerUITags(final PsiReferenceRegistrar registrar) {

    // common attributes --------------------------------------

    registerTags(new ThemeReferenceProvider(),
                 "theme", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "disabled", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS);
//    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER, // TODO ?!
//                 "jsTooltipEnabled", TAGLIB_STRUTS_UI_NAMESPACE,
//                 TAGLIB_UI_FORM_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "top"),
                 "labelposition", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS);
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "required", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS); // TODO all tags included?

    // elements with "readonly"
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "readonly", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "autocompleter", "combobox", "password", "textarea", "textfield");

    // elements with "action"
    registerTags(ACTION_REFERENCE_PROVIDER,
                 "action", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "form", "submit", "url");

    registerTags(ACTION_REFERENCE_PROVIDER,
                 "name", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "action");

    // elements with "value" (relative path)
    registerTags(RELATIVE_PATH_PROVIDER,
                 "value", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "include", "url");

    // elements with "namespace"
    registerTags(new NamespaceReferenceProvider(),
                 "namespace", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "action", "form", "url");

    // elements with "cssClass"
    registerTags(new CssInHtmlClassOrIdReferenceProvider(),
                 "cssClass", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 TAGLIB_UI_FORM_TAGS);

    // specific tags ---------------------------------------------------------------------------------------------------

    // <action>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "flush", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "action");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "executeResult", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "action");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "ignoreContextParams", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "action");

    // <form>
    registerTags(new StaticStringValuesReferenceProvider(false,
                                                         "application/x-www-form-urlencoded",
                                                         "multipart/form-data"),
                 "enctype", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("GET", "POST"),
                 "method", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "form");
    // TODO portletMode
    registerTags(new StaticStringValuesReferenceProvider("_blank", "_parent", "_self", "_top"),
                 "target", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "form");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "validate", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "form");
    // TODO windowState

    // <property>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "escape", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "property");

    // <select>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "emptyOption", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "select");

    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "multiple", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "select");

    // <set>
    registerTags(new StaticStringValuesReferenceProvider(false, "application", "session", "request", "page", "action"),
                 "scope", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "set");

    // <submit>
    registerTags(new StaticStringValuesReferenceProvider(false, "input", "button", "image", "submit"),
                 "type", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "submit");

    registerTags(RELATIVE_PATH_PROVIDER,
                 "src", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "submit");

    // <table>
    registerTags(new StaticStringValuesReferenceProvider(false, "ASC", "DESC", "NONE"),
                 "sortOrder", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "table");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "sortable", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "table");

    // <url>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "encode", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "url");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "escapeAmp", TAGLIB_STRUTS_UI_NAMESPACE, registrar,
                 "url");
  }

  /**
   * Register the given provider on the given XmlAttribute/Namespace/XmlTag(s) combination.
   *
   * @param provider         Provider to install.
   * @param attributeName    Attribute name.
   * @param namespacePattern Namespace for tag(s).
   * @param registrar        Registrar instance.
   * @param tagNames         Tag name(s).
   */
  private static void registerTags(final PsiReferenceProvider provider,
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