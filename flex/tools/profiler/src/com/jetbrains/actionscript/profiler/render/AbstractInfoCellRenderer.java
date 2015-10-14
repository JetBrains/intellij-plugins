package com.jetbrains.actionscript.profiler.render;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
abstract public class AbstractInfoCellRenderer extends ColoredTreeCellRenderer {
  @Override
  public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    customizeCellRenderer(value, selected);
    setToolTipText(toString());
  }

  protected void appendFrameInfo(@NotNull FrameInfo info, boolean selected) {
    append(info.toSimpleString());
    final SimpleTextAttributes additionalInfoColorAttributes =
      selected ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES;
    if (info.getPackageName() != null && info.getPackageName().length() > 0) {
      append(" (" + info.getPackageName() + ")", additionalInfoColorAttributes);
    }
    if (info.isInnerClass() && info.getFileName() != null) {
      String location = info.getFileName();
      if (info.getFileLine() >= 0) {
        location += ":" + info.getFileLine();
      }
      append(" " + location, additionalInfoColorAttributes);
    }
  }

  protected abstract void customizeCellRenderer(Object value, boolean selected);
}
