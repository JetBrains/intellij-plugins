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
package com.intellij.struts2;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.Interceptor;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRef;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorStack;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomIconProvider;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides icons for Struts-related files. <br/>Original code posted by Sascha Weinreuter.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2IconProvider extends DomIconProvider implements DumbAware {

  private boolean computingLayeredIcon;

  public Icon getIcon(@NotNull final DomElement element, final int flags) {
    if (element instanceof InterceptorRef) {
      final InterceptorOrStackBase interceptorOrStackBase = ((InterceptorRef) element).getName().getValue();
      if (interceptorOrStackBase instanceof Interceptor) {
        return StrutsIcons.INTERCEPTOR;
      }
      if (interceptorOrStackBase instanceof InterceptorStack) {
        return StrutsIcons.INTERCEPTOR_STACK;
      }
    }
    return null;
  }

  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {

    // for getting original icon from IDEA
    if (computingLayeredIcon) {
      return null;
    }

    if (element instanceof JspFile) {
      return null;
    }
    if (!(element instanceof PsiClass || element instanceof XmlFile)) {
      return null;
    }

    // IconProvider queries non-physical PSI as well (e.g. completion items); check validity
    if (!element.isPhysical() ||
        !element.isValid()) {
      return null;
    }

    // no icons when no facet present
    final StrutsFacet strutsFacet = StrutsFacet.getInstance(element);
    if (strutsFacet == null) {
      return null;
    }

    // handle XML files --> fixed icons
    if (element instanceof XmlFile) {
      final XmlFile xmlFile = (XmlFile) element;
      final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());

      if (domManager.getFileElement(xmlFile, StrutsRoot.class) != null) {
        return StrutsIcons.STRUTS_CONFIG_FILE_ICON;
      }

      if (domManager.getFileElement(xmlFile, Validators.class) != null) {
        return StrutsIcons.VALIDATION_CONFIG_FILE_ICON;
      }

      if (domManager.getFileElement(xmlFile, ValidatorsConfig.class) != null) {
        return StrutsIcons.VALIDATION_CONFIG_FILE_ICON;
      }

      return null;
    }

    // handle JAVA classes --> overlay icon
    try {
      computingLayeredIcon = true;

      final PsiClass psiClass = (PsiClass) element;
      final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
      final StrutsModel strutsModel = StrutsManager.getInstance(psiClass.getProject()).getCombinedModel(module);
      if (strutsModel == null ||
          strutsModel.findActionsByClass(psiClass).isEmpty()) {
        return null;
      }

      final LayeredIcon layeredIcon = new LayeredIcon(2);
      final Icon original = element.getIcon(flags & ~Iconable.ICON_FLAG_VISIBILITY);
      layeredIcon.setIcon(original, 0);
      layeredIcon.setIcon(StrutsIcons.ACTION_SMALL, 1, 0, StrutsIcons.OVERLAY_Y_OFFSET);

      return layeredIcon;
    } finally {
      computingLayeredIcon = false;
    }

  }

}