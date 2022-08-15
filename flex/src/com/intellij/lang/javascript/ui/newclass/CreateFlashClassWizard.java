// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.ui.newclass;

import com.intellij.ide.wizard.AbstractWizardEx;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Arrays;

public class CreateFlashClassWizard extends AbstractWizardEx {

  private final WizardModel myModel;
  private final String myHelpId;

  public CreateFlashClassWizard(@NlsContexts.DialogTitle String title,
                                @NotNull Project project,
                                WizardModel model,
                                String helpId,
                                AbstractWizardStepEx... steps) {
    super(title, project, Arrays.asList(steps));
    myModel = model;
    myHelpId = helpId;

    ((MainStep)steps[0]).addListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        adjustHeight(false);
      }
    }, getDisposable());
  }

  private void adjustHeight(boolean force) {
    int preferredHeight = getContentPane().getLayout().minimumLayoutSize(getContentPane()).height;
    Dimension currentSize = getSize();
    if (force || preferredHeight > currentSize.height) {
      setSize(currentSize.width, preferredHeight);
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPanel;
  }

  @Override
  protected String getDimensionServiceKey() {
    return null; //CreateFlashClassWizard.class.getName();
  }

  @Override
  protected void doOKAction() {
    if (!myModel.commit()) {
      return;
    }
    super.doOKAction();
  }

  @Override
  protected void updateStep() {
    super.updateStep();
    if (getCurrentStepObject() instanceof CustomVariablesStep) {
      ((CustomVariablesStep)getCurrentStepObject()).shown();
    }
  }

  @Override
  protected void updateButtons() {
    super.updateButtons();
    getPreviousButton().setVisible(getCurrentStep() > 0);
    if (isLastStep()) {
      getNextButton().setText(JavaScriptBundle.message("create.class.ok.button.text"));
    }
  }

  @Override
  protected String getHelpID() {
    return myHelpId;
  }
}
