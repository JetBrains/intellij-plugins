// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util.icons;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public class EmptyIcon implements Icon {
  private final int myIconWidth;
  private final int myIconHeight;

  public EmptyIcon(int iconWidth, int iconHeight) {
    myIconWidth = iconWidth;
    myIconHeight = iconHeight;
  }

  @Override
  public int getIconHeight() {
    return myIconHeight;
  }

  @Override
  public int getIconWidth() {
    return myIconWidth;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {

  }
}
