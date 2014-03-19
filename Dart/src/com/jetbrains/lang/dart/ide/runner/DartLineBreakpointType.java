package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartLineBreakpointType extends XLineBreakpointTypeBase {

  // todo remove in 13.1.1 (when JavaScriptDebugAware.isOnlySourceMappedBreakpoints() is introduced)
  private static boolean jsBreakpointTypeInitialized = false;
  private static XLineBreakpointTypeBase jsBreakpointType = null;

  protected DartLineBreakpointType() {
    super("Dart", DartBundle.message("dart.line.breakpoints.title"), new DartDebuggerEditorsProvider());
  }

  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    return getJSBreakpointType() == null && DartFileType.INSTANCE.equals(file.getFileType());
  }

  @Nullable
  public static XLineBreakpointTypeBase getJSBreakpointType() {
    if (!jsBreakpointTypeInitialized) {
      jsBreakpointTypeInitialized = true;

      try {
        final Class<?> jsBreakpointTypeClass = Class.forName("com.intellij.javascript.debugger.breakpoints.JavaScriptBreakpointType");
        if (jsBreakpointTypeClass != null) {
          final XBreakpointType type =
            ContainerUtil.find(XBreakpointType.EXTENSION_POINT_NAME.getExtensions(), new Condition<XBreakpointType>() {
              public boolean value(final XBreakpointType type) {
                return jsBreakpointTypeClass.isInstance(type);
              }
            });
          jsBreakpointType = type instanceof XLineBreakpointTypeBase ? (XLineBreakpointTypeBase)type : null;
        }
      }
      catch (Throwable ignored) {/*ignore*/}
    }

    return jsBreakpointType;
  }
}
