package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.XSourcePosition;
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

  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private String myIsolateId;
  @NotNull private final InstanceRef myInstanceRef;
  @Nullable private final FieldRef myFieldRef;
  private final boolean myIsException;

  private Ref<Integer> myCollectionChildrenAlreadyShown = new Ref<Integer>(0);

  public DartVmServiceValue(@NotNull final DartVmServiceDebugProcess debugProcess,
                            @NotNull final String isolateId,
                            @NotNull final String name,
                            @NotNull final InstanceRef instanceRef,
                            @Nullable final FieldRef fieldRef,
                            boolean isException) {
    super(name);
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myInstanceRef = instanceRef;
    myFieldRef = fieldRef;
    myIsException = isException;
  }

  @Override
  public boolean canNavigateToSource() {
    return myFieldRef != null;
  }

  @Override
  public void computeSourcePosition(@NotNull final XNavigatable navigatable) {
    if (myFieldRef == null) {
      navigatable.setSourcePosition(null);
      return;
    }

    doComputeSourcePosition(myDebugProcess, navigatable, myIsolateId, myFieldRef);
  }

  static void doComputeSourcePosition(@NotNull final DartVmServiceDebugProcess debugProcess,
                                      @NotNull final XNavigatable navigatable,
                                      @NotNull final String isolateId,
                                      @NotNull final FieldRef fieldRef) {
    debugProcess.getVmServiceWrapper().getObject(isolateId, fieldRef.getId(), new GetObjectConsumer() {
      @Override
      public void received(final Obj field) {
        reportSourcePosition(debugProcess, navigatable, isolateId, ((Field)field).getLocation());
      }

      @Override
      public void received(final Sentinel sentinel) {
        navigatable.setSourcePosition(null);
      }

      @Override
      public void onError(final RPCError error) {
        navigatable.setSourcePosition(null);
      }
    });
  }

  @Override
  public boolean canNavigateToTypeSource() {
    return true;
  }

  @Override
  public void computeTypeSourcePosition(@NotNull final XNavigatable navigatable) {
    myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, myInstanceRef.getClassRef().getId(), new GetObjectConsumer() {
      @Override
      public void received(final Obj classObj) {
        reportSourcePosition(myDebugProcess, navigatable, myIsolateId, ((ClassObj)classObj).getLocation());
      }

      @Override
      public void received(final Sentinel response) {
        navigatable.setSourcePosition(null);
      }

      @Override
      public void onError(final RPCError error) {
        navigatable.setSourcePosition(null);
      }
    });
  }

  private static void reportSourcePosition(@NotNull final DartVmServiceDebugProcess debugProcess,
                                           @NotNull final XNavigatable navigatable,
                                           @NotNull final String isolateId,
                                           @Nullable final SourceLocation location) {
    if (location == null) {
      navigatable.setSourcePosition(null);
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        final XSourcePosition sourcePosition = debugProcess.getSourcePosition(isolateId, location.getScript(), location.getTokenPos());
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {
            navigatable.setSourcePosition(sourcePosition);
          }
        });
      }
    });
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

  private boolean computeVarHavingStringValuePresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
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
        final String presentableValue = StringUtil.replace(instanceRef.getValueAsString(), "\"", "\\\"");
        node.setPresentation(AllIcons.Debugger.Db_primitive, new XStringValuePresentation(presentableValue), false);

        if (instanceRef.getValueAsStringIsTruncated()) {
          addFullStringValueEvaluator(node, instanceRef);
        }
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

  private void addFullStringValueEvaluator(@NotNull final XValueNode node, @NotNull final InstanceRef stringInstanceRef) {
    assert stringInstanceRef.getKind() == InstanceKind.String : stringInstanceRef;
    node.setFullValueEvaluator(new XFullValueEvaluator() {
      @Override
      public void startEvaluation(@NotNull final XFullValueEvaluationCallback callback) {
        myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, stringInstanceRef.getId(), new GetObjectConsumer() {
          @Override
          public void received(Obj instance) {
            assert instance instanceof Instance && ((Instance)instance).getKind() == InstanceKind.String : instance;
            callback.evaluated(((Instance)instance).getValueAsString());
          }

          @Override
          public void received(Sentinel response) {
            callback.errorOccurred(response.getValueAsString());
          }

          @Override
          public void onError(RPCError error) {
            callback.errorOccurred(error.getMessage());
          }
        });
      }
    });
  }

  private boolean computeRegExpPresentation(@NotNull final XValueNode node, @NotNull final InstanceRef instanceRef) {
    if (instanceRef.getKind() == InstanceKind.RegExp) {
      // The pattern is always an instance of kind String.
      final InstanceRef pattern = instanceRef.getPattern();
      assert pattern.getKind() == InstanceKind.String : pattern;

      final String patternString = StringUtil.replace(pattern.getValueAsString(), "\"", "\\\"");
      node.setPresentation(AllIcons.Debugger.Value, new XStringValuePresentation(patternString) {
        @Nullable
        @Override
        public String getType() {
          return instanceRef.getClassRef().getName();
        }
      }, true);

      if (pattern.getValueAsStringIsTruncated()) {
        addFullStringValueEvaluator(node, pattern);
      }

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

    if (myInstanceRef.getJson().get("id") == null) {
      int i = 0;
    }

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

    if ((isListKind(myInstanceRef.getKind()) || myInstanceRef.getKind() == InstanceKind.Map)) {
      computeCollectionChildren(node);
    }
    else {
      myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, myInstanceRef.getId(), new GetObjectConsumer() {
        @Override
        public void received(Obj instance) {
          addFields(myDebugProcess, node, myIsolateId, ((Instance)instance).getFields());
          node.addChildren(XValueChildrenList.EMPTY, true);
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
  }

  private void computeCollectionChildren(@NotNull final XCompositeNode node) {
    final int offset = myCollectionChildrenAlreadyShown.get();
    final int count = Math.min(myInstanceRef.getLength() - offset, XCompositeNode.MAX_CHILDREN_TO_SHOW);

    myDebugProcess.getVmServiceWrapper().getCollectionObject(myIsolateId, myInstanceRef.getId(), offset, count, new GetObjectConsumer() {
      @Override
      public void received(Obj instance) {
        if (isListKind(myInstanceRef.getKind())) {
          addListChildren(node, ((Instance)instance).getElements());
        }
        else if (myInstanceRef.getKind() == InstanceKind.Map) {
          addMapChildren(node, ((Instance)instance).getAssociations());
        }
        else {
          assert false : myInstanceRef.getKind();
        }

        myCollectionChildrenAlreadyShown.set(myCollectionChildrenAlreadyShown.get() + count);

        if (offset + count < myInstanceRef.getLength()) {
          node.tooManyChildren(myInstanceRef.getLength() - offset - count);
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
    int index = myCollectionChildrenAlreadyShown.get();
    for (InstanceRef listElement : listElements) {
      childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, String.valueOf(index++), listElement, null, false));
    }
    node.addChildren(childrenList, true);
  }

  private void addMapChildren(@NotNull final XCompositeNode node, @NotNull final ElementList<MapAssociation> mapAssociations) {
    final XValueChildrenList childrenList = new XValueChildrenList(mapAssociations.size());
    int index = myCollectionChildrenAlreadyShown.get();
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
          final DartVmServiceValue key = new DartVmServiceValue(myDebugProcess, myIsolateId, "key", keyInstanceRef, null, false);
          final DartVmServiceValue value = new DartVmServiceValue(myDebugProcess, myIsolateId, "value", valueInstanceRef, null, false);
          node.addChildren(XValueChildrenList.singleton(key), false);
          node.addChildren(XValueChildrenList.singleton(value), true);
        }
      });
    }

    node.addChildren(childrenList, true);
  }

  static void addFields(@NotNull final DartVmServiceDebugProcess debugProcess,
                        @NotNull final XCompositeNode node,
                        @NotNull String isolateId,
                        @NotNull final ElementList<BoundField> fields) {
    final XValueChildrenList childrenList = new XValueChildrenList(fields.size());
    for (BoundField field : fields) {
      final InstanceRef value = field.getValue();
      if (value != null) {
        childrenList.add(new DartVmServiceValue(debugProcess, isolateId, field.getDecl().getName(), value, field.getDecl(), false));
      }
    }
    node.addChildren(childrenList, false);
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
