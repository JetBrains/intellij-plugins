package com.jetbrains.lang.dart.ide.runner.server;

import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DartCommandLineBreakpointsHandler {
  private final DartCommandLineDebugProcess myDebugProcess;
  private XBreakpointHandler<?>[] myBreakpointHandlers;
  private final Map<XLineBreakpoint<XBreakpointProperties>, Integer> myBreakpointToIndexMap =
    new THashMap<XLineBreakpoint<XBreakpointProperties>, Integer>();
  private final Map<Integer, XLineBreakpoint<XBreakpointProperties>> myIndexToBreakpointMap =
    new THashMap<Integer, XLineBreakpoint<XBreakpointProperties>>();

  public DartCommandLineBreakpointsHandler(DartCommandLineDebugProcess process) {
    myDebugProcess = process;

    List<XBreakpointHandler> handlers = new ArrayList<XBreakpointHandler>(2);
    for (DartCommandLineBreakpointTypeProvider breakpointTypeProvider : DartCommandLineBreakpointTypeProvider.EP_NAME.getExtensions()) {
      handlers.add(new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(breakpointTypeProvider.provideBreakpointClass()) {
        public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
          final XSourcePosition position = breakpoint.getSourcePosition();
          if (position != null) {
            myDebugProcess.sendCommand(getSetBreakpointCommand(breakpoint), new AbstractResponseToRequestHandler<JsonResponse>() {
              @Override
              public boolean processResponse(JsonResponse response) {
                if (response.getJsonObject().get("error") != null) {
                  myDebugProcess.getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint, null);
                  return true;
                }
                final JsonObject result = response.getJsonObject().get("result").getAsJsonObject();
                final int id = result.get("breakpointId").getAsInt();
                myBreakpointToIndexMap.put(breakpoint, id);
                myIndexToBreakpointMap.put(id, breakpoint);
                return true;
              }
            });
          }
        }

        private boolean isValidSourceBreakpoint(XSourcePosition position) {
          ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(myDebugProcess.getSession().getProject()).getFileIndex();
          VirtualFile rootForFile = projectFileIndex.getSourceRootForFile(position.getFile());
          return rootForFile != null;
        }

        public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
          final XSourcePosition position = breakpoint.getSourcePosition();
          if (position != null && isValidSourceBreakpoint(position)) {
            myDebugProcess.sendCommand(getRemoveBreakpointCommand(breakpoint), new AbstractResponseToRequestHandler<JsonResponse>() {
              @Override
              public boolean processResponse(JsonResponse response) {
                final Integer id = myBreakpointToIndexMap.remove(breakpoint);
                if (myIndexToBreakpointMap.containsKey(id)) {
                  myIndexToBreakpointMap.remove(id);
                }
                return true;
              }
            });
          }
        }
      });
    }

    myBreakpointHandlers = handlers.toArray(new XBreakpointHandler<?>[handlers.size()]);
  }

  public XLineBreakpoint<XBreakpointProperties> getBreakpointById(int id) {
    return myIndexToBreakpointMap.get(id);
  }

  private JsonObject getRemoveBreakpointCommand(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    final JsonObject result = new JsonObject();
    result.addProperty("command", "removeBreakpoint");
    final JsonObject params = new JsonObject();
    params.addProperty("breakpointId", myBreakpointToIndexMap.get(breakpoint));
    result.add("params", params);
    return result;
  }

  private static JsonObject getSetBreakpointCommand(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    final JsonObject result = new JsonObject();
    result.addProperty("command", "setBreakpoint");
    result.add("params", getParams(breakpoint));
    return result;
  }

  private static JsonObject getParams(XLineBreakpoint<XBreakpointProperties> breakpoint) {
    final JsonObject result = new JsonObject();
    result.addProperty("url", breakpoint.getFileUrl());
    result.addProperty("line", breakpoint.getLine() + 1);
    return result;
  }

  public static void handleRunToPosition(XSourcePosition position, DartCommandLineDebugProcess process) {
    final JsonObject command = new JsonObject();
    command.addProperty("command", "setBreakpoint");
    final JsonObject params = new JsonObject();
    params.addProperty("url", position.getFile().getUrl());
    params.addProperty("line", position.getLine());
    command.add("params", params);

    process.sendCommand(command);
    process.resume();
  }

  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }
}
