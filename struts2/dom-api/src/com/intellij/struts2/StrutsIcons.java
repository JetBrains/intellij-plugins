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

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * All icons.
 *
 * @author Yann C&eacute;bron
 */
public final class StrutsIcons {

  @NonNls
  private static final String ICON_BASE_PATH = "/resources/icons/";

  // "static" icons (DOM)

  public static final String ACTION_PATH = ICON_BASE_PATH + "action.png";
  public static final String ACTION_SMALL_PATH = ICON_BASE_PATH + "action_small.png";

  public static final String INCLUDE_PATH = ICON_BASE_PATH + "import1.png";

  public static final String INTERCEPTOR_PATH = ICON_BASE_PATH + "funnel.png";
  public static final String INTERCEPTOR_STACK_PATH = ICON_BASE_PATH + "funnel_up.png";

  public static final String PACKAGE_PATH = ICON_BASE_PATH + "folder_gear.png";
  public static final String PARAM_PATH = ICON_BASE_PATH + "preferences.png";

  public static final String RESULT_PATH = ICON_BASE_PATH + "arrow_right_blue.png";

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
   * Loads the icon with the given path.
   *
   * @param iconPath Relative path of the icon to load.
   * @return Icon.
   */
  private static Icon loadIcon(@NonNls final String iconPath) {
    return IconLoader.getIcon(iconPath);
  }

  /**
   * Overlay icon for "global" elements.
   */
  private static final Icon OVERLAY_GLOBAL = IconLoader.getIcon("/general/web.png");

  /**
   * Overlay icon for "default" elements.
   */
  private static final Icon OVERLAY_DEFAULT = IconLoader.getIcon("/gutter/check.png");

  // struts.xml
  public static final Icon ACTION = loadIcon(ACTION_PATH);

  public static final Icon ACTION_SMALL = loadIcon(ACTION_SMALL_PATH);
  public static final Icon EXCEPTION_MAPPING = Icons.EXCEPTION_CLASS_ICON;
  public static final Icon INTERCEPTOR = loadIcon(INTERCEPTOR_PATH);
  public static final Icon INTERCEPTOR_STACK = loadIcon(INTERCEPTOR_STACK_PATH);
  public static final Icon PACKAGE = loadIcon(PACKAGE_PATH);
  public static final Icon PARAM = loadIcon(PARAM_PATH);
  public static final Icon RESULT = loadIcon(RESULT_PATH);

  public static final LayeredIcon GLOBAL_RESULT = new LayeredIcon(2);
  public static final LayeredIcon GLOBAL_EXCEPTION_MAPPING = new LayeredIcon(2);

  public static final LayeredIcon DEFAULT_ACTION_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_CLASS_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_INTERCEPTOR_REF = new LayeredIcon(2);

  // validation.xml
  private static final Icon VALIDATOR_SMALL = loadIcon(ICON_BASE_PATH + "validation_small.png");

  // generic reference providers
  public static final Icon THEME = loadIcon(ICON_BASE_PATH + "transform.png");

  static {
    STRUTS_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    STRUTS_CONFIG_FILE.setIcon(ACTION_SMALL, 1, 0, OVERLAY_Y_OFFSET);

    VALIDATION_CONFIG_FILE.setIcon(StdFileTypes.XML.getIcon(), 0);
    VALIDATION_CONFIG_FILE.setIcon(VALIDATOR_SMALL, 1, 0, OVERLAY_Y_OFFSET);

    ACTION_CLASS.setIcon(Icons.CLASS_ICON, 0);
    ACTION_CLASS.setIcon(StrutsIcons.ACTION_SMALL, 1, 0, OVERLAY_Y_OFFSET);

    STRUTS_VARIABLE.setIcon(Icons.VARIABLE_ICON, 0);
    STRUTS_VARIABLE.setIcon(StrutsIcons.ACTION_SMALL, 1, 0, OVERLAY_Y_OFFSET);

    createGlobalIcon(GLOBAL_RESULT, RESULT);
    createGlobalIcon(GLOBAL_EXCEPTION_MAPPING, EXCEPTION_MAPPING);

    createDefaultIcon(DEFAULT_ACTION_REF, ACTION);
    createDefaultIcon(DEFAULT_CLASS_REF, Icons.CLASS_ICON);
    createDefaultIcon(DEFAULT_INTERCEPTOR_REF, INTERCEPTOR);
  }

  private static void createGlobalIcon(final LayeredIcon icon, final Icon baseIcon) {
    icon.setIcon(baseIcon, 0);
    icon.setIcon(OVERLAY_GLOBAL, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);
  }

  private static void createDefaultIcon(final LayeredIcon icon, final Icon baseIcon) {
    icon.setIcon(baseIcon, 0);
    icon.setIcon(OVERLAY_DEFAULT, 1, OVERLAY_X_OFFSET, OVERLAY_Y_OFFSET);
  }

}