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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.scale.JBUIScale;

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

  public static final LayeredIcon STRUTS_PACKAGE = new LayeredIcon(2);

  /**
   * Vertical offset for small overlay icons.
   */
  static final int OVERLAY_Y_OFFSET = JBUIScale.scale(7);

  /**
   * Horizontal offset for small overlay icons.
   */
  static final int OVERLAY_X_OFFSET = JBUIScale.scale(8);

  private StrutsIcons() {
  }

  /**
   * Overlay icon for "default" elements.
   */
  private static final Icon OVERLAY_DEFAULT = AllIcons.Actions.Checked;

  public static final LayeredIcon RESULT_TYPE_DEFAULT = new LayeredIcon(2);

  public static final LayeredIcon GLOBAL_RESULT = new LayeredIcon(2);
  public static final LayeredIcon GLOBAL_EXCEPTION_MAPPING = new LayeredIcon(2);

  public static final LayeredIcon DEFAULT_ACTION_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_CLASS_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_INTERCEPTOR_REF = new LayeredIcon(2);

  // generic reference providers
  public static final Icon THEME = AllIcons.Gutter.Colors;

  static {
    STRUTS_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    STRUTS_CONFIG_FILE.setIcon(Struts2Icons.Action_small, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);

    VALIDATION_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    VALIDATION_CONFIG_FILE.setIcon(Struts2Icons.Edit_small, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);

    ACTION_CLASS.setIcon(AllIcons.Nodes.Class, 0);
    ACTION_CLASS.setIcon(Struts2Icons.Action_small, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);

    STRUTS_VARIABLE.setIcon(AllIcons.Nodes.Variable, 0);
    STRUTS_VARIABLE.setIcon(Struts2Icons.Action_small, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);

    STRUTS_PACKAGE.setIcon(AllIcons.Nodes.Folder, 0);
    STRUTS_PACKAGE.setIcon(Struts2Icons.Action_small, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);

    createGlobalIcon(GLOBAL_RESULT, AllIcons.Vcs.Arrow_right);
    createGlobalIcon(GLOBAL_EXCEPTION_MAPPING, AllIcons.Nodes.ExceptionClass);

    createDefaultIcon(DEFAULT_ACTION_REF, Struts2Icons.Action);
    createDefaultIcon(DEFAULT_CLASS_REF, AllIcons.Nodes.Class);
    createDefaultIcon(DEFAULT_INTERCEPTOR_REF, AllIcons.Nodes.Plugin);
    createDefaultIcon(RESULT_TYPE_DEFAULT, AllIcons.Debugger.Console);
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
