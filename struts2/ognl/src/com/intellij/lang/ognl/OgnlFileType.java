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

package com.intellij.lang.ognl;

import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.ui.JBUI;
import icons.OgnlIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlFileType extends LanguageFileType {

  public static final OgnlFileType INSTANCE = new OgnlFileType();

  private static final NotNullLazyValue<LayeredIcon> ICON = new AtomicNotNullLazyValue<LayeredIcon>() {
    @NotNull
    @Override
    protected LayeredIcon compute() {
      final LayeredIcon icon = new LayeredIcon(2);
      icon.setIcon(FileTypes.PLAIN_TEXT.getIcon(), 0);
      icon.setIcon(OgnlIcons.Action_small, 1, 0, 6);
      return JBUI.scale(icon);
    }
  };

  private OgnlFileType() {
    super(OgnlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "OGNL";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Object Graph Navigation Language";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "ognl";
  }

  @Override
  public Icon getIcon() {
    return ICON.getValue();
  }

}
