package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartStackFrame extends XStackFrame {
  private final DartCommandLineDebugProcess myDebugProcess;
  private final String myFunctionName;
  private final String myFileUrl;
  private final int myLine;
  private final int myLibraryId;
  private final List<DartVMValue> myLocals;

  public static List<DartStackFrame> fromJson(DartCommandLineDebugProcess debugProcess, JsonArray callFrames) {
    final List<DartStackFrame> result = new ArrayList<DartStackFrame>(callFrames.size());
    for (int i = 0; i < callFrames.size(); ++i) {
      result.add(fromJson(debugProcess, callFrames.get(i).getAsJsonObject()));
    }
    return result;
  }

  public static DartStackFrame fromJson(DartCommandLineDebugProcess debugProcess, JsonObject object) {
    final String functionName = object.get("functionName").getAsString();
    JsonObject location = object.get("location").getAsJsonObject();
    final String url = location.get("url").getAsString();
    final int line = location.get("lineNumber").getAsInt() - 1;
    final int libraryId = object.get("libraryId").getAsInt();
    return new DartStackFrame(debugProcess, functionName, url, line, libraryId,
                              DartVMValue.fromJson(debugProcess, object.getAsJsonArray("locals")));
  }

  protected DartStackFrame(DartCommandLineDebugProcess debugProcess,
                           String functionName,
                           String url,
                           int line,
                           int libraryId,
                           List<DartVMValue> locals) {
    myDebugProcess = debugProcess;
    myFunctionName = functionName;
    myFileUrl = url;
    myLine = line;
    myLibraryId = libraryId;
    myLocals = locals;
  }

  public String getFileUrl() {
    return myFileUrl;
  }

  @Override
  public XSourcePosition getSourcePosition() {
    return XSourcePositionImpl.create(VirtualFileManager.getInstance().findFileByUrl(myFileUrl), myLine);
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
  public void customizePresentation(SimpleColoredComponent component) {
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
