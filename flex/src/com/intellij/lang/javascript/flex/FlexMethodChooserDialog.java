package com.intellij.lang.javascript.flex;

import com.intellij.execution.ExecutionBundle;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Comparator;

public class FlexMethodChooserDialog extends DialogWrapper {

  private static final Comparator<JSFunction> METHOD_NAME_COMPARATOR = (method1, method2) -> {
    //noinspection ConstantConditions
    return method1.getName().compareToIgnoreCase(method2.getName());
  };

  private final SortedListModel<JSFunction> myListModel = new SortedListModel<>(METHOD_NAME_COMPARATOR);
  private final JList<JSFunction> myList = new JBList<>(myListModel);
  private final JPanel myWholePanel = new JPanel(new BorderLayout());

  public FlexMethodChooserDialog(final JSClass clazz,
                                 final Condition<JSFunction> filter,
                                 final JComponent parent,
                                 @Nullable String initialSelection) {
    super(parent, false);
    createList(clazz.getFunctions(), filter);
    myWholePanel.add(ScrollPaneFactory.createScrollPane(myList));
    myList.setCellRenderer(SimpleListCellRenderer.create("", JSFunction::getName));

    myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@NotNull MouseEvent e) {
        FlexMethodChooserDialog.this.close(OK_EXIT_CODE);
        return true;
      }
    }.installOn(myList);

    int index = getIndexToSelect(clazz, initialSelection);
    if (index == -1) {
      myList.clearSelection();
    }
    else {
      ScrollingUtil.selectItem(myList, index);
    }

    setTitle(ExecutionBundle.message("choose.test.method.dialog.title"));
    init();

    myList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateOkAction();
      }
    });
    updateOkAction();
  }

  private void updateOkAction() {
    setOKActionEnabled(myList.getSelectedIndex() != -1);
  }

  private int getIndexToSelect(JSClass clazz, @Nullable String methodName) {
    if (methodName == null) {
      return -1;
    }
    else {
      final JSFunction method = clazz.findFunctionByName(methodName);
      if (method == null) {
        return -1;
      }
      return myListModel.indexOf(method);
    }
  }

  private void createList(final JSFunction[] methods, final Condition<JSFunction> filter) {
    for (final JSFunction method : methods) {
      if (filter.value(method)) myListModel.add(method);
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myWholePanel;
  }

  public JSFunction getSelectedMethod() {
    return myList.getSelectedValue();
  }
}
