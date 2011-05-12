package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.io.DataInput;
import java.io.IOException;

public final class ProjectWindowBounds {
  private static final String X = "fud_pw_x";
  private static final String Y = "fud_pw_y";
  private static final String W = "fud_pw_w";
  private static final String H = "fud_pw_h";

  public static void save(Project project, DataInput input) throws IOException {
    PropertiesComponent d = PropertiesComponent.getInstance(project);
    d.setValue(X, readValue(input));
    d.setValue(Y, readValue(input));
    d.setValue(W, readValue(input));
    d.setValue(H, readValue(input));
  }

  private static String readValue(DataInput input) throws IOException {
    return String.valueOf(input.readUnsignedShort());
  }

  public static void write(Project project, AmfOutputStream out) {
    Rectangle projectWindowBounds = getProjectWindowBounds(project);
    if (projectWindowBounds == null) {
      out.write(false);
    }
    else {
      out.write(true);
      out.writeShort(projectWindowBounds.x);
      out.writeShort(projectWindowBounds.y);
      out.writeShort(projectWindowBounds.width);
      out.writeShort(projectWindowBounds.height);
    }
  }

  private static Rectangle getProjectWindowBounds(Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return new Rectangle(0, 0, 1280, 770);
    }

    PropertiesComponent d = PropertiesComponent.getInstance(project);
    try {
      return d.isValueSet(X)
             ? new Rectangle(parsePwV(d, X), parsePwV(d, Y), parsePwV(d, W), parsePwV(d, H))
             : null;
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  private static int parsePwV(PropertiesComponent propertiesComponent, String key) {
    int v = Integer.parseInt(propertiesComponent.getValue(key));
    if (v < 0 || v > 65535) {
      throw new NumberFormatException("Value " + v + " out of range 0-65535");
    }
    return v;
  }
}
