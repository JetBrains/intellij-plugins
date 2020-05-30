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

/**
 * Base class for jQuery-plugin contributors.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
abstract class StrutsJQueryTaglibReferenceContributorBase extends StrutsTaglibReferenceContributorBase {

  protected void installCSS(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(Holder.CSS_CLASS_PROVIDER, "cssClass", registrar, tagNames);
    registerTags(Holder.CSS_CLASS_PROVIDER, "cssErrorClass", registrar, tagNames);
    registerTags(Holder.CSS_CLASS_PROVIDER, "tooltipCssClass", registrar, tagNames);
  }

  protected void installDraggable(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerBoolean("draggableAddClasses", registrar, tagNames);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("parent"),
                 "draggableAppendTo", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "x", "y"),
                 "draggableAxis", registrar, tagNames);
    registerBoolean("draggableCancel", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "parent", "document", "window", "y1", "x2", "y2"),
                 "draggableContainment", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "original", "clone"),
                 "draggableHelper", registrar, tagNames);
    registerBoolean("draggableIframeFix", registrar, tagNames);
    registerBoolean("draggableRefreshPositions", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "true", "false", "valid", "invalid"),
                 "draggableRevert", registrar, tagNames);
    registerBoolean("draggableScroll", registrar, tagNames);
    registerBoolean("draggableSnap", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "inner", "outer", "both"),
                 "draggableSnapMode", registrar, tagNames);
  }

  protected void installDroppable(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(Holder.CSS_CLASS_PROVIDER, "droppableHoverClass", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "fit", "intersect", "pointer", "touch"),
                 "droppableTolerance", registrar, tagNames);
  }

  protected void installEffect(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(new StaticStringValuesReferenceProvider("bounce", "highlight", "pulsate", "shake", "size", "transfer"),
                 "effect", registrar, tagNames);
  }

  protected void installErrorElementId(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "errorElementId", registrar, tagNames);
  }

  protected void installEvents(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(new StaticStringValuesReferenceProvider(false, "click", "dblclick", "mouseover", "mouseenter", "mouseleave"),
                 "events", registrar, tagNames);
  }

  protected void installIndicator(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "indicator", registrar, tagNames);
  }

  protected void installLabelposition(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerTags(new StaticStringValuesReferenceProvider(false, "top", "left"),
                 "labelposition", registrar, tagNames);
  }

  protected void installRequired(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerBoolean("required", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "right"),
                 "requiredposition", registrar, tagNames);
  }

  protected void installResizable(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerBoolean("resizable", registrar, tagNames);
    registerBoolean("resizableAnimate", registrar, tagNames);
    registerBoolean("resizableGhost", registrar, tagNames);
    registerBoolean("resizableAspectRatio", registrar, tagNames);
    registerBoolean("resizableAutoHide", registrar, tagNames);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("document", "parent"),
                 "resizableContainment", registrar, tagNames);
    registerTags(Holder.CSS_CLASS_PROVIDER, "resizableHelper", registrar, tagNames);
  }

  protected void installSelectable(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerBoolean("selectable", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "touch", "fit"),
                 "selectableTolerance", registrar, tagNames);
  }

  protected void installSortable(final PsiReferenceRegistrar registrar, final String... tagNames) {
    registerBoolean("sortable", registrar, tagNames);
    registerTags(new HtmlIdWithAdditionalVariantsReferenceProvider("parent"),
                 "sortableAppendTo", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider("x", "y"),
                 "sortableAxis", registrar, tagNames);
    registerBoolean("sortableDropOnEmpty", registrar, tagNames);
    registerBoolean("sortableForceHelperSize", registrar, tagNames);
    registerBoolean("sortableForcePlaceholderSize", registrar, tagNames);
    registerTags(Holder.CSS_CLASS_PROVIDER, "sortablePlaceholder", registrar, tagNames);
    registerBoolean("sortableRevert", registrar, tagNames);
    registerBoolean("sortableScroll", registrar, tagNames);
    registerTags(new StaticStringValuesReferenceProvider(false, "intersect", "pointer"),
                 "sortableTolerance", registrar, tagNames);
  }

  protected void installTargets(final PsiReferenceRegistrar registrar, final String... tagNames) {
    // TODO allow multiple comma-separated
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "targets", registrar, tagNames);
  }

}