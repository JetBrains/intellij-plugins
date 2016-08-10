/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
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
package org.osmorc.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;

public class OsgiUiUtil {
  public static class FrameworkInstanceRenderer extends ColoredListCellRenderer<FrameworkInstanceDefinition> {
    private final String myDefaultText;

    public FrameworkInstanceRenderer() {
      this(null);
    }

    public FrameworkInstanceRenderer(@Nullable String defaultText) {
      myDefaultText = defaultText;
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends FrameworkInstanceDefinition> list,
                                         FrameworkInstanceDefinition instance,
                                         int index,
                                         boolean selected,
                                         boolean hasFocus) {
      if (instance != null) {
        append(instance.getName());
        if (isInstanceDefined(instance)) {
          String version = instance.getVersion();
          if (StringUtil.isEmptyOrSpaces(version)) version = "unknown";
          append(" [" + instance.getFrameworkIntegratorName() + ", " + version + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else {
          append(" [invalid]", SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
      }
      else if (myDefaultText != null) {
        append(myDefaultText, SimpleTextAttributes.GRAYED_ATTRIBUTES);
      }
    }

    protected boolean isInstanceDefined(@NotNull FrameworkInstanceDefinition instance) {
      return true;
    }
  }
}