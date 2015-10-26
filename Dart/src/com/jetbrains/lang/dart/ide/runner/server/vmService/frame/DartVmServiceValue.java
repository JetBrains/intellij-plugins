package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XKeywordValuePresentation;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.vmService.VmServiceConsumers;
import org.dartlang.vm.service.consumer.GetObjectConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartVmServiceValue extends XNamedValue {
  private static final int MAX_COLLECTION_ELEMENTS_TO_SHOW = 1000;

  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private String myIsolateId;
  @NotNull private final InstanceRef myInstanceRef;
  private final boolean myIsException;

  public DartVmServiceValue(@NotNull final DartVmServiceDebugProcess debugProcess,
                            @NotNull final String isolateId,
                            @NotNull final String name,
                            @NotNull final InstanceRef instanceRef) {
    this(debugProcess, isolateId, name, instanceRef, false);
  }

  public DartVmServiceValue(@NotNull final DartVmServiceDebugProcess debugProcess,
                            @NotNull final String isolateId,
                            @NotNull final String name,
                            @NotNull final InstanceRef instanceRef,
                            boolean isException) {
    super(name);
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myInstanceRef = instanceRef;
    myIsException = isException;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    if (computeVarHavingStringValuePresentation(node, myInstanceRef)) return;
    if (computeRegExpPresentation(node, myInstanceRef)) return;
    if (computeMapPresentation(node, myInstanceRef)) return;
    if (computeListPresentation(node, myInstanceRef)) return;
    computeDefaultPresentation(node);
    // todo handle other special kinds: Type, TypeParameter, Pattern, may be some others as well
  }

  private static boolean computeVarHavingStringValuePresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
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
        node.setPresentation(AllIcons.Debugger.Value, instanceRef.getClassRef().getName(), "", true);
        break;
      default:
        return false;
    }
    return true;
  }

  private static boolean computeRegExpPresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
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

  private static boolean computeMapPresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    // Map kind only
    if (instanceRef.getKind() == InstanceKind.Map) {
      final String value = "size = " + instanceRef.getLength();
      node.setPresentation(AllIcons.Debugger.Db_array, instanceRef.getClassRef().getName(), value, instanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private static boolean computeListPresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    if (isListKind(instanceRef.getKind())) {
      final String value = "size = " + instanceRef.getLength();
      node.setPresentation(AllIcons.Debugger.Db_array, instanceRef.getClassRef().getName(), value, instanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private void computeDefaultPresentation(@NotNull final XValueNode node) {
    final Icon icon = myIsException ? AllIcons.Debugger.Db_exception_breakpoint : AllIcons.Debugger.Value;
    myDebugProcess.getVmServiceWrapper()
      .evaluateInTargetContext(myIsolateId, myInstanceRef.getId(), "toString()", new VmServiceConsumers.EvaluateConsumerWrapper() {
        @Override
        public void received(final InstanceRef toStringInstanceRef) {
          if (toStringInstanceRef.getKind() == InstanceKind.String) {
            final String string = toStringInstanceRef.getValueAsString();
            // default toString() implementation returns "Instance of 'ClassName'" - no interest to show
            if (string.equals("Instance of '" + myInstanceRef.getClassRef().getName() + "'")) {
              noGoodResult();
            }
            else {
              node.setPresentation(icon, myInstanceRef.getClassRef().getName(), string, true);
            }
          }
          else {
            noGoodResult(); // unlikely possible
          }
        }

        @Override
        public void noGoodResult() {
          node.setPresentation(icon, myInstanceRef.getClassRef().getName(), "", true);
        }
      });
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    if (myInstanceRef.getKind() == InstanceKind.Null) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    if ((isListKind(myInstanceRef.getKind()) || myInstanceRef.getKind() == InstanceKind.Map) &&
        myInstanceRef.getLength() > MAX_COLLECTION_ELEMENTS_TO_SHOW) {
      node.setErrorMessage("Too many items");
      return;
    }

    myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, myInstanceRef.getId(), new GetObjectConsumer() {
      @Override
      public void received(Obj obj) {
        if (isListKind(myInstanceRef.getKind())) {
          addListChildren(node, ((Instance)obj).getElements());
        }
        else if (myInstanceRef.getKind() == InstanceKind.Map) {
          addMapChildren(node, ((Instance)obj).getAssociations());
        }
        else {
          addFields(node, ((Instance)obj).getFields());
        }
      }

      @Override
      public void received(Sentinel sentinel) {
        node.setErrorMessage(sentinel.getValueAsString());
      }

      @Override
      public void onError(RPCError error) {
        node.setErrorMessage(error.getMessage());
      }
    });
  }

  private void addListChildren(@NotNull final XCompositeNode node, @NotNull final ElementList<InstanceRef> listElements) {
    final XValueChildrenList childrenList = new XValueChildrenList(listElements.size());
    int index = 0;
    for (InstanceRef listElement : listElements) {
      childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, String.valueOf(index++), listElement));
    }
    node.addChildren(childrenList, true);
  }

  private void addMapChildren(@NotNull final XCompositeNode node, @NotNull final ElementList<MapAssociation> mapAssociations) {
    final XValueChildrenList childrenList = new XValueChildrenList(mapAssociations.size());
    int index = 0;
    for (MapAssociation mapAssociation : mapAssociations) {
      final InstanceRef keyInstanceRef = mapAssociation.getKey();
      final InstanceRef valueInstanceRef = mapAssociation.getValue();

      childrenList.add(String.valueOf(index++), new XValue() {
        @Override
        public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
          final String value = getShortPresentableValue(keyInstanceRef) + " -> " + getShortPresentableValue(valueInstanceRef);
          node.setPresentation(AllIcons.Debugger.Value, "map entry", value, true);
        }

        @Override
        public void computeChildren(@NotNull XCompositeNode node) {
          node.addChildren(XValueChildrenList.singleton(new DartVmServiceValue(myDebugProcess, myIsolateId, "key", keyInstanceRef)), false);
          node.addChildren(XValueChildrenList.singleton(new DartVmServiceValue(myDebugProcess, myIsolateId, "value", valueInstanceRef)),
                           true);
        }
      });
    }

    node.addChildren(childrenList, true);
  }

  private void addFields(@NotNull final XCompositeNode node, @NotNull final ElementList<BoundField> fields) {
    final XValueChildrenList childrenList = new XValueChildrenList(fields.size());
    for (BoundField field : fields) {
      final InstanceRef value = field.getValue();
      if (value != null) {
        childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, field.getDecl().getName(), value));
      }
    }
    node.addChildren(childrenList, true);
  }

  @NotNull
  private static String getShortPresentableValue(@NotNull final InstanceRef instanceRef) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    switch (instanceRef.getKind()) {
      case String:
        String string = instanceRef.getValueAsString();
        if (string.length() > 103) string = string.substring(0, 100) + "...";
        return "\"" + StringUtil.replace(string, "\"", "\\\"") + "\"";
      case Null:
      case Bool:
      case Double:
      case Int:
      case Float32x4:
      case Float64x2:
      case Int32x4:
        // case StackTrace:  getValueAsString() is too long for StackTrace
        return instanceRef.getValueAsString();
      default:
        return "[" + instanceRef.getClassRef().getName() + "]";
    }
  }

  private static boolean isListKind(@NotNull final InstanceKind kind) {
    // List, Uint8ClampedList, Uint8List, Uint16List, Uint32List, Uint64List, Int8List, Int16List, Int32List, Int64List, Float32List, Float64List, Int32x4List, Float32x4List, Float64x2List
    return kind == InstanceKind.List ||
           kind == InstanceKind.Uint8ClampedList ||
           kind == InstanceKind.Uint8List ||
           kind == InstanceKind.Uint16List ||
           kind == InstanceKind.Uint32List ||
           kind == InstanceKind.Uint64List ||
           kind == InstanceKind.Int8List ||
           kind == InstanceKind.Int16List ||
           kind == InstanceKind.Int32List ||
           kind == InstanceKind.Int64List ||
           kind == InstanceKind.Float32List ||
           kind == InstanceKind.Float64List ||
           kind == InstanceKind.Int32x4List ||
           kind == InstanceKind.Float32x4List ||
           kind == InstanceKind.Float64x2List;
  }

  @NotNull
  public InstanceRef getInstanceRef() {
    return myInstanceRef;
  }
}
