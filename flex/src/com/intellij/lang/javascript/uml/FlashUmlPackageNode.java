// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.diagram.DiagramProvider;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlashUmlPackageNode extends DiagramNodeBase<Object> {

  private final @NotNull String myPackage;

  public FlashUmlPackageNode(@NotNull String aPackage, @NotNull DiagramProvider provider) {
    super(provider);
    myPackage = aPackage;
  }

  @Override
  public @Nullable Icon getIcon() {
    return IconManager.getInstance().getPlatformIcon(PlatformIcons.Package);
  }

  @Override
  public @NotNull String getIdentifyingElement() {
    return myPackage;
  }

  @Override
  public String getTooltip() {
    return "<html><b>" + (!myPackage.isEmpty() ? myPackage : FlexBundle.message("top.level")) + "</b></html>";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FlashUmlPackageNode that = (FlashUmlPackageNode)o;

    if (!myPackage.equals(that.myPackage)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myPackage.hashCode();
  }
}
