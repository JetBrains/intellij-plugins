// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.framework.FrameworkType;
import icons.AngularJSIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFramework extends FrameworkType {
  public static final AngularJSFramework INSTANCE = new AngularJSFramework();

  protected AngularJSFramework() {
    super("AngularCLI");
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Angular CLI";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }
}
