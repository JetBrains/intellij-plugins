/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Annotates custom tags with "action" attribute.
 *
 * @author Yann C&eacute;bron
 */
public class JspActionAnnotator implements LineMarkerProvider {

  @NonNls
  private static final String ACTION_ATTRIBUTE_NAME = "action";

  @NonNls
  private static final String[] TAGS_WITH_ACTION_ATTRIBUTE = new String[]{"action", "form", "reset", "submit", "url"};

  private static final NullableFunction<Action, PsiMethod> ACTION_METHOD_FUNCTION = new NullableFunction<Action, PsiMethod>() {
    public PsiMethod fun(final Action action) {
      return action.searchActionMethod();
    }
  };

  @Override
  public LineMarkerInfo getLineMarkerInfo(final PsiElement psiElement) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(final List<PsiElement> psiElements,
                                     final Collection<LineMarkerInfo> lineMarkerInfos) {
    if (psiElements.isEmpty()) {
      return;
    }

    for (final PsiElement element : psiElements) {
      annotate(element, lineMarkerInfos);
    }
  }

  private static void annotate(@NotNull final PsiElement element,
                               @NotNull final Collection<LineMarkerInfo> lineMarkerInfos) {
    if (!(element instanceof XmlTag)) {
      return;
    }

    final XmlTag xmlTag = (XmlTag) element;

    if (!Comparing.equal(xmlTag.getNamespace(), StrutsConstants.TAGLIB_STRUTS_UI_URI)) {
      return;
    }

    // any of our tags?
    final String tagName = xmlTag.getLocalName();
    if (Arrays.binarySearch(TAGS_WITH_ACTION_ATTRIBUTE, tagName) < 0) {
      return;
    }


    // short exit when Struts 2 facet not present
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module == null) {
      return;
    }

    if (StrutsFacet.getInstance(module) == null) {
      return;
    }

    // special case for <action>
    final String actionPath = Comparing.equal(tagName, ACTION_ATTRIBUTE_NAME) ?
        xmlTag.getAttributeValue("name") :
        xmlTag.getAttributeValue(ACTION_ATTRIBUTE_NAME);
    if (actionPath == null) {
      return;
    }

    final StrutsModel strutsModel = StrutsManager.getInstance(element.getProject()).getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    final String namespace = xmlTag.getAttributeValue("namespace");
    final List<Action> actions = strutsModel.findActionsByName(actionPath, namespace);
    if (actions.isEmpty()) {
      return;
    }

    // resolve to action method should be exactly 1
    final NavigationGutterIconBuilder<PsiElement> gutterIconBuilder =
        NavigationGutterIconBuilder.create(StrutsIcons.ACTION_CLASS).
            setAlignment(GutterIconRenderer.Alignment.LEFT).
            setTooltipText(StrutsBundle.message("annotators.jsp.goto.action.method")).
            setEmptyPopupText(StrutsBundle.message("annotators.jsp.goto.action.method.notfound")).
            setTargets(new NotNullLazyValue<Collection<? extends PsiElement>>() {
              @NotNull
              protected Collection<PsiMethod> compute() {
                return ContainerUtil.mapNotNull(actions, ACTION_METHOD_FUNCTION);
              }
            });

    lineMarkerInfos.add(gutterIconBuilder.createLineMarkerInfo(xmlTag));
  }

}