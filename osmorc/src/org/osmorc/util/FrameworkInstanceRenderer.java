// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JList;

public class FrameworkInstanceRenderer extends ColoredListCellRenderer<FrameworkInstanceDefinition> {
  private final @Nls String myDefaultText;

  public FrameworkInstanceRenderer() {
    this(null);
  }

  public FrameworkInstanceRenderer(@Nullable @Nls String defaultText) {
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
        if (StringUtil.isEmptyOrSpaces(version)) version = OsmorcBundle.message("framework.version.unknown");
        append(" [" + instance.getFrameworkIntegratorName() + ", " + version + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
      }
      else {
        append(" [" + OsmorcBundle.message("framework.undefined") + "]", SimpleTextAttributes.ERROR_ATTRIBUTES);
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
