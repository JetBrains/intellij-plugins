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

package com.intellij.struts2.reference;

import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Provides support for struts2-jquery mobile plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/">struts2-jquery plugin homepage</a>.
 *
 * @author Johannes Geppert
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryMobileTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String[] BUTTON_TAGS =
    new String[]{"a"};

  private static final String[] CSS_TAGS =
    new String[]{"a", "checkbox", "checkboxlist", "div", "flipSwitch",
      "list", "listItem", "password", "searchfield", "slider",
      "textfield", "textarea", "radio", "select"};

  private static final String[] REQUIRED_TAGS =
    new String[]{"a", "checkbox", "checkboxlist", "div", "flipSwitch",
      "list", "listItem", "password", "searchfield", "slider",
      "textfield", "textarea", "radio", "select"};

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_MOBILE_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // common attributes -------------------------------------

    // CSS*
    installCSS(registrar, CSS_TAGS);

    // button
    registerBoolean("button", registrar, BUTTON_TAGS);

    // effect
    installEffect(registrar, "a", "div");

    // "events"
    installEvents(registrar, "div", "select");

    // "indicator"
    installIndicator(registrar, "a", "div");

    // "disabled"
    registerBoolean("disabled", registrar, ArrayUtil.mergeArrays(CSS_TAGS, REQUIRED_TAGS));

    // "required[position]"
    installRequired(registrar, REQUIRED_TAGS);

    // "targets" 
    installTargets(registrar, "a");

    // "labelposition"
    installLabelposition(registrar, ArrayUtil.mergeArrays(CSS_TAGS, REQUIRED_TAGS));

    // specific tags --------------------------------------------------------------

    // <checkboxList>
    registerBoolean("horizontal", registrar, "checkboxList");
  }
}
