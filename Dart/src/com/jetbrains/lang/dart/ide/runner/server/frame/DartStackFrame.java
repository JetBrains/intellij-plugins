package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.DebuggerUtils;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallFrame;
import com.jetbrains.lang.dart.ide.runner.server.google.VmLocation;
import com.jetbrains.lang.dart.ide.runner.server.google.VmVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartStackFrame extends XStackFrame {
  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private final @NotNull VmCallFrame myVmCallFrame;
  private final @Nullable XSourcePosition mySourcePosition;
  private final @Nullable String myLocationUrl;

  public DartStackFrame(@NotNull final DartCommandLineDebugProcess debugProcess, final @NotNull VmCallFrame vmCallFrame) {
    myDebugProcess = debugProcess;
    myVmCallFrame = vmCallFrame;

    final VmLocation location = vmCallFrame.getLocation();
    myLocationUrl = location == null ? null : location.getUnescapedUrl();
    if (myLocationUrl != null) {
      final VirtualFile file = myDebugProcess.getDartUrlResolver().findFileByDartUrl(myLocationUrl);
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
      component.append(DebuggerUtils.demangleVmName(myVmCallFrame.getFunctionName()), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    if (position != null) {
      final String text = " (" + position.getFile().getName() + ":" + (position.getLine() + 1) + ")";
      component.append(text, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
    else if (myLocationUrl != null) {
      component.append(" (" + myLocationUrl + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

    component.setIcon(AllIcons.Debugger.StackFrame);
  }
}
