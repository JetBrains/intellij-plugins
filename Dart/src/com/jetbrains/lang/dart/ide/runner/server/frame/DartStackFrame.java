package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartStackFrame extends XStackFrame {
  private final DartCommandLineDebugProcess myDebugProcess;
  private final String myFunctionName;
  private final String myFileUrl;
  private int myLine = -1;
  private final int myOffset;
  private final int myLibraryId;
  private final List<DartVMValue> myLocals;

  public static List<DartStackFrame> fromJson(DartCommandLineDebugProcess debugProcess, @NotNull JsonArray callFrames) {
    final List<DartStackFrame> result = new ArrayList<DartStackFrame>(callFrames.size());
    for (int i = 0; i < callFrames.size(); ++i) {
      result.add(fromJson(debugProcess, callFrames.get(i).getAsJsonObject()));
    }
    return result;
  }

  public static void requestLines(DartCommandLineDebugProcess debugProcess,
                                  @NotNull List<DartStackFrame> stackFrames,
                                  @Nullable final Runnable after) {
    if (stackFrames.isEmpty() && after != null) {
      after.run();
      return;
    }
    for (int i = 0; i < stackFrames.size(); i++) {
      final DartStackFrame stackFrame = stackFrames.get(i);
      JsonObject commandObject = DartCommandLineDebugProcess.getCommandObject("getLineNumberTable");
      JsonObject params = new JsonObject();
      params.addProperty("url", stackFrame.getFileUrl());
      params.addProperty("libraryId", stackFrame.getLibraryId());
      commandObject.add("params", params);
      final boolean isLast = i == stackFrames.size() - 1;
      debugProcess.sendCommand(commandObject, new AbstractResponseToRequestHandler<JsonResponse>() {
        @Override
        public boolean processResponse(JsonResponse response) {
          if (response.getJsonObject().get("error") == null) {
            JsonObject result = response.getJsonObject().get("result").getAsJsonObject();
            JsonArray lines = result.get("lines").getAsJsonArray();
            stackFrame.setLine(findLineByOffset(lines, stackFrame.getOffset()) - 1);
          }
          if (isLast && after != null) {
            after.run();
          }
          return true;
        }
      });
    }
  }

  private static int findLineByOffset(JsonArray lines, int offset) {
    for (JsonElement line : lines) {
      JsonArray lineInfo = line.getAsJsonArray();
      for (int i = 1; i < lineInfo.size(); i += 2) {
        if (offset == lineInfo.get(i).getAsInt()) {
          return lineInfo.get(0).getAsInt();
        }
      }
    }

    return -1;
  }

  public static DartStackFrame fromJson(DartCommandLineDebugProcess debugProcess, JsonObject object) {
    final String functionName = object.get("functionName").getAsString();
    JsonObject location = object.get("location").getAsJsonObject();
    final String url = location.get("url").getAsString();
    JsonElement tokenOffset = location.get("tokenOffset");
    final int offset = (tokenOffset == null ? -1 : tokenOffset.getAsInt());
    final int libraryId = location.get("libraryId").getAsInt();
    return new DartStackFrame(debugProcess, functionName, url, offset, libraryId,
                              DartVMValue.fromJson(debugProcess, object.getAsJsonArray("locals")));
  }

  protected DartStackFrame(DartCommandLineDebugProcess debugProcess,
                           String functionName,
                           String url,
                           int offset,
                           int libraryId,
                           List<DartVMValue> locals) {
    myDebugProcess = debugProcess;
    myFunctionName = functionName;
    myFileUrl = url;
    myOffset = offset;
    myLibraryId = libraryId;
    myLocals = locals;
  }

  public String getFileUrl() {
    return myFileUrl;
  }

  public int getLibraryId() {
    return myLibraryId;
  }

  public int getOffset() {
    return myOffset;
  }

  public int getLine() {
    return myLine;
  }

  public void setLine(int line) {
    myLine = line;
  }

  @Override
  public XSourcePosition getSourcePosition() {
    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(myFileUrl);
    return virtualFile != null && myLine >= 0 ? XSourcePositionImpl.create(virtualFile, myLine) : null;
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
    final XValueChildrenList childrenList = new XValueChildrenList(myLocals.size() + 1);
    for (DartVMValue local : myLocals) {
      childrenList.add(local.getName(), local);
    }
    childrenList.add("library", new DartLibraryValue(myDebugProcess, myLibraryId));
    node.addChildren(childrenList, true);
  }

  @Override
  public void customizePresentation(ColoredTextContainer component) {
    XSourcePosition position = getSourcePosition();
    component.append(myFunctionName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);

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
