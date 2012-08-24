package com.intellij.lang.javascript.flex.debug;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Mossienko
 *         Date: 30.01.2009
 *         Time: 9:44:23
 */
class FlexBreakpointsHandler {
  private final FlexDebugProcess myDebugProcess;
  private int lastBreakpointId;
  private final XBreakpointHandler<?>[] myBreakpointHandlers;
  private final TObjectIntHashMap<XLineBreakpoint<XBreakpointProperties>> myBreakpointToIndexMap =
    new TObjectIntHashMap<XLineBreakpoint<XBreakpointProperties>>();
  private final TIntObjectHashMap<XLineBreakpoint<XBreakpointProperties>> myIndexToBreakpointMap =
    new TIntObjectHashMap<XLineBreakpoint<XBreakpointProperties>>();

  FlexBreakpointsHandler(FlexDebugProcess debugProcess) {
    myDebugProcess = debugProcess;

    myBreakpointHandlers = new XBreakpointHandler<?>[]{
      new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(FlexBreakpointType.class) {
        public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
          final XSourcePosition position = breakpoint.getSourcePosition();
          if (position != null) {
            if (isValidSourceBreakpoint(position)) {
              myDebugProcess.sendCommand(new InsertBreakpointCommand(breakpoint));
            }
          }
        }

        private boolean isValidSourceBreakpoint(XSourcePosition position) {
          final Project project = myDebugProcess.getSession().getProject();
          final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
          final VirtualFile file = position.getFile();
          final VirtualFile rootForFile = fileIndex.getSourceRootForFile(file);
          if (rootForFile == null) {
            return false;
          }

          final ModuleWithDependenciesScope scope = FlexUtils
            .getModuleWithDependenciesAndLibrariesScope(myDebugProcess.getModule(), myDebugProcess.getBC(), myDebugProcess.isFlexUnit());
          if (scope.contains(file) || isInSourcesOfLibraryInScope(fileIndex, file, scope)) {
            return true;
          }

          final String relPath = VfsUtilCore.getRelativePath(file.getParent(), rootForFile, '.');
          final String fqn = StringUtil.getQualifiedName(relPath, file.getNameWithoutExtension());
          // ignore breakpoints in classes that are out of scope of debugged BC if this scope also contains class with the same fqn
          return JSResolveUtil.findClassByQName(fqn, scope) == null;
        }

        private boolean isInSourcesOfLibraryInScope(final ProjectFileIndex fileIndex,
                                                    final VirtualFile file,
                                                    final GlobalSearchScope scope) {
          if (!fileIndex.isInLibrarySource(file)) {
            return false;
          }

          for (OrderEntry entry : fileIndex.getOrderEntriesForFile(file)) {
            final VirtualFile[] classesRoots = entry.getFiles(OrderRootType.CLASSES);
            if (classesRoots.length > 0 && scope.contains(classesRoots[0])) {
              return true;
            }
          }

          return false;
        }

        public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
          final XSourcePosition position = breakpoint.getSourcePosition();
          if (position != null && isValidSourceBreakpoint(position)) {
            myDebugProcess.sendCommand(new RemoveBreakpointCommand(breakpoint));
          }
        }
      }
    };
  }

  void updateBreakpointStatusToInvalid(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    if (breakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint, null);
    }
  }

  void updateBreakpointStatusToVerified(String breakPointNumber) {
    int spacePos = breakPointNumber.indexOf(' ');
    if (spacePos != -1) breakPointNumber = breakPointNumber.substring(0, spacePos); // "24 at 0xf3103"
    final int index = Integer.parseInt(breakPointNumber);
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myIndexToBreakpointMap.get(index);

    if (breakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
    }
    else {
      // run to cursor
    }
  }

  private String buildInsertBreakpointCommandText(XSourcePosition sourcePosition) {
    String marker = myDebugProcess.resolveFileReference(sourcePosition.getFile());

    return "break " + marker + ":" + (sourcePosition.getLine() + 1);
  }

  void handleRunToPosition(XSourcePosition position, FlexDebugProcess flexDebugProcess) {
    flexDebugProcess.sendCommand(
      new CompositeDebuggerCommand(new InsertBreakpointCommand(position), new FlexDebugProcess.ContinueCommand()));
  }

  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  XLineBreakpoint<XBreakpointProperties> getBreakpointByIndex(int breakpointId) {
    return myIndexToBreakpointMap.get(breakpointId);
  }

  class InsertBreakpointCommand extends DebuggerCommand {
    private @Nullable XLineBreakpoint<XBreakpointProperties> myBreakpoint; // null if this is breakpoint for 'Run To Cursor' action
    private @NotNull final XSourcePosition mySourcePosition;

    InsertBreakpointCommand(@NotNull XSourcePosition sourcePosition) {
      super(buildInsertBreakpointCommandText(sourcePosition), CommandOutputProcessingType.SPECIAL_PROCESSING);
      mySourcePosition = sourcePosition;
    }

    InsertBreakpointCommand(@NotNull XLineBreakpoint<XBreakpointProperties> _breakpoint) {
      this(_breakpoint.getSourcePosition());
      myBreakpoint = _breakpoint;
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(final String s) {
      int index;
      if ((index = s.indexOf(FlexDebugProcess.BREAKPOINT_MARKER)) != -1) {
        if (s.contains(" created")) {
          registerBreakPoint();
        }
        else if (s.contains("not set; no executable code")) {
          updateBreakpointStatusToInvalid(myBreakpoint);
        }
        else {
          // Breakpoint 2: file A.mxml, line 15
          final int from = index + FlexDebugProcess.BREAKPOINT_MARKER.length();

          if (s.contains("file")) { // Breakpoint 2: file A.mxml, line 15
            registerBreakPoint();
            updateBreakpointStatusToVerified(s.substring(from, s.indexOf(':', from)));
          }
        }
      }
      else if (s.contains(FlexDebugProcess.AMBIGUOUS_MATCHING_FILE_NAMES)) {
        if (myDebugProcess.getFileId(mySourcePosition.getFile().getPath()) != null) {
          final DebuggerCommand command = myBreakpoint == null
                                          ? new InsertBreakpointCommand(mySourcePosition)
                                          : new InsertBreakpointCommand(myBreakpoint);
          myDebugProcess.sendAndProcessOneCommand(command, null);
        }
        else {
          updateBreakpointStatusToInvalid(myBreakpoint);
        }
      }
      return CommandOutputProcessingMode.DONE;
    }

    private void registerBreakPoint() {
      final int breakPointIndex = ++lastBreakpointId;
      if (myBreakpoint != null) {
        myBreakpointToIndexMap.put(myBreakpoint, breakPointIndex);
        myIndexToBreakpointMap.put(breakPointIndex, myBreakpoint);
      }
    }
  }

  private static String getRemoveBreakpointCommandText(int breakPointIndex) {
    return "delete " + breakPointIndex;
  }

  class RemoveBreakpointCommand extends DebuggerCommand {
    private int myBreakpointIndex;
    private final XLineBreakpoint<XBreakpointProperties> myBreakpoint; // null if this is breakpoint for 'Run To Cursor' action
    private static final int UNKNOWN_ID = -1;

    RemoveBreakpointCommand(int breakPointIndex, @Nullable XLineBreakpoint<XBreakpointProperties> breakpoint) {
      super(getRemoveBreakpointCommandText(breakPointIndex), CommandOutputProcessingType.SPECIAL_PROCESSING);

      myBreakpointIndex = breakPointIndex;
      myBreakpoint = breakpoint;
    }

    RemoveBreakpointCommand(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
      this(UNKNOWN_ID, breakpoint); // the breakpoint can be not registered yet so we will wait till command posting (getText)
    }

    @NotNull
    @Override
    String getText() {
      if (myBreakpointIndex == UNKNOWN_ID) {
        myBreakpointIndex = myBreakpointToIndexMap.get(myBreakpoint);
        return getRemoveBreakpointCommandText(myBreakpointIndex);
      }
      return super.getText();
    }

    @Override
    CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
      myBreakpointToIndexMap.remove(myBreakpoint);
      myIndexToBreakpointMap.remove(myBreakpointIndex);
      return super.onTextAvailable(s);
    }
  }
}
