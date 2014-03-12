package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmLocation;
import com.jetbrains.lang.dart.ide.runner.server.google.VmVariable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;
import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.threeSlashizeFileUrl;

public class DartStackFrame extends XStackFrame {
  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private final @NotNull VmCallFrame myVmCallFrame;
  private final @Nullable XSourcePosition mySourcePosition;

  public DartStackFrame(@NotNull final DartCommandLineDebugProcess debugProcess, final @NotNull VmCallFrame vmCallFrame) {
    myDebugProcess = debugProcess;
    myVmCallFrame = vmCallFrame;

    final VmLocation location = vmCallFrame.getLocation();
    final String locationUrl = location == null ? null : location.getUrl();
    if (locationUrl != null) {
      final VirtualFile file;
      if (locationUrl.startsWith("file:")) {
        file = VirtualFileManager.getInstance().findFileByUrl(threeSlashizeFileUrl(locationUrl));
      }
      else if (locationUrl.startsWith("dart:")) {
        final DartSdk sdk = DartSdk.getGlobalDartSdk();
        final String path = sdk == null ? null : sdk.getHomePath() + "/lib/" + locationUrl.substring("dart:".length());
        // todo find internal patches somehow (real files are not available, but debugger can provide its contents)
        file = path == null ? null : LocalFileSystem.getInstance().findFileByPath(path);
      }
      else if (locationUrl.startsWith("package:")) {
        final VirtualFile packagesFolder = myDebugProcess.getPackagesFolder();
        final String path = packagesFolder == null ? null : packagesFolder.getPath() + "/" + locationUrl.substring("package:".length());
        file = path == null ? null : LocalFileSystem.getInstance().findFileByPath(path);
      }
      else {
        LOG.warn("Unexpected URL:" + locationUrl);
        file = null;
      }

      final int line = location.getLineNumber(debugProcess.getVmConnection()) - 1;
      mySourcePosition = file == null || line < 0 ? null : XDebuggerUtil.getInstance().createPosition(file, line);
    }
    else {
      mySourcePosition = null;
    }
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    return mySourcePosition;
  }

  @Override
  public void computeChildren(final @NotNull XCompositeNode node) {
    final List<VmVariable> locals = myVmCallFrame.getLocals();

    final XValueChildrenList childrenList = new XValueChildrenList(locals == null ? 1 : locals.size() + 1);

    if (locals != null) {
      for (final VmVariable local : locals) {
        childrenList.add(new DartValue(myDebugProcess, local));
      }
    }

    if (myVmCallFrame.getIsolate() != null && myVmCallFrame.getLibraryId() != -1) {
      // todo make library info presentable
      //childrenList.add(new DartLibraryValue(myDebugProcess, myVmCallFrame.getIsolate(), myVmCallFrame.getLibraryId()));
    }

    node.addChildren(childrenList, true);
  }

  @Override
  public void customizePresentation(@NotNull ColoredTextContainer component) {
    final XSourcePosition position = getSourcePosition();

    if (myVmCallFrame.getFunctionName() != null) {
      component.append(myVmCallFrame.getFunctionName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    if (position != null) {
      component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(":" + (position.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    else {
      component.append("<file name is not available>", SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    component.setIcon(AllIcons.Debugger.StackFrame);
  }
}
