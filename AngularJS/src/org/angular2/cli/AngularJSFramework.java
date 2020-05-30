// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.framework.FrameworkType;
import icons.AngularJSIcons;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFramework extends FrameworkType {
  public static final AngularJSFramework INSTANCE = new AngularJSFramework();

  @NonNls public static final String ID = "AngularCLI";

  protected AngularJSFramework() {
    super(ID);
  }

  @Override
  public @NotNull String getPresentableName() {
    return Angular2Bundle.message("angular.description.angular-cli");
  }

  @Override
  public @NotNull Icon getIcon() {
    return AngularJSIcons.Angular2;
  }
}
