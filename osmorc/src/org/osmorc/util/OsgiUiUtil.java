package org.osmorc.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;

public class OsgiUiUtil {
  public static class FrameworkInstanceRenderer extends ColoredListCellRendererWrapper<FrameworkInstanceDefinition> {
    private final String myDefaultText;

    public FrameworkInstanceRenderer() {
      this(null);
    }

    public FrameworkInstanceRenderer(@Nullable String defaultText) {
      myDefaultText = defaultText;
    }

    @Override
    protected void doCustomize(JList list, FrameworkInstanceDefinition instance, int index, boolean selected, boolean hasFocus) {
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
