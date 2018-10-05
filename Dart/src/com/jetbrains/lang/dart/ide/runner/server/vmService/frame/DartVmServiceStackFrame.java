// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.SmartList;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.consumer.GetObjectConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartVmServiceStackFrame extends XStackFrame {

  @NotNull private final DartVmServiceDebugProcess myDebugProcess;
  @NotNull private final String myIsolateId;
  @NotNull private final Frame myVmFrame;
  @Nullable private final InstanceRef myException;
  @Nullable private final XSourcePosition mySourcePosition;
  @Nullable private final List<Frame> myVmFrames;
  private boolean myIsDroppableFrame;

  public DartVmServiceStackFrame(@NotNull final DartVmServiceDebugProcess debugProcess,
                                 @NotNull final String isolateId,
                                 @NotNull final Frame vmFrame,
                                 @Nullable List<Frame> vmFrames,
                                 @Nullable final InstanceRef exception) {
    myDebugProcess = debugProcess;
    myIsolateId = isolateId;
    myVmFrame = vmFrame;
    myVmFrames = vmFrames;
    myException = exception;
    if (vmFrame.getLocation() == null) {
      mySourcePosition = null;
    }
    else {
      mySourcePosition = debugProcess.getSourcePosition(isolateId, vmFrame.getLocation().getScript(), vmFrame.getLocation().getTokenPos());
    }
  }

  @NotNull
  public String getIsolateId() {
    return myIsolateId;
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    return mySourcePosition;
  }

  public int getFrameIndex() {
    return myVmFrames == null ? 0 : myVmFrames.indexOf(myVmFrame);
  }

  public void setIsDroppableFrame(boolean value) {
    myIsDroppableFrame = value;
  }

  private boolean isLastFrame() {
    if (myVmFrames == null) {
      return true;
    }
    return getFrameIndex() == (myVmFrames.size() - 1);
  }

  @Override
  public void customizePresentation(@NotNull final ColoredTextContainer component) {
    final String name = StringUtil.trimEnd(myVmFrame.getCode().getName(), "="); // trim setter postfix
    final boolean causal = myVmFrame.getKind() == FrameKind.AsyncCausal;
    component.append(name, causal ? SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);

    if (mySourcePosition != null) {
      final String text = " (" + mySourcePosition.getFile().getName() + ":" + (mySourcePosition.getLine() + 1) + ")";
      component.append(text, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

    component.setIcon(AllIcons.Debugger.Frame);
  }

  @NotNull
  @Override
  public Object getEqualityObject() {
    return myVmFrame.getLocation().getScript().getId() + ":" + myVmFrame.getCode().getId();
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    if (myException != null) {
      final DartVmServiceValue exception = new DartVmServiceValue(myDebugProcess, myIsolateId, "exception", myException, null, null, true);
      node.addChildren(XValueChildrenList.singleton(exception), false);
    }

    final ElementList<BoundVariable> vars = myVmFrame.getVars();

    if (vars == null) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }

    BoundVariable thisVar = null;
    for (BoundVariable var : vars) {
      if ("this".equals(var.getName())) {
        // in some cases "this" var is not the first one in the list, no idea why
        thisVar = var;
        break;
      }
    }

    addStaticFieldsIfPresentAndThenAllVars(node, thisVar, vars);
  }

  private void addStaticFieldsIfPresentAndThenAllVars(@NotNull final XCompositeNode node,
                                                      @Nullable final BoundVariable thisVar,
                                                      @NotNull final ElementList<BoundVariable> vars) {
    if (thisVar == null) {
      addVars(node, vars);
      return;
    }

    final Object thisVarValue = thisVar.getValue();
    if (!(thisVarValue instanceof InstanceRef)) {
      addVars(node, vars);
      return;
    }

    final ClassRef classRef = ((InstanceRef)thisVarValue).getClassRef();
    myDebugProcess.getVmServiceWrapper().getObject(myIsolateId, classRef.getId(), new GetObjectConsumer() {
      @Override
      public void received(Obj classObj) {
        final SmartList<FieldRef> staticFields = new SmartList<>();
        for (FieldRef fieldRef : ((ClassObj)classObj).getFields()) {
          if (fieldRef.isStatic()) {
            staticFields.add(fieldRef);
          }
        }

        if (!staticFields.isEmpty()) {
          final XValueChildrenList list = new XValueChildrenList();
          list.addTopGroup(new DartStaticFieldsGroup(myDebugProcess, myIsolateId, ((ClassObj)classObj).getName(), staticFields));
          node.addChildren(list, false);
        }

        addVars(node, vars);
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

  private void addVars(@NotNull final XCompositeNode node, @NotNull final ElementList<BoundVariable> vars) {
    final XValueChildrenList childrenList = new XValueChildrenList(vars.size());

    for (BoundVariable var : vars) {
      final Object value = var.getValue();
      if (value instanceof InstanceRef) {
        final InstanceRef instanceRef = (InstanceRef)value;
        final DartVmServiceValue.LocalVarSourceLocation varLocation =
          "this".equals(var.getName())
          ? null
          : new DartVmServiceValue.LocalVarSourceLocation(myVmFrame.getLocation().getScript(), var.getDeclarationTokenPos());
        childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, var.getName(), instanceRef, varLocation, null, false));
      }
    }

    node.addChildren(childrenList, true);
  }

  @Nullable
  @Override
  public XDebuggerEvaluator getEvaluator() {
    return new DartVmServiceEvaluatorInFrame(myDebugProcess, myIsolateId, myVmFrame);
  }

  public boolean isInDartSdkPatchFile() {
    return mySourcePosition != null && (mySourcePosition.getFile() instanceof LightVirtualFile);
  }

  public boolean canDrop() {
    return myIsDroppableFrame && !isLastFrame();
  }

  public void dropFrame() {
    myDebugProcess.dropFrame(this);
  }
}
