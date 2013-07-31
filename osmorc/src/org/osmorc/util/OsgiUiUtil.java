package org.osmorc.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;

public class OsgiUiUtil {
  public static class FrameworkInstanceRenderer extends ColoredListCellRendererWrapper<FrameworkInstanceDefinition> {
    @Override
    protected void doCustomize(JList list, FrameworkInstanceDefinition instance, int index, boolean selected, boolean hasFocus) {
      append(instance.getName());
      String version = instance.getVersion();
      if (StringUtil.isEmptyOrSpaces(version)) version = "(unknown)";
      append(" [" + instance.getFrameworkIntegratorName() + ", " + version + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
  }
}
