package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.DebuggerUtils;
import com.jetbrains.lang.dart.ide.runner.server.google.VmValue;
import org.jetbrains.annotations.NotNull;

class DartMapEntryValue extends XNamedValue {
  @NotNull private final DartCommandLineDebugProcess myDebugProcess;
  @NotNull private final VmValue myKey;
  @NotNull private final VmValue myValue;

  protected DartMapEntryValue(@NotNull final DartCommandLineDebugProcess debugProcess,
                              @NotNull final String nodeName,
                              @NotNull final VmValue mapKey,
                              @NotNull final VmValue mapValue) {
    super(nodeName);
    myDebugProcess = debugProcess;
    myKey = mapKey;
    myValue = mapValue;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    final String text = getShortText(myKey) + " -> " + getShortText(myValue);
    node.setPresentation(AllIcons.Debugger.Value, "Map entry", text, true);
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    node.addChildren(XValueChildrenList.singleton(new DartValue(myDebugProcess, "key", myKey, false)), false);
    node.addChildren(XValueChildrenList.singleton(new DartValue(myDebugProcess, "value", myValue, false)), true);
  }

  @NotNull
  private static String getShortText(@NotNull final VmValue vmValue) {
    if (vmValue.isNull()) return "null";
    if (vmValue.isPrimitive()) return vmValue.getText();
    if (vmValue.isFunction()) return "function";
    if (vmValue.isList()) return "List";

    final String text = StringUtil.notNullize(vmValue.getText(), "null");
    if (text.startsWith(DartValue.OBJECT_OF_TYPE_PREFIX)) return text.substring(DartValue.OBJECT_OF_TYPE_PREFIX.length());

    return DebuggerUtils.demangleVmName(vmValue.getKind());
  }
}
