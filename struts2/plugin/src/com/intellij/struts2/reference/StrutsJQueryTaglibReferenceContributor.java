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
 * Provides support for struts2-jquery plugin taglib.
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
    new String[]{"a", "accordion", "div", "tabbedpanel", "datepicker", "dialog", "progressbar",
                 "slider", "grid", "gridColumn", "textfield", "textarea", "select"};

  private static final String[] DRAG_DROP_TAGS =
    new String[]{"div", "textfield", "textarea", "select"};

  private static final String[] SORTABLE_TAGS =
    new String[]{"div", "select", "textfield"};

  private static final String[] SELECTABLE_TAGS =
    new String[]{"div", "select", "textfield"};

  private static final String[] RESIZABLE_TAGS =
    new String[]{"dialog", "div", "textarea", "textfield", "select"};

  private static final StaticStringValuesReferenceProvider ALL_EFFECTS_PROVIDER =
    new StaticStringValuesReferenceProvider("slide", "scale", "blind", "clip", "puff", "explode", "fold", "drop");

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

    // effect
    registerTags(new StaticStringValuesReferenceProvider("bounce", "highlight", "pulsate", "shake", "size", "transfer"),
                 "effect", registrar, "a", "div", "gridColumn", "submit", "tab", "textfield", "textarea");

    // draggable*
    registerBoolean("draggableAddClasses", registrar, DRAG_DROP_TAGS);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("parent"),
                 "draggableAppendTo", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "x", "y"),
                 "draggableAxis", registrar, DRAG_DROP_TAGS);
    registerBoolean("draggableCancel", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "parent", "document", "window", "y1", "x2", "y2"),
                 "draggableContainment", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "original", "clone"),
                 "draggableHelper", registrar, DRAG_DROP_TAGS);
    registerBoolean("draggableIframeFix", registrar, DRAG_DROP_TAGS);
    registerBoolean("draggableRefreshPositions", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "true", "false", "valid", "invalid"),
                 "draggableRevert", registrar, DRAG_DROP_TAGS);
    registerBoolean("draggableScroll", registrar, DRAG_DROP_TAGS);
    registerBoolean("draggableSnap", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "inner", "outer", "both"),
                 "draggableSnapMode", registrar, DRAG_DROP_TAGS);

    // droppable*
    registerTags(CSS_CLASS_PROVIDER, "droppableHoverClass", registrar, DRAG_DROP_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "fit", "intersect", "pointer", "touch"),
                 "droppableTolerance", registrar, DRAG_DROP_TAGS);

    // "events"
    registerTags(new StaticStringValuesReferenceProvider(false, "click", "dblclick", "mouseover", "mouseenter", "mouseleave"),
                 "events", registrar, "div", "select");

    // sortable**
    registerBoolean("sortable", registrar, SORTABLE_TAGS);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("parent"),
                 "sortableAppendTo", registrar, SORTABLE_TAGS);
    registerTags(new StaticStringValuesReferenceProvider("x", "y"),
                 "sortableAxis", registrar, SORTABLE_TAGS);
    registerBoolean("sortableDropOnEmpty", registrar, SORTABLE_TAGS);
    registerBoolean("sortableForceHelperSize", registrar, SORTABLE_TAGS);
    registerBoolean("sortableForcePlaceholderSize", registrar, SORTABLE_TAGS);
    registerTags(CSS_CLASS_PROVIDER, "sortablePlaceholder", registrar, SORTABLE_TAGS);
    registerBoolean("sortableRevert", registrar, SORTABLE_TAGS);
    registerBoolean("sortableScroll", registrar, SORTABLE_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "intersect", "pointer"),
                 "sortableTolerance", registrar, SORTABLE_TAGS);

    // "resizableXX"
    registerBoolean("resizable", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAnimate", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableGhost", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAspectRatio", registrar, RESIZABLE_TAGS);
    registerBoolean("resizableAutoHide", registrar, RESIZABLE_TAGS);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("document", "parent"),
                 "resizableContainment", registrar, RESIZABLE_TAGS);
    registerTags(CSS_CLASS_PROVIDER, "resizableHelper", registrar, RESIZABLE_TAGS);

    // "selectable"
    registerBoolean("selectable", registrar, SELECTABLE_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "touch", "fit"),
                 "selectableTolerance", registrar, SELECTABLE_TAGS);

    // "indicator"
    registerTags(HTML_ID_REFERENCE_PROVIDER, "indicator", registrar,
                 "a", "dialog", "div", "grid", "gridColumn", "submit", "textfield", "textarea", "select");

    // "errorElementId"
    registerTags(HTML_ID_REFERENCE_PROVIDER, "errorElementId", registrar,
                 "a", "dialog", "grid", "select", "submit", "tab", "textfield");

    // "disabled"
    registerBoolean("disabled", registrar, ArrayUtil.mergeArrays(CSS__TAGS, REQUIRED_TAGS, String.class));

    // "validate"
    registerTags(new StaticStringValuesReferenceProvider(false, "true", "false", "only"),
                 "validate", registrar,
                 "a", "dialog", "grid", "gridColumn", "select", "submit", "tab", "textarea", "textfield");

    // "required[position]"
    registerBoolean("required", registrar, REQUIRED_TAGS);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", registrar, REQUIRED_TAGS);

    // "targets" TODO allow multiple comma-separated
    registerTags(HTML_ID_REFERENCE_PROVIDER, "targets", registrar,
                 "a", "dialog", "grid", "gridColumn", "select", "submit", "textarea", "textfield", "tab");

    // "labelposition"
    registerTags(new StaticStringValuesReferenceProvider(false, "top", "left"),
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
    registerTags(new StaticStringValuesReferenceProvider(false, "button", "input", "image"),
                 "type", registrar, "submit");

    // <dialog>
    registerBoolean("autoOpen", registrar, "dialog");
    registerTags(CSS_CLASS_PROVIDER, "dialogClass", registrar, "dialog");
    registerTags(ALL_EFFECTS_PROVIDER, "hideEffect", registrar, "dialog");
    registerBoolean("modal", registrar, "dialog");
    registerTags(ALL_EFFECTS_PROVIDER, "showEffect", registrar, "dialog");

    // <accordion>
    registerTags(new StaticStringValuesReferenceProvider("false", "slide", "bounceslide"),
                 "animated", registrar, "accordion");
    registerBoolean("autoHeight", registrar, "accordion");
    registerBoolean("clearStyle", registrar, "accordion");
    registerBoolean("collapsible", registrar, "accordion");
    registerBoolean("fillSpace", registrar, "accordion");
    registerBoolean("openOnMouseover", registrar, "accordion");

    // <tabbedpanel>
    registerBoolean("animate", registrar, "tabbedpanel");
    registerBoolean("cache", registrar, "tabbedpanel");
    registerBoolean("collapsible", registrar, "tabbedpanel");
    registerBoolean("openOnMouseover", registrar, "tabbedpanel");
    registerBoolean("useSelectedTabCookie", registrar, "tabbedpanel");

    // <tab>
    registerTags(HTML_ID_REFERENCE_PROVIDER, "target", registrar, "tab");

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
    registerTags(HTML_ID_REFERENCE_PROVIDER, "displayValueElement", registrar, "slider");
    registerTags(new StaticStringValuesReferenceProvider(false, "horizontal", "vertical", "auto"),
                 "orientation", registrar, "slider");

    // <grid>
    registerBoolean("autoencode", registrar, "grid");
    registerBoolean("cellEdit", registrar, "grid");
    registerBoolean("editinline", registrar, "grid");
    registerBoolean("footerrow", registrar, "grid");
    registerBoolean("hiddengrid", registrar, "grid");
    registerBoolean("hidegrid", registrar, "grid");
    registerBoolean("hoverrows", registrar, "grid");
    registerBoolean("loadonce", registrar, "grid");
    registerBoolean("multiselect", registrar, "grid");
    registerBoolean("navigator", registrar, "grid");
    registerBoolean("navigatorAdd", registrar, "grid");
    registerBoolean("navigatorDelete", registrar, "grid");
    registerBoolean("navigatorEdit", registrar, "grid");
    registerBoolean("navigatorRefresh", registrar, "grid");
    registerBoolean("navigatorSearch", registrar, "grid");
    registerBoolean("navigatorView", registrar, "grid");
    registerBoolean("pager", registrar, "grid");
    registerBoolean("rownumbers", registrar, "grid");
    registerBoolean("scroll", registrar, "grid");
    registerBoolean("scrollrows", registrar, "grid");
    registerBoolean("shrinkToFit", registrar, "grid");
    registerBoolean("sortable", registrar, "grid");
    registerTags(new StaticStringValuesReferenceProvider(false, "asc", "desc"),
                 "sortorder", registrar, "grid");
    registerBoolean("userDataOnFooter", registrar, "grid");
    registerBoolean("viewrecords", registrar, "grid");

    // <gridColumn>
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "center", "right"),
                 "align", registrar, "gridColumn");
    registerBoolean("editable", registrar, "gridColumn");
    registerTags(new StaticStringValuesReferenceProvider(false, "text", "textarea", "select", "checkbox",
                                                         "password", "button", "image", "file"),
                 "edittype", registrar, "gridColumn");
    registerTags(new StaticStringValuesReferenceProvider("integer", "currency", "date", "checkbox"),
                 "formatter", registrar, "gridColumn");
    registerBoolean("hidden", registrar, "gridColumn");
    registerBoolean("hidedlg", registrar, "gridColumn");
    registerBoolean("resizable", registrar, "gridColumn");
    registerBoolean("search", registrar, "gridColumn");
    registerBoolean("sortable", registrar, "gridColumn");

    // <select>
    registerBoolean("emptyOption", registrar, "select");

  }

}