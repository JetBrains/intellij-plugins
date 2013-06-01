package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.google.gson.JsonObject;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartLibraryValue extends XValue {

  private final DartCommandLineDebugProcess myDebugProcess;
  private final int myLibraryId;
  private final List<DartVMValue> myGlobals = new ArrayList<DartVMValue>();

  public DartLibraryValue(DartCommandLineDebugProcess debugProcess, int libraryId) {
    myDebugProcess = debugProcess;
    myLibraryId = libraryId;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull XValuePlace place) {
    myDebugProcess.sendCommand(getGetPropertiesCommand(), new AbstractResponseToRequestHandler<JsonResponse>() {
      @Override
      public boolean processResponse(JsonResponse response) {
        JsonObject result = response.getJsonObject().getAsJsonObject("result");
        if (result != null) {
          myGlobals.clear();
          myGlobals.addAll(DartVMValue.fromJson(myDebugProcess, result.getAsJsonArray("globals")));
        }
        node.setPresentation(icons.DartIcons.Dart_16, "library", Integer.toString(myLibraryId), !myGlobals.isEmpty());
        return true;
      }
    });
  }

  private JsonObject getGetPropertiesCommand() {
    final JsonObject result = new JsonObject();
    result.addProperty("command", "getGlobalVariables");
    final JsonObject params = new JsonObject();
    params.addProperty("libraryId", myLibraryId);
    result.add("params", params);
    return result;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    final XValueChildrenList childrenList = new XValueChildrenList(myGlobals.size());
    for (DartVMValue field : myGlobals) {
      childrenList.add(field.getName(), field);
    }
    node.addChildren(childrenList, true);
  }
}
