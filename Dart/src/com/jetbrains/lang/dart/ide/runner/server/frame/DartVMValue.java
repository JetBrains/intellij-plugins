package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.util.io.socketConnection.AbstractResponseToRequestHandler;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.connection.JsonResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DartVMValue extends XValue {
  private final String myName;
  private final int myObjectId;
  private final String myKind;
  @Nullable
  private final String myValue;
  private final DartCommandLineDebugProcess myDebugProcess;
  private final List<DartVMValue> myFields = new ArrayList<DartVMValue>();

  protected DartVMValue(DartCommandLineDebugProcess debugProcess, String name, int objectId, String kind, @Nullable String value) {
    myDebugProcess = debugProcess;
    myName = name;
    myObjectId = objectId;
    myKind = kind;
    myValue = value;
  }

  public static List<DartVMValue> fromJson(DartCommandLineDebugProcess debugProcess, JsonArray locals) {
    final List<DartVMValue> result = new ArrayList<DartVMValue>(locals.size());
    for (int i = 0; i < locals.size(); ++i) {
      result.add(fromJson(debugProcess, locals.get(i).getAsJsonObject()));
    }
    return result;
  }

  private static DartVMValue fromJson(DartCommandLineDebugProcess debugProcess, JsonObject object) {
    String name = object.get("name").getAsString();
    JsonObject value = object.getAsJsonObject("value");
    int objectId = value.get("objectId").getAsInt();
    String kind = value.get("kind").getAsString();
    JsonElement textValue = value.get("text");
    return new DartVMValue(debugProcess, name, objectId, kind, textValue.isJsonNull() ? null : textValue.getAsString());
  }

  public String getName() {
    return myName;
  }

  public int getObjectId() {
    return myObjectId;
  }

  public String getKind() {
    return myKind;
  }

  @Nullable
  public String getValue() {
    return myValue;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull XValuePlace place) {
    myDebugProcess.sendCommand(getGetPropertiesCommand(), new AbstractResponseToRequestHandler<JsonResponse>() {
      @Override
      public boolean processResponse(JsonResponse response) {
        JsonObject result = response.getJsonObject().getAsJsonObject("result");
        if (result != null) {
          myFields.clear();
          myFields.addAll(DartVMValue.fromJson(myDebugProcess, result.getAsJsonArray("fields")));
        }
        final String value = myValue == null ? "null" : myValue;
        node.setPresentation(getIcon(), myKind, isString() ? "\"" + value + "\"" : value, !myFields.isEmpty());
        return true;
      }
    });
  }

  private boolean isString() {
    return "string".equals(myKind);
  }

  private Icon getIcon() {
    if ("list".equals(myKind)) {
      return AllIcons.Debugger.Db_array;
    }
    else if ("string".equals(myKind) || "integer".equals(myKind)) {
      return AllIcons.Debugger.Db_primitive;
    }
    return AllIcons.Debugger.Value;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    final XValueChildrenList childrenList = new XValueChildrenList(myFields.size());
    for (DartVMValue field : myFields) {
      childrenList.add(field.getName(), field);
    }
    node.addChildren(childrenList, true);
  }

  private JsonObject getGetPropertiesCommand() {
    final JsonObject result = new JsonObject();
    result.addProperty("command", "getObjectProperties");
    final JsonObject params = new JsonObject();
    params.addProperty("objectId", myObjectId);
    result.add("params", params);
    return result;
  }
}
