package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.google.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

// todo navigate to source, type
public class DartValue extends XNamedValue {
  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private final @NotNull VmVariable myVmVariable;
  private @Nullable VmValue myVmValue;

  public DartValue(final @NotNull DartCommandLineDebugProcess debugProcess, final @NotNull VmVariable vmVariable) {
    super(StringUtil.notNullize(vmVariable.getName(), "<unknown>"));
    myDebugProcess = debugProcess;
    myVmVariable = vmVariable;
  }

  @Override
  public void computePresentation(final @NotNull XValueNode node, final @NotNull XValuePlace place) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        if (node.isObsolete()) return;

        myVmValue = myVmVariable.getValue();

        if (myVmValue == null) {
          node.setPresentation(AllIcons.Debugger.Value, null, "<no value>", false);
          return;
        }


        final String value = StringUtil.notNullize(myVmValue.getText(), "null");
        final XValuePresentation presentation;

        if (myVmValue.isString()) {
          presentation = new XStringValuePresentation(StringUtil.stripQuotesAroundValue(value));
        }
        else if (myVmValue.isNumber()) {
          presentation = new XNumericValuePresentation(value);
        }
        else {
          presentation = new XRegularValuePresentation(value, myVmValue.getKind());
        }

        final boolean neverHasChildren = myVmValue.isPrimitive() || myVmValue.isNull() || myVmValue.isFunction();
        node.setPresentation(getIcon(myVmValue), presentation, !neverHasChildren);
      }
    });
  }

  private static Icon getIcon(final @NotNull VmValue vmValue) {
    if (vmValue.isList()) return AllIcons.Debugger.Db_array;
    if (vmValue.isPrimitive()) return AllIcons.Debugger.Db_primitive;
    if (vmValue.isFunction()) return AllIcons.Nodes.Function;

    return AllIcons.Debugger.Value; // todo m.b. resolve and show corresponding icon?
  }

  @Override
  public void computeChildren(final @NotNull XCompositeNode node) {
    // myVmValue is already calculated in computePresentation()
    if (myVmValue == null) node.addChildren(XValueChildrenList.EMPTY, true);

    // see com.google.dart.tools.debug.core.server.ServerDebugValue#fillInFieldsSync()
    try {
      myDebugProcess.getVmConnection()
        .getObjectProperties(myVmValue.getIsolate(),
                             myVmValue.getObjectId(),
                             new VmCallback<VmObject>() {
                               @Override
                               public void handleResult(final VmResult<VmObject> result) {
                                 if (node.isObsolete()) return;

                                 final VmObject vmObject = result == null ? null : result.getResult();
                                 final List<VmVariable> fields = vmObject == null ? null : vmObject.getFields();

                                 if (fields == null || result.isError()) return;

                                 // todo sort
                                 final XValueChildrenList childrenList = new XValueChildrenList(fields.size());
                                 for (final VmVariable field : fields) {
                                   childrenList.add(new DartValue(myDebugProcess, field));
                                 }

                                 node.addChildren(childrenList, true);
                               }
                             }
        );
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }
}