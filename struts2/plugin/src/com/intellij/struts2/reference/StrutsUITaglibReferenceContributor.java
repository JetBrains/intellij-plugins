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

package com.intellij.struts2.reference;

import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.reference.jsp.ActionMethodReferenceProvider;
import com.intellij.struts2.reference.jsp.NamespaceReferenceProvider;
import com.intellij.struts2.reference.jsp.ThemeReferenceProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Registers support for struts2 default UI taglib.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsUITaglibReferenceContributor extends StrutsTaglibReferenceContributorBase {
  private static class Holder {
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
      "hidden",
      "inputtransferselect",
      "optiontransferselect",
      "password",
      "radio",
      "select",
      "textarea",
      "textfield",
      "updownselect"
    };
  }
  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_STRUTS_UI_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // common attributes --------------------------------------

    registerTags(new ThemeReferenceProvider(),
                 "theme", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    registerBoolean("disabled", registrar, Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "top"),
                 "labelposition", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "name", registrar,
                 Holder.TAGLIB_UI_FORM_INPUT_TAGS);
    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "list", registrar,
                 "doubleselect", "inputtransferselect", "optiontransferselect", "select", "updownselect");

    registerBoolean("required", registrar, Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(StrutsTaglibReferenceContributorBase.Holder.ID_REFERENCE_PROVIDER,
                 "id", registrar,
                 Holder.TAGLIB_UI_FORM_INPUT_TAGS);

    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(wrappedPropertiesProvider,
                 "key", registrar,
                 Holder.TAGLIB_UI_FORM_INPUT_TAGS);

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
    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_REFERENCE_PROVIDER,
                 "action", registrar,
                 "a", "form", "reset", "submit", "url");

    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_REFERENCE_PROVIDER,
                 "name", registrar,
                 "action");

    registerTags(new ActionMethodReferenceProvider(),
                 "method", registrar,
                 "a", "reset", "submit", "url");

    // elements with "value" (relative path)
    registerTags(StrutsTaglibReferenceContributorBase.Holder.RELATIVE_PATH_PROVIDER,
                 "value", registrar,
                 "include", "url");

    // elements with "namespace"
    registerTags(new NamespaceReferenceProvider(),
                 "namespace", registrar,
                 "a", "action", "form", "url");

    // CSS classes
    registerTags(StrutsTaglibReferenceContributorBase.Holder.CSS_CLASS_PROVIDER,
                 "cssClass", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(StrutsTaglibReferenceContributorBase.Holder.CSS_CLASS_PROVIDER,
                 "cssErrorClass", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    registerTags(StrutsTaglibReferenceContributorBase.Holder.CSS_CLASS_PROVIDER,
                 "tooltipCssClass", registrar,
                 Holder.TAGLIB_UI_FORM_TAGS);

    // *transfer/double-tags
    registerTags(StrutsTaglibReferenceContributorBase.Holder.CSS_CLASS_PROVIDER,
                 "buttonCssClass", registrar,
                 "inputtransferselect", "optiontransferselect");

    registerTags(StrutsTaglibReferenceContributorBase.Holder.CSS_CLASS_PROVIDER,
                 "doubleCssClass", registrar,
                 "inputtransferselect", "optiontransferselect");

    registerBoolean("doubleEmptyOption",
                    registrar,
                    "doubleselect", "inputtransferselect", "optiontransferselect");

    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_PROPERTY_REFERENCE_PROVIDER,
                 "doubleName", registrar,
                 "doubleselect", "optiontransferselect");
    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_PROPERTY_REFERENCE_PROVIDER,
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
    registerTags(StrutsTaglibReferenceContributorBase.Holder.ACTION_PROPERTY_REFERENCE_PROVIDER,
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
    registerTags(StrutsTaglibReferenceContributorBase.Holder.RELATIVE_PATH_PROVIDER,
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