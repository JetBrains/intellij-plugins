// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.LayeredIcon;
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

// TODO: implement some combination of XValue.getEvaluationExpression() /
// XValue.calculateEvaluationExpression() in order to support evaluate expression in variable values.
// See https://youtrack.jetbrains.com/issue/WEB-17629.

public class DartVmServiceValue extends XNamedValue {

  private static final LayeredIcon FINAL_FIELD_ICON = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.FinalMark);
  private static final LayeredIcon STATIC_FIELD_ICON = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark);
  private static final LayeredIcon STATIC_FINAL_FIELD_ICON =
    new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark, AllIcons.Nodes.FinalMark);

  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final String myIsolateId;
  @NotNull private final InstanceRef myInstanceRef;
  @Nullable private final LocalVarSourceLocation myLocalVarSourceLocation;
  @Nullable private final FieldRef myFieldRef;
  private final boolean myIsException;

  private final Ref<Integer> myCollectionChildrenAlreadyShown = new Ref<>(0);

  public DartVmServiceValue(@NotNull final DartVmServiceDebugProcess debugProcess,
                            @NotNull final String isolateId,
                            @NotNull final String name,
                            @NotNull final InstanceRef instanceRef,
                            @Nullable final LocalVarSourceLocation localVarSourceLocation,
                            @Nullable final FieldRef fieldRef,
                            boolean isException) {
    super(name);
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myInstanceRef = instanceRef;
    myLocalVarSourceLocation = localVarSourceLocation;
    myFieldRef = fieldRef;
    myIsException = isException;
  }

  @Override
  public boolean canNavigateToSource() {
    return myLocalVarSourceLocation != null || myFieldRef != null;
  }

  @Override
  public void computeSourcePosition(@NotNull final XNavigatable navigatable) {
    if (myLocalVarSourceLocation != null) {
      reportSourcePosition(myDebugProcess, navigatable, myIsolateId, myLocalVarSourceLocation.myScriptRef,
                           myLocalVarSourceLocation.myTokenPos);
    }
    else if (myFieldRef != null) {
      doComputeSourcePosition(myDebugProcess, navigatable, myIsolateId, myFieldRef);
    }
    else {
      navigatable.setSourcePosition(null);
    }
  }

  static void doComputeSourcePosition(@NotNull final DartVmServiceDebugProcess debugProcess,
                                      @NotNull final XNavigatable navigatable,
                                      @NotNull final String isolateId,
                                      @NotNull final FieldRef fieldRef) {
    debugProcess.getVmServiceWrapper().getObject(isolateId, fieldRef.getId(), new GetObjectConsumer() {
      @Override
      public void received(final Obj field) {
        final SourceLocation location = ((Field)field).getLocation();
        reportSourcePosition(debugProcess, navigatable, isolateId,
                             location == null ? null : location.getScript(),
                             location == null ? -1 : location.getTokenPos());
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
        final SourceLocation location = ((ClassObj)classObj).getLocation();
        reportSourcePosition(myDebugProcess, navigatable, myIsolateId,
                             location == null ? null : location.getScript(),
                             location == null ? -1 : location.getTokenPos());
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
                                           @Nullable final ScriptRef script,
                                           final int tokenPos) {
    if (script == null || tokenPos <= 0) {
      navigatable.setSourcePosition(null);
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      final XSourcePosition sourcePosition = debugProcess.getSourcePosition(isolateId, script, tokenPos);
      ApplicationManager.getApplication().runReadAction(() -> navigatable.setSourcePosition(sourcePosition));
    });
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    if (computeVarHavingStringValuePresentation(node)) return;
    if (computeRegExpPresentation(node)) return;
    if (computeMapPresentation(node)) return;
    if (computeListPresentation(node)) return;

    // computeDefaultPresentation is called internally here when no result is got.
    // The reason for this is that the async method used cannot be properly waited.
    computeDebugPresentation(node);

    // todo handle other special kinds: Type, TypeParameter, Pattern, may be some others as well
  }

  private void computeDebugPresentation(final XValueNode node) {
    myDebugProcess.getVmServiceWrapper()
      .evaluateInTargetContext(myIsolateId, myInstanceRef.getId(), "toStringDeep()", new VmServiceConsumers.EvaluateConsumerWrapper() {
        @Override
        public void received(final InstanceRef toStringInstanceRef) {
          if (toStringInstanceRef.getKind() == InstanceKind.String) {
            String content = toStringInstanceRef.getValueAsString();
            int firstLineBreak = content.indexOf('\n');
            String summary = firstLineBreak < 0 ? content : content.substring(0, firstLineBreak);
            node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), summary, true);
            if (toStringInstanceRef.getValueAsStringIsTruncated()) {
              addFullStringValueEvaluator(node, toStringInstanceRef);
            }
            else if (firstLineBreak >= 0) {
              // Multi-line content. Display a View link to reveal full content.
              node.setFullValueEvaluator(new ImmediateFullValueEvaluator("...View", content));
            }
          }
          else {
            noGoodResult();
          }
        }

        @Override
        public void noGoodResult() {
          computeDefaultPresentation(node);
        }
      });
  }

  private Icon getIcon() {
    if (myIsException) return AllIcons.Debugger.Db_exception_breakpoint;

    if (myFieldRef != null) {
      if (myFieldRef.isStatic() && (myFieldRef.isFinal() || myFieldRef.isConst())) {
        return STATIC_FINAL_FIELD_ICON;
      }
      if (myFieldRef.isStatic()) {
        return STATIC_FIELD_ICON;
      }
      if (myFieldRef.isFinal() || myFieldRef.isConst()) {
        return FINAL_FIELD_ICON;
      }
      return AllIcons.Nodes.Field;
    }

    final InstanceKind kind = myInstanceRef.getKind();

    if (kind == InstanceKind.Map || isListKind(kind)) return AllIcons.Debugger.Db_array;

    if (kind == InstanceKind.Null ||
        kind == InstanceKind.Bool ||
        kind == InstanceKind.Double ||
        kind == InstanceKind.Int ||
        kind == InstanceKind.String) {
      return AllIcons.Debugger.Db_primitive;
    }

    return AllIcons.Debugger.Value;
  }

  private boolean computeVarHavingStringValuePresentation(@NotNull final XValueNode node) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    switch (myInstanceRef.getKind()) {
      case Null:
      case Bool:
        node.setPresentation(getIcon(), new XKeywordValuePresentation(myInstanceRef.getValueAsString()), false);
        break;
      case Double:
      case Int:
        node.setPresentation(getIcon(), new XNumericValuePresentation(myInstanceRef.getValueAsString()), false);
        break;
      case String:
        final String presentableValue = StringUtil.replace(myInstanceRef.getValueAsString(), "\"", "\\\"");
        node.setPresentation(getIcon(), new XStringValuePresentation(presentableValue), false);

        if (myInstanceRef.getValueAsStringIsTruncated()) {
          addFullStringValueEvaluator(node, myInstanceRef);
        }
        break;
      case Float32x4:
      case Float64x2:
      case Int32x4:
      case StackTrace:
        node.setFullValueEvaluator(new ImmediateFullValueEvaluator("Click to see stack trace...", myInstanceRef.getValueAsString()));
        node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), "", true);
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

  private boolean computeRegExpPresentation(@NotNull final XValueNode node) {
    if (myInstanceRef.getKind() == InstanceKind.RegExp) {
      // The pattern is always an instance of kind String.
      final InstanceRef pattern = myInstanceRef.getPattern();
      assert pattern.getKind() == InstanceKind.String : pattern;

      final String patternString = StringUtil.replace(pattern.getValueAsString(), "\"", "\\\"");
      node.setPresentation(getIcon(), new XStringValuePresentation(patternString) {
        @Nullable
        @Override
        public String getType() {
          return myInstanceRef.getClassRef().getName();
        }
      }, true);

      if (pattern.getValueAsStringIsTruncated()) {
        addFullStringValueEvaluator(node, pattern);
      }

      return true;
    }
    return false;
  }

  private boolean computeMapPresentation(@NotNull final XValueNode node) {
    if (myInstanceRef.getKind() == InstanceKind.Map) {
      final String value = "size = " + myInstanceRef.getLength();
      node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), value, myInstanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private boolean computeListPresentation(@NotNull final XValueNode node) {
    if (isListKind(myInstanceRef.getKind())) {
      final String value = "size = " + myInstanceRef.getLength();
      node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), value, myInstanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private void computeDefaultPresentation(@NotNull final XValueNode node) {
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
              node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), string, true);
            }
          }
          else {
            noGoodResult(); // unlikely possible
          }
        }

        @Override
        public void noGoodResult() {
          node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), "", true);
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
          addFields(node, ((Instance)instance).getFields());
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

  private void addListChildren(@NotNull final XCompositeNode node, @Nullable final ElementList<InstanceRef> listElements) {
    if (listElements == null) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    final XValueChildrenList childrenList = new XValueChildrenList(listElements.size());
    int index = myCollectionChildrenAlreadyShown.get();
    for (InstanceRef listElement : listElements) {
      childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, String.valueOf(index++), listElement, null, null, false));
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
          final DartVmServiceValue key = new DartVmServiceValue(myDebugProcess, myIsolateId, "key", keyInstanceRef, null, null, false);
          final DartVmServiceValue value =
            new DartVmServiceValue(myDebugProcess, myIsolateId, "value", valueInstanceRef, null, null, false);
          node.addChildren(XValueChildrenList.singleton(key), false);
          node.addChildren(XValueChildrenList.singleton(value), true);
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
        childrenList
          .add(new DartVmServiceValue(myDebugProcess, myIsolateId, field.getDecl().getName(), value, null, field.getDecl(), false));
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

  static class LocalVarSourceLocation {
    @NotNull private final ScriptRef myScriptRef;
    private final int myTokenPos;

    LocalVarSourceLocation(@NotNull final ScriptRef scriptRef, final int tokenPos) {
      myScriptRef = scriptRef;
      myTokenPos = tokenPos;
    }
  }
}
