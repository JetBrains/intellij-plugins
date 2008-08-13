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
import com.intellij.psi.*;
import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import static com.intellij.struts2.reference.ReferenceFilters.NAMESPACE_STRUTS_XML;
import static com.intellij.struts2.reference.ReferenceFilters.NAMESPACE_TAGLIB_STRUTS_UI;
import com.intellij.struts2.reference.jsp.ActionReferenceProvider;
import com.intellij.struts2.reference.jsp.NamespaceReferenceProvider;
import com.intellij.struts2.reference.jsp.ThemeReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlUtil;
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

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {

    registerUITags(registrar);

    registerStrutsXmlTags(registrar);
  }

  private static void registerStrutsXmlTags(final PsiReferenceRegistrar registrar) {

    // <result> "name" common values
    registerTags(new StaticStringValuesReferenceProvider("error", "input", "login", "success"),
                 "name", NAMESPACE_STRUTS_XML, registrar,
                 "result");
  }

  private static void registerUITags(final PsiReferenceRegistrar registrar) {

    // common attributes --------------------------------------

    registerTags(new ThemeReferenceProvider(),
                 "theme", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "disabled", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);
//    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER, // TODO ?!
//                 "jsTooltipEnabled", ReferenceFilters.NAMESPACE_TAGLIB_STRUTS_UI,
//                 TAGLIB_UI_FORM_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "top"),
                 "labelposition", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "required", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS); // TODO all tags included?

    // elements with "readonly"
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "readonly", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "autocompleter", "combobox", "password", "textarea", "textfield");

    // elements with "action"
    registerTags(ACTION_REFERENCE_PROVIDER,
                 "action", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "form", "submit", "url");

    registerTags(ACTION_REFERENCE_PROVIDER,
                 "name", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "action");

    // elements with "value" (relative path)
    registerTags(RELATIVE_PATH_PROVIDER,
                 "value", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "include", "url");

    // elements with "namespace"
    registerTags(new NamespaceReferenceProvider(),
                 "namespace", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "action", "form", "url");

    // elements with "cssClass"
    registerTags(new CssInHtmlClassOrIdReferenceProvider(),
                 "cssClass", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 TAGLIB_UI_FORM_TAGS);

    // specific tags ---------------------------------------------------------------------------------------------------

    // <action>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "flush", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "action");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "executeResult", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "action");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "ignoreContextParams", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "action");

    // <form>
    registerTags(new StaticStringValuesReferenceProvider(false,
                                                         "application/x-www-form-urlencoded",
                                                         "multipart/form-data"),
                 "enctype", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("GET", "POST"),
                 "method", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "form");
    // TODO portletMode
    registerTags(new StaticStringValuesReferenceProvider("_blank", "_parent", "_self", "_top"),
                 "target", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "form");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "validate", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "form");
    // TODO windowState

    // <property>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "escape", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "property");

    // <select>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "emptyOption", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "select");

    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "multiple", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "select");

    // <set>
    registerTags(new StaticStringValuesReferenceProvider(false, "application", "session", "request", "page", "action"),
                 "scope", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "set");

    // <submit>
    registerTags(new StaticStringValuesReferenceProvider(false, "input", "button", "image", "submit"),
                 "type", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "submit");

    registerTags(RELATIVE_PATH_PROVIDER,
                 "src", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "submit");

    // <table>
    registerTags(new StaticStringValuesReferenceProvider(false, "ASC", "DESC", "NONE"),
                 "sortOrder", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "table");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "sortable", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "table");

    // <url>
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "encode", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "url");
    registerTags(BOOLEAN_VALUE_REFERENCE_PROVIDER,
                 "escapeAmp", NAMESPACE_TAGLIB_STRUTS_UI, registrar,
                 "url");
  }

  /**
   * Register the given provider on the given XmlAttribute/Namespace/XmlTag(s) combination.
   *
   * @param provider        Provider to install.
   * @param attributeName   Attribute name.
   * @param namespaceFilter Namespace for tag(s).
   * @param registrar       Registrar instance.
   * @param tagNames        Tag name(s).
   */
  private static void registerTags(final PsiReferenceProvider provider,
                                   @NonNls final String attributeName,
                                   final NamespaceFilter namespaceFilter,
                                   final PsiReferenceRegistrar registrar,
                                   @NonNls final String... tagNames) {
    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{attributeName},
                                                       ReferenceFilters.andTagNames(namespaceFilter, tagNames),
                                                       provider);
  }

}