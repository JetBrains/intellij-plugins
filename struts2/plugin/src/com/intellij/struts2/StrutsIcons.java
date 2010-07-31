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

  /**
   * Icon for struts.xml files in tree/structure view.
   */
  public static final LayeredIcon STRUTS_CONFIG_FILE_ICON = new LayeredIcon(2);

  /**
   * Icon for validation.xml files in tree/structure view.
   */
  public static final LayeredIcon VALIDATION_CONFIG_FILE_ICON = new LayeredIcon(2);

  private StrutsIcons() {
  }

  @NonNls
  private static final String ICON_BASE_PATH = "/resources/icons/";

  /**
   * Loads the icon with the given name from the default icon base path.
   *
   * @param iconName Relative file name of the icon to load.
   * @return Icon.
   */
  private static Icon getIcon(@NonNls final String iconName) {
    return IconLoader.getIcon(ICON_BASE_PATH + iconName);
  }

  /**
   * Vertical offset for small overlay icons.
   */
  public static final int SMALL_ICON_Y_OFFSET = 6;

  /**
   * Overlay icon for "global" elements.
   */
  private static final Icon OVERLAY_GLOBAL = IconLoader.getIcon("/general/web.png");
  private static final int OVERLAY_GLOBAL_Y_OFFSET = 8;
  private static final int OVERLAY_GLOBAL_X_OFFSET = 8;

  /**
   * Overlay icon for "default" elements.
   */
  private static final Icon OVERLAY_DEFAULT = IconLoader.getIcon("/gutter/check.png");
  private static final int OVERLAY_DEFAULT_Y_OFFSET = 6;
  private static final int OVERLAY_DEFAULT_X_OFFSET = 8;

  // struts.xml
  public static final Icon ACTION = getIcon("action.png");
  public static final Icon ACTION_SMALL = getIcon("action_small.png");
  public static final Icon EXCEPTION_MAPPING = Icons.EXCEPTION_CLASS_ICON;
  public static final Icon INTERCEPTOR = getIcon("funnel.png");
  public static final Icon INTERCEPTOR_STACK = getIcon("funnel_up.png");
  public static final Icon PACKAGE = getIcon("folder_gear.png");
  public static final Icon PARAM = getIcon("preferences.png");
  public static final Icon RESULT = getIcon("arrow_right_blue.png");

  public static final LayeredIcon GLOBAL_RESULT = new LayeredIcon(2);
  public static final LayeredIcon GLOBAL_EXCEPTION_MAPPING = new LayeredIcon(2);

  public static final LayeredIcon DEFAULT_ACTION_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_CLASS_REF = new LayeredIcon(2);
  public static final LayeredIcon DEFAULT_INTERCEPTOR_REF = new LayeredIcon(2);

  // validation.xml
  public static final Icon VALIDATOR_SMALL = getIcon("validation_small.png");

  // generic reference providers
  public static final Icon THEME = getIcon("transform.png");

  static {
    STRUTS_CONFIG_FILE_ICON.setIcon(StdFileTypes.XML.getIcon(), 0);
    STRUTS_CONFIG_FILE_ICON.setIcon(ACTION_SMALL, 1, 0, SMALL_ICON_Y_OFFSET);

    VALIDATION_CONFIG_FILE_ICON.setIcon(StdFileTypes.XML.getIcon(), 0);
    VALIDATION_CONFIG_FILE_ICON.setIcon(VALIDATOR_SMALL, 1, 0, SMALL_ICON_Y_OFFSET);

    GLOBAL_RESULT.setIcon(RESULT, 0);
    GLOBAL_RESULT.setIcon(OVERLAY_GLOBAL, 1, OVERLAY_GLOBAL_X_OFFSET, OVERLAY_GLOBAL_Y_OFFSET);

    GLOBAL_EXCEPTION_MAPPING.setIcon(EXCEPTION_MAPPING, 0);
    GLOBAL_EXCEPTION_MAPPING.setIcon(OVERLAY_GLOBAL, 1, OVERLAY_GLOBAL_X_OFFSET, OVERLAY_GLOBAL_Y_OFFSET);

    DEFAULT_ACTION_REF.setIcon(ACTION, 0);
    DEFAULT_ACTION_REF.setIcon(OVERLAY_DEFAULT, 1, OVERLAY_DEFAULT_X_OFFSET, OVERLAY_DEFAULT_Y_OFFSET);

    DEFAULT_CLASS_REF.setIcon(Icons.CLASS_ICON, 0);
    DEFAULT_CLASS_REF.setIcon(OVERLAY_DEFAULT, 1, OVERLAY_DEFAULT_X_OFFSET, OVERLAY_DEFAULT_Y_OFFSET);

    DEFAULT_INTERCEPTOR_REF.setIcon(INTERCEPTOR, 0);
    DEFAULT_INTERCEPTOR_REF.setIcon(OVERLAY_DEFAULT, 1, OVERLAY_DEFAULT_X_OFFSET, OVERLAY_DEFAULT_Y_OFFSET);
  }

}