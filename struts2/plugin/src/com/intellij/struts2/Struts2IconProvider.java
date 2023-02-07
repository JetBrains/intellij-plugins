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

package com.intellij.struts2;

import com.intellij.ide.IconProvider;
import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.scale.JBUIScale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides icons for Struts-related code. <br/>Original code posted by Sascha Weinreuter.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2IconProvider extends IconProvider {

  @Override
  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {

    if (!(element instanceof PsiClass psiClass)) {
      return null;
    }

    // IconProvider queries non-physical PSI as well (e.g. completion items); check validity
    if (!element.isPhysical() ||
        !element.isValid()) {
      return null;
    }

    // no icons when no facet present
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) {
      return null;
    }

    final StrutsFacet strutsFacet = StrutsFacet.getInstance(module);
    if (strutsFacet == null) {
      return null;
    }

    // handle suitable JAVA classes --> overlay icon
    if (psiClass.isInterface() ||
        psiClass.isEnum() ||
        !psiClass.hasModifierProperty(PsiModifier.PUBLIC) ||
        psiClass.hasModifierProperty(PsiModifier.ABSTRACT) ||
        !JamCommonUtil.isPlainJavaFile(psiClass.getContainingFile())) {
      return null;
    }

    final StrutsModel strutsModel = StrutsManager.getInstance(psiClass.getProject()).getCombinedModel(module);
    if (strutsModel == null ||
        !strutsModel.isActionClass(psiClass)) {
      return null;
    }

    final LayeredIcon layeredIcon = new LayeredIcon(2);
    final Icon original = PsiClassImplUtil.getClassIcon(flags, psiClass);
    layeredIcon.setIcon(original, 0);
    layeredIcon.setIcon(Struts2Icons.Action_small, 1, StrutsIcons.OVERLAY_X_OFFSET, StrutsIcons.OVERLAY_Y_OFFSET);
    return JBUIScale.scaleIcon(layeredIcon);
  }
}
