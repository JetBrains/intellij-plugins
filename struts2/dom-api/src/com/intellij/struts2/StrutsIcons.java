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

package com.intellij.struts2;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.PlatformIcons;
import icons.Struts2DomApiIcons;

import javax.swing.*;

/**
 * All icons.
 *
 * @author Yann C&eacute;bron
 */
public final class StrutsIcons {
  /**
   * Icon for struts.xml files.
   */
  public static final LayeredIcon STRUTS_CONFIG_FILE = new LayeredIcon(2);

  /**
   * Icon for validation.xml files.
   */
  public static final LayeredIcon VALIDATION_CONFIG_FILE = new LayeredIcon(2);


  public static final LayeredIcon ACTION_CLASS = new LayeredIcon(2);

  public static final LayeredIcon STRUTS_VARIABLE = new LayeredIcon(2);

  /**
   * Vertical offset for small overlay icons.
   */
  static final int OVERLAY_Y_OFFSET = 6;

  /**
   * Horizontal offset for small overlay icons.
   */
  private static final int OVERLAY_X_OFFSET = 8;

  private StrutsIcons() {
  }

  /**
   * Overlay icon for "default" elements.
   */
  private static final Icon OVERLAY_DEFAULT = PlatformIcons.CHECK_ICON;

  public static final LayeredIcon RESULT_TYPE_DEFAULT = new LayeredIcon(2);

  public static final LayeredIcon GLOBAL_RESULT = new LayeredIcon(2);
  public static final LayeredIcon GLOBAL_EXCEPTION_MAPPING = new LayeredIcon(2);

  public static final LayeredIcon DEFAULT_ACTION_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_CLASS_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_INTERCEPTOR_REF = new LayeredIcon(2);

  // generic reference providers
  public static final Icon THEME = Struts2DomApiIcons.Transform;

  static {
    STRUTS_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    STRUTS_CONFIG_FILE.setIcon(Struts2DomApiIcons.Action_small, 1, 0, OVERLAY_Y_OFFSET);

    VALIDATION_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    VALIDATION_CONFIG_FILE.setIcon(Struts2DomApiIcons.Validation_small, 1, 0, OVERLAY_Y_OFFSET);

    ACTION_CLASS.setIcon(PlatformIcons.CLASS_ICON, 0);
    ACTION_CLASS.setIcon(Struts2DomApiIcons.Action_small, 1, 0, OVERLAY_Y_OFFSET);

    STRUTS_VARIABLE.setIcon(PlatformIcons.VARIABLE_ICON, 0);
    STRUTS_VARIABLE.setIcon(Struts2DomApiIcons.Action_small, 1, 0, OVERLAY_Y_OFFSET);

    createGlobalIcon(GLOBAL_RESULT, Struts2DomApiIcons.Arrow_right_blue);
    createGlobalIcon(GLOBAL_EXCEPTION_MAPPING, PlatformIcons.EXCEPTION_CLASS_ICON);

    createDefaultIcon(DEFAULT_ACTION_REF, Struts2DomApiIcons.Action);
    createDefaultIcon(DEFAULT_CLASS_REF, PlatformIcons.CLASS_ICON);
    createDefaultIcon(DEFAULT_INTERCEPTOR_REF, Struts2DomApiIcons.Funnel);
    createDefaultIcon(RESULT_TYPE_DEFAULT, Struts2DomApiIcons.Presentation);
  }

  private static void createGlobalIcon(final LayeredIcon icon, final Icon baseIcon) {
    icon.setIcon(baseIcon, 0);
    icon.setIcon(AllIcons.General.Web, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);
  }

  private static void createDefaultIcon(final LayeredIcon icon, final Icon baseIcon) {
    icon.setIcon(baseIcon, 0);
    icon.setIcon(OVERLAY_DEFAULT, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);
  }

}
