/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package jetbrains.communicator.util.icons;

import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kir
 */
public class CompositeIcon implements Icon {
  private final int myGap;
  private final List<Icon> myIcons = new ArrayList<Icon>();

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

  public int getIconHeight() {
    int max = 0;
    for (Iterator<Icon> it = myIcons.iterator(); it.hasNext();) {
      Icon icon = it.next();
      max = Math.max(max, icon.getIconHeight());
    }
    return max;
  }

  public int getIconWidth() {
    int width = 0;
    for (int i = 0; i < myIcons.size(); i++) {
      Icon icon = myIcons.get(i);
      width += icon.getIconWidth();
    }
    if (width > 0) {
      width += myGap * (myIcons.size() - 1);
    }
    return width;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    int height = getIconHeight();

    int currX = x;
    for (int i = 0; i < myIcons.size(); i++) {
      Icon icon = myIcons.get(i);
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
