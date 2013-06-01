package com.jetbrains.lang.dart.ide.runner.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.util.io.socketConnection.AbstractResponseHandler;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.base.DartDebuggerEditorsProvider;
import com.jetbrains.lang.dart.ide.runner.server.connection.DartVMConnection;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import com.jetbrains.lang.dart.ide.runner.server.frame.DartStackFrame;
import com.jetbrains.lang.dart.ide.runner.server.frame.DartSuspendContext;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

public class DartCommandLineDebugProcess extends XDebugProcess {
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(DartCommandLineDebugProcess.class.getName());
  private final ExecutionResult myExecutionResult;
  private final DartVMConnection myConnection = new DartVMConnection();
  private final DartCommandLineBreakpointsHandler myBreakpointsHandler;

  private static final Set<String> CORE_LIBS = new THashSet<String>(Arrays.asList(
    "dart:core", "dart:coreimpl", "dart:nativewrappers"
  ));
  private int myMainIsolateId;

  enum InitializingState {NOT_INITIALIZED, INITIALIZING, INITIALIZED}

  private volatile InitializingState myInitialized = InitializingState.NOT_INITIALIZED;

  private final List<Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>> postponedCommands =
    new LinkedList<Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>>();

  private final LinkedList<Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>> commandsToWrite =
    new LinkedList<Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>>() {
      @Override
      public synchronized Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> removeFirst() {
        waitForData();
        return super.removeFirst();
      }

      @Override
      public synchronized void addFirst(final Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> debuggerCommand) {
        super.addFirst(debuggerCommand);
        notify();
      }

      @Override
      public synchronized void addLast(final Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> debuggerCommand) {
        super.addLast(debuggerCommand);
        notify();
      }

      private void waitForData() {
        try {
          while (size() == 0) {
            wait();
          }
        }
        catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      }
    };

  @Override
  protected ProcessHandler doGetProcessHandler() {
    return myExecutionResult != null ? myExecutionResult.getProcessHandler() : null;
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    if (myExecutionResult != null) {
      return myExecutionResult.getExecutionConsole();
    }
    return super.createConsole();
  }

  public DartCommandLineDebugProcess(@NotNull XDebugSession session,
                                     int debuggingPort,
                                     ExecutionResult executionResult)
    throws IOException {
    super(session);

    myBreakpointsHandler = new DartCommandLineBreakpointsHandler(this);
    myExecutionResult = executionResult;
    startCommandProcessingThread(debuggingPort);
  }

  private void startCommandProcessingThread(final int debuggingPort) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        myConnection.connect(debuggingPort);
        myConnection.registerHandler(new AbstractResponseHandler<JsonResponse>() {
          @Override
          public void processResponse(JsonResponse response) {
            DartCommandLineDebugProcess.this.processResponse(response);
          }
        });

        boolean connected = false;
        int attempts = 10;
        do {
          --attempts;
          try {
            myConnection.open();
            connected = true;
          }
          catch (ConnectException ex) {
            synchronized (this) {
              try {
                wait(200);
              }
              catch (InterruptedException ignored) {
              }
            }
          }
          catch (IOException ignored) {
          }
        }
        while (!connected && attempts > 0);

        while (true) {
          processOneCommandLoop();
        }
      }
    });
  }

  private void processOneCommandLoop() {
    final Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> command = commandsToWrite.removeFirst();
    JsonObject commandObject = command.getFirst();
    assert commandObject != null;
    JsonObject params = null;
    if (commandObject.has("params")) {
      params = commandObject.get("params").getAsJsonObject();
    }
    else {
      params = new JsonObject();
      commandObject.add("params", params);
    }
    params.addProperty("isolateId", myMainIsolateId);
    myConnection.sendCommand(commandObject, command.getSecond());
  }

  public void processResponse(JsonResponse response) {
    LOG.debug("processResponse:" + response.getJsonObject().toString());
    JsonElement jsonEventElement = response.getJsonObject().get("event");
    final String event = jsonEventElement == null ? null : jsonEventElement.getAsString();
    final JsonElement paramsElement = response.getJsonObject().get("params");
    if ("isolate".equals(event) && myInitialized == InitializingState.NOT_INITIALIZED) {
      myInitialized = InitializingState.INITIALIZING;
      myMainIsolateId = paramsElement.getAsJsonObject().get("id").getAsInt();
      enableAllStepping(new Runnable() {
        @Override
        public void run() {
          myInitialized = InitializingState.INITIALIZED;
          // wait until commands are executed
          Iterator<Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>> iterator = postponedCommands.iterator();
          if (!iterator.hasNext()) {
            handleBreakpointResolved(paramsElement);
          }
          while (iterator.hasNext()) {
            final Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> command = iterator.next();
            if (iterator.hasNext()) {
              commandsToWrite.addLast(command);
            }
            else {
              commandsToWrite.addLast(Pair.<JsonObject, AbstractResponseToRequestHandler<JsonResponse>>create(
                command.getFirst(),
                new AbstractResponseToRequestHandler<JsonResponse>() {
                  @Override
                  public boolean processResponse(JsonResponse response) {
                    handleBreakpointResolved(paramsElement);
                    return command.getSecond().processResponse(response);
                  }
                }
              ));
            }
          }
        }
      });
    }
    else if ("breakpointResolved".equals(event) && myInitialized == InitializingState.INITIALIZED) {
      handleBreakpointResolved(paramsElement);
    }
    else if ("paused".equals(event) && myInitialized == InitializingState.INITIALIZED) {
      handlePaused(paramsElement);
    }
    else {
      LOG.debug("unhandled response: " + response.getJsonObject().toString());
    }
  }

  private void enableAllStepping(final Runnable runnable) {
    sendPriorityCommand(getCommandObject("getLibraries"), new AbstractResponseToRequestHandler<JsonResponse>() {
      @Override
      public boolean processResponse(JsonResponse response) {
        if (response.getJsonObject().get("error") != null) {
          runnable.run();
          return true;
        }
        JsonArray libraries = response.getJsonObject().getAsJsonObject("result").getAsJsonArray("libraries");
        Iterator<JsonElement> iterator = libraries.iterator();
        if (!iterator.hasNext()) {
          runnable.run();
          return true;
        }
        while (iterator.hasNext()) {
          JsonElement library = iterator.next();
          int id = library.getAsJsonObject().get("id").getAsInt();
          String url = library.getAsJsonObject().get("url").getAsString();
          if (url != null && !CORE_LIBS.contains(url)) {
            sendEnableLibrary(id, iterator.hasNext() ? null : new AbstractResponseToRequestHandler<JsonResponse>() {
              @Override
              public boolean processResponse(JsonResponse response) {
                runnable.run();
                return true;
              }
            });
          }
        }
        return true;
      }
    });
  }

  private void handlePaused(JsonElement paramsElement) {
    final JsonArray callFrames = paramsElement.getAsJsonObject().getAsJsonArray("callFrames");
    if (callFrames != null) {
      final List<DartStackFrame> frames = DartStackFrame.fromJson(this, callFrames);
      DartStackFrame.requestLines(this, frames, new Runnable() {
        @Override
        public void run() {
          getSession().positionReached(new DartSuspendContext(DartCommandLineDebugProcess.this, frames));
        }
      });
    }
    else {
      sendSimpleCommand("getStackTrace", new AbstractResponseToRequestHandler<JsonResponse>() {
        @Override
        public boolean processResponse(JsonResponse response) {
          JsonObject result = response.getJsonObject().getAsJsonObject().getAsJsonObject("result");
          final JsonArray callFrames = result == null ? null : result.getAsJsonArray("callFrames");
          final List<DartStackFrame> frames = callFrames == null ?
                                              new ArrayList<DartStackFrame>() :
                                              DartStackFrame.fromJson(DartCommandLineDebugProcess.this, callFrames);
          DartStackFrame.requestLines(DartCommandLineDebugProcess.this, frames, new Runnable() {
            @Override
            public void run() {
              getSession().positionReached(new DartSuspendContext(DartCommandLineDebugProcess.this, frames));
            }
          });
          return true;
        }
      });
    }
  }

  private void initLines(List<DartStackFrame> frames, Runnable runnable) {
    for (DartStackFrame stackFrame : frames) {
      JsonObject command = getCommandObject("getLineNumberTable");
      command.addProperty("url", stackFrame.getFileUrl());
      sendCommand(command);
    }
    runnable.run();
  }

  private void sendEnableLibrary(int id, @Nullable AbstractResponseToRequestHandler<JsonResponse> requestHandler) {
    JsonObject command = getCommandObject("setLibraryProperties");

    JsonObject params = new JsonObject();
    params.addProperty("libraryId", id);
    params.addProperty("debuggingEnabled", "true");

    command.add("params", params);
    sendPriorityCommand(command, requestHandler);
  }

  private void handleBreakpointResolved(JsonElement paramsElement) {
    final int breakpointId = getBreakpointId(paramsElement);
    XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpointsHandler.getBreakpointById(breakpointId);
    if (breakpoint == null) {
      resume();
    }
  }

  private static int getBreakpointId(JsonElement paramsElement) {
    final JsonObject params = paramsElement == null ? null : paramsElement.getAsJsonObject();
    JsonElement jsonEventElement = params == null ? null : params.get("breakpointId");
    return jsonEventElement == null ? -1 : jsonEventElement.getAsInt();
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new DartDebuggerEditorsProvider();
  }

  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointsHandler.getBreakpointHandlers();
  }

  public static JsonObject getCommandObject(String commandName) {
    JsonObject command = new JsonObject();
    command.addProperty("command", commandName);
    return command;
  }

  @Override
  public void startStepOver() {
    sendSimpleCommand("stepOver");
  }

  @Override
  public void startStepInto() {
    sendSimpleCommand("stepInto");
  }

  @Override
  public void startStepOut() {
    sendSimpleCommand("stepOut");
  }

  public void sendSimpleCommand(String commandName) {
    sendSimpleCommand(commandName, null);
  }

  public void sendSimpleCommand(String commandName, @Nullable AbstractResponseToRequestHandler<JsonResponse> requestHandler) {
    sendCommand(getCommandObject(commandName), requestHandler);
  }

  public void sendCommand(JsonObject out) {
    sendCommand(out, null);
  }

  public void sendPriorityCommand(final JsonObject out, @Nullable final AbstractResponseToRequestHandler<JsonResponse> requestHandler) {
    commandsToWrite.addLast(Pair.create(out, requestHandler));
  }

  public void sendCommand(final JsonObject out, @Nullable final AbstractResponseToRequestHandler<JsonResponse> requestHandler) {
    Pair<JsonObject, AbstractResponseToRequestHandler<JsonResponse>> command = Pair.create(out, requestHandler);
    if (myInitialized != InitializingState.INITIALIZED) {
      postponedCommands.add(command);
    }
    else {
      commandsToWrite.addLast(command);
    }
  }

  @Override
  public void stop() {
    myConnection.close();
  }

  @Override
  public void resume() {
    sendSimpleCommand("resume");
  }

  @Override
  public void startPausing() {
    sendSimpleCommand("pause");
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
    DartCommandLineBreakpointsHandler.handleRunToPosition(position, this);
  }
}
