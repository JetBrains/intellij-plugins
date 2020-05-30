// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util.icons;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public class IconSizeWrapper implements Icon {
  private final int myWidth, myHeight;
  private final Icon myBaseIcon;

  public IconSizeWrapper(int width, int height, Icon iconToPaint) {
    myWidth = width;
    myHeight = height;
    myBaseIcon = iconToPaint;
  }

  public IconSizeWrapper(Icon templateIcon, Icon iconToPaint) {
    this(templateIcon.getIconWidth(), templateIcon.getIconHeight(), iconToPaint);
  }


  @Override
  public int getIconHeight() {
    return myHeight;
  }

  @Override
  public int getIconWidth() {
    return myWidth;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Shape savedClip = g.getClip();
    g.setClip(x, y, myWidth, myHeight);
    myBaseIcon.paintIcon(c, g, x, y);
    g.setClip(savedClip);
  }
}
