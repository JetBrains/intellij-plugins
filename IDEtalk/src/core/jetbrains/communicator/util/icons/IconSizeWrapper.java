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


  public int getIconHeight() {
    return myHeight;
  }

  public int getIconWidth() {
    return myWidth;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Shape savedClip = g.getClip();
    g.setClip(x, y, myWidth, myHeight);
    myBaseIcon.paintIcon(c, g, x, y);
    g.setClip(savedClip);
  }
}
