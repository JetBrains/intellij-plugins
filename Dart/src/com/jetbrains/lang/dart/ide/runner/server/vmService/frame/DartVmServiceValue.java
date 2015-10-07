package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.frame.ImmediateFullValueEvaluator;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XKeywordValuePresentation;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.BoundVariable;
import org.dartlang.vm.service.element.InstanceKind;
import org.dartlang.vm.service.element.InstanceRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartVmServiceValue extends XNamedValue {
  private final DartVmServiceDebugProcess myDebugProcess;
  private final InstanceRef myInstanceRef;

  public DartVmServiceValue(@NotNull final DartVmServiceDebugProcess debugProcess, @NotNull final BoundVariable var) {
    super(var.getName());
    myDebugProcess = debugProcess;
    myInstanceRef = var.getValue();
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    if (handleVarHavingStringValue(node, myInstanceRef)) return;
    if (handleRegExp(node, myInstanceRef)) return;
    if (handleMap(node, myInstanceRef)) return;
    if (handleList(node, myInstanceRef)) return;

    // todo handle other special kinds: Type, TypeParameter, Pattern, may be some others as well
    node.setPresentation(AllIcons.Debugger.Value, new XRegularValuePresentation("", myInstanceRef.getClassRef().getName()), true);
  }

  private static boolean handleVarHavingStringValue(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    switch (instanceRef.getKind()) {
      case Null:
      case Bool:
        node.setPresentation(AllIcons.Debugger.Db_primitive, new XKeywordValuePresentation(instanceRef.getValueAsString()), false);
        break;
      case Double:
      case Int:
        node.setPresentation(AllIcons.Debugger.Db_primitive, new XNumericValuePresentation(instanceRef.getValueAsString()), false);
        break;
      case String:
        final String suffix = instanceRef.getValueAsStringIsTruncated() ? "... (truncated value)" : "";
        final String presentableValue = StringUtil.replace(instanceRef.getValueAsString() + suffix, "\"", "\\\"");
        node.setPresentation(AllIcons.Debugger.Db_primitive, new XStringValuePresentation(presentableValue), false);
        break;
      case Float32x4:
      case Float64x2:
      case Int32x4:
      case StackTrace:
        node.setFullValueEvaluator(new ImmediateFullValueEvaluator("Click to see stack trace...", instanceRef.getValueAsString()));
        node.setPresentation(AllIcons.Debugger.Value,
                             new XRegularValuePresentation("", instanceRef.getClassRef().getName()), true);
        break;
      default:
        return false;
    }
    return true;
  }

  private static boolean handleRegExp(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    if (instanceRef.getKind() == InstanceKind.RegExp) {
      // The pattern is always an instance of kind String.
      final InstanceRef pattern = instanceRef.getPattern();
      final String suffix = pattern.getValueAsStringIsTruncated() ? "... (truncated value)" : "";
      final String patternString = StringUtil.replace(pattern.getValueAsString() + suffix, "\"", "\\\"");

      node.setPresentation(AllIcons.Debugger.Value, new XStringValuePresentation(patternString) {
        @Nullable
        @Override
        public String getType() {
          return instanceRef.getClassRef().getName();
        }
      }, true);
      return true;
    }
    return false;
  }

  private static boolean handleMap(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    // Map kind only
    if (instanceRef.getKind() == InstanceKind.Map) {
      final String value = "size = " + instanceRef.getLength();
      node.setPresentation(AllIcons.Debugger.Value, new XRegularValuePresentation(value, instanceRef.getClassRef().getName()), true);
      return true;
    }
    return false;
  }

  private static boolean handleList(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    // List, Uint8ClampedList, Uint8List, Uint16List, Uint32List, Uint64List, Int8List, Int16List, Int32List, Int64List, Float32List,
    // Float64List, Int32x4List, Float32x4List, Float64x2List
    switch (instanceRef.getKind()) {
      case List:
      case Uint8ClampedList:
      case Uint8List:
      case Uint16List:
      case Uint32List:
      case Uint64List:
      case Int8List:
      case Int16List:
      case Int32List:
      case Int64List:
      case Float32List:
      case Float64List:
      case Int32x4List:
      case Float32x4List:
      case Float64x2List:
        final String value = "size = " + instanceRef.getLength();
        node.setPresentation(AllIcons.Debugger.Value, new XRegularValuePresentation(value, instanceRef.getClassRef().getName()), true);
        break;
      default:
        return false;
    }
    return true;
  }
}
