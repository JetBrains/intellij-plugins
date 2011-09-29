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

package com.intellij.struts2.reference;

import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.reference.jsp.NamespaceReferenceProvider;
import com.intellij.struts2.reference.jsp.ThemeReferenceProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Registers support for struts2 default UI taglib.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsUITaglibReferenceContributor extends StrutsTaglibReferenceContributorBase {

  /**
   * Form tags names.
   */
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

  private static final String[] TAGLIB_UI_FORM_INPUT_TAGS = new String[]{
      "checkbox",
      "checkboxlist",
      "combobox",
      "doubleselect",
      "file",
      "inputtransferselect",
      "optiontransferselect",
      "password",
      "radio",
      "select",
      "textarea",
      "textfield",
      "updownselect"
  };

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_STRUTS_UI_URI;
  }

  @Override
  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    // common attributes --------------------------------------

    registerTags(new ThemeReferenceProvider(),
                 "theme", registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerBoolean("disabled", registrar, TAGLIB_UI_FORM_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "top"),
                 "labelposition", registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "name", registrar,
                 TAGLIB_UI_FORM_INPUT_TAGS);
    registerTags(ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "list", registrar,
                 "doubleselect", "inputtransferselect", "optiontransferselect", "select", "updownselect");

    registerBoolean("required", registrar, TAGLIB_UI_FORM_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", registrar,
                 TAGLIB_UI_FORM_TAGS);

    // elements with "readonly"
    registerBoolean("readonly", registrar, "combobox", "password", "textarea", "textfield");

    // selection elements with "emptyOption"|"multiple"
    registerBoolean("emptyOption",
                    registrar,
                    "doubleselect", "inputtransferselect", "optiontransferselect", "select", "updownselect");
    registerBoolean("multiple",
                    registrar,
                    "doubleselect", "inputtransferselect", "optiontransferselect", "select", "updownselect");

    // elements with "action"
    registerTags(ACTION_REFERENCE_PROVIDER,
                 "action", registrar,
                 "form", "reset", "submit", "url");

    registerTags(ACTION_REFERENCE_PROVIDER,
                 "name", registrar,
                 "action");

    // elements with "value" (relative path)
    registerTags(RELATIVE_PATH_PROVIDER,
                 "value", registrar,
                 "include", "url");

    // elements with "namespace"
    registerTags(new NamespaceReferenceProvider(),
                 "namespace", registrar,
                 "action", "form", "url");

    // CSS classes
    registerTags(CSS_CLASS_PROVIDER,
                 "cssClass", registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(CSS_CLASS_PROVIDER,
                 "cssErrorClass", registrar,
                 TAGLIB_UI_FORM_TAGS);

    registerTags(CSS_CLASS_PROVIDER,
                 "tooltipCssClass", registrar,
                 TAGLIB_UI_FORM_TAGS);

    // *transfer/double-tags
    registerTags(CSS_CLASS_PROVIDER,
                 "buttonCssClass", registrar,
                 "inputtransferselect", "optiontransferselect");

    registerTags(CSS_CLASS_PROVIDER,
                 "doubleCssClass", registrar,
                 "inputtransferselect", "optiontransferselect");

    registerBoolean("doubleEmptyOption",
                    registrar,
                    "doubleselect", "inputtransferselect", "optiontransferselect");

    registerTags(ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "doubleName", registrar,
                 "doubleselect", "optiontransferselect");
    registerTags(ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "doubleList", registrar,
                 "doubleselect", "optiontransferselect");

    // specific tags ---------------------------------------------------------------------------------------------------

    // <action>
    registerBoolean("flush", registrar, "action");
    registerBoolean("executeResult", registrar, "action");
    registerBoolean("ignoreContextParams", registrar, "action");

    // <date>
    registerBoolean("nice", registrar, "date");

    // <form>
    registerTags(new StaticStringValuesReferenceProvider(false,
                                                         "application/x-www-form-urlencoded",
                                                         "multipart/form-data"),
                 "enctype", registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("GET", "POST"),
                 "method", registrar,
                 "form");
    registerTags(new StaticStringValuesReferenceProvider("_blank", "_parent", "_self", "_top"),
                 "target", registrar,
                 "form");
    registerBoolean("validate", registrar, "form");

    // <param>
    registerTags(ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "name", registrar,
                 "param");

    // <property>
    registerBoolean("escape", registrar, "property");
    registerBoolean("escapeJavaScript", registrar, "property");

    // <set>
    registerTags(new StaticStringValuesReferenceProvider(false, "application", "session", "request", "page", "action"),
                 "scope", registrar,
                 "set");

    // <submit>
    registerTags(new StaticStringValuesReferenceProvider(false, "input", "button", "image", "submit"),
                 "type", registrar,
                 "submit");
    registerTags(RELATIVE_PATH_PROVIDER,
                 "src", registrar,
                 "submit");

    // <text>
    registerTags(wrappedPropertiesProvider, "name", registrar, "text");
    registerBoolean("searchValueStack", registrar, "text");

    // <url>
    registerBoolean("encode", registrar, "url");
    registerBoolean("escapeAmp", registrar, "url");
    registerBoolean("forceAddSchemeHostAndPort", registrar, "url");
    registerBoolean("includeContext", registrar, "url");
    registerTags(new StaticStringValuesReferenceProvider(false, "none", "get", "all"),
                 "includeParams", registrar,
                 "url");
  }

}