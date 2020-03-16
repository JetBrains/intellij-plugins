/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.diagram.DiagramProvider;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlashUmlPackageNode extends DiagramNodeBase<Object> {

  @NotNull private final String myPackage;

  public FlashUmlPackageNode(@NotNull String aPackage, @NotNull DiagramProvider provider) {
    super(provider);
    myPackage = aPackage;
  }

  @Override
  public Icon getIcon() {
    return PlatformIcons.PACKAGE_ICON;
  }

  @Override
  @NotNull
  public String getIdentifyingElement() {
    return myPackage;
  }

  @Override
  public String getTooltip() {
    return "<html><b>" + (myPackage.length() > 0 ? myPackage : JavaScriptBundle.message("top.level")) + "</b></html>";
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
