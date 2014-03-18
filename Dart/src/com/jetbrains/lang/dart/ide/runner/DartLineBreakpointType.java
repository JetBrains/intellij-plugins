package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

public class DartLineBreakpointType extends XLineBreakpointTypeBase {

  protected DartLineBreakpointType() {
    super("Dart", DartBundle.message("dart.line.breakpoints.title"), new DartDebuggerEditorsProvider());
  }

  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    return DartProjectComponent.JS_BREAKPOINT_TYPE == null && DartFileType.INSTANCE.equals(file.getFileType());
  }
}
