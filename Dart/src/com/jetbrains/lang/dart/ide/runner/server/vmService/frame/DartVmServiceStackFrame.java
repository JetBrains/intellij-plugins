// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsSafe;
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
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.consumer.GetObjectConsumer;
import org.dartlang.vm.service.element.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartVmServiceStackFrame extends XStackFrame {

  private final @NotNull DartVmServiceDebugProcess myDebugProcess;
  private final @NotNull String myIsolateId;
  private final @NotNull Frame myVmFrame;
  private final @Nullable InstanceRef myException;
  private final @Nullable XSourcePosition mySourcePosition;
  private final @Nullable List<Frame> myVmFrames;
  private boolean myIsDroppableFrame;

  public DartVmServiceStackFrame(@NotNull DartVmServiceDebugProcess debugProcess,
                                 @NotNull String isolateId,
                                 @NotNull Frame vmFrame,
                                 @Nullable List<Frame> vmFrames,
                                 @Nullable InstanceRef exception) {
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

  public @NotNull String getIsolateId() {
    return myIsolateId;
  }

  @Override
  public @Nullable XSourcePosition getSourcePosition() {
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
  public void customizePresentation(@NotNull ColoredTextContainer component) {
    final CodeRef code = myVmFrame.getCode();

    String name;
    if (code != null) {
      // trim specific prefix and setter postfix
      @NonNls String unoptimizedPrefix = "[Unoptimized] ";
      @NlsSafe String codeName = code.getName();
      name = StringUtil.trimStart(StringUtil.trimEnd(codeName, "="), unoptimizedPrefix);
    }
    else {
      name = DartBundle.message("debugger.unnamed.frame");
    }

    final boolean causal = myVmFrame.getKind() == FrameKind.AsyncCausal;
    component.append(name, causal ? SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);

    if (mySourcePosition != null) {
      final String text = " (" + mySourcePosition.getFile().getName() + ":" + (mySourcePosition.getLine() + 1) + ")";
      component.append(text, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

    component.setIcon(AllIcons.Debugger.Frame);
  }

  @Override
  public @Nullable Object getEqualityObject() {
    SourceLocation location = myVmFrame.getLocation();
    CodeRef code = myVmFrame.getCode();
    return location != null && code != null ? location.getScript().getId() + ":" + code.getId() : null;
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
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

  private void addStaticFieldsIfPresentAndThenAllVars(@NotNull XCompositeNode node,
                                                      @Nullable BoundVariable thisVar,
                                                      @NotNull ElementList<BoundVariable> vars) {
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
        addVars(node, vars);
      }

      @Override
      public void onError(RPCError error) {
        addVars(node, vars);
      }
    });
  }

  private void addVars(@NotNull XCompositeNode node, @NotNull ElementList<BoundVariable> vars) {
    final XValueChildrenList childrenList = new XValueChildrenList(vars.size());

    for (BoundVariable var : vars) {
      final Object value = var.getValue();
      if (value instanceof InstanceRef) {
        final InstanceRef instanceRef = (InstanceRef)value;
        final SourceLocation location = myVmFrame.getLocation();
        final DartVmServiceValue.LocalVarSourceLocation varLocation =
          "this".equals(var.getName()) || location == null
          ? null
          : new DartVmServiceValue.LocalVarSourceLocation(location.getScript(), var.getDeclarationTokenPos());
        childrenList.add(new DartVmServiceValue(myDebugProcess, myIsolateId, var.getName(), instanceRef, varLocation, null, false));
      }
    }

    node.addChildren(childrenList, true);
  }

  @Override
  public @Nullable XDebuggerEvaluator getEvaluator() {
    // Enable Expression evaluation for all run configurations except webdev run instances, until supported, progress tracked here:
    // https://github.com/dart-lang/webdev/issues/715
    if (!myDebugProcess.isWebdevDebug()) {
      return new DartVmServiceEvaluatorInFrame(myDebugProcess, myIsolateId, myVmFrame);
    }
    return null;
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
