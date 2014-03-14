package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.icons.AllIcons;
import com.intellij.javascript.debugger.breakpoints.JavaScriptBreakpointType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;
import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.threeSlashizeFileUrl;

public class DartCommandLineBreakpointsHandler {
  private final DartCommandLineDebugProcess myDebugProcess;
  private final XBreakpointHandler<?>[] myBreakpointHandlers;
  private final Collection<XLineBreakpoint<?>> myInitialBreakpoints = new ArrayList<XLineBreakpoint<?>>();
  private final Map<XLineBreakpoint<?>, List<VmBreakpoint>> myCreatedBreakpoints = new THashMap<XLineBreakpoint<?>, List<VmBreakpoint>>();
  private final TIntObjectHashMap<XLineBreakpoint<?>> myIndexToBreakpointMap = new TIntObjectHashMap<XLineBreakpoint<?>>();

  // todo handle breakpoints in IntelliJ IDEA Community Edition, there's no JavaScriptBreakpointType
  public DartCommandLineBreakpointsHandler(final @NotNull DartCommandLineDebugProcess debugProcess) {
    myDebugProcess = debugProcess;

    List<XBreakpointHandler> handlers = new ArrayList<XBreakpointHandler>(1);
    handlers.add(new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(JavaScriptBreakpointType.class) {
      public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
        final XSourcePosition position = breakpoint.getSourcePosition();
        if (position == null) return;
        if (position.getFile().getFileType() != DartFileType.INSTANCE) return;

        if (myDebugProcess.isVmConnected()) {
          doRegisterBreakpoint(breakpoint);
        }
        else {
          myInitialBreakpoints.add(breakpoint);
        }
      }

      public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
        doUnregisterBreakpoint(breakpoint);
      }
    });

    myBreakpointHandlers = handlers.toArray(new XBreakpointHandler<?>[handlers.size()]);
  }

  XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  void registerInitialBreakpoints() {
    for (XLineBreakpoint<?> breakpoint : myInitialBreakpoints) {
      doRegisterBreakpoint(breakpoint);
    }
    //myInitialBreakpoints.clear(); do not clear - it is used later in hasInitialBreakpointHere()
  }

  boolean hasInitialBreakpointHere(final @Nullable VmLocation vmLocation) {
    if (vmLocation == null) return false;

    for (XLineBreakpoint<?> breakpoint : myInitialBreakpoints) {
      final XSourcePosition sourcePosition = breakpoint.getSourcePosition();
      if (sourcePosition != null &&
          threeSlashizeFileUrl(sourcePosition.getFile().getUrl()).equals(threeSlashizeFileUrl(vmLocation.getUrl())) &&
          sourcePosition.getLine() == vmLocation.getLineNumber(myDebugProcess.getVmConnection()) - 1) {
        return true;
      }
    }

    return false;
  }

  private void doUnregisterBreakpoint(final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    final XSourcePosition position = breakpoint.getSourcePosition();
    if (position == null) return;
    if (position.getFile().getFileType() != DartFileType.INSTANCE) return;

    suspendPerformActionAndResume(new ThrowableRunnable<IOException>() {
      public void run() throws IOException {
        // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#breakpointRemoved()
        final List<VmBreakpoint> breakpoints = myCreatedBreakpoints.remove(breakpoint);

        if (breakpoints != null) {
          for (VmBreakpoint vmBreakpoint : breakpoints) {
            myDebugProcess.getVmConnection().removeBreakpoint(vmBreakpoint.getIsolate(), vmBreakpoint);
          }
        }
      }
    });
  }

  private void doRegisterBreakpoint(final XLineBreakpoint<?> breakpoint) {
    final XSourcePosition position = breakpoint.getSourcePosition();
    final VmIsolate isolate = myDebugProcess.getMainIsolate();
    if (position == null || isolate == null) return;

    suspendPerformActionAndResume(new ThrowableRunnable<IOException>() {
      public void run() throws IOException {
        final String urlToSetBreakpoint = getUrlToSetBreakpoint(position);
        final int line = breakpoint.getLine() + 1;
        sendSetBreakpointCommand(isolate, breakpoint, urlToSetBreakpoint, line);
      }
    });
  }

  private void suspendPerformActionAndResume(final ThrowableRunnable<IOException> action) {
    final VmIsolate isolate = myDebugProcess.getMainIsolate();
    if (isolate == null) return;

    final Runnable runnable = new Runnable() {
      public void run() {
        // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#addBreakpoint()
        try {
          final VmInterruptResult interruptResult = myDebugProcess.getVmConnection().interruptConditionally(isolate);
          action.run();
          interruptResult.resume();
        }
        catch (IOException exception) {
          LOG.error(exception);
        }
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }
    else {
      runnable.run();
    }
  }

  private String getUrlToSetBreakpoint(final XSourcePosition position) {
    final VirtualFile file = position.getFile();

    final Project project = myDebugProcess.getSession().getProject();
    final DartSdk sdk = DartSdk.getGlobalDartSdk();

    final VirtualFile sdkLibFolder = sdk == null ? null : LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib");
    final String relativeToSdkLib = sdkLibFolder == null ? null : VfsUtilCore.getRelativePath(file, sdkLibFolder, '/');
    final String sdkLibName = relativeToSdkLib == null
                              ? null
                              : DartLibraryIndex.getStandardLibraryNameByRelativePath(project, relativeToSdkLib);

    final VirtualFile packagesFolder = myDebugProcess.getPackagesFolder();
    final String relativeToPackages = packagesFolder == null || relativeToSdkLib != null
                                      ? null
                                      : VfsUtilCore.getRelativePath(file, packagesFolder, '/');

    final VirtualFile pubspecYamlFile = myDebugProcess.getPubspecYamlFile();
    final VirtualFile libFolder = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild("lib");
    final String pubspecName = pubspecYamlFile == null ? null : PubspecYamlUtil.getPubspecName(pubspecYamlFile);
    final String relativeToLibFolder = libFolder == null || relativeToSdkLib != null || relativeToPackages != null
                                       ? null
                                       : VfsUtilCore.getRelativePath(file, libFolder, '/');

    return sdkLibName != null
           ? "dart:" + sdkLibName
           : relativeToSdkLib != null
             ? "dart:" + relativeToSdkLib
             : relativeToPackages != null
               ? "package:" + relativeToPackages
               : relativeToLibFolder != null && pubspecName != null
                 ? "package:" + pubspecName + "/" + relativeToLibFolder
                 : getAbsoluteUrlForResource(file);
  }

  private void sendSetBreakpointCommand(final VmIsolate isolate,
                                        final XLineBreakpoint<?> breakpoint,
                                        final String url,
                                        final int line) throws IOException {
    myDebugProcess.getVmConnection().setBreakpoint(isolate, url, line, new VmCallback<VmBreakpoint>() {
      @Override
      public void handleResult(VmResult<VmBreakpoint> result) {
        if (result.isError()) {
          myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint, result.getError());
        }
        else {
          addCreatedBreakpoint(breakpoint, result.getResult());
        }
      }
    });
  }

  private void addCreatedBreakpoint(final XLineBreakpoint<?> breakpoint, final VmBreakpoint vmBreakpoint) {
    List<VmBreakpoint> vmBreakpoints = myCreatedBreakpoints.get(breakpoint);

    if (vmBreakpoints == null) {
      vmBreakpoints = new ArrayList<VmBreakpoint>();
      myCreatedBreakpoints.put(breakpoint, vmBreakpoints);
    }

    vmBreakpoints.add(vmBreakpoint);
    myIndexToBreakpointMap.put(vmBreakpoint.getBreakpointId(), breakpoint);
  }

  public void breakpointResolved(final VmBreakpoint vmBreakpoint) {
    final XLineBreakpoint<?> breakpoint = myIndexToBreakpointMap.get(vmBreakpoint.getBreakpointId());
    if (breakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
    }
    else {
      LOG.info("Unknown breakpoint id: " + vmBreakpoint.getBreakpointId());
    }

    // todo see com.google.dart.tools.debug.core.server.ServerBreakpointManager#handleBreakpointResolved: breakpoint could be automatically shifted down to another line if there's no code at initial line
  }

  // see com.google.dart.tools.debug.core.server.ServerBreakpointManager#getAbsoluteUrlForResource()
  @NotNull
  private static String getAbsoluteUrlForResource(final @NotNull VirtualFile file) {
    return new File(file.getPath()).toURI().toString();
  }
}
