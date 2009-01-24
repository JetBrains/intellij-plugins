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
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.patterns.ElementPattern;
import static com.intellij.patterns.PlatformPatterns.virtualFile;
import static com.intellij.patterns.StandardPatterns.*;
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

  private static final CssInHtmlClassOrIdReferenceProvider CSS_CLASS_PROVIDER = new CssInHtmlClassOrIdReferenceProvider();

  /**
   * Struts UI taglib pattern (JSP(X)).
   */
  private static final ElementPattern<XmlAttributeValue> TAGLIB_STRUTS_UI =
      xmlAttributeValue()
          .inVirtualFile(or(virtualFile().ofType(StdFileTypes.JSP),
                            virtualFile().ofType(StdFileTypes.JSPX)))
          .withSuperParent(2, xmlTag().withNamespace(StrutsConstants.TAGLIB_STRUTS_UI_URI));

  /**
   * struts.xml pattern.
   */
  private static final ElementPattern<XmlAttributeValue> STRUTS_XML =
      xmlAttributeValue()
          .inVirtualFile(virtualFile().ofType(StdFileTypes.XML))
          .withSuperParent(2, xmlTag().withNamespace(string().oneOf(
              StrutsConstants.STRUTS_2_0_DTD_ID, StrutsConstants.STRUTS_2_0_DTD_URI)));

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {

    registerUITags(registrar);

    registerStrutsXmlTags(registrar);
  }

  private static void registerStrutsXmlTags(final PsiReferenceRegistrar registrar) {

    // <result> "name" common values
    registerTags(new StaticStringValuesReferenceProvider("error", "input", "login", "success"),
                 "name", STRUTS_XML, registrar,
                 "result");
  }

  private static void registerUITags(final PsiReferenceRegistrar registrar) {

    // common attributes --------------------------------------

    registerTags(new ThemeReferenceProvider(),
                 "theme", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerBooleanUI("disabled", registrar, TAGLIB_UI_FORM_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "top"),
                 "labelposition", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerBooleanUI("required", registrar, TAGLIB_UI_FORM_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    // elements with "readonly"
    registerBooleanUI("readonly", registrar, "combobox", "password", "textarea", "textfield");

    // elements with "action"
    registerTags(ACTION_REFERENCE_PROVIDER,
                 "action", TAGLIB_STRUTS_UI, registrar,
                 "form", "reset", "submit", "url");

    registerTags(ACTION_REFERENCE_PROVIDER,
                 "name", TAGLIB_STRUTS_UI, registrar,
                 "action");

    // elements with "value" (relative path)
    registerTags(RELATIVE_PATH_PROVIDER,
                 "value", TAGLIB_STRUTS_UI, registrar,
                 "include", "url");

    // elements with "namespace"
    registerTags(new NamespaceReferenceProvider(),
                 "namespace", TAGLIB_STRUTS_UI, registrar,
                 "action", "form", "url");

    // CSS classes
    // FIX TODO move to separate!! CSS plugin is optional ==========================
    registerTags(CSS_CLASS_PROVIDER,
                 "cssClass", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(CSS_CLASS_PROVIDER,
                 "cssErrorClass", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(CSS_CLASS_PROVIDER,
                 "tooltipCssClass", TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    // *transfer-tags with additional CSS
    registerTags(CSS_CLASS_PROVIDER,
                 "buttonCssClass", TAGLIB_STRUTS_UI, registrar,
                 "inputtransferselect", "optiontransferselect");

    registerTags(CSS_CLASS_PROVIDER,
                 "doubleCssClass", TAGLIB_STRUTS_UI, registrar,
                 "inputtransferselect", "optiontransferselect");

    // specific tags ---------------------------------------------------------------------------------------------------

    // <action>
    registerBooleanUI("flush", registrar, "action");
    registerBooleanUI("executeResult", registrar, "action");
    registerBooleanUI("ignoreContextParams", registrar, "action");

    // date
    registerBooleanUI("nice", registrar, "date");

    // <form>
    registerTags(new StaticStringValuesReferenceProvider(false,
                                                         "application/x-www-form-urlencoded",
                                                         "multipart/form-data"),
                 "enctype", TAGLIB_STRUTS_UI, registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("GET", "POST"),
                 "method", TAGLIB_STRUTS_UI, registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("_blank", "_parent", "_self", "_top"),
                 "target", TAGLIB_STRUTS_UI, registrar,
                 "form");
    registerBooleanUI("validate", registrar, "form");


    // <property>
    registerBooleanUI("escape", registrar, "property");
    registerBooleanUI("escapeJavaScript", registrar, "property");

    // <select>
    registerBooleanUI("emptyOption", registrar, "select");
    registerBooleanUI("multiple", registrar, "select");

    // <set>
    registerTags(new StaticStringValuesReferenceProvider(false, "application", "session", "request", "page", "action"),
                 "scope", TAGLIB_STRUTS_UI, registrar,
                 "set");

    // <submit>
    registerTags(new StaticStringValuesReferenceProvider(false, "input", "button", "image", "submit"),
                 "type", TAGLIB_STRUTS_UI, registrar,
                 "submit");
    registerTags(RELATIVE_PATH_PROVIDER,
                 "src", TAGLIB_STRUTS_UI, registrar,
                 "submit");

    // <text>
    registerBooleanUI("searchValueStack", registrar, "text");

    // <url>
    registerBooleanUI("encode", registrar, "url");
    registerBooleanUI("escapeAmp", registrar, "url");
    registerBooleanUI("forceAddSchemeHostAndPort", registrar, "url");
    registerBooleanUI("includeContext", registrar, "url");
    registerTags(new StaticStringValuesReferenceProvider(false, "none", "get", "all"),
                 "includeParams", TAGLIB_STRUTS_UI, registrar,
                 "url");
  }

  /**
   * Registers a boolean value (true/false) provider on the given Struts-UI tag(s)/attribute-combination.
   *
   * @param attributeName Struts UI tag attribute name.
   * @param registrar     Registrar instance.
   * @param tagNames      Struts UI Tag name(s).
   */
  private static void registerBooleanUI(@NonNls final String attributeName,
                                        final PsiReferenceRegistrar registrar,
                                        @NonNls final String... tagNames) {
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 attributeName,
                 TAGLIB_STRUTS_UI,
                 registrar,
                 tagNames);
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