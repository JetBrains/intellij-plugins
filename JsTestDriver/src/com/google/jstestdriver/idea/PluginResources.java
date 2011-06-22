/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.jstestdriver.browser.BrowserCaptureEvent.Event;
import com.google.jstestdriver.idea.ui.ToolPanel;

import static com.intellij.openapi.util.IconLoader.findIcon;

/**
 * Access to all the text and image resources for the plugin.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class PluginResources {

  private static final Icon JSTD_SMALL_ICON = findIcon("JsTestDriver.png", ToolPanel.class);

  private PluginResources() {}

  public static String getPluginName() {
    return MessageBundle.message("plugin.name");
  }

  public static Icon getJstdSmallIcon() {
    return JSTD_SMALL_ICON;
  }

  public static Icon getServerStartIcon() {
    return findIcon("startServer.png", ToolPanel.class);
  }

  public static Icon getServerStopIcon() {
    return findIcon("stopServer.png", ToolPanel.class);
  }
  public static String getCaptureUrlMessage() {
    return MessageBundle.message("captureLabel");
  }

  public static class BrowserIcon {
    private final BufferedImage color;
    private final BufferedImage greyscale;
    private ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    public BrowserIcon(BufferedImage color) {
      this.color = color;
      greyscale = op.filter(color, null);
    }

    public static BrowserIcon buildFromResource(String resourceName) throws IOException {
      return new BrowserIcon(ImageIO.read(ToolPanel.class.getResourceAsStream(resourceName)));
    }

    public Icon getColorIcon() {
      return new ImageIcon(color);
    }

    public Icon getGreyscaleIcon() {
      return new ImageIcon(greyscale);
    }

    public Icon getIconForEvent(Event event) {
      switch (event) {
        case CONNECTED:
          return getColorIcon();
        case DISCONNECTED:
          return getGreyscaleIcon();
        default:
          throw new IllegalStateException("Unknown event " + event);
      }
    }
  }
}
