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
 * Provides support for struts2-jquery plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/">struts2-jquery plugin homepage</a>.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String[] BUTTON_TAGS =
    new String[]{"a", "submit"};

  private static final String[] CSS_TAGS =
    new String[]{"a", "accordionItem", "autocompleter", "checkboxlist", "div", "submit",
      "tabbedpanel", "datepicker", "dialog", "accordion", "progressbar", "slider", "grid", "tab",
      "textfield", "textarea", "radio", "select", "spinner"};

  private static final String[] REQUIRED_TAGS =
    new String[]{"a", "accordionItem", "autocompleter", "accordion", "checkboxlist", "div", "tabbedpanel", "datepicker", "dialog",
      "progressbar", "slider", "grid", "gridColumn", "radio", "textfield", "textarea", "select", "spinner"};

  private static final String[] DRAG_DROP_TAGS =
    new String[]{"autocompleter", "checkboxlist", "div", "radio", "textfield", "textarea", "select", "spinner"};

  private static final String[] SORTABLE_TAGS =
    new String[]{"autocompleter", "checkboxlist", "div", "radio", "select", "textfield", "spinner"};

  private static final String[] SELECTABLE_TAGS =
    new String[]{"autocompleter", "checkboxlist", "div", "radio", "select", "textfield", "spinner"};

  private static final String[] RESIZABLE_TAGS =
    new String[]{"autocompleter", "checkboxlist", "dialog", "div", "radio", "textarea", "textfield", "select", "spinner"};

  private static final StaticStringValuesReferenceProvider ALL_EFFECTS_PROVIDER =
    new StaticStringValuesReferenceProvider("slide", "scale", "blind", "clip", "puff", "explode", "fold", "drop");

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // common attributes -------------------------------------

    // CSS*
    installCSS(registrar, CSS_TAGS);

    // button
    registerBoolean("button", registrar, BUTTON_TAGS);
    registerTags(Holder.CSS_CLASS_PROVIDER, "buttonIcon", registrar, BUTTON_TAGS);
    registerTags(Holder.CSS_CLASS_PROVIDER, "buttonIconSecondary", registrar, BUTTON_TAGS);

    // effect
    installEffect(registrar, "a", "div", "gridColumn", "radio", "submit", "tab", "textfield", "textarea");

    // draggable*
    installDraggable(registrar, DRAG_DROP_TAGS);

    // droppable*
    installDroppable(registrar, DRAG_DROP_TAGS);

    // "events"
    installEvents(registrar, "autocompleter", "div", "select");

    // sortable**
    installSortable(registrar, SORTABLE_TAGS);

    // "resizableXX"
    installResizable(registrar, RESIZABLE_TAGS);

    // "selectable"
    installSelectable(registrar, SELECTABLE_TAGS);

    // "indicator"
    installIndicator(registrar, "a", "autocompleter", "checkboxlist", "dialog", "div", "grid", "gridColumn",
                     "submit", "textfield", "textarea", "radio", "select");

    // "errorElementId"
    installErrorElementId(registrar, "a", "autocompleter", "checkboxlist", "dialog", "grid", "radio", "select", "submit", "tab",
                          "textfield");

    // "disabled"
    registerBoolean("disabled", registrar, ArrayUtil.mergeArrays(CSS_TAGS, REQUIRED_TAGS));

    // "validate"
    registerTags(new StaticStringValuesReferenceProvider(false, "true", "false", "only"),
                 "validate", registrar,
                 "a", "dialog", "grid", "gridColumn", "select", "submit", "tab", "textarea", "textfield");

    // "required[position]"
    installRequired(registrar, REQUIRED_TAGS);

    // "targets" 
    installTargets(registrar, "a", "autocompleter", "checkboxlist", "dialog", "grid", "gridColumn", "select",
                   "submit", "textarea", "textfield", "tab");

    // "labelposition"
    installLabelposition(registrar, ArrayUtil.mergeArrays(CSS_TAGS, REQUIRED_TAGS));

    // specific tags --------------------------------------------------------------

    // <head>
    registerBoolean("ajaxcache", registrar, "head");
    registerBoolean("ajaxhistory", registrar, "head");
    registerBoolean("compressed", registrar, "head");
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "defaultIndicator", registrar, "head");
    registerTags(new StaticStringValuesReferenceProvider("black-tie", "blitzer", "cupertino", "dot-luv",
                                                         "eggplant", "excite-bike", "flick", "hot-sneaks",
                                                         "humanity", "le-frog", "mint-choc", "overcast",
                                                         "pepper-grinder", "redmond", "smoothness", "start",
                                                         "sunny", "swanky-purse", "trontastic", "ui-darkness",
                                                         "ui-lightness", "vader"),
                 "jquerytheme", registrar, "head");
    registerBoolean("jqueryui", registrar, "head");
    registerBoolean("loadFromGoogle", registrar, "head");


    // <submit>
    registerBoolean("clearForm", registrar, "submit");
    registerTags(Holder.RELATIVE_PATH_PROVIDER, "href", registrar, "submit");
    registerBoolean("iframe", registrar, "submit");
    registerBoolean("resetForm", registrar, "submit");
    registerTags(new StaticStringValuesReferenceProvider(false, "button", "input", "image"),
                 "type", registrar, "submit");

    // <dialog>
    registerBoolean("autoOpen", registrar, "dialog");
    registerTags(Holder.CSS_CLASS_PROVIDER, "dialogClass", registrar, "dialog");
    registerTags(ALL_EFFECTS_PROVIDER, "hideEffect", registrar, "dialog");
    registerBoolean("modal", registrar, "dialog");
    registerTags(ALL_EFFECTS_PROVIDER, "showEffect", registrar, "dialog");

    // <accordion>
    registerTags(new StaticStringValuesReferenceProvider("false", "slide", "bounceslide", "fade"),
                 "animate", registrar, "accordion");
    registerTags(new StaticStringValuesReferenceProvider("auto", "content", "fill"),
                 "heightStyle", registrar, "accordion");
    registerBoolean("collapsible", registrar, "accordion");
    registerBoolean("openOnMouseover", registrar, "accordion");

    // <tabbedpanel>
    registerTags(ALL_EFFECTS_PROVIDER, "hide", registrar, "tabbedpanel");
    registerTags(ALL_EFFECTS_PROVIDER, "show", registrar, "tabbedpanel");
    registerBoolean("cache", registrar, "tabbedpanel");
    registerBoolean("collapsible", registrar, "tabbedpanel");
    registerBoolean("openOnMouseover", registrar, "tabbedpanel");
    registerBoolean("useSelectedTabCookie", registrar, "tabbedpanel");

    // <tab>
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "target", registrar, "tab");

    // <datepicker>
    registerBoolean("buttonImageOnly", registrar, "datepicker");
    registerBoolean("changeMonth", registrar, "datepicker");
    registerBoolean("changeYear", registrar, "datepicker");
    registerTags(new StaticStringValuesReferenceProvider(false, "slow", "normal", "fast"),
                 "duration", registrar, "datepicker");
    registerTags(new StaticStringValuesReferenceProvider(false, "0", "1", "2", "3", "4", "5", "6"),
                 "firstDay", registrar, "datepicker");
    registerTags(new StaticStringValuesReferenceProvider("show", "slideDown", "fadeIn"),
                 "showAnim", registrar, "datepicker");
    registerBoolean("showButtonPanel", registrar, "datepicker");
    registerTags(new StaticStringValuesReferenceProvider(false, "focus", "button", "both"),
                 "showOn", registrar, "datepicker");

    // <slider>
    registerBoolean("animate", registrar, "slider");
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "displayValueElement", registrar, "slider");
    registerTags(new StaticStringValuesReferenceProvider(false, "horizontal", "vertical", "auto"),
                 "orientation", registrar, "slider");

    // <select>
    registerBoolean("emptyOption", registrar, "select");
  }
}