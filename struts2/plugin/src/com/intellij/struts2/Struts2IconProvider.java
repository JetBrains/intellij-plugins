/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.struts2;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.xml.DomManager;
import com.intellij.ide.IconProvider;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author peter
 */
public class Struts2IconProvider extends IconProvider {
  // IconProvider -------------------------------------------------------------
  // original code posted by Sascha Weinreuter

  private boolean active;
  private static final Key<TIntObjectHashMap<Icon>> ICON_KEY = Key.create("STRUTS2_OVERLAY_ICON");

  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {

    // for getting the original icon from IDEA
    if (active) {
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

    active = true;

    try {
      TIntObjectHashMap<Icon> icons = element.getUserData(ICON_KEY);
      if (icons != null) {
        final Icon icon = icons.get(flags);
        if (icon != null) {
          return icon;
        }
      }

      Icon strutsIcon = null;
      LayeredIcon icon = null;

      // handle XML files
      if (element instanceof XmlFile) {
        final XmlFile xmlFile = (XmlFile) element;
        final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());

        if (domManager.getFileElement(xmlFile, StrutsRoot.class) != null) {
          strutsIcon = StrutsIcons.ACTION_SMALL;
        } else if (domManager.getFileElement(xmlFile, Validators.class) != null) {
          strutsIcon = StrutsIcons.VALIDATOR_SMALL;
        } else if (domManager.getFileElement(xmlFile, ValidatorsConfig.class) != null) {
          strutsIcon = StrutsIcons.VALIDATOR_SMALL;
        }
      }
      // handle JAVA classes
      else {
        final PsiClass psiClass = (PsiClass) element;
        final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        final StrutsModel strutsModel = StrutsManager.getInstance(psiClass.getProject()).getCombinedModel(module);
        if (strutsModel != null &&
            !strutsModel.findActionsByClass(psiClass).isEmpty()) {
          strutsIcon = StrutsIcons.ACTION_SMALL;
        }
      }

      // match? build new layered icon
      if (strutsIcon != null) {
        icon = new LayeredIcon(2);
        final Icon original = element.getIcon(flags & ~Iconable.ICON_FLAG_VISIBILITY);
        icon.setIcon(original, 0);
        icon.setIcon(strutsIcon, 1, 0, StrutsIcons.SMALL_ICON_Y_OFFSET);
      }

      // cache built icon
      if (icon != null) {
        if (icons == null) {
          element.putUserData(ICON_KEY, icons = new TIntObjectHashMap<Icon>(3));
        }
        icons.put(flags, icon);
      }

      return icon;
    } finally {
      active = false;
    }

  }
}
