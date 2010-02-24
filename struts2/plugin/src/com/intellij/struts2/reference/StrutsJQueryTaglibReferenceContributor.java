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
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Provides support for struts2-jquery plugin taglib. TODO heavy WIP
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/">struts2-jquery plugin homepage</a>.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsJQueryTaglibReferenceContributor extends StrutsTaglibReferenceContributorBase {

  private static final String[] CSS__TAGS =
    new String[]{"a", "div", "submit",
                 "tabbedpanel", "datepicker", "dialog", "accordion", "progressbar", "slider", "grid", "tab",
                 "textfield", "textarea", "select"};

  private static final String[] REQUIRED_TAGS =
    new String[]{"a", "div", "div", "tabbedpanel", "datepicker", "dialog", "progressbar",
                 "slider", "grid", "textfield", "textarea", "select"};

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    // common attributes -------------------------------------

    // CSS*
    registerTags(CSS_CLASS_PROVIDER, "cssClass", registrar, CSS__TAGS);
    registerTags(CSS_CLASS_PROVIDER, "cssErrorClass", registrar, CSS__TAGS);
    registerTags(CSS_CLASS_PROVIDER, "tooltipCssClass", registrar, CSS__TAGS);

    // "resizableXX"
    final String[] RESIZABLE_TAGS = new String[]{"dialog", "div", "textarea", "textfield", "select"};
    registerBoolean("resizable", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAnimate", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableGhost", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAspectRatio", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAutoHide", registrar, RESIZABLE_TAGS);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("document", "parent"),
                 "resizableContainment", registrar, RESIZABLE_TAGS);
    registerTags(CSS_CLASS_PROVIDER, "resizableHelper", registrar, RESIZABLE_TAGS);

    // "indicator"
    registerTags(HTML_ID_REFERENCE_PROVIDER, "indicator", registrar,
                 "a", "dialog", "div", "grid", "submit", "textfield", "textarea", "select");

    // "errorElementId"
    registerTags(HTML_ID_REFERENCE_PROVIDER, "errorElementId", registrar,
                 "a", "dialog", "grid", "select", "submit", "tab", "textfield");

    // TODO "disabled"

    // "required[position]"
    registerBoolean("required", registrar, REQUIRED_TAGS);
    registerTags(new StaticStringValuesReferenceProvider("left", "right"),
                 "requiredposition", registrar, REQUIRED_TAGS);

    // "labelposition"
    registerTags(new StaticStringValuesReferenceProvider("top", "left"),
                 "labelposition", registrar, ArrayUtil.mergeArrays(CSS__TAGS, REQUIRED_TAGS, String.class));

    // specific tags --------------------------------------------------------------

    // <head>
    registerBoolean("ajaxcache", registrar, "head");
    registerBoolean("ajaxhistory", registrar, "head");
    registerBoolean("compressed", registrar, "head");
    registerTags(HTML_ID_REFERENCE_PROVIDER, "defaultIndicator", registrar, "head");
    registerTags(new StaticStringValuesReferenceProvider("cupertino",
                                                         "darkness",
                                                         "lightness",
                                                         "redmond",
                                                         "smothness"),
                 "jquerytheme", registrar, "head");
    registerBoolean("jqueryui", registrar, "head");
    registerBoolean("loadFromGoogle", registrar, "head");
    registerBoolean("useJqGridPlugin", registrar, "head");


    // <submit>
    registerBoolean("clearForm", registrar, "submit");
    registerTags(RELATIVE_PATH_PROVIDER, "href", registrar, "submit");
    registerBoolean("iframe", registrar, "submit");
    registerBoolean("resetForm", registrar, "submit");
    registerTags(new StaticStringValuesReferenceProvider("button", "input", "image"),
                 "type", registrar, "submit");
    registerTags(new StaticStringValuesReferenceProvider("true", "false", "only"),
                 "validate", registrar, "submit");
  }

}