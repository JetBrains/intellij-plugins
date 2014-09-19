package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.SortedList;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// todo navigate to source, type
public class DartValue extends XNamedValue {
  public static final String NODE_NAME_RESULT = "result";
  public static final String NODE_NAME_EXCEPTION = "exception";

  private final @NotNull DartCommandLineDebugProcess myDebugProcess;
  private final @Nullable VmVariable myVmVariable;
  private @Nullable VmValue myVmValue;
  private final boolean myIsException;

  private Ref<Integer> myListOrMapChildrenAlreadyShown = new Ref<Integer>(0);

  static final String OBJECT_OF_TYPE_PREFIX = "object of type ";

  public DartValue(final @NotNull DartCommandLineDebugProcess debugProcess, final @NotNull VmVariable vmVariable) {
    super(StringUtil.notNullize(DebuggerUtils.demangleVmName(vmVariable.getName()), "<unknown>"));
    myDebugProcess = debugProcess;
    myVmVariable = vmVariable;
    myIsException = false;
  }

  public DartValue(@NotNull final DartCommandLineDebugProcess debugProcess,
                   @NotNull final String nodeName,
                   @NotNull @SuppressWarnings("NullableProblems") final VmValue vmValue,
                   final boolean isException) {
    super(nodeName);
    myDebugProcess = debugProcess;
    myVmVariable = null;
    myVmValue = vmValue;
    myIsException = isException;
  }

  @Override
  public void computePresentation(final @NotNull XValueNode node, final @NotNull XValuePlace place) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        if (node.isObsolete()) return;

        if (myVmValue == null && myVmVariable != null) {
          myVmValue = myVmVariable.getValue();
        }

        if (myVmValue == null) {
          node.setPresentation(AllIcons.Debugger.Value, null, "<no value>", false);
          return;
        }

        final String value = myVmValue.isList() ? "size = " + myVmValue.getLength()
                                                : StringUtil.notNullize(myVmValue.getText(), "null");
        final XValuePresentation presentation;

        final int objectId = myVmValue.getObjectId();
        final String objectIdPostfix = /*objectId == 0 ? "" :*/ "[id=" + objectId + "]"; // 0 is also a valid id

        if (myVmValue.isNull()) {
          presentation = new XRegularValuePresentation("null", null);
        }
        else if (myVmValue.isString()) {
          presentation = new XStringValuePresentation(StringUtil.stripQuotesAroundValue(value));
        }
        else if (myVmValue.isNumber()) {
          presentation = new XNumericValuePresentation(value);
        }
        else if ("boolean".equals(myVmValue.getKind())) {
          presentation = new XRegularValuePresentation(value, null);
        }
        else if (myVmValue.isList()) {
          presentation = new XRegularValuePresentation(value, "List" + objectIdPostfix); // VmValue doesn't contain real List subclass name
        }
        else {
          if (value.startsWith(OBJECT_OF_TYPE_PREFIX)) {
            presentation = new XRegularValuePresentation("", value.substring(OBJECT_OF_TYPE_PREFIX.length()) + objectIdPostfix);
          }
          else {
            presentation = new XRegularValuePresentation(value, DebuggerUtils.demangleVmName(myVmValue.getKind()) + objectIdPostfix);
          }
        }

        final boolean neverHasChildren = myVmValue.isPrimitive() ||
                                         myVmValue.isNull() ||
                                         myVmValue.isFunction() ||
                                         myVmValue.isList() && myVmValue.getLength() == 0;
        node.setPresentation(getIcon(), presentation, !neverHasChildren);

        if (!myVmValue.isList() && !myVmValue.isPrimitive() && !myVmValue.isNull() && !myVmValue.isFunction()) {
          scheduleToStringOrCollectionSizePresentation(node, presentation.getType());
        }
      }
    });
  }

  private void scheduleToStringOrCollectionSizePresentation(@NotNull final XValueNode node, final String objectType) {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null && !myVmValue.isList(), myVmValue);

    try {
      final String expression = "(this is Iterable || this is Map) ? ('IntelliJ marker:${this.length}') : toString()";
      myDebugProcess.getVmConnection().evaluateObject(myVmValue.getIsolate(), myVmValue, expression, new VmCallback<VmValue>() {
        public void handleResult(final VmResult<VmValue> result) {
          if (node.isObsolete() || result.isError() || result.getResult() == null || !"string".equals(result.getResult().getKind())) {
            return;
          }

          final String text = StringUtil.stripQuotesAroundValue(result.getResult().getText());
          if (text.startsWith("IntelliJ marker:")) {
            try {
              final int collectionSize = Integer.parseInt(text.substring("IntelliJ marker:".length()));
              if (collectionSize >= 0) {
                node.setPresentation(AllIcons.Debugger.Db_array, objectType, "size = " + collectionSize, collectionSize > 0);
              }
            }
            catch (NumberFormatException ignore) {/**/}
          }
          // default toString() implementation returns "Instance of 'ClassName'" - do not show it
          else if (!text.startsWith("Instance of '") || !text.endsWith("'")) {
            node.setPresentation(getIcon(), objectType, text, true);
          }
        }
      });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private Icon getIcon() {
    if (myIsException) return AllIcons.Debugger.Db_exception_breakpoint;
    if (myVmValue != null && myVmValue.isList()) return AllIcons.Debugger.Db_array;
    if (myVmValue != null && myVmValue.isPrimitive()) return AllIcons.Debugger.Db_primitive;
    if (myVmValue != null && myVmValue.isFunction()) return AllIcons.Nodes.Function;

    return AllIcons.Debugger.Value; // todo m.b. resolve and show corresponding icon?
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    computeChildren(node, myListOrMapChildrenAlreadyShown);
  }

  private void computeChildren(@NotNull final XCompositeNode node, @NotNull final Ref<Integer> listChildrenAlreadyShown) {
    // myVmValue is already calculated in computePresentation()
    if (myVmValue == null) node.addChildren(XValueChildrenList.EMPTY, true);

    // see com.google.dart.tools.debug.core.server.ServerDebugValue#fillInFieldsSync()
    try {
      if (myVmValue.isList()) {
        computeListChildren(node, listChildrenAlreadyShown);
        return;
      }

      final String expression = "this is Iterable ? 'Iterable' : this is Map ? 'Map' : ''";
      myDebugProcess.getVmConnection().evaluateObject(myVmValue.getIsolate(), myVmValue, expression, new VmCallbackAdapter<VmValue>(node) {
        @Override
        protected void handleGoodResult(@NotNull final VmValue result) {
          final String text = StringUtil.stripQuotesAroundValue(result.getText());

          if ("string".equals(result.getKind()) && "Map".equals(text)) {
            computeMapChildren(node);
          }
          else if ("string".equals(result.getKind()) && "Iterable".equals(text)) {
            computeIterableChildren(node);
          }
          else {
            computeObjectChildren(node);
          }
        }
      });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private void computeListChildren(@NotNull final XCompositeNode node,
                                   @NotNull final Ref<Integer> listChildrenAlreadyShown) throws IOException {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null && myVmValue.isList(), myVmValue);

    final Integer fromIndex = listChildrenAlreadyShown.get();
    final int childrenToShow = Math.min(myVmValue.getLength() - fromIndex, XCompositeNode.MAX_CHILDREN_TO_SHOW);
    if (childrenToShow == 0) {
      node.addChildren(XValueChildrenList.EMPTY, true);
      return;
    }


    computeListChildren(node, myDebugProcess, myVmValue, fromIndex, childrenToShow, new Consumer<List<DartValue>>() {
      @Override
      public void consume(final List<DartValue> listChildren) {
        final XValueChildrenList resultList = new XValueChildrenList(listChildren.size());
        for (DartValue value : listChildren) {
          resultList.add(value);
        }

        node.addChildren(resultList, true);
        listChildrenAlreadyShown.set(listChildrenAlreadyShown.get() + listChildren.size());

        if (myVmValue.getLength() > listChildrenAlreadyShown.get()) {
          node.tooManyChildren(myVmValue.getLength() - listChildrenAlreadyShown.get());
        }
      }
    });
  }

  private static void computeListChildren(@NotNull final XCompositeNode node,
                                          @NotNull final DartCommandLineDebugProcess debugProcess,
                                          @NotNull final VmValue listValue,
                                          final int fromIndex,
                                          final int childrenAmount,
                                          @NotNull final Consumer<List<DartValue>> listChildrenConsumer) throws IOException {
    DartCommandLineDebugProcess.LOG.assertTrue(listValue.isList(), listValue);

    final AtomicInteger handledResponsesAmount = new AtomicInteger(0);

    final SortedList<DartValue> sortedChildren = new SortedList<DartValue>(new Comparator<DartValue>() {
      public int compare(DartValue o1, DartValue o2) {
        return StringUtil.naturalCompare(o1.getName(), o2.getName());
      }
    });

    for (int listIndex = fromIndex; listIndex < fromIndex + childrenAmount; listIndex++) {
      final String nodeName = String.valueOf(listIndex);
      debugProcess.getVmConnection()
        .getListElements(listValue.getIsolate(), listValue.getObjectId(), listIndex, new VmCallbackAdapter<VmValue>(node) {
          @Override
          public void handleResult(final VmResult<VmValue> result) {
            synchronized (node) {
              handledResponsesAmount.addAndGet(1);
              super.handleResult(result);
            }
          }

          @Override
          protected void handleGoodResult(@NotNull final VmValue result) {
            sortedChildren.add(new DartValue(debugProcess, nodeName, result, false));

            if (handledResponsesAmount.get() == childrenAmount) {
              listChildrenConsumer.consume(sortedChildren);
            }
          }
        });
    }
  }

  private void computeMapChildren(@NotNull final XCompositeNode node) {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null);

    try {
      myDebugProcess.getVmConnection()
        .evaluateObject(myVmValue.getIsolate(), myVmValue, "keys.toList()", new VmCallbackAdapter<VmValue>(node) {
          @Override
          protected void handleGoodResult(@NotNull final VmValue result) {
            if (result.isList()) { // result is a list of map keys
              computeMapChildrenForKeys(node, result);
            }
            else {
              computeObjectChildren(node); // shouldn't happen
            }
          }
        });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private void computeMapChildrenForKeys(@NotNull final XCompositeNode node, @NotNull final VmValue mapKeysList) {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null && mapKeysList.isList());

    try {
      myDebugProcess.getVmConnection()
        .evaluateObject(myVmValue.getIsolate(), myVmValue, "values.toList()", new VmCallbackAdapter<VmValue>(node) {
          @Override
          protected void handleGoodResult(@NotNull final VmValue result) {
            if (result.isList()) { // result is a list of map values
              computeMapChildrenForKeysAndValues(node, mapKeysList, result);
            }
            else {
              computeObjectChildren(node); // shouldn't happen
            }
          }
        });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private void computeMapChildrenForKeysAndValues(@NotNull final XCompositeNode node,
                                                  @NotNull final VmValue mapKeysList,
                                                  @NotNull final VmValue mapValuesList) {
    try {
      final Integer fromIndex = myListOrMapChildrenAlreadyShown.get();
      final int childrenToShow = Math.min(mapKeysList.getLength() - fromIndex, XCompositeNode.MAX_CHILDREN_TO_SHOW);
      DartCommandLineDebugProcess.LOG.assertTrue(childrenToShow > 0);

      computeListChildren(node, myDebugProcess, mapKeysList, fromIndex, childrenToShow, new Consumer<List<DartValue>>() {
        @Override
        public void consume(final List<DartValue> mapKeys) {
          try {
            computeListChildren(node, myDebugProcess, mapValuesList, fromIndex, childrenToShow, new Consumer<List<DartValue>>() {
              @Override
              public void consume(final List<DartValue> mapValues) {
                addMapChildrenToNode(node, mapKeys, mapValues, mapKeysList.getLength());
              }
            });
          }
          catch (IOException e) {
            DartCommandLineDebugProcess.LOG.error(e);
          }
        }
      });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private void addMapChildrenToNode(@NotNull final XCompositeNode node,
                                    @NotNull final List<DartValue> mapKeys,
                                    @NotNull final List<DartValue> mapValues,
                                    final int mapSize) {
    if (mapKeys.size() != mapValues.size()) {
      DartCommandLineDebugProcess.LOG.warn(mapKeys.size() + " keys, " + mapValues.size() + " values");
      node.setErrorMessage("failed to show Map contents");
      return;
    }

    final XValueChildrenList resultList = new XValueChildrenList(mapKeys.size());

    for (int i = 0; i < mapKeys.size(); i++) {
      final DartValue mapKey = mapKeys.get(i);
      final DartValue mapValue = mapValues.get(i);
      DartCommandLineDebugProcess.LOG.assertTrue(mapKey.myVmValue != null && mapValue.myVmValue != null);

      resultList.add(new DartMapEntryValue(myDebugProcess, mapKey.getName(), mapKey.myVmValue, mapValue.myVmValue));
    }

    node.addChildren(resultList, true);

    myListOrMapChildrenAlreadyShown.set(myListOrMapChildrenAlreadyShown.get() + mapKeys.size());
    if (mapSize > myListOrMapChildrenAlreadyShown.get()) {
      node.tooManyChildren(mapSize - myListOrMapChildrenAlreadyShown.get());
    }
  }

  private void computeIterableChildren(@NotNull final XCompositeNode node) {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null);

    try {
      myDebugProcess.getVmConnection().evaluateObject(myVmValue.getIsolate(), myVmValue, "toList()", new VmCallbackAdapter<VmValue>(node) {
        @Override
        protected void handleGoodResult(@NotNull final VmValue result) {
          // result is a list that will return collection contents
          new DartValue(myDebugProcess, "fake node", result, false).computeChildren(node, myListOrMapChildrenAlreadyShown);
        }
      });
    }
    catch (IOException e) {
      DartCommandLineDebugProcess.LOG.error(e);
    }
  }

  private void computeObjectChildren(@NotNull final XCompositeNode node) {
    DartCommandLineDebugProcess.LOG.assertTrue(myVmValue != null);

    try {
      myDebugProcess.getVmConnection()
        .getObjectProperties(myVmValue.getIsolate(), myVmValue.getObjectId(), new VmCallbackAdapter<VmObject>(node) {
                               @Override
                               protected void handleGoodResult(@NotNull final VmObject result) {
                                 final List<VmVariable> fields = result.getFields();
                                 if (fields == null) {
                                   node.addChildren(XValueChildrenList.EMPTY, true);
                                   return;
                                 }

                                 // todo sort somehow?
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