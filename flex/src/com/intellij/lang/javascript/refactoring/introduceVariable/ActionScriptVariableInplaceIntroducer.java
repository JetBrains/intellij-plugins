// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.refactoring.introduceVariable;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.refactoring.introduce.BaseIntroduceSettings;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceDialog;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.NonFocusableCheckBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptVariableInplaceIntroducer extends JSVariableInplaceIntroducerBase {

  private final Runnable myCallback;
  private JCheckBox myMakeConstant;

  public ActionScriptVariableInplaceIntroducer(Project project,
                                               Editor editor,
                                               JSExpression[] occurrences,
                                               JSBaseIntroduceHandler<? extends JSElement, BaseIntroduceSettings, ? extends JSBaseIntroduceDialog> handler,
                                               JSBaseIntroduceHandler.BaseIntroduceContext<BaseIntroduceSettings> context,
                                               Runnable callback) {
    super(project, editor, occurrences, ActionScriptFileType.INSTANCE, handler, context);
    myCallback = callback;
  }

  @Override
  protected Settings getInplaceIntroduceSettings(final String name) {
    return new Settings() {
      @Override
      public IntroducedVarType getIntroducedVarType() {
        return myMakeConstant != null && myMakeConstant.isSelected() ? IntroducedVarType.CONST : IntroducedVarType.VAR;
      }

      @Override
      public boolean isReplaceAllOccurrences() {
        return myInitialSettings.isReplaceAllOccurrences();
      }

      @Override
      public String getVariableName() {
        return name;
      }

      @Override
      public String getVariableType() {
        return myInitialSettings.getVariableType();
      }
    };
  }

  @Override
  protected JComponent getComponent() {
    myMakeConstant = new NonFocusableCheckBox(JavaScriptBundle.message("javascript.introduce.variable.make.constant"));

    Settings.IntroducedVarType lastSelected = JSIntroduceVariableHandler.getLastIntroduceType(myProject, DialectOptionHolder.ECMA_4);
    myMakeConstant.setSelected(lastSelected == Settings.IntroducedVarType.CONST);
    myMakeConstant.setMnemonic('c');
    myMakeConstant.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof NonFocusableCheckBox checkBox) {
          final Settings.IntroducedVarType newVarModifier = checkBox.isSelected() ?
                                                            Settings.IntroducedVarType.CONST : Settings.IntroducedVarType.VAR;
          JSIntroduceVariableHandler.setLastIntroduceType(myProject, newVarModifier);

          restartInplaceIntroduceTemplate();
        }
      }
    });

    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(null);
    panel.add(myMakeConstant);

    return panel;
  }

  @Override
  protected void performPostIntroduceTasks() {
    super.performPostIntroduceTasks();
    myCallback.run();
  }
}
