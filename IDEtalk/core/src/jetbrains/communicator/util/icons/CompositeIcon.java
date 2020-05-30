// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util.icons;

import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class CompositeIcon implements Icon {
  private final int myGap;
  private final List<Icon> myIcons = new ArrayList<>();

  public CompositeIcon() {
    this (1);
  }

  public CompositeIcon(int gap) {
    myGap = gap;
  }

  public CompositeIcon(Icon icon1, Icon icon2) {
    this (1, icon1, icon2);
  }

  public CompositeIcon(int gap, Icon icon1, Icon icon2) {
    myGap = gap;
    myIcons.add(icon1);
    myIcons.add(icon2);
  }


  public void addIcon(Icon icon) {
    myIcons.add(icon);
  }

  public void removeIcon(Icon icon) {
    myIcons.remove(icon);
  }

  @Override
  public int getIconHeight() {
    int max = 0;
    for (Icon icon : myIcons) {
      max = Math.max(max, icon.getIconHeight());
    }
    return max;
  }

  @Override
  public int getIconWidth() {
    int width = 0;
    for (Icon icon : myIcons) {
      width += icon.getIconWidth();
    }
    if (width > 0) {
      width += myGap * (myIcons.size() - 1);
    }
    return width;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    int height = getIconHeight();

    int currX = x;
    for (Icon icon : myIcons) {
      int iconY = y + (height - icon.getIconHeight()) / 2;
      icon.paintIcon(c, g, currX, iconY);
      currX += icon.getIconWidth() + myGap;
    }
  }

  public static void main(String[] args) {
    ImageIcon icon2 = new ImageIcon(CompositeIcon.class.getResource("/stacktrace.png"));
    ImageIcon icon1 = new ImageIcon(CompositeIcon.class.getResource("/message.png"));
    CompositeIcon compositeIcon = new CompositeIcon(icon1, icon2);

    UIUtil.run(new JLabel(compositeIcon));
  }
}
