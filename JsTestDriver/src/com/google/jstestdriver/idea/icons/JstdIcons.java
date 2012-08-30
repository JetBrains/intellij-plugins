package com.google.jstestdriver.idea.icons;

import icons.JsTestDriverIcons;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sergey Simonchik
 */
public class JstdIcons {

  private JstdIcons() {}

  public static final Icon JSTD_SMALL_ICON = JsTestDriverIcons.JsTestDriver;
  public static final Icon TEST_FILE_ICON = JsTestDriverIcons.JsTestFile;

  @NotNull
  public static ImageIcon getIcon(@NotNull String resourceName) {
    try {
      BufferedImage image = loadImage(resourceName);
      return new ImageIcon(image);
    } catch (IOException e) {
      throw new RuntimeException("Can't load '" + resourceName + "' icon!", e);
    }
  }

  @NotNull
  private static BufferedImage loadImage(@NotNull String resourceName) throws IOException {
    InputStream inputStream = JstdIcons.class.getResourceAsStream(resourceName);
    try {
      if (inputStream == null) {
        throw new RuntimeException("Can't find resource by name '" + resourceName + "'");
      }
      return ImageIO.read(inputStream);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }
}
