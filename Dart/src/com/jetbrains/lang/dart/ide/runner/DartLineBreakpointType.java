// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

public final class DartLineBreakpointType extends XLineBreakpointTypeBase {

  private DartLineBreakpointType() {
    super("Dart", DartBundle.message("dart.line.breakpoints.title"), new DartDebuggerEditorsProvider());
  }

  @Override
  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    return FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE);
  }
}
