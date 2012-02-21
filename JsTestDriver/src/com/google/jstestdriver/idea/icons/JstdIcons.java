package com.google.jstestdriver.idea.icons;

import com.google.jstestdriver.idea.server.ui.ToolPanel;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class JstdIcons {

  private JstdIcons() {}

  public static final Icon JSTD_SMALL_ICON = IconLoader.findIcon("JsTestDriver.png");

  public static Icon getIcon(@NotNull String resourceName) {
    try {
      BufferedImage image = loadImage(resourceName);
      return new ImageIcon(image);
    } catch (IOException e) {
      throw new RuntimeException("Can't load '" + resourceName + "' icon!", e);
    }
  }

  public static BufferedImage loadImage(@NotNull String resourceName) throws IOException {
    return ImageIO.read(JstdIcons.class.getResourceAsStream(resourceName));
  }
}
