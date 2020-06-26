// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Maxim.Mossienko
 */
public class FlexBreakpointsHandler {

  public interface BreakpointTypeProvider {
    Class<? extends XBreakpointType<XLineBreakpoint<XBreakpointProperties>, ?>> getBreakpointTypeClass();
  }

  private static final ExtensionPointName<BreakpointTypeProvider> BREAKPOINT_TYPE_PROVIDER_EP =
    ExtensionPointName.create("com.intellij.flex.breakpoint.type.provider");

  private final FlexDebugProcess myDebugProcess;
  private int lastBreakpointId;
  private final Collection<XBreakpointHandler<?>> myBreakpointHandlers;
  private final TObjectIntHashMap<XLineBreakpoint<XBreakpointProperties>> myBreakpointToIndexMap =
    new TObjectIntHashMap<>();
  private final TIntObjectHashMap<XLineBreakpoint<XBreakpointProperties>> myIndexToBreakpointMap =
    new TIntObjectHashMap<>();

  FlexBreakpointsHandler(FlexDebugProcess debugProcess) {
    myDebugProcess = debugProcess;

    myBreakpointHandlers = new ArrayList<>();
    myBreakpointHandlers.add(new MyBreakpointHandler(FlexBreakpointType.class));
    for (BreakpointTypeProvider provider : BREAKPOINT_TYPE_PROVIDER_EP.getExtensions()) {
      myBreakpointHandlers.add(new MyBreakpointHandler(provider.getBreakpointTypeClass()));
    }
  }

  void updateBreakpointStatusToInvalid(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    if (breakpoint != null) {
      myDebugProcess.getSession().setBreakpointInvalid(breakpoint, null);
    }
  }

  void updateBreakpointStatusToVerified(String breakPointNumber) {
    int spacePos = breakPointNumber.indexOf(' ');
    if (spacePos != -1) breakPointNumber = breakPointNumber.substring(0, spacePos); // "24 at 0xf3103"
    final int index = Integer.parseInt(breakPointNumber);
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myIndexToBreakpointMap.get(index);

    if (breakpoint != null) {
      myDebugProcess.getSession().setBreakpointVerified(breakpoint);
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
    return myBreakpointHandlers.toArray(XBreakpointHandler.EMPTY_ARRAY);
  }

  XLineBreakpoint<XBreakpointProperties> getBreakpointByIndex(int breakpointId) {
    return myIndexToBreakpointMap.get(breakpointId);
  }

  private static String getRemoveBreakpointCommandText(int breakPointIndex) {
    return "delete " + breakPointIndex;
  }

  private final class MyBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
    private MyBreakpointHandler(@NotNull Class<? extends XBreakpointType<XLineBreakpoint<XBreakpointProperties>, ?>> breakpointTypeClass) {
      super(breakpointTypeClass);
    }

    @Override
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
      VirtualFile rootForFile = fileIndex.getSourceRootForFile(file); // project sources, SDK or or SWC library sources
      if (rootForFile == null) {
        rootForFile = fileIndex.getClassRootForFile(file); // raw AS library sources
      }
      if (rootForFile == null) {
        return false;
      }

      final Module module = myDebugProcess.getModule();
      if (module == null) return false;

      final GlobalSearchScope scope = FlexUtils.getModuleWithDependenciesAndLibrariesScope(module, myDebugProcess.getBC(),
                                                                                           myDebugProcess.isFlexUnit());
      if (scope.contains(file) || isInSourcesOfLibraryInScope(fileIndex, file, scope)) {
        return true;
      }

      if (DumbService.getInstance(project).isDumb()) {
        return false;
      }

      final String relPath = VfsUtilCore.getRelativePath(file.getParent(), rootForFile, '.');
      final String fqn = StringUtil.getQualifiedName(relPath, file.getNameWithoutExtension());
      final PsiElement clazz = ActionScriptClassResolver.findClassByQNameStatic(fqn, scope);
      // ignore breakpoints in classes that are out of scope of debugged BC if this scope also contains class with the same fqn;
      // but if resolved class is in decompiled code (library.swf inside *.swc file) then there's a chance that source file for this library
      // is not configured properly (or it is in the unrelated Haxe module), in this case allow to set breakpoint.
      return clazz == null || clazz.getContainingFile() instanceof PsiCompiledFile;
    }

    private boolean isInSourcesOfLibraryInScope(final ProjectFileIndex fileIndex,
                                                final VirtualFile file,
                                                final GlobalSearchScope scope) {
      if (!fileIndex.isInLibrarySource(file)) {
        return false;
      }

      for (OrderEntry entry : fileIndex.getOrderEntriesForFile(file)) {
        final VirtualFile[] classesRoots = entry.getFiles(OrderRootType.CLASSES);
        for (VirtualFile root : classesRoots) {
          if (scope.contains(root)) {
            return true;
          }
        }
      }

      return false;
    }

    @Override
    public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
      final XSourcePosition position = breakpoint.getSourcePosition();
      if (position != null && isValidSourceBreakpoint(position)) {
        myDebugProcess.sendCommand(new RemoveBreakpointCommand(breakpoint));
      }
    }
  }

  private class InsertBreakpointCommand extends DebuggerCommand {
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
