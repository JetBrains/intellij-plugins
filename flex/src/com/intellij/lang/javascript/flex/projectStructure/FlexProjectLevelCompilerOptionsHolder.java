// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.openapi.project.Project;

public abstract class FlexProjectLevelCompilerOptionsHolder {
  // TODO should be getModifiableModel()!
  public abstract ModuleOrProjectCompilerOptions getProjectLevelCompilerOptions();

  public static FlexProjectLevelCompilerOptionsHolder getInstance(final Project project) {
    return project.getService(FlexProjectLevelCompilerOptionsHolder.class);
  }
}
