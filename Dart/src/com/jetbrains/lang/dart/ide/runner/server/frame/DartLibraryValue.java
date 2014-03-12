package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmIsolate;
import com.jetbrains.lang.dart.ide.runner.server.google.VmLibrary;
import com.jetbrains.lang.dart.ide.runner.server.google.VmVariable;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartLibraryValue extends XNamedValue {

  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private final @NotNull VmIsolate myIsolate;
  private final int myLibraryId;

  public DartLibraryValue(@NotNull final DartCommandLineDebugProcess debugProcess, @NotNull final VmIsolate isolate, final int libraryId) {
    super("library");
    myDebugProcess = debugProcess;
    myIsolate = isolate;
    myLibraryId = libraryId;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull XValuePlace place) {
    node.setPresentation(DartIcons.Dart_16, "library", Integer.toString(myLibraryId), true);
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        if (node.isObsolete()) return;

        final VmLibrary vmLibrary = myDebugProcess.getVmConnection().getLibraryPropertiesSync(myIsolate, myLibraryId);
        final List<VmVariable> globals = vmLibrary == null ? null : vmLibrary.getGlobals();

        if (globals != null) {
          final XValueChildrenList childrenList = new XValueChildrenList(globals.size());
          for (VmVariable vmVariable : globals) {
            childrenList.add(new DartValue(myDebugProcess, vmVariable));
          }

          node.addChildren(childrenList, true);
        }
      }
    });
  }
}
