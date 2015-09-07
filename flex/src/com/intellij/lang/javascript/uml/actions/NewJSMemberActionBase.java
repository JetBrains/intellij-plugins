package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.diagram.actions.DiagramCreateNewNodeElementAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public abstract class NewJSMemberActionBase extends DiagramCreateNewNodeElementAction<Object, Runnable> {
  public NewJSMemberActionBase(final String name, final String descr, final Icon icon) {
    super(name, descr, icon);
  }

  @Override
  public boolean isEnabledOn(Object o) {
    return o instanceof JSClass;
  }

  @Override
  public void execute(DiagramBuilder builder, Runnable run, AnActionEvent e) {
    run.run();
  }
}
