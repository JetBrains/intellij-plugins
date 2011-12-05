package com.jetbrains.actionscript.profiler.render;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author: Fedor.Korotkov
 */
public class FrameInfoCelleRenderer extends ColoredTreeCellRenderer {
  private Icon openIcon;
  private Icon closedIcon;
  private Icon leafIcon;

  public void setOpenIcon(Icon openIcon) {
    this.openIcon = openIcon;
  }

  public void setClosedIcon(Icon closedIcon) {
    this.closedIcon = closedIcon;
  }

  public void setLeafIcon(Icon leafIcon) {
    this.leafIcon = leafIcon;
  }

  @Override
  public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    setToolTipText(value.toString());
    Icon icon = expanded ? openIcon : getIcon();
    if (icon == null) {
      icon = expanded ? openIcon : closedIcon;
    }
    if (leaf) {
      icon = leafIcon;
    }
    setIcon(icon);
    if (value instanceof DefaultMutableTreeNode) {
      value = ((DefaultMutableTreeNode)value).getUserObject();
    }
    if (!(value instanceof FrameInfoProducer) && !(value instanceof FrameInfo)) {
      if (value != null && value.toString() != null) {
        append(value.toString());
      }
      return;
    }

    FrameInfo frameInfo;
    if(value instanceof FrameInfoProducer){
      frameInfo = ((FrameInfoProducer)value).getFrameInfo();
    }else {
      frameInfo = (FrameInfo)value;
    }
    customizeRenderer(frameInfo);
  }

  protected void customizeRenderer(FrameInfo info) {
    append(info.toSimpleString());
    if (info.isInnerClass() && info.getFileName() != null) {
      String location = info.getFileName();
      if (info.getFileLine() >= 0) {
        location += ":" + info.getFileLine();
      }
      append(" " + location, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
  }
}
