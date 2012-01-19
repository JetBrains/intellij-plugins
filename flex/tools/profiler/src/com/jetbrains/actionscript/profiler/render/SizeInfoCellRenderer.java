package com.jetbrains.actionscript.profiler.render;

import com.intellij.ui.SimpleTextAttributes;
import com.jetbrains.actionscript.profiler.livetable.SizeInfoNode;

/**
 * @author: Fedor.Korotkov
 */
public class SizeInfoCellRenderer extends AbstractInfoCellRenderer {
  @Override
  protected void customizeCellRenderer(Object value) {
    if (!(value instanceof SizeInfoNode)) {
      if (value != null && value.toString() != null) {
        append(value.toString());
      }
      return;
    }

    final SizeInfoNode sizeInfoNode = (SizeInfoNode)value;
    if (sizeInfoNode.isMethod()) {
      appendFrameInfo(sizeInfoNode.getFrameInfo());
      return;
    }

    appendQName(sizeInfoNode);
  }

  private void appendQName(SizeInfoNode sizeInfoNode) {
    append(sizeInfoNode.getClassName());
    if (sizeInfoNode.getPackageName() != null) {
      append(" (" + sizeInfoNode.getPackageName() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
  }
}
