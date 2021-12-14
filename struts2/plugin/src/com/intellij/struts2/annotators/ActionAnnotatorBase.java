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
package com.intellij.struts2.annotators;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Base class for annotating Action-Classes.
 * Provides gutter icon/Go To Related File-navigation to &lt;action&gt; declaration(s) in struts.xml,
 * corresponding {@code validation.xml}-files and navigation to result(s) from action methods.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ActionAnnotatorBase extends RelatedItemLineMarkerProvider {
  private static DomElementListCellRenderer<Action> getActionRenderer() {
    return new DomElementListCellRenderer<>(StrutsBundle.message("annotators.action.no.name")) {
      @Override
      @NotNull
      @NonNls
      public String getAdditionalLocation(final Action action) {
        return action != null ? "[" + action.getNamespace() + "] " : "";
      }
    };
  }

  private static final NotNullFunction<PathReference, Collection<? extends PsiElement>> PATH_REFERENCE_CONVERTER =
    pathReference -> {
      final PsiElement resolve = pathReference.resolve();
      return resolve != null ? Collections.singleton(resolve) : Collections.emptyList();
    };

  private static final NotNullFunction<PathReference, Collection<? extends GotoRelatedItem>> PATH_REFERENCE_GOTO_RELATED_ITEM_PROVIDER =
    pathReference -> {
      final PsiElement resolve = pathReference.resolve();
      return resolve != null ? Collections.singleton(new GotoRelatedItem(resolve) {
        @Override
        public Icon getCustomIcon() {
          return pathReference.getIcon();
        }

        @Override
        public String getCustomName() {
          return pathReference.getPath();
        }
      }) : Collections.emptyList();
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
                                          final @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> lineMarkerInfos) {
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
    final Module module = ModuleUtilCore.findModuleForPsiElement(clazz);
    if (module == null ||
        StrutsFacet.getInstance(module) == null) {
      return;
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(element.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    installValidationTargets(element, lineMarkerInfos, clazz);

    final List<Action> actions = strutsModel.findActionsByClass(clazz);
    if (actions.isEmpty()) {
      return;
    }

    installActionTargets(element, lineMarkerInfos, actions);
    installActionMethods(lineMarkerInfos, clazz, actions);
  }

  /**
   * Annotate action class to {@code <action>}-declarations.
   *
   * @param element         Class element to annotate.
   * @param lineMarkerInfos Current line markers.
   * @param actions         Corresponding Actions.
   */
  private static void installActionTargets(final PsiElement element,
                                           final Collection<? super RelatedItemLineMarkerInfo<?>> lineMarkerInfos,
                                           final List<? extends Action> actions) {
    final String tooltip = actions.size() == 1 ? StrutsBundle.message("annotators.action.goto.tooltip.single") :
        StrutsBundle.message("annotators.action.goto.tooltip");
    final NavigationGutterIconBuilder<DomElement> gutterIconBuilder =
        NavigationGutterIconBuilder.create(Struts2Icons.Action, NavigationGutterIconBuilder.DEFAULT_DOM_CONVERTOR,
                                           NavigationGutterIconBuilder.DOM_GOTO_RELATED_ITEM_PROVIDER)
                                   .setAlignment(GutterIconRenderer.Alignment.LEFT)
                                   .setPopupTitle(StrutsBundle.message("annotators.action.goto.declaration"))
                                   .setTargets(actions)
                                   .setTooltipTitle(tooltip)
                                   .setCellRenderer(ActionAnnotatorBase::getActionRenderer);
    lineMarkerInfos.add(gutterIconBuilder.createLineMarkerInfo(element));
  }

  /**
   * Annotate action-methods of this class with result(s).
   *
   * @param lineMarkerInfos Current line markers.
   * @param clazz           Class to annotate.
   * @param actions         Corresponding Actions.
   */
  private static void installActionMethods(final Collection<? super RelatedItemLineMarkerInfo<?>> lineMarkerInfos,
                                           final PsiClass clazz,
                                           final List<? extends Action> actions) {
    final Map<PsiMethod, Set<PathReference>> pathReferenceMap = new HashMap<>();
    for (final Action action : actions) {
      final PsiMethod method = action.searchActionMethod();
      if (method == null || !clazz.equals(method.getContainingClass())) {
        continue;
      }

      final Set<PathReference> pathReferences = new HashSet<>();
      final List<Result> results = action.getResults();
      for (Result result : results) {
        ContainerUtil.addIfNotNull(pathReferences, result.getValue());
      }

      Set<PathReference> toStore = pathReferenceMap.computeIfAbsent(method, __ -> new HashSet<>());
      toStore.addAll(pathReferences);
      pathReferenceMap.put(method, toStore);
    }

    for (final Map.Entry<PsiMethod, Set<PathReference>> entries : pathReferenceMap.entrySet()) {
      final NavigationGutterIconBuilder<PathReference> gutterIconBuilder =
          NavigationGutterIconBuilder.create(AllIcons.Actions.Forward, PATH_REFERENCE_CONVERTER,
                                             PATH_REFERENCE_GOTO_RELATED_ITEM_PROVIDER)
                                     .setAlignment(GutterIconRenderer.Alignment.LEFT)
                                     .setPopupTitle(StrutsBundle.message("annotators.action.goto.result"))
                                     .setTargets(entries.getValue())
                                     .setTooltipTitle(StrutsBundle.message("annotators.action.goto.result.tooltip"));

      PsiMethod method = entries.getKey();
      PsiIdentifier identifier = method.getNameIdentifier();
      if (identifier != null) {
        lineMarkerInfos.add(gutterIconBuilder.createLineMarkerInfo(identifier));
      }
    }
  }

  /**
   * Related {@code validation.xml} files.
   *
   * @param element         Class element to annotate.
   * @param lineMarkerInfos Current line markers.
   * @param clazz           Class to find validation files for.
   */
  private static void installValidationTargets(final PsiElement element,
                                               final Collection<? super RelatedItemLineMarkerInfo<?>> lineMarkerInfos,
                                               final PsiClass clazz) {
    final List<XmlFile> files = ValidatorManager.getInstance(element.getProject()).findValidationFilesFor(clazz);
    if (files.isEmpty()) {
      return;
    }

    final NavigationGutterIconBuilder<PsiElement> validatorBuilder =
        NavigationGutterIconBuilder.create(StrutsIcons.VALIDATION_CONFIG_FILE)
                                   .setAlignment(GutterIconRenderer.Alignment.LEFT)
                                   .setTargets(files)
                                   .setPopupTitle(StrutsBundle.message("annotators.action.goto.validation"))
                                   .setTooltipTitle(StrutsBundle.message("annotators.action.goto.validation.tooltip"));
    lineMarkerInfos.add(validatorBuilder.createLineMarkerInfo(element));
  }

}
