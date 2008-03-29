/*
 * Copyright 2007 The authors
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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiModifier;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Iterator;

/**
 * Annotator for Action-classes.
 * Provides gutter icon navigation to &lt;action&gt; declaration(s).
 *
 * @author Yann CŽbron
 */
public class ActionAnnotator implements Annotator {

  private static final DomElementListCellRenderer ACTION_RENDERER =
      new DomElementListCellRenderer<Action>("No name") {
        @NotNull
        @NonNls
        public String getAdditionalLocation(final Action action) {
          return action != null ? "[" + action.getNamespace() + "] " : "";
        }
      };

  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (!(psiElement instanceof PsiIdentifier)) {
      return;
    }

    final PsiElement parentPsiElement = psiElement.getParent();
    if (!(parentPsiElement instanceof PsiClass)) {
      return;
    }

    // do not run on classes within JSPs
    if (psiElement.getContainingFile().getFileType() != StdFileTypes.JAVA) {
      return;
    }

    final PsiClass clazz = (PsiClass) parentPsiElement;

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

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    final List<Action> actions = strutsModel.findActionsByClass(clazz);
    if (!actions.isEmpty()) {
      NavigationGutterIconBuilder.create(StrutsIcons.ACTION, NavigationGutterIconBuilder.DEFAULT_DOM_CONVERTOR).
          setPopupTitle("Go To Action declaration").
          setTargets(actions).setTooltipTitle("<b>Action path(s):</b>").
          setCellRenderer(ACTION_RENDERER).
          install(holder, clazz.getNameIdentifier());
    }

  }

}
