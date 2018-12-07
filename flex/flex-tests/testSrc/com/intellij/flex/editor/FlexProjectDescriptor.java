// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.editor;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class FlexProjectDescriptor extends LightProjectDescriptor {
  public final static FlexProjectDescriptor DESCRIPTOR = new FlexProjectDescriptor();

  @NotNull
  @Override
  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}
