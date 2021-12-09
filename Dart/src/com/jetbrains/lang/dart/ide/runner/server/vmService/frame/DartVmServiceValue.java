// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.LayeredIcon;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XKeywordValuePresentation;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.vmService.VmServiceConsumers;
import org.dartlang.vm.service.consumer.GetObjectConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

// TODO: implement some combination of XValue.getEvaluationExpression() /
// XValue.calculateEvaluationExpression() in order to support evaluate expression in variable values.
// See https://youtrack.jetbrains.com/issue/WEB-17629.

public class DartVmServiceValue extends XNamedValue {

  private static final LayeredIcon FINAL_FIELD_ICON = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.FinalMark);
  private static final LayeredIcon STATIC_FIELD_ICON = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark);
  private static final LayeredIcon STATIC_FINAL_FIELD_ICON =
    new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark, AllIcons.Nodes.FinalMark);

  private final @NotNull DartVmServiceDebugProcess myDebugProcess;
  private final @NotNull String myIsolateId;
  private final @NotNull InstanceRef myInstanceRef;
  private final @Nullable LocalVarSourceLocation myLocalVarSourceLocation;
  private final @Nullable FieldRef myFieldRef;
  private final boolean myIsException;

  public DartVmServiceValue(@NotNull DartVmServiceDebugProcess debugProcess,
                            @NotNull String isolateId,
                            @NotNull String name,
                            @NotNull InstanceRef instanceRef,
                            @Nullable LocalVarSourceLocation localVarSourceLocation,
                            @Nullable FieldRef fieldRef,
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
  public void computeSourcePosition(@NotNull XNavigatable navigatable) {
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

  static void doComputeSourcePosition(@NotNull DartVmServiceDebugProcess debugProcess,
                                      @NotNull XNavigatable navigatable,
                                      @NotNull String isolateId,
                                      @NotNull FieldRef fieldRef) {
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
  public void computeTypeSourcePosition(@NotNull XNavigatable navigatable) {
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

  private static void reportSourcePosition(@NotNull DartVmServiceDebugProcess debugProcess,
                                           @NotNull XNavigatable navigatable,
                                           @NotNull String isolateId,
                                           @Nullable ScriptRef script,
                                           int tokenPos) {
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
  public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
    if (computeVarHavingStringValuePresentation(node)) return;
    if (computeRegExpPresentation(node)) return;
    if (computeMapPresentation(node)) return;
    if (computeListPresentation(node)) return;

    // computeDefaultPresentation is called internally here when no result is got.
    // The reason for this is that the async method used cannot be properly waited.
    computeDefaultPresentation(node);

    // todo handle other special kinds: Type, TypeParameter, Pattern, may be some others as well
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

  private boolean computeVarHavingStringValuePresentation(@NotNull XValueNode node) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    switch (myInstanceRef.getKind()) {
      case Null:
      case Bool:
        node.setPresentation(getIcon(), new XKeywordValuePresentation(Objects.requireNonNull(myInstanceRef.getValueAsString())), false);
        break;
      case Double:
      case Int:
        node.setPresentation(getIcon(), new XNumericValuePresentation(myInstanceRef.getValueAsString()), false);
        break;
      case String:
        final String presentableValue = StringUtil.replace(Objects.requireNonNull(myInstanceRef.getValueAsString()), "\"", "\\\"");
        node.setPresentation(getIcon(), new XStringValuePresentation(presentableValue), false);

        if (myInstanceRef.getValueAsStringIsTruncated()) {
          addFullStringValueEvaluator(node, myInstanceRef);
        }
        break;
      case StackTrace:
        node.setFullValueEvaluator(new ImmediateFullValueEvaluator(DartBundle.message("debugger.link.see.stack.trace"),
                                                                   Objects.requireNonNull(myInstanceRef.getValueAsString())));
        node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), "", true);
        break;
      default:
        return false;
    }
    return true;
  }

  private void addFullStringValueEvaluator(@NotNull XValueNode node, @NotNull InstanceRef stringInstanceRef) {
    assert stringInstanceRef.getKind() == InstanceKind.String : stringInstanceRef;
    node.setFullValueEvaluator(new XFullValueEvaluator() {
      @Override
      public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
        myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, stringInstanceRef.getId(), new GetObjectConsumer() {
          @Override
          public void received(Obj instance) {
            assert instance instanceof Instance && ((Instance)instance).getKind() == InstanceKind.String : instance;
            callback.evaluated(Objects.requireNonNull(((Instance)instance).getValueAsString()));
          }

          @Override
          public void received(Sentinel response) {
            @NlsSafe String message = response.getValueAsString();
            callback.errorOccurred(message);
          }

          @Override
          public void onError(RPCError error) {
            @NlsSafe String message = error.getMessage();
            callback.errorOccurred(message);
          }
        });
      }
    });
  }

  private boolean computeRegExpPresentation(@NotNull XValueNode node) {
    if (myInstanceRef.getKind() == InstanceKind.RegExp) {
      // The pattern is always an instance of kind String.
      final InstanceRef pattern = Objects.requireNonNull(myInstanceRef.getPattern());
      assert pattern.getKind() == InstanceKind.String : pattern;

      final String patternString = StringUtil.replace(Objects.requireNonNull(pattern.getValueAsString()), "\"", "\\\"");
      node.setPresentation(getIcon(), new XStringValuePresentation(patternString) {
        @Override
        public @Nullable String getType() {
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

  private boolean computeMapPresentation(@NotNull XValueNode node) {
    if (myInstanceRef.getKind() == InstanceKind.Map) {
      final String value = "size = " + myInstanceRef.getLength();
      node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), value, myInstanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private boolean computeListPresentation(@NotNull XValueNode node) {
    if (isListKind(myInstanceRef.getKind())) {
      final String value = "size = " + myInstanceRef.getLength();
      node.setPresentation(getIcon(), myInstanceRef.getClassRef().getName(), value, myInstanceRef.getLength() > 0);
      return true;
    }
    return false;
  }

  private void computeDefaultPresentation(@NotNull XValueNode node) {
    final String typeName = myInstanceRef.getClassRef().getName();

    InstanceKind kind = myInstanceRef.getKind();
    // other InstanceKinds that can't have children don't reach this method.
    boolean canHaveChildren = kind != InstanceKind.Int32x4 && kind != InstanceKind.Float32x4 && kind != InstanceKind.Float64x2;

    // Check if the string value is populated.
    if (myInstanceRef.getValueAsString() != null && !myInstanceRef.getValueAsStringIsTruncated()) {
      node.setPresentation(getIcon(), typeName, myInstanceRef.getValueAsString(), canHaveChildren);
      return;
    }

    myDebugProcess.getVmServiceWrapper().callToString(myIsolateId, myInstanceRef.getId(), new VmServiceConsumers.InvokeConsumerWrapper() {
      @Override
      public void received(final InstanceRef toStringInstanceRef) {
        if (toStringInstanceRef.getKind() == InstanceKind.String) {
          final String value = Objects.requireNonNull(toStringInstanceRef.getValueAsString());
          // We don't need to show the default implementation of toString() ("Instance of ...").
          if (value.startsWith("Instance of ")) {
            node.setPresentation(getIcon(), typeName, "", canHaveChildren);
          }
          else {
            node.setPresentation(getIcon(), typeName, value, canHaveChildren);
          }
        }
        else {
          presentationFallback(node);
        }
      }

      @Override
      public void noGoodResult() {
        presentationFallback(node);
      }

      private void presentationFallback(@NotNull XValueNode node) {
        if (myInstanceRef.getValueAsString() != null) {
          node.setPresentation(
            getIcon(),
            typeName,
            myInstanceRef.getValueAsString() + (myInstanceRef.getValueAsStringIsTruncated() ? "..." : ""),
            true);
        }
        else {
          node.setPresentation(getIcon(), typeName, "", true);
        }
      }
    });
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
    if (myInstanceRef.getKind() == InstanceKind.Null) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    if ((isListKind(myInstanceRef.getKind()) || myInstanceRef.getKind() == InstanceKind.Map)) {
      computeCollectionChildren(myInstanceRef, 0, node);
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

  private void computeCollectionChildren(@NotNull InstanceRef instanceRef, int offset, @NotNull XCompositeNode node) {
    final int count = Math.min(instanceRef.getLength() - offset, XCompositeNode.MAX_CHILDREN_TO_SHOW);

    myDebugProcess.getVmServiceWrapper().getCollectionObject(myIsolateId, instanceRef.getId(), offset, count, new GetObjectConsumer() {
      @Override
      public void received(Obj instance) {
        if (isListKind(instanceRef.getKind())) {
          addListChildren(offset, node, ((Instance)instance));
        }
        else if (instanceRef.getKind() == InstanceKind.Map) {
          addMapChildren(offset, node, Objects.requireNonNull(((Instance)instance).getAssociations()));
        }
        else {
          assert false : instanceRef.getKind();
        }

        if (offset + count < instanceRef.getLength()) {
          node.tooManyChildren(instanceRef.getLength() - offset - count,
                               () -> computeCollectionChildren(instanceRef, offset + count, node));
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

  private void addListChildren(int offset, @NotNull XCompositeNode node, @NotNull Instance instance) {
    ElementList<InstanceRef> listElementsRef = instance.getElements();
    if (listElementsRef != null) {
      final XValueChildrenList childrenList = new XValueChildrenList(listElementsRef.size());
      int index = offset;
      for (InstanceRef listElement : listElementsRef) {
        childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, String.valueOf(index++), listElement, null, null, false));
      }
      node.addChildren(childrenList, true);
      return;
    }

    if (instance.getKind() == InstanceKind.List) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    // Show contents of special lists using toList() method
    myDebugProcess.getVmServiceWrapper().callToList(myIsolateId, instance.getId(), new VmServiceConsumers.InvokeConsumerWrapper() {
      @Override
      public void received(InstanceRef toListInstanceRef) {
        if (toListInstanceRef.getKind() == InstanceKind.List) {
          computeCollectionChildren(toListInstanceRef, offset, node);
        }
        else {
          node.addChildren(XValueChildrenList.EMPTY, true);
        }
      }

      @Override
      public void noGoodResult() {
        node.addChildren(XValueChildrenList.EMPTY, true);
      }
    });
  }

  private void addMapChildren(int offset, @NotNull XCompositeNode node, @NotNull ElementList<MapAssociation> mapAssociations) {
    final XValueChildrenList childrenList = new XValueChildrenList(mapAssociations.size());
    int index = offset;
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

  private void addFields(@NotNull XCompositeNode node, @Nullable ElementList<BoundField> fields) {
    if (fields == null) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

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

  private static @NotNull String getShortPresentableValue(@NotNull InstanceRef instanceRef) {
    // getValueAsString() is provided for the instance kinds: Null, Bool, Double, Int, String (value may be truncated), Float32x4, Float64x2, Int32x4, StackTrace
    switch (instanceRef.getKind()) {
      case String:
        String string = Objects.requireNonNull(instanceRef.getValueAsString());
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
        return Objects.requireNonNull(instanceRef.getValueAsString());
      default:
        return "[" + instanceRef.getClassRef().getName() + "]";
    }
  }

  private static boolean isListKind(@NotNull InstanceKind kind) {
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

  public @NotNull InstanceRef getInstanceRef() {
    return myInstanceRef;
  }

  static class LocalVarSourceLocation {
    private final @NotNull ScriptRef myScriptRef;
    private final int myTokenPos;

    LocalVarSourceLocation(@NotNull ScriptRef scriptRef, int tokenPos) {
      myScriptRef = scriptRef;
      myTokenPos = tokenPos;
    }
  }
}
