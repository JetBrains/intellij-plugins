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

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.*;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Base class for annotating Action-Classes.
 * Provides gutter icon/Go To Related File-navigation to &lt;action&gt; declaration(s) in struts.xml and
 * navigation to result(s) from action methods.
 *
 * @author Yann C&eacute;bron
 */
abstract class ActionAnnotatorBase extends RelatedItemLineMarkerProvider {

  private static DomElementListCellRenderer ACTION_RENDERER;

  private static final NotNullFunction<PathReference, Collection<? extends PsiElement>> PATH_REFERENCE_CONVERTER =
      new NotNullFunction<PathReference, Collection<? extends PsiElement>>() {
        @NotNull
        @Override
        public Collection<? extends PsiElement> fun(final PathReference pathReference) {
          final PsiElement resolve = pathReference.resolve();
          return resolve != null ? Collections.singleton(resolve) : Collections.<PsiElement>emptyList();
        }
      };

  private static final NotNullFunction<PathReference, Collection<? extends GotoRelatedItem>> PATH_REFERENCE_GOTO_RELATED_ITEM_PROVIDER =
      new NotNullFunction<PathReference, Collection<? extends GotoRelatedItem>>() {
        @NotNull
        @Override
        public Collection<? extends GotoRelatedItem> fun(final PathReference pathReference) {
          final PsiElement resolve = pathReference.resolve();
          return resolve != null ? Collections.singleton(new GotoRelatedItem(resolve)) : Collections.<GotoRelatedItem>emptyList();
        }
      };

  /**
   * Determine the Action-PsiClass.
   *
   * @param psiElement Passed from annotator.
   * @return null if PsiClass cannot be determined or is not suitable.
   */
  @Nullable
  protected abstract PsiClass getActionPsiClass(@NotNull final PsiElement psiElement);

  @Override
  protected void collectNavigationMarkers(final @NotNull PsiElement element,
                                          final Collection<? super RelatedItemLineMarkerInfo> lineMarkerInfos) {
    if (!(element instanceof PsiIdentifier)) return;
    final PsiClass clazz = getActionPsiClass(element.getParent());
    if (clazz == null || clazz.getNameIdentifier() != element) {
      return;
    }

    // do not run on non-public, abstract classes or interfaces
    if (clazz.isInterface() ||
        clazz.isAnnotationType() ||
        !clazz.hasModifierProperty(PsiModifier.PUBLIC) ||
        clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return;
    }

    // short exit if Struts Facet not present
    final Module module = ModuleUtil.findModuleForPsiElement(clazz);
    if (module == null ||
        StrutsFacet.getInstance(module) == null) {
      return;
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(element.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    final List<Action> actions = strutsModel.findActionsByClass(clazz);
    if (!actions.isEmpty()) {
      final NavigationGutterIconBuilder<DomElement> gutterIconBuilder =
          NavigationGutterIconBuilder.create(StrutsIcons.ACTION, NavigationGutterIconBuilder.DEFAULT_DOM_CONVERTOR,
                                             NavigationGutterIconBuilder.DOM_GOTO_RELATED_ITEM_PROVIDER)
                                     .setPopupTitle(StrutsBundle.message("annotators.action.goto.declaration"))
                                     .setTargets(actions)
                                     .setTooltipTitle(StrutsBundle.message("annotators.action.goto.tooltip"))
                                     .setCellRenderer(getActionRenderer());

      lineMarkerInfos.add(gutterIconBuilder.createLineMarkerInfo(element));
    }


    // annotate action-methods of *this* class with result(s)
    final Map<PsiMethod, Set<PathReference>> pathReferenceMap = new HashMap<PsiMethod, Set<PathReference>>();
    for (final Action action : actions) {
      final PsiMethod method = action.searchActionMethod();
      if (method == null || !clazz.equals(method.getContainingClass())) {
        continue;
      }

      final Set<PathReference> pathReferences = new HashSet<PathReference>();
      final List<Result> results = action.getResults();
      for (final Result result : results) {
        final PathReference pathReference = result.getValue();
        ContainerUtil.addIfNotNull(pathReferences, pathReference);
      }

      final Set<PathReference> toStore = ContainerUtil.getOrCreate(pathReferenceMap,
                                                                   method,
                                                                   new HashSet<PathReference>());
      toStore.addAll(pathReferences);
      pathReferenceMap.put(method, toStore);
    }

    for (final Map.Entry<PsiMethod, Set<PathReference>> entries : pathReferenceMap.entrySet()) {
      final NavigationGutterIconBuilder<PathReference> gutterIconBuilder =
          NavigationGutterIconBuilder.create(StrutsIcons.RESULT, PATH_REFERENCE_CONVERTER,
                                             PATH_REFERENCE_GOTO_RELATED_ITEM_PROVIDER)
                                     .setPopupTitle(StrutsBundle.message("annotators.action.goto.result"))
                                     .setTargets(entries.getValue())
                                     .setTooltipTitle(StrutsBundle.message("annotators.action.goto.result.tooltip"));

      lineMarkerInfos.add(gutterIconBuilder.createLineMarkerInfo(entries.getKey()));
    }

  }

  private static synchronized PsiElementListCellRenderer getActionRenderer() {
    if (ACTION_RENDERER == null) {
      ACTION_RENDERER = new DomElementListCellRenderer<Action>(StrutsBundle.message("annotators.action.noname")) {
        @NotNull
        @NonNls
        public String getAdditionalLocation(final Action action) {
          return action != null ? "[" + action.getNamespace() + "] " : "";
        }
      };
    }
    return ACTION_RENDERER;
  }

}